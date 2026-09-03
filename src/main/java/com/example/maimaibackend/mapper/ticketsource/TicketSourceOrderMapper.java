package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderBridge;
import com.example.maimaibackend.ticketsource.order.model.TicketSourceOrderSkuContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TicketSourceOrderMapper {
    TicketSourceOrderSkuContext selectSkuContext(@Param("skuId") Long skuId);

    int insertBridge(
            @Param("orderId") Long orderId,
            @Param("providerId") Long providerId,
            @Param("skuMappingId") Long skuMappingId,
            @Param("providerProjectId") String providerProjectId,
            @Param("providerSessionId") String providerSessionId,
            @Param("providerSkuId") String providerSkuId,
            @Param("quantity") Integer quantity,
            @Param("unitPrice") BigDecimal unitPrice,
            @Param("payAmount") BigDecimal payAmount,
            @Param("currencyCode") String currencyCode,
            @Param("createIdempotencyKey") String createIdempotencyKey,
            @Param("paymentIdempotencyKey") String paymentIdempotencyKey,
            @Param("cancelIdempotencyKey") String cancelIdempotencyKey,
            @Param("reservationExpireTime") LocalDateTime reservationExpireTime,
            @Param("requestSnapshot") String requestSnapshot
    );

    TicketSourceOrderBridge selectBridgeByOrderId(@Param("orderId") Long orderId);

    TicketSourceOrderBridge selectBridgeByOrderIdForUpdate(@Param("orderId") Long orderId);

    List<TicketSourceOrderBridge> selectDueWaitPayOrders(@Param("limit") int limit);

    int markReserved(
            @Param("orderId") Long orderId,
            @Param("providerOrderId") String providerOrderId,
            @Param("providerOrderNo") String providerOrderNo,
            @Param("providerOrderStatus") String providerOrderStatus,
            @Param("reservationExpireTime") LocalDateTime reservationExpireTime,
            @Param("providerCreateTime") LocalDateTime providerCreateTime,
            @Param("responseSnapshot") String responseSnapshot
    );

    int markCreateFailed(
            @Param("orderId") Long orderId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("retryable") boolean retryable,
            @Param("responseSnapshot") String responseSnapshot
    );


    int markCreateUncertain(
            @Param("orderId") Long orderId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("retryable") boolean retryable,
            @Param("responseSnapshot") String responseSnapshot
    );

    int markCreateCompensated(
            @Param("orderId") Long orderId,
            @Param("providerOrderId") String providerOrderId,
            @Param("providerOrderNo") String providerOrderNo,
            @Param("providerOrderStatus") String providerOrderStatus,
            @Param("providerCancelTime") LocalDateTime providerCancelTime,
            @Param("responseSnapshot") String responseSnapshot
    );

    int markManualReview(
            @Param("orderId") Long orderId,
            @Param("providerOrderId") String providerOrderId,
            @Param("providerOrderNo") String providerOrderNo,
            @Param("providerOrderStatus") String providerOrderStatus,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("retryable") boolean retryable,
            @Param("responseSnapshot") String responseSnapshot
    );

    int markPaymentConfirming(@Param("orderId") Long orderId);

    int restoreReservedAfterPaymentFailure(
            @Param("orderId") Long orderId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("retryable") boolean retryable
    );

    int markPaid(
            @Param("orderId") Long orderId,
            @Param("providerOrderStatus") String providerOrderStatus,
            @Param("providerPayTime") LocalDateTime providerPayTime,
            @Param("responseSnapshot") String responseSnapshot
    );

    int markCanceling(@Param("orderId") Long orderId);

    int restoreReservedAfterCancelFailure(
            @Param("orderId") Long orderId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("retryable") boolean retryable
    );

    int markCanceled(
            @Param("orderId") Long orderId,
            @Param("bridgeStatus") String bridgeStatus,
            @Param("providerOrderStatus") String providerOrderStatus,
            @Param("providerCancelTime") LocalDateTime providerCancelTime,
            @Param("responseSnapshot") String responseSnapshot
    );

    int updateOrderCanceledWithoutLocalStock(
            @Param("orderId") Long orderId,
            @Param("cancelTime") LocalDateTime cancelTime
    );

    int updateOrderPaymentStatus(
            @Param("orderId") Long orderId,
            @Param("paymentStatus") String paymentStatus
    );

    int updateProviderInventorySnapshot(
            @Param("providerId") Long providerId,
            @Param("skuMappingId") Long skuMappingId,
            @Param("skuId") Long skuId,
            @Param("remainingStock") Integer remainingStock,
            @Param("dataVersion") String dataVersion
    );
}
