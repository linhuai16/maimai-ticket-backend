package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.refund.model.TicketSourceRefundBridge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TicketSourceRefundMapper {
    int insertPendingBridge(@Param("refundId") Long refundId,
                            @Param("orderId") Long orderId,
                            @Param("refundNo") String refundNo,
                            @Param("maxRetryCount") Integer maxRetryCount);

    int countTicketSourceOrder(@Param("orderId") Long orderId);

    TicketSourceRefundBridge selectByRefundId(@Param("refundId") Long refundId);

    TicketSourceRefundBridge selectByOrderId(@Param("orderId") Long orderId);

    List<TicketSourceRefundBridge> selectDueRefunds(@Param("now") LocalDateTime now,
                                                     @Param("limit") Integer limit);

    int markRequesting(@Param("refundId") Long refundId);

    int markProviderProgress(@Param("refundId") Long refundId,
                             @Param("providerRefundId") String providerRefundId,
                             @Param("providerRefundNo") String providerRefundNo,
                             @Param("bridgeStatus") String bridgeStatus,
                             @Param("providerRefundStatus") String providerRefundStatus,
                             @Param("nextAttemptTime") LocalDateTime nextAttemptTime,
                             @Param("providerRequestTime") LocalDateTime providerRequestTime,
                             @Param("providerRefundTime") LocalDateTime providerRefundTime,
                             @Param("responseSnapshot") String responseSnapshot,
                             @Param("operation") String operation);

    int markFailure(@Param("refundId") Long refundId,
                    @Param("bridgeStatus") String bridgeStatus,
                    @Param("errorCode") String errorCode,
                    @Param("errorMessage") String errorMessage,
                    @Param("retryable") Boolean retryable,
                    @Param("nextAttemptTime") LocalDateTime nextAttemptTime,
                    @Param("manualHold") Boolean manualHold,
                    @Param("operation") String operation);

    int markRejected(@Param("refundId") Long refundId);

    int finalizeLocalRefund(@Param("refundId") Long refundId,
                            @Param("orderId") Long orderId,
                            @Param("refundTime") LocalDateTime refundTime);

    int finalizeLocalOrder(@Param("orderId") Long orderId,
                           @Param("refundTime") LocalDateTime refundTime);

    int finalizeLocalTickets(@Param("orderId") Long orderId,
                             @Param("refundTime") LocalDateTime refundTime);

    int markOrderBridgeRefunding(@Param("orderId") Long orderId);

    int markOrderBridgeRefunded(@Param("orderId") Long orderId,
                                @Param("providerRefundTime") LocalDateTime providerRefundTime);

    int stopIssueTaskForRefund(@Param("orderId") Long orderId);
}
