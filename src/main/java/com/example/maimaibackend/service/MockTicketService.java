package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.ticket.IssueFailedRefundInsertDTO;
import com.example.maimaibackend.dto.ticket.IssueOrderBaseDTO;
import com.example.maimaibackend.dto.ticket.TicketCheckBaseDTO;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.mapper.MockTicketMapper;
import com.example.maimaibackend.vo.ticket.MockTicketCheckResponse;
import com.example.maimaibackend.vo.ticket.MockTicketIssueFailedResponse;
import com.example.maimaibackend.vo.ticket.MockTicketIssueSuccessResponse;
import com.example.maimaibackend.service.admin.AdminTicketLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MockTicketService {

    private static final String ORDER_STATUS_WAIT_USE = "WAIT_USE";
    private static final String ORDER_STATUS_REFUND_SUCCESS = "REFUND_SUCCESS";
    private static final String TICKET_STATUS_UNUSED = "UNUSED";
    private static final String TICKET_STATUS_CHECKED = "CHECKED";
    private static final String TICKET_STATUS_EXPIRED = "EXPIRED";
    private static final String DEFAULT_ISSUE_FAILED_REASON = "Mock 出票失败，系统自动全额退款";

    private final MockTicketMapper mockTicketMapper;
    private final AdminTicketLogService adminTicketLogService;

    public MockTicketService(MockTicketMapper mockTicketMapper, AdminTicketLogService adminTicketLogService) {
        this.mockTicketMapper = mockTicketMapper;
        this.adminTicketLogService = adminTicketLogService;
    }

    @Transactional(rollbackFor = Exception.class)
    public MockTicketIssueSuccessResponse issueSuccess(Long orderId) {
        validateId(orderId, "orderId");

        IssueOrderBaseDTO order = mockTicketMapper.selectIssueOrderForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!ORDER_STATUS_WAIT_USE.equals(order.getOrderStatus())) {
            throw new BusinessException("只有待使用订单可以模拟出票成功");
        }
        requireLocalCompat(order);

        int generatingCount = mockTicketMapper.countGeneratingTickets(orderId);
        if (generatingCount <= 0) {
            throw new BusinessException("当前订单没有生成中的电子票");
        }

        LocalDateTime issueTime = LocalDateTime.now();
        int issuedCount = mockTicketMapper.issueGeneratingTickets(orderId, issueTime);
        if (issuedCount <= 0) {
            throw new BusinessException("出票失败，请刷新后重试");
        }
        mockTicketMapper.updateOrderTicketIssuedTime(orderId, issueTime);

        MockTicketIssueSuccessResponse response = new MockTicketIssueSuccessResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setOrderStatus(ORDER_STATUS_WAIT_USE);
        response.setIssuedCount(issuedCount);
        response.setTicketIssuedTime(issueTime);
        adminTicketLogService.recordSuccess(TicketOperationContext.system("MockTicketService"),
                "ISSUE", "SYSTEM_ISSUE_SUCCESS", "ORDER", orderId, orderId, null,
                "GENERATING", "ISSUED", "系统模拟出票成功，共处理 " + issuedCount + " 张电子票", null);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public MockTicketIssueFailedResponse issueFailed(Long orderId, String abnormalReason) {
        validateId(orderId, "orderId");

        IssueOrderBaseDTO order = mockTicketMapper.selectIssueOrderForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!ORDER_STATUS_WAIT_USE.equals(order.getOrderStatus())) {
            throw new BusinessException("只有待使用订单可以模拟出票失败");
        }
        requireLocalCompat(order);
        if (mockTicketMapper.countRefundRecordByOrderId(orderId) > 0) {
            throw new BusinessException("当前订单已存在退款记录，不能重复模拟出票失败");
        }

        int candidateCount = mockTicketMapper.countIssueFailedCandidateTickets(orderId);
        if (candidateCount <= 0) {
            throw new BusinessException("当前订单没有可置为失效的生成中/异常电子票");
        }

        LocalDateTime now = LocalDateTime.now();
        String realReason = normalizeIssueFailedReason(abnormalReason);
        int expiredCount = mockTicketMapper.expireTicketsForIssueFailed(orderId, realReason, now);
        if (expiredCount <= 0) {
            throw new BusinessException("更新电子票失效状态失败");
        }

        IssueFailedRefundInsertDTO refund = new IssueFailedRefundInsertDTO();
        refund.setRefundNo(generateRefundNo(orderId));
        refund.setOrderId(orderId);
        refund.setRefundTypeSnapshot("SYSTEM_REFUND");
        refund.setApplyTime(now);
        refund.setReason(realReason);
        refund.setRefundAmount(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
        refund.setFeeAmount(BigDecimal.ZERO);
        refund.setRefundStatus(ORDER_STATUS_REFUND_SUCCESS);
        refund.setRefundTime(now);
        refund.setUpdateTime(now);
        mockTicketMapper.insertIssueFailedRefundRecord(refund);

        int updateOrderRows = mockTicketMapper.updateOrderRefundSuccess(orderId, now);
        if (updateOrderRows != 1) {
            throw new BusinessException("更新订单退款成功状态失败");
        }

        MockTicketIssueFailedResponse response = new MockTicketIssueFailedResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setOrderStatus(ORDER_STATUS_REFUND_SUCCESS);
        response.setExpiredCount(expiredCount);
        response.setRefundId(refund.getRefundId());
        response.setRefundNo(refund.getRefundNo());
        response.setRefundStatus(ORDER_STATUS_REFUND_SUCCESS);
        response.setRefundAmount(refund.getRefundAmount());
        response.setRefundTime(now);
        adminTicketLogService.recordSuccess(TicketOperationContext.system("MockTicketService"),
                "ISSUE", "SYSTEM_ISSUE_FAILED_REFUND", "ORDER", orderId, orderId, null,
                "GENERATING/ERROR", ORDER_STATUS_REFUND_SUCCESS,
                "系统模拟出票失败并全额退款：" + realReason, null);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public MockTicketCheckResponse checkTicket(Long ticketId) {
        validateId(ticketId, "ticketId");

        TicketCheckBaseDTO ticket = mockTicketMapper.selectTicketForCheck(ticketId);
        if (ticket == null) {
            throw new BusinessException("电子票不存在");
        }
        if (!TICKET_STATUS_UNUSED.equals(ticket.getTicketStatus())) {
            throw new BusinessException("只有未使用电子票可以检票");
        }

        LocalDateTime checkTime = LocalDateTime.now();
        int updated = mockTicketMapper.checkTicket(ticketId, checkTime);
        if (updated <= 0) {
            throw new BusinessException("检票失败，请刷新后重试");
        }

        boolean orderFinished = mockTicketMapper.finishOrderIfAllTicketsChecked(ticket.getOrderId(), checkTime) == 1;
        MockTicketCheckResponse response = new MockTicketCheckResponse();
        response.setSuccess(true);
        response.setTicketId(ticketId);
        response.setOrderId(ticket.getOrderId());
        response.setTicketStatus(TICKET_STATUS_CHECKED);
        response.setOrderFinished(orderFinished);
        response.setOrderStatus(orderFinished ? "FINISHED" : ORDER_STATUS_WAIT_USE);
        response.setCheckTime(checkTime);
        adminTicketLogService.recordSuccess(TicketOperationContext.system("MockTicketService"),
                "CHECK", "SYSTEM_CHECK_TICKET", "TICKET", ticketId, ticket.getOrderId(), ticketId,
                TICKET_STATUS_UNUSED, TICKET_STATUS_CHECKED, "系统模拟检票成功", null);
        return response;
    }

    private void requireLocalCompat(IssueOrderBaseDTO order) {
        if ("TICKET_SOURCE".equals(order.getFulfillmentMode())) {
            throw new BusinessException("第三方票源订单必须通过自动出票任务同步，不能使用本地 Mock 出票");
        }
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException("请求参数无效：" + name);
        }
    }

    private String normalizeIssueFailedReason(String abnormalReason) {
        if (abnormalReason == null || abnormalReason.trim().isEmpty()) {
            return DEFAULT_ISSUE_FAILED_REASON;
        }
        String trimmed = abnormalReason.trim();
        if (trimmed.length() > 255) {
            throw new BusinessException("出票失败原因不能超过 255 个字符");
        }
        return trimmed;
    }

    private String generateRefundNo(Long orderId) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RF" + timePart + orderId + randomPart;
    }
}
