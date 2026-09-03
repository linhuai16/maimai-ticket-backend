package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.refund.MockRefundBaseDTO;
import com.example.maimaibackend.dto.refund.RefundApplyBaseDTO;
import com.example.maimaibackend.dto.refund.RefundOrderRequest;
import com.example.maimaibackend.dto.refund.RefundRecordInsertDTO;
import com.example.maimaibackend.mapper.RefundMapper;
import com.example.maimaibackend.ticketsource.refund.TicketSourceRefundService;
import com.example.maimaibackend.ticketsource.provider.model.ProviderRefundQuote;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12RefundRequest;
import com.example.maimaibackend.vo.refund.MockRefundSuccessResponse;
import com.example.maimaibackend.vo.refund.RefundApplyPageVO;
import com.example.maimaibackend.vo.refund.RefundOrderResponse;
import com.example.maimaibackend.vo.refund.RefundProgressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RefundService {

    private static final String ORDER_STATUS_WAIT_USE = "WAIT_USE";
    private static final String ORDER_STATUS_REFUNDING = "REFUNDING";
    private static final String ORDER_STATUS_REFUND_SUCCESS = "REFUND_SUCCESS";
    private static final String REFUND_STATUS_REFUNDING = "REFUNDING";
    private static final String REFUND_STATUS_REFUND_SUCCESS = "REFUND_SUCCESS";
    private static final String REFUND_TYPE_CONDITIONAL = "CONDITIONAL_REFUND";
    private static final String REFUND_TYPE_NO_REFUND = "NO_REFUND";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal DEFAULT_FEE_RATE = BigDecimal.ZERO.setScale(4);

    private final RefundMapper refundMapper;
    private final TicketSourceRefundService ticketSourceRefundService;
    private final TicketSourceWorkflowService workflowService;

    public RefundService(RefundMapper refundMapper,
                         TicketSourceRefundService ticketSourceRefundService,
                         TicketSourceWorkflowService workflowService) {
        this.refundMapper = refundMapper;
        this.ticketSourceRefundService = ticketSourceRefundService;
        this.workflowService = workflowService;
    }

    public RefundApplyPageVO getRefundConfirm(Long userId, Long orderId) {
        validateId(userId, "userId");
        validateId(orderId, "orderId");

        RefundApplyBaseDTO base = refundMapper.selectRefundApplyBase(orderId, userId, LocalDateTime.now());
        if (base == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }
        if (!ORDER_STATUS_WAIT_USE.equals(base.getOrderStatus())) {
            throw new BusinessException("只有待使用订单可以申请退款");
        }

        if ("TICKET_SOURCE".equals(base.getFulfillmentMode())) {
            ProviderRefundQuote quote = workflowService.quoteRefund(orderId, userId);
            RefundApplyPageVO vo = baseVo(base);
            vo.setFeeRate(null);
            vo.setFeeAmount(quote.serviceFee() == null ? ZERO : quote.serviceFee().toMajor());
            vo.setExpectedRefundAmount(quote.refundableAmount() == null ? ZERO : quote.refundableAmount().toMajor());
            vo.setRefundType("FULL_ORDER");
            vo.setCanRefund(quote.refundable());
            BigDecimal refundableDelivery = quote.refundableDeliveryFee() == null ? ZERO : quote.refundableDeliveryFee().toMajor();
            BigDecimal nonRefundableDelivery = quote.nonRefundableDeliveryFee() == null ? ZERO : quote.nonRefundableDeliveryFee().toMajor();
            vo.setRefundRuleText(quote.refundable()
                    ? "本订单仅支持整单退款；预计可退配送费=" + refundableDelivery + "，不可退配送费=" + nonRefundableDelivery + "；最终金额以提交时确认结果为准"
                    : "当前订单暂不支持退款");
            return vo;
        }

        RefundAmount amount = calculateRefundAmount(base, false);

        RefundApplyPageVO vo = new RefundApplyPageVO();
        vo.setOrderId(base.getOrderId());
        vo.setOrderNo(base.getOrderNo());
        vo.setProjectId(base.getProjectId());
        vo.setSessionId(base.getSessionId());
        vo.setTitle(base.getTitle());
        vo.setPosterUrl(base.getPosterUrl());
        vo.setStartTime(base.getStartTime());
        vo.setVenueName(base.getVenueName());
        vo.setVenueAddress(base.getVenueAddress());
        vo.setSkuName(base.getSkuName());
        vo.setQuantity(base.getQuantity());
        vo.setPayAmount(scale2(base.getPayAmount()));
        vo.setFeeRate(amount.getFeeRate());
        vo.setFeeAmount(amount.getFeeAmount());
        vo.setExpectedRefundAmount(amount.getRefundAmount());
        vo.setRefundType(base.getRefundType());
        vo.setCanRefund(amount.getCanRefund());
        vo.setRefundRuleText(amount.getRuleText());
        return vo;
    }

    public RefundProgressVO getRefundProgress(Long userId, Long orderId) {
        validateId(userId, "userId");
        validateId(orderId, "orderId");
        RefundProgressVO value = refundMapper.selectRefundProgressByOrder(orderId, userId);
        if (value == null) {
            throw new BusinessException("当前订单暂无退款记录");
        }
        return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundOrderResponse applyRefund(Long orderId, RefundOrderRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        validateId(orderId, "orderId");
        validateId(request.getUserId(), "userId");

        LocalDateTime now = LocalDateTime.now();
        RefundApplyBaseDTO base = refundMapper.selectRefundApplyBase(orderId, request.getUserId(), now);
        if (base == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }
        if (!ORDER_STATUS_WAIT_USE.equals(base.getOrderStatus())) {
            throw new BusinessException("只有待使用订单可以申请退款");
        }
        if (refundMapper.countRefundByOrderId(orderId) > 0) {
            throw new BusinessException("该订单已存在退款记录");
        }

        if ("TICKET_SOURCE".equals(base.getFulfillmentMode())) {
            // 保留麦麦既有“用户申请 -> 后台审核 -> 第三方整单退款”流程。
            // 用户申请只创建 V1.2 PENDING_REVIEW 桥接；管理员审核通过时再重新向第三方试算并提交。
            java.util.Map<String,Object> result = workflowService.prepareRefund(orderId,
                    new V12RefundRequest(request.getUserId(), "USER_REQUEST", request.getReason()));
            RefundOrderResponse response = new RefundOrderResponse();
            response.setRefundId(((Number) result.get("refundId")).longValue());
            response.setStatus(String.valueOf(result.get("refundStatus")));
            Object amount = result.get("refundAmount");
            response.setExpectedRefundAmount(amount instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(amount)));
            return response;
        }

        RefundAmount amount = calculateRefundAmount(base, true);
        if (!Boolean.TRUE.equals(amount.getCanRefund())) {
            throw new BusinessException(amount.getRuleText());
        }

        RefundRecordInsertDTO dto = new RefundRecordInsertDTO();
        dto.setRefundNo(generateRefundNo(now));
        dto.setOrderId(orderId);
        dto.setRefundRuleId(base.getRefundRuleId());
        dto.setMatchedStageId(base.getStageId());
        dto.setRefundTypeSnapshot(base.getRefundType());
        dto.setFeeRateSnapshot(amount.getFeeRate());
        dto.setApplyTime(now);
        dto.setReason(trimToNull(request.getReason()));
        dto.setRefundAmount(amount.getRefundAmount());
        dto.setFeeAmount(amount.getFeeAmount());
        dto.setRefundStatus(REFUND_STATUS_REFUNDING);
        dto.setUpdateTime(now);

        refundMapper.insertRefundRecord(dto);

        int orderUpdated = refundMapper.updateOrderToRefunding(orderId, now);
        if (orderUpdated <= 0) {
            throw new BusinessException("订单状态已变化，退款申请失败");
        }

        refundMapper.expireTicketsByOrderId(orderId, now, "用户申请退款，电子票暂时失效");
        ticketSourceRefundService.createPendingBridge(dto.getRefundId(), orderId, dto.getRefundNo());

        RefundOrderResponse response = new RefundOrderResponse();
        response.setRefundId(dto.getRefundId());
        response.setStatus(REFUND_STATUS_REFUNDING);
        response.setExpectedRefundAmount(amount.getRefundAmount());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public MockRefundSuccessResponse mockRefundSuccess(Long refundId) {
        validateId(refundId, "refundId");

        if (ticketSourceRefundService.isTicketSourceRefund(refundId)) {
            throw new BusinessException("第三方履约退款不能使用本地模拟退款成功入口");
        }

        MockRefundBaseDTO base = refundMapper.selectMockRefundBase(refundId);
        if (base == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (!REFUND_STATUS_REFUNDING.equals(base.getRefundStatus())) {
            throw new BusinessException("只有退款中的记录可以模拟退款成功");
        }
        if (!ORDER_STATUS_REFUNDING.equals(base.getOrderStatus())) {
            throw new BusinessException("订单不是退款中状态，不能模拟退款成功");
        }

        LocalDateTime now = LocalDateTime.now();
        refundMapper.updateRefundSuccess(refundId, now);
        refundMapper.updateOrderRefundSuccess(base.getOrderId(), now);
        refundMapper.ensureTicketsExpired(base.getOrderId(), now);

        MockRefundSuccessResponse response = new MockRefundSuccessResponse();
        response.setSuccess(true);
        response.setRefundId(refundId);
        response.setRefundStatus(REFUND_STATUS_REFUND_SUCCESS);
        response.setRefundTime(now);
        return response;
    }

    private RefundApplyPageVO baseVo(RefundApplyBaseDTO base) {
        RefundApplyPageVO vo = new RefundApplyPageVO();
        vo.setOrderId(base.getOrderId());
        vo.setOrderNo(base.getOrderNo());
        vo.setProjectId(base.getProjectId());
        vo.setSessionId(base.getSessionId());
        vo.setTitle(base.getTitle());
        vo.setPosterUrl(base.getPosterUrl());
        vo.setStartTime(base.getStartTime());
        vo.setVenueName(base.getVenueName());
        vo.setVenueAddress(base.getVenueAddress());
        vo.setSkuName(base.getSkuName());
        vo.setQuantity(base.getQuantity());
        vo.setPayAmount(scale2(base.getPayAmount()));
        return vo;
    }

    private RefundAmount calculateRefundAmount(RefundApplyBaseDTO base, boolean strict) {
        if (base.getRefundRuleId() == null || base.getRefundType() == null) {
            if (strict) {
                throw new BusinessException("当前演出未配置退款规则");
            }
            return new RefundAmount(false, DEFAULT_FEE_RATE, ZERO, ZERO, "当前演出未配置退款规则，暂不可退");
        }

        if (REFUND_TYPE_NO_REFUND.equals(base.getRefundType())) {
            return new RefundAmount(false, DEFAULT_FEE_RATE, ZERO, ZERO, "本项目为不可退项目，支付成功后不支持主动退款");
        }

        if (!REFUND_TYPE_CONDITIONAL.equals(base.getRefundType())) {
            return new RefundAmount(false, DEFAULT_FEE_RATE, ZERO, ZERO, "未知退款类型，暂不可退");
        }

        if (base.getStageId() == null || base.getFeeRate() == null) {
            return new RefundAmount(false, DEFAULT_FEE_RATE, ZERO, ZERO, "当前距离开演时间过近，已不支持退款");
        }

        BigDecimal payAmount = scale2(base.getPayAmount());
        BigDecimal feeRate = base.getFeeRate().setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal feeAmount = payAmount.multiply(feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal refundAmount = payAmount.subtract(feeAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
        String ruleText = "条件退项目，当前手续费比例为 " + feeRate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
        return new RefundAmount(true, feeRate, feeAmount, refundAmount, ruleText);
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(fieldName + " 不合法");
        }
    }

    private BigDecimal scale2(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private String generateRefundNo(LocalDateTime now) {
        return "RF" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private static class RefundAmount {
        private final Boolean canRefund;
        private final BigDecimal feeRate;
        private final BigDecimal feeAmount;
        private final BigDecimal refundAmount;
        private final String ruleText;

        private RefundAmount(Boolean canRefund, BigDecimal feeRate, BigDecimal feeAmount,
                             BigDecimal refundAmount, String ruleText) {
            this.canRefund = canRefund;
            this.feeRate = feeRate;
            this.feeAmount = feeAmount;
            this.refundAmount = refundAmount;
            this.ruleText = ruleText;
        }

        public Boolean getCanRefund() {
            return canRefund;
        }

        public BigDecimal getFeeRate() {
            return feeRate;
        }

        public BigDecimal getFeeAmount() {
            return feeAmount;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public String getRuleText() {
            return ruleText;
        }
    }
}
