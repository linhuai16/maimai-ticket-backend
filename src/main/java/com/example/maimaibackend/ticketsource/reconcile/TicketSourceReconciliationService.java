package com.example.maimaibackend.ticketsource.reconcile;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceProviderMapper;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceReconciliationMapper;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.*;
import com.example.maimaibackend.ticketsource.reconcile.dto.TicketSourceReconciliationRequest;
import com.example.maimaibackend.ticketsource.reconcile.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TicketSourceReconciliationService {
    private final TicketSourceReconciliationMapper mapper;
    private final TicketSourceProviderMapper providerMapper;
    private final TicketSourceGateway gateway;

    public TicketSourceReconciliationService(TicketSourceReconciliationMapper mapper,
                                             TicketSourceProviderMapper providerMapper,
                                             TicketSourceGateway gateway) {
        this.mapper = mapper;
        this.providerMapper = providerMapper;
        this.gateway = gateway;
    }

    public TicketSourceReconciliationBatch run(TicketSourceReconciliationRequest request) {
        if (request == null || request.getProviderCode() == null || request.getProviderCode().isBlank()) {
            throw new BusinessException("providerCode 不能为空");
        }
        String providerCode = request.getProviderCode().trim().toUpperCase(Locale.ROOT);
        TicketSourceProvider provider = providerMapper.selectByCode(providerCode);
        if (provider == null) throw new BusinessException("票源不存在: " + providerCode);
        List<Long> orderIds = normalizeOrderIds(request.getOrderIds());
        if (orderIds.isEmpty()) throw new BusinessException("本次对账必须指定 orderIds，避免误扫全库");

        TicketSourceReconciliationBatch batch = new TicketSourceReconciliationBatch();
        batch.setBatchNo(generateBatchNo());
        batch.setProviderId(provider.getProviderId());
        batch.setProviderCode(providerCode);
        batch.setStartTime(LocalDateTime.now());
        batch.setRemark("指定订单对账，orderIds=" + orderIds);
        mapper.insertBatch(batch);

        List<TicketSourceReconciliationCandidate> candidates = mapper.selectCandidates(provider.getProviderId(), orderIds);
        int matched = 0;
        int differences = 0;
        int errors = 0;
        for (TicketSourceReconciliationCandidate candidate : candidates) {
            TicketSourceReconciliationDetail detail = reconcileOne(batch.getBatchId(), candidate);
            mapper.insertDetail(detail);
            if ("MATCH".equals(detail.getCompareStatus())) matched++;
            else if ("DIFFERENCE".equals(detail.getCompareStatus())) differences++;
            else errors++;
        }
        int missing = Math.max(0, orderIds.size() - candidates.size());
        errors += missing;
        String status = errors == 0 && differences == 0 ? "SUCCESS"
                : matched == 0 ? "FAILED" : "PARTIAL_FAILED";
        String remark = missing == 0 ? "对账完成" : "有 " + missing + " 个指定订单不属于该票源或不存在";
        mapper.finishBatch(batch.getBatchId(), status, orderIds.size(), matched, differences, errors, remark);
        return getBatch(batch.getBatchId());
    }

    public TicketSourceReconciliationBatch getBatch(Long batchId) {
        if (batchId == null || batchId <= 0) throw new BusinessException("batchId 必须为正整数");
        TicketSourceReconciliationBatch batch = mapper.selectBatch(batchId);
        if (batch == null) throw new BusinessException("对账批次不存在");
        batch.setDetails(mapper.selectDetails(batchId));
        return batch;
    }

    private TicketSourceReconciliationDetail reconcileOne(Long batchId,
                                                          TicketSourceReconciliationCandidate local) {
        TicketSourceReconciliationDetail d = baseDetail(batchId, local);
        TicketSourceCallResult<TicketSourceProviderOrder> orderCall =
                gateway.getOrder(local.getProviderCode(), local.getProviderOrderId());
        if (!ok(orderCall)) return error(d, "GET_ORDER_FAILED", message(orderCall));

        TicketSourceCallResult<TicketSourceDelivery> ticketCall =
                gateway.getTickets(local.getProviderCode(), local.getProviderOrderId());
        if (!ok(ticketCall)) return error(d, "GET_TICKETS_FAILED", message(ticketCall));

        TicketSourceProviderOrder providerOrder = orderCall.getData();
        TicketSourceDelivery delivery = ticketCall.getData();
        String providerRefundStatus = "NOT_REQUESTED";
        BigDecimal providerRefundAmount = null;
        if (local.getProviderRefundId() != null && !local.getProviderRefundId().isBlank()) {
            TicketSourceCallResult<TicketSourceRefund> refundCall =
                    gateway.getRefund(local.getProviderCode(), local.getProviderRefundId());
            if (!ok(refundCall)) return error(d, "GET_REFUND_FAILED", message(refundCall));
            providerRefundStatus = normalize(refundCall.getData().getRefundStatus());
            providerRefundAmount = refundCall.getData().getRefundAmount();
        }

        int providerTotal = delivery.getTickets() == null ? 0 : delivery.getTickets().size();
        int providerValid = delivery.getTickets() == null ? 0 : (int) delivery.getTickets().stream()
                .filter(t -> "ISSUED".equals(normalize(t.getTicketStatus()))).count();
        d.setProviderOrderStatus(normalize(providerOrder.getOrderStatus()));
        d.setProviderPayAmount(providerOrder.getTotalAmount());
        d.setProviderRefundStatus(providerRefundStatus);
        d.setProviderRefundAmount(providerRefundAmount);
        d.setProviderTicketTotal(providerTotal);
        d.setProviderValidTicketCount(providerValid);

        List<String> diff = new ArrayList<>();
        if (!expectedProviderOrderStatus(local.getLocalOrderStatus()).contains(d.getProviderOrderStatus())) {
            diff.add("ORDER_STATUS");
        }
        if (!sameAmount(local.getLocalPayAmount(), d.getProviderPayAmount())) diff.add("PAY_AMOUNT");
        if (!refundMatches(local.getLocalRefundStatus(), providerRefundStatus)) diff.add("REFUND_STATUS");
        if (requiresRefundAmountCompare(local.getLocalRefundStatus(), providerRefundStatus)
                && !sameAmount(local.getLocalRefundAmount(), providerRefundAmount)) {
            diff.add("REFUND_AMOUNT");
        }
        if (!Objects.equals(safe(local.getLocalTicketTotal()), providerTotal)) diff.add("TICKET_TOTAL");
        if (!Objects.equals(safe(local.getLocalValidTicketCount()), providerValid)) diff.add("VALID_TICKET_COUNT");

        d.setDifferenceTypes(diff.isEmpty() ? null : String.join(",", diff));
        d.setCompareStatus(diff.isEmpty() ? "MATCH" : "DIFFERENCE");
        d.setSnapshotText("paymentStatus=" + local.getPaymentStatus()
                + ",deliveryStatus=" + delivery.getDeliveryStatus()
                + ",providerRefundStatus=" + providerRefundStatus);
        return d;
    }

    private TicketSourceReconciliationDetail baseDetail(Long batchId,
                                                         TicketSourceReconciliationCandidate c) {
        TicketSourceReconciliationDetail d = new TicketSourceReconciliationDetail();
        d.setBatchId(batchId);
        d.setOrderId(c.getOrderId());
        d.setOrderNo(c.getOrderNo());
        d.setProviderOrderId(c.getProviderOrderId());
        d.setLocalOrderStatus(c.getLocalOrderStatus());
        d.setLocalPayAmount(c.getLocalPayAmount());
        d.setLocalRefundStatus(c.getLocalRefundStatus() == null ? "NONE" : c.getLocalRefundStatus());
        d.setLocalRefundAmount(c.getLocalRefundAmount());
        d.setLocalValidTicketCount(safe(c.getLocalValidTicketCount()));
        d.setLocalTicketTotal(safe(c.getLocalTicketTotal()));
        d.setProviderValidTicketCount(0);
        d.setProviderTicketTotal(0);
        return d;
    }

    private TicketSourceReconciliationDetail error(TicketSourceReconciliationDetail d,
                                                    String code, String message) {
        d.setCompareStatus("ERROR");
        d.setErrorCode(code);
        d.setErrorMessage(message);
        d.setDifferenceTypes("REMOTE_CALL_ERROR");
        return d;
    }

    private Set<String> expectedProviderOrderStatus(String localStatus) {
        if ("REFUND_SUCCESS".equals(localStatus)) return Set.of("REFUNDED");
        if ("REFUNDING".equals(localStatus)) return Set.of("PAID", "REFUNDING", "REFUNDED");
        if ("WAIT_USE".equals(localStatus) || "FINISHED".equals(localStatus)) return Set.of("PAID");
        if ("WAIT_PAY".equals(localStatus)) return Set.of("WAIT_PAY");
        if ("CANCELED".equals(localStatus)) return Set.of("CANCELED", "EXPIRED");
        return Set.of(localStatus == null ? "UNKNOWN" : localStatus);
    }

    private boolean refundMatches(String localStatus, String providerStatus) {
        String local = normalize(localStatus);
        String provider = normalize(providerStatus);
        if ("REFUND_SUCCESS".equals(local)) return "SUCCESS".equals(provider);
        if ("REFUNDING".equals(local)) return Set.of("PROCESSING", "PENDING").contains(provider);
        if ("REFUND_FAILED".equals(local) || "NONE".equals(local) || "UNKNOWN".equals(local)) {
            return "NOT_REQUESTED".equals(provider) || "FAILED".equals(provider);
        }
        return true;
    }

    private boolean requiresRefundAmountCompare(String localStatus, String providerStatus) {
        String local = normalize(localStatus);
        String provider = normalize(providerStatus);
        return Set.of("REFUNDING", "REFUND_SUCCESS").contains(local)
                || !Set.of("NOT_REQUESTED", "FAILED", "UNKNOWN").contains(provider);
    }

    private boolean sameAmount(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) == 0;
    }

    private boolean ok(TicketSourceCallResult<?> call) {
        return call != null && call.isSuccess() && call.getData() != null;
    }

    private String message(TicketSourceCallResult<?> call) {
        if (call == null) return "第三方票源无响应";
        return (call.getProviderErrorCode() == null ? String.valueOf(call.getErrorCode()) : call.getProviderErrorCode())
                + ": " + call.getMessage();
    }

    private int safe(Integer value) { return value == null ? 0 : value; }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
    }

    private List<Long> normalizeOrderIds(List<Long> values) {
        if (values == null) return List.of();
        return values.stream().filter(Objects::nonNull).filter(v -> v > 0).distinct().limit(200).toList();
    }

    private String generateBatchNo() {
        return "REC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}
