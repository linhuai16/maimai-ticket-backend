package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminTicketCheckRequest;
import com.example.maimaibackend.dto.admin.AdminTicketErrorRequest;
import com.example.maimaibackend.dto.admin.AdminTicketStateDTO;
import com.example.maimaibackend.dto.admin.AdminTicketSystemRefundRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateTicketSeatRequest;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.dto.ticket.IssueFailedRefundInsertDTO;
import com.example.maimaibackend.dto.ticket.IssueOrderBaseDTO;
import com.example.maimaibackend.mapper.admin.AdminTicketMapper;
import com.example.maimaibackend.vo.admin.AdminIssueOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminIssueOrderItemVO;
import com.example.maimaibackend.vo.admin.AdminIssueOrderListPageVO;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminTicketCheckResponse;
import com.example.maimaibackend.vo.admin.AdminTicketDetailVO;
import com.example.maimaibackend.vo.admin.AdminTicketIssueResponse;
import com.example.maimaibackend.vo.admin.AdminTicketItemVO;
import com.example.maimaibackend.vo.admin.AdminTicketListPageVO;
import com.example.maimaibackend.vo.admin.AdminTicketSystemRefundResponse;
import com.example.maimaibackend.vo.admin.AdminTicketVerifyVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdminTicketService {

    private static final String ORDER_STATUS_WAIT_USE = "WAIT_USE";
    private static final String ORDER_STATUS_REFUND_SUCCESS = "REFUND_SUCCESS";
    private static final String REFUND_STATUS_SUCCESS = "REFUND_SUCCESS";

    private static final String TICKET_STATUS_GENERATING = "GENERATING";
    private static final String TICKET_STATUS_UNUSED = "UNUSED";
    private static final String TICKET_STATUS_CHECKED = "CHECKED";
    private static final String TICKET_STATUS_EXPIRED = "EXPIRED";
    private static final String TICKET_STATUS_ERROR = "ERROR";

    private static final String DEFAULT_TICKET_ERROR_REASON = "后台标记出票异常";
    private static final String DEFAULT_SYSTEM_REFUND_REASON = "出票失败，后台发起系统全额退款";

    private static final Set<String> TICKET_STATUS_SET = new HashSet<>(Arrays.asList(
            TICKET_STATUS_GENERATING, TICKET_STATUS_UNUSED, TICKET_STATUS_CHECKED,
            TICKET_STATUS_EXPIRED, TICKET_STATUS_ERROR
    ));

    private static final Set<String> ISSUE_STATUS_SET = new HashSet<>(Arrays.asList(
            "GENERATING", "ERROR", "PARTIAL", "ISSUED", "NO_TICKET"
    ));

    private final AdminTicketMapper adminTicketMapper;
    private final AdminTicketLogService adminTicketLogService;

    public AdminTicketService(AdminTicketMapper adminTicketMapper, AdminTicketLogService adminTicketLogService) {
        this.adminTicketMapper = adminTicketMapper;
        this.adminTicketLogService = adminTicketLogService;
    }

    public AdminTicketListPageVO getTicketList(String keyword, Long orderId, Long userId,
                                                Long projectId, Long sessionId, String ticketStatus,
                                                String dateFrom, String dateTo,
                                                Integer pageNo, Integer pageSize) {
        validateOptionalId(orderId, "orderId");
        validateOptionalId(userId, "userId");
        validateOptionalId(projectId, "projectId");
        validateOptionalId(sessionId, "sessionId");
        PageParam page = normalizePage(pageNo, pageSize);
        String safeStatus = trimToNull(ticketStatus);
        if (safeStatus != null && !TICKET_STATUS_SET.contains(safeStatus)) {
            throw new BusinessException("电子票状态不合法");
        }
        DateRange range = normalizeDateRange(dateFrom, dateTo);
        String safeKeyword = trimToNull(keyword);
        Integer total = adminTicketMapper.countTicketList(
                safeKeyword, orderId, userId, projectId, sessionId, safeStatus,
                range.getDateFrom(), range.getDateTo()
        );
        List<AdminTicketItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminTicketMapper.selectTicketList(
                        safeKeyword, orderId, userId, projectId, sessionId, safeStatus,
                        range.getDateFrom(), range.getDateTo(), page.getPageSize(), page.getOffset()
                );
        return buildTicketPage(total, page, items);
    }

    public AdminTicketDetailVO getTicketDetail(Long ticketId) {
        validateId(ticketId, "ticketId");
        AdminTicketDetailVO detail = adminTicketMapper.selectTicketDetail(ticketId);
        if (detail == null) {
            throw new BusinessException("电子票不存在");
        }
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse updateSeatInfo(Long ticketId, AdminUpdateTicketSeatRequest request, TicketOperationContext context) {
        validateId(ticketId, "ticketId");
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        String seatInfo = trimToNull(request.getSeatInfo());
        if (seatInfo != null && seatInfo.length() > 100) {
            throw new BusinessException("座位信息不能超过 100 个字符");
        }

        AdminTicketStateDTO state = requireTicketStateForUpdate(ticketId);
        requireLocalCompatOrder(state.getOrderId());
        requireWaitUseOrder(state.getOrderStatus());
        if (TICKET_STATUS_CHECKED.equals(state.getTicketStatus())
                || TICKET_STATUS_EXPIRED.equals(state.getTicketStatus())) {
            throw new BusinessException("已检票或已失效电子票不能修改座位信息");
        }

        int updated = adminTicketMapper.updateTicketSeat(ticketId, seatInfo, LocalDateTime.now());
        if (updated != 1) {
            throw new BusinessException("座位信息更新失败，请刷新后重试");
        }
        adminTicketLogService.recordSuccess(context, "TICKET", "UPDATE_SEAT", "TICKET", ticketId,
                state.getOrderId(), ticketId, state.getTicketStatus(), state.getTicketStatus(),
                seatInfo == null ? "管理员清空电子票座位信息" : "管理员更新电子票座位信息", null);
        return new AdminOperateResponse(true, seatInfo == null ? "座位信息已清空" : "座位信息已更新");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse markTicketError(Long ticketId, AdminTicketErrorRequest request, TicketOperationContext context) {
        validateId(ticketId, "ticketId");
        AdminTicketStateDTO state = requireTicketStateForUpdate(ticketId);
        requireLocalCompatOrder(state.getOrderId());
        requireWaitUseOrder(state.getOrderStatus());
        if (TICKET_STATUS_ERROR.equals(state.getTicketStatus())) {
            throw new BusinessException("电子票已经是异常状态");
        }
        if (!TICKET_STATUS_GENERATING.equals(state.getTicketStatus())
                && !TICKET_STATUS_UNUSED.equals(state.getTicketStatus())) {
            throw new BusinessException("只有生成中或未使用电子票可以标记异常");
        }

        String reason = normalizeReason(request == null ? null : request.getAbnormalReason(),
                DEFAULT_TICKET_ERROR_REASON, 500);
        int updated = adminTicketMapper.markTicketError(ticketId, reason, LocalDateTime.now());
        if (updated != 1) {
            throw new BusinessException("电子票异常状态更新失败，请刷新后重试");
        }
        adminTicketLogService.recordSuccess(context, "ISSUE", "MARK_TICKET_ERROR", "TICKET", ticketId,
                state.getOrderId(), ticketId, state.getTicketStatus(), TICKET_STATUS_ERROR,
                "管理员标记单张电子票异常：" + reason, null);
        return new AdminOperateResponse(true, "电子票已标记为异常");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse retryTicket(Long ticketId, TicketOperationContext context) {
        validateId(ticketId, "ticketId");
        AdminTicketStateDTO state = requireTicketStateForUpdate(ticketId);
        requireLocalCompatOrder(state.getOrderId());
        requireWaitUseOrder(state.getOrderStatus());
        if (!TICKET_STATUS_ERROR.equals(state.getTicketStatus())) {
            throw new BusinessException("只有异常电子票可以重新进入出票队列");
        }

        int updated = adminTicketMapper.retryTicket(ticketId, LocalDateTime.now());
        if (updated != 1) {
            throw new BusinessException("电子票重试失败，请刷新后重试");
        }
        adminTicketLogService.recordSuccess(context, "ISSUE", "RETRY_TICKET", "TICKET", ticketId,
                state.getOrderId(), ticketId, TICKET_STATUS_ERROR, TICKET_STATUS_GENERATING,
                "管理员手动重新出票", null);
        return new AdminOperateResponse(true, "电子票已重新进入出票队列");
    }

    public AdminIssueOrderListPageVO getIssueOrderList(String keyword, Long projectId, Long sessionId,
                                                        String issueStatus, String dateFrom, String dateTo,
                                                        Integer pageNo, Integer pageSize) {
        validateOptionalId(projectId, "projectId");
        validateOptionalId(sessionId, "sessionId");
        PageParam page = normalizePage(pageNo, pageSize);
        String safeIssueStatus = trimToNull(issueStatus);
        if (safeIssueStatus != null && !ISSUE_STATUS_SET.contains(safeIssueStatus)) {
            throw new BusinessException("出票任务状态不合法");
        }
        DateRange range = normalizeDateRange(dateFrom, dateTo);
        String safeKeyword = trimToNull(keyword);
        Integer total = adminTicketMapper.countIssueOrderList(
                safeKeyword, projectId, sessionId, safeIssueStatus,
                range.getDateFrom(), range.getDateTo()
        );
        List<AdminIssueOrderItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminTicketMapper.selectIssueOrderList(
                        safeKeyword, projectId, sessionId, safeIssueStatus,
                        range.getDateFrom(), range.getDateTo(), page.getPageSize(), page.getOffset()
                );

        AdminIssueOrderListPageVO vo = new AdminIssueOrderListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(page.getPageNo());
        vo.setPageSize(page.getPageSize());
        vo.setItems(items);
        return vo;
    }

    public AdminIssueOrderDetailVO getIssueOrderDetail(Long orderId) {
        validateId(orderId, "orderId");
        AdminIssueOrderDetailVO detail = adminTicketMapper.selectIssueOrderSummary(orderId);
        if (detail == null) {
            throw new BusinessException("出票订单不存在");
        }
        detail.setTickets(adminTicketMapper.selectTicketsByOrder(orderId));
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminTicketIssueResponse issueOrderSuccess(Long orderId, TicketOperationContext context) {
        requireLocalCompatOrder(orderId);
        IssueOrderBaseDTO order = requireIssueOrderForUpdate(orderId);
        requireWaitUseOrder(order.getOrderStatus());

        int candidateCount = safeCount(adminTicketMapper.countIssueSuccessCandidates(orderId));
        if (candidateCount <= 0) {
            throw new BusinessException("当前订单没有生成中或异常电子票");
        }

        LocalDateTime now = LocalDateTime.now();
        int issuedCount = safeCount(adminTicketMapper.issueOrderTickets(orderId, now));
        if (issuedCount != candidateCount) {
            throw new BusinessException("出票数量发生变化，请刷新后重试");
        }
        int orderUpdated = safeCount(adminTicketMapper.updateOrderTicketIssuedTime(orderId, now));
        if (orderUpdated != 1) {
            throw new BusinessException("订单出票时间更新失败");
        }
        adminTicketLogService.recordSuccess(context, "ISSUE", "ISSUE_ORDER_SUCCESS", "ORDER", orderId,
                orderId, null, "GENERATING/ERROR", "ISSUED",
                "管理员确认整单出票成功，共处理 " + issuedCount + " 张电子票", null);
        return buildIssueResponse(orderId, issuedCount, "ISSUED", now, "订单电子票已出票成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminTicketIssueResponse markOrderIssueError(Long orderId, AdminTicketErrorRequest request, TicketOperationContext context) {
        requireLocalCompatOrder(orderId);
        IssueOrderBaseDTO order = requireIssueOrderForUpdate(orderId);
        requireWaitUseOrder(order.getOrderStatus());
        String reason = normalizeReason(request == null ? null : request.getAbnormalReason(),
                DEFAULT_TICKET_ERROR_REASON, 500);
        LocalDateTime now = LocalDateTime.now();
        int affected = safeCount(adminTicketMapper.markOrderTicketsError(orderId, reason, now));
        if (affected <= 0) {
            throw new BusinessException("当前订单没有生成中或未使用电子票可标记异常");
        }
        adminTicketLogService.recordSuccess(context, "ISSUE", "MARK_ORDER_ERROR", "ORDER", orderId,
                orderId, null, "GENERATING/UNUSED", "ERROR",
                "管理员标记整单出票异常，共影响 " + affected + " 张：" + reason, null);
        return buildIssueResponse(orderId, affected, "ERROR", now, "订单电子票已标记为异常");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminTicketIssueResponse retryOrderIssue(Long orderId, TicketOperationContext context) {
        requireLocalCompatOrder(orderId);
        IssueOrderBaseDTO order = requireIssueOrderForUpdate(orderId);
        requireWaitUseOrder(order.getOrderStatus());
        LocalDateTime now = LocalDateTime.now();
        int affected = safeCount(adminTicketMapper.retryOrderTickets(orderId, now));
        if (affected <= 0) {
            throw new BusinessException("当前订单没有异常电子票可重试");
        }
        AdminIssueOrderDetailVO refreshed = adminTicketMapper.selectIssueOrderSummary(orderId);
        String aggregateStatus = refreshed == null || refreshed.getIssueStatus() == null
                ? "GENERATING" : refreshed.getIssueStatus();
        adminTicketLogService.recordSuccess(context, "ISSUE", "RETRY_ORDER", "ORDER", orderId,
                orderId, null, "ERROR", aggregateStatus,
                "管理员手动重试订单内 " + affected + " 张异常电子票，其他正常电子票保持不变", null);
        return buildIssueResponse(orderId, affected, aggregateStatus, now,
                "订单内异常电子票已重新进入出票队列，其他电子票状态未改变");
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminTicketSystemRefundResponse systemRefundForIssueFailure(
            Long orderId, AdminTicketSystemRefundRequest request, TicketOperationContext context) {
        requireLocalCompatOrder(orderId);
        IssueOrderBaseDTO order = requireIssueOrderForUpdate(orderId);
        requireWaitUseOrder(order.getOrderStatus());
        if (safeCount(adminTicketMapper.countCheckedTickets(orderId)) > 0) {
            throw new BusinessException("订单存在已检票电子票，不能发起出票失败退款");
        }
        if (safeCount(adminTicketMapper.countRefundRecordByOrderId(orderId)) > 0) {
            throw new BusinessException("订单已存在退款记录，不能重复退款");
        }
        if (safeCount(adminTicketMapper.countErrorTicketsByOrderId(orderId)) <= 0) {
            throw new BusinessException("订单不存在出票异常电子票，不能执行出票失败退款");
        }

        String reason = normalizeReason(request == null ? null : request.getReason(),
                DEFAULT_SYSTEM_REFUND_REASON, 255);
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = safeCount(adminTicketMapper.expireOrderTicketsForRefund(orderId, reason, now));
        if (expiredCount <= 0) {
            throw new BusinessException("当前订单没有可失效的电子票");
        }

        IssueFailedRefundInsertDTO refund = new IssueFailedRefundInsertDTO();
        refund.setRefundNo(generateRefundNo(orderId));
        refund.setOrderId(orderId);
        refund.setRefundTypeSnapshot("SYSTEM_REFUND");
        refund.setApplyTime(now);
        refund.setReason(reason);
        refund.setRefundAmount(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
        refund.setFeeAmount(BigDecimal.ZERO);
        refund.setRefundStatus(REFUND_STATUS_SUCCESS);
        refund.setRefundTime(now);
        refund.setUpdateTime(now);
        int inserted = safeCount(adminTicketMapper.insertSystemRefundRecord(refund));
        if (inserted != 1) {
            throw new BusinessException("系统退款记录创建失败");
        }

        int orderUpdated = safeCount(adminTicketMapper.updateOrderRefundSuccess(orderId, now));
        if (orderUpdated != 1) {
            throw new BusinessException("订单退款状态更新失败");
        }
        adminTicketLogService.recordSuccess(context, "ISSUE", "ISSUE_FAILED_SYSTEM_REFUND", "ORDER", orderId,
                orderId, null, ORDER_STATUS_WAIT_USE, ORDER_STATUS_REFUND_SUCCESS,
                "管理员因出票失败执行系统全额退款：" + reason, null);

        AdminTicketSystemRefundResponse response = new AdminTicketSystemRefundResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setOrderStatus(ORDER_STATUS_REFUND_SUCCESS);
        response.setExpiredCount(expiredCount);
        response.setRefundId(refund.getRefundId());
        response.setRefundNo(refund.getRefundNo());
        response.setRefundStatus(REFUND_STATUS_SUCCESS);
        response.setRefundAmount(refund.getRefundAmount());
        response.setRefundTime(now);
        response.setMessage("出票失败系统全额退款已完成");
        return response;
    }

    public AdminTicketVerifyVO verifyTicket(String code) {
        String safeCode = normalizeTicketCode(code);
        AdminTicketVerifyVO ticket = adminTicketMapper.selectTicketByCode(safeCode);
        if (ticket == null) {
            AdminTicketVerifyVO invalid = new AdminTicketVerifyVO();
            invalid.setValid(false);
            invalid.setCanCheck(false);
            invalid.setMessage("票码不存在");
            return invalid;
        }
        applyVerifyResult(ticket);
        return ticket;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminTicketCheckResponse checkTicket(AdminTicketCheckRequest request, TicketOperationContext context) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        String code = normalizeTicketCode(request.getCode());
        AdminTicketVerifyVO ticket = adminTicketMapper.selectTicketByCodeForUpdate(code);
        if (ticket == null) {
            throw new BusinessException("票码不存在");
        }
        if (!ORDER_STATUS_WAIT_USE.equals(ticket.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可检票");
        }
        if (!TICKET_STATUS_UNUSED.equals(ticket.getTicketStatus())) {
            throw new BusinessException(ticketStatusMessage(ticket.getTicketStatus()));
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = safeCount(adminTicketMapper.checkTicket(ticket.getTicketId(), now));
        if (updated != 1) {
            throw new BusinessException("检票状态已变化，请重新扫码");
        }

        boolean orderFinished = safeCount(adminTicketMapper.finishOrderIfAllTicketsChecked(ticket.getOrderId(), now)) == 1;
        AdminTicketCheckResponse response = new AdminTicketCheckResponse();
        response.setSuccess(true);
        response.setTicketId(ticket.getTicketId());
        response.setOrderId(ticket.getOrderId());
        response.setTicketNo(ticket.getTicketNo());
        response.setTicketStatus(TICKET_STATUS_CHECKED);
        response.setOrderFinished(orderFinished);
        response.setOrderStatus(orderFinished ? "FINISHED" : ORDER_STATUS_WAIT_USE);
        response.setCheckTime(now);
        response.setMessage(orderFinished ? "检票成功，订单内全部电子票已核销" : "检票成功");
        adminTicketLogService.recordSuccess(context, "CHECK", "CHECK_TICKET", "TICKET", ticket.getTicketId(),
                ticket.getOrderId(), ticket.getTicketId(), TICKET_STATUS_UNUSED, TICKET_STATUS_CHECKED,
                "管理员完成检票", null);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public byte[] getTicketQrCode(Long ticketId, TicketOperationContext context) {
        AdminTicketDetailVO detail = getTicketDetail(ticketId);
        String qrValue = trimToNull(detail.getQrCodeValue());
        if (qrValue == null) {
            throw new BusinessException("当前电子票尚未生成二维码");
        }
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(qrValue, BarcodeFormat.QR_CODE, 360, 360, hints);
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF111827 : 0xFFFFFFFF);
                }
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);
            adminTicketLogService.recordSuccess(context, "QR", "VIEW_QR", "TICKET", ticketId,
                    detail.getOrderId(), ticketId, detail.getTicketStatus(), detail.getTicketStatus(),
                    "管理员查看电子票二维码", null);
            return output.toByteArray();
        } catch (WriterException | IOException e) {
            throw new BusinessException("二维码生成失败");
        }
    }

    public AdminTicketListPageVO getCheckRecords(String keyword, Long projectId, Long sessionId,
                                                  String dateFrom, String dateTo,
                                                  Integer pageNo, Integer pageSize) {
        validateOptionalId(projectId, "projectId");
        validateOptionalId(sessionId, "sessionId");
        PageParam page = normalizePage(pageNo, pageSize);
        DateRange range = normalizeDateRange(dateFrom, dateTo);
        String safeKeyword = trimToNull(keyword);
        Integer total = adminTicketMapper.countCheckRecords(
                safeKeyword, projectId, sessionId, range.getDateFrom(), range.getDateTo()
        );
        List<AdminTicketItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminTicketMapper.selectCheckRecords(
                        safeKeyword, projectId, sessionId, range.getDateFrom(), range.getDateTo(),
                        page.getPageSize(), page.getOffset()
                );
        return buildTicketPage(total, page, items);
    }

    private AdminTicketListPageVO buildTicketPage(Integer total, PageParam page, List<AdminTicketItemVO> items) {
        AdminTicketListPageVO vo = new AdminTicketListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(page.getPageNo());
        vo.setPageSize(page.getPageSize());
        vo.setItems(items);
        return vo;
    }

    private AdminTicketStateDTO requireTicketStateForUpdate(Long ticketId) {
        AdminTicketStateDTO state = adminTicketMapper.selectTicketStateForUpdate(ticketId);
        if (state == null) {
            throw new BusinessException("电子票不存在");
        }
        return state;
    }

    private IssueOrderBaseDTO requireIssueOrderForUpdate(Long orderId) {
        validateId(orderId, "orderId");
        IssueOrderBaseDTO order = adminTicketMapper.selectIssueOrderForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void requireLocalCompatOrder(Long orderId) {
        String mode = adminTicketMapper.selectOrderFulfillmentMode(orderId);
        if ("TICKET_SOURCE".equals(mode)) {
            throw new BusinessException("第三方票源订单的凭证和座位由票源同步，请使用第三方出票任务接口");
        }
    }

    private void requireWaitUseOrder(String orderStatus) {
        if (!ORDER_STATUS_WAIT_USE.equals(orderStatus)) {
            throw new BusinessException("只有待使用订单可以执行该操作");
        }
    }

    private AdminTicketIssueResponse buildIssueResponse(Long orderId, int affectedCount,
                                                         String issueStatus, LocalDateTime operationTime,
                                                         String message) {
        AdminTicketIssueResponse response = new AdminTicketIssueResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setAffectedCount(affectedCount);
        response.setIssueStatus(issueStatus);
        response.setOperationTime(operationTime);
        response.setMessage(message);
        return response;
    }

    private void applyVerifyResult(AdminTicketVerifyVO ticket) {
        ticket.setValid(true);
        if (!ORDER_STATUS_WAIT_USE.equals(ticket.getOrderStatus())) {
            ticket.setCanCheck(false);
            ticket.setMessage("当前订单状态不可检票");
            return;
        }
        if (TICKET_STATUS_UNUSED.equals(ticket.getTicketStatus())) {
            ticket.setCanCheck(true);
            ticket.setMessage("电子票有效，可以检票");
            return;
        }
        ticket.setCanCheck(false);
        ticket.setMessage(ticketStatusMessage(ticket.getTicketStatus()));
    }

    private String ticketStatusMessage(String ticketStatus) {
        if (TICKET_STATUS_CHECKED.equals(ticketStatus)) {
            return "电子票已检票，不能重复入场";
        }
        if (TICKET_STATUS_GENERATING.equals(ticketStatus)) {
            return "电子票仍在生成中";
        }
        if (TICKET_STATUS_EXPIRED.equals(ticketStatus)) {
            return "电子票已失效";
        }
        if (TICKET_STATUS_ERROR.equals(ticketStatus)) {
            return "电子票状态异常";
        }
        return "电子票状态不可检票";
    }

    private String normalizeTicketCode(String code) {
        String safeCode = trimToNull(code);
        if (safeCode == null) {
            throw new BusinessException("票码不能为空");
        }
        if (safeCode.length() > 1024) {
            throw new BusinessException("票码长度不合法");
        }
        return safeCode;
    }

    private String normalizeReason(String value, String defaultValue, int maxLength) {
        String safeValue = trimToNull(value);
        if (safeValue == null) {
            return defaultValue;
        }
        if (safeValue.length() > maxLength) {
            throw new BusinessException("原因不能超过 " + maxLength + " 个字符");
        }
        return safeValue;
    }

    private PageParam normalizePage(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        return new PageParam(safePageNo, safePageSize, (safePageNo - 1) * safePageSize);
    }

    private DateRange normalizeDateRange(String dateFrom, String dateTo) {
        String safeFrom = normalizeDate(dateFrom, "dateFrom");
        String safeTo = normalizeDate(dateTo, "dateTo");
        if (safeFrom != null && safeTo != null
                && LocalDate.parse(safeFrom).isAfter(LocalDate.parse(safeTo))) {
            throw new BusinessException("dateFrom 不能晚于 dateTo");
        }
        return new DateRange(safeFrom, safeTo);
    }

    private String normalizeDate(String value, String name) {
        String safeValue = trimToNull(value);
        if (safeValue == null) {
            return null;
        }
        try {
            return LocalDate.parse(safeValue).toString();
        } catch (DateTimeParseException e) {
            throw new BusinessException(name + " 格式必须为 yyyy-MM-dd");
        }
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }

    private void validateOptionalId(Long id, String name) {
        if (id != null && id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateRefundNo(Long orderId) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RF" + timePart + orderId + randomPart;
    }

    private static class PageParam {
        private final int pageNo;
        private final int pageSize;
        private final int offset;

        private PageParam(int pageNo, int pageSize, int offset) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.offset = offset;
        }

        public int getPageNo() {
            return pageNo;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getOffset() {
            return offset;
        }
    }

    private static class DateRange {
        private final String dateFrom;
        private final String dateTo;

        private DateRange(String dateFrom, String dateTo) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }

        public String getDateFrom() {
            return dateFrom;
        }

        public String getDateTo() {
            return dateTo;
        }
    }
}
