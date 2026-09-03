package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminRefundRejectRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.mapper.admin.AdminRefundMapper;
import com.example.maimaibackend.ticketsource.refund.TicketSourceRefundService;
import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBridge;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminRefundDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundItemVO;
import com.example.maimaibackend.vo.admin.AdminRefundListPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminRefundService {

    private static final Set<String> REFUND_STATUS_SET = new HashSet<>(Arrays.asList(
            "REFUNDING", "REFUND_SUCCESS", "REFUND_FAILED"
    ));
    private static final String REFUNDING = "REFUNDING";

    private final AdminRefundMapper adminRefundMapper;
    private final AdminTicketLogService adminTicketLogService;
    private final TicketSourceRefundService ticketSourceRefundService;
    private final TicketSourceWorkflowService workflowService;

    public AdminRefundService(AdminRefundMapper adminRefundMapper,
                              AdminTicketLogService adminTicketLogService,
                              TicketSourceRefundService ticketSourceRefundService,
                              TicketSourceWorkflowService workflowService) {
        this.adminRefundMapper = adminRefundMapper;
        this.adminTicketLogService = adminTicketLogService;
        this.ticketSourceRefundService = ticketSourceRefundService;
        this.workflowService = workflowService;
    }

    public AdminRefundListPageVO getRefundList(String keyword, String refundStatus, Long userId, Long orderId,
                                               String dateFrom, String dateTo, Integer pageNo, Integer pageSize) {
        validateOptionalId(userId, "userId");
        validateOptionalId(orderId, "orderId");
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeStatus = trimToNull(refundStatus);
        if (safeStatus != null && !REFUND_STATUS_SET.contains(safeStatus)) {
            throw new BusinessException("退款状态不合法");
        }
        DateRange range = normalizeDateRange(dateFrom, dateTo);
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminRefundMapper.countRefundList(safeKeyword, safeStatus, userId, orderId,
                range.dateFrom, range.dateTo);
        List<AdminRefundItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminRefundMapper.selectRefundList(safeKeyword, safeStatus, userId, orderId,
                range.dateFrom, range.dateTo, safePageSize, offset);
        AdminRefundListPageVO vo = new AdminRefundListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminRefundDetailVO getRefundDetail(Long refundId) {
        requireRefundId(refundId);
        AdminRefundDetailVO vo = adminRefundMapper.selectRefundDetail(refundId);
        if (vo == null) {
            throw new BusinessException("退款记录不存在");
        }
        vo.setTickets(adminRefundMapper.selectRefundOrderTickets(vo.getOrderId()));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse approveRefund(Long refundId, TicketOperationContext context) {
        AdminRefundDetailVO current = requireRefund(refundId);
        if (ticketSourceRefundService.isTicketSourceRefund(refundId)) {
            TicketSourceRefundBridge currentBridge = ticketSourceRefundService.getBridge(refundId);
            if ("SUCCESS".equals(currentBridge.getBridgeStatus())
                    && "REFUND_SUCCESS".equals(current.getRefundStatus())
                    && "REFUND_SUCCESS".equals(current.getOrderStatus())) {
                return new AdminOperateResponse(true, "第三方退款已完成");
            }
            requirePendingRefund(current);
            if (workflowService.isSingleSkuProviderRefund(refundId)) {
                java.util.Map<String,Object> result = workflowService.approvePreparedRefund(refundId);
                String bridgeStatus = String.valueOf(result.get("bridgeStatus"));
                adminTicketLogService.recordSuccess(context, "REFUND", "APPROVE_SOURCE_REFUND_V12", "REFUND", refundId,
                        current.getOrderId(), null, REFUNDING, bridgeStatus,
                        "管理员审核通过V1.2第三方整单退款：" + current.getRefundNo(), null);
                String message = "SUCCESS".equals(bridgeStatus)
                        ? "第三方退款成功" : "第三方整单退款已发起，当前状态：" + bridgeStatus;
                return new AdminOperateResponse(true, message);
            }
            // 历史非V1.2第三方退款保留旧兼容处理，避免迁移期破坏只读/存量数据。
            TicketSourceRefundBridge bridge = ticketSourceRefundService.approveAndProcess(refundId);
            adminTicketLogService.recordSuccess(context, "REFUND", "APPROVE_SOURCE_REFUND", "REFUND", refundId,
                    current.getOrderId(), null, REFUNDING, bridge.getBridgeStatus(),
                    "管理员审核通过第三方退款：" + current.getRefundNo(), null);
            String message = "SUCCESS".equals(bridge.getBridgeStatus())
                    ? "第三方退款成功" : "第三方退款已发起，当前状态：" + bridge.getBridgeStatus();
            return new AdminOperateResponse(true, message);
        }
        requirePendingRefund(current);
        int refundUpdated = adminRefundMapper.updateRefundStatusIfCurrent(
                refundId, REFUNDING, "REFUND_SUCCESS", null);
        if (refundUpdated != 1) {
            throw new BusinessException(409, "退款状态已变化，请刷新后重试");
        }
        int orderUpdated = adminRefundMapper.updateOrderStatusIfCurrent(
                current.getOrderId(), REFUNDING, "REFUND_SUCCESS");
        if (orderUpdated != 1) {
            throw new BusinessException(409, "关联订单状态已变化，退款审核已回滚");
        }
        adminRefundMapper.finalizeTicketsAfterRefundApproved(current.getOrderId());
        adminTicketLogService.recordSuccess(context, "REFUND", "APPROVE_REFUND", "REFUND", refundId,
                current.getOrderId(), null, REFUNDING, "REFUND_SUCCESS",
                "管理员审核通过退款：" + current.getRefundNo(), null);
        return new AdminOperateResponse(true, "退款审核通过");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse rejectRefund(Long refundId, AdminRefundRejectRequest request,
                                             TicketOperationContext context) {
        String reason = trimToNull(request == null ? null : request.getReason());
        if (reason == null) {
            throw new BusinessException("拒绝原因不能为空");
        }
        if (reason.length() > 255) {
            throw new BusinessException("拒绝原因不能超过 255 个字符");
        }
        AdminRefundDetailVO current = requirePendingRefund(refundId);
        if (ticketSourceRefundService.isTicketSourceRefund(refundId)) {
            if (workflowService.isSingleSkuProviderRefund(refundId)) {
                workflowService.rejectPreparedRefundBeforeProvider(refundId);
            } else {
                ticketSourceRefundService.rejectBeforeProvider(refundId);
            }
        }
        int refundUpdated = adminRefundMapper.updateRefundStatusIfCurrent(
                refundId, REFUNDING, "REFUND_FAILED", reason);
        if (refundUpdated != 1) {
            throw new BusinessException(409, "退款状态已变化，请刷新后重试");
        }
        int orderUpdated = adminRefundMapper.updateOrderStatusIfCurrent(
                current.getOrderId(), REFUNDING, "WAIT_USE");
        if (orderUpdated != 1) {
            throw new BusinessException(409, "关联订单状态已变化，退款驳回已回滚");
        }
        int heldTicketCount = safeCount(adminRefundMapper.countRefundHeldTickets(current.getOrderId()));
        if (heldTicketCount > 0) {
            int restoredTicketCount = safeCount(
                    adminRefundMapper.restoreTicketsAfterRefundRejected(current.getOrderId()));
            if (restoredTicketCount != heldTicketCount) {
                throw new BusinessException(409, "电子票恢复数量发生变化，退款驳回已回滚");
            }
        }
        adminTicketLogService.recordSuccess(context, "REFUND", "REJECT_REFUND", "REFUND", refundId,
                current.getOrderId(), null, REFUNDING, "REFUND_FAILED",
                "管理员驳回退款：" + current.getRefundNo() + "；原因：" + reason, null);
        return new AdminOperateResponse(true, "退款已拒绝");
    }

    private AdminRefundDetailVO requireRefund(Long refundId) {
        requireRefundId(refundId);
        AdminRefundDetailVO current = adminRefundMapper.selectRefundDetail(refundId);
        if (current == null) {
            throw new BusinessException("退款记录不存在");
        }
        return current;
    }

    private AdminRefundDetailVO requirePendingRefund(Long refundId) {
        AdminRefundDetailVO current = requireRefund(refundId);
        requirePendingRefund(current);
        return current;
    }

    private void requirePendingRefund(AdminRefundDetailVO current) {
        if (!REFUNDING.equals(current.getRefundStatus())) {
            throw new BusinessException(409, "只有退款中的申请可以审核");
        }
        if (!REFUNDING.equals(current.getOrderStatus())) {
            throw new BusinessException(409, "关联订单不处于退款中，不能审核该退款");
        }
    }

    private DateRange normalizeDateRange(String dateFrom, String dateTo) {
        String safeFrom = normalizeDate(dateFrom, "dateFrom");
        String safeTo = normalizeDate(dateTo, "dateTo");
        if (safeFrom != null && safeTo != null && LocalDate.parse(safeFrom).isAfter(LocalDate.parse(safeTo))) {
            throw new BusinessException("dateFrom 不能晚于 dateTo");
        }
        return new DateRange(safeFrom, safeTo);
    }

    private String normalizeDate(String value, String name) {
        String safe = trimToNull(value);
        if (safe == null) {
            return null;
        }
        try {
            return LocalDate.parse(safe).toString();
        } catch (DateTimeParseException e) {
            throw new BusinessException(name + " 格式必须为 yyyy-MM-dd");
        }
    }

    private void requireRefundId(Long refundId) {
        if (refundId == null || refundId <= 0) {
            throw new BusinessException("refundId 不合法");
        }
    }

    private void validateOptionalId(Long id, String name) {
        if (id != null && id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }


    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class DateRange {
        private final String dateFrom;
        private final String dateTo;

        private DateRange(String dateFrom, String dateTo) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }
    }
}
