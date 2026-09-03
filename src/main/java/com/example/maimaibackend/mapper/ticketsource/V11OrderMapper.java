package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.dto.order.OrderAudienceSnapshotDTO;
import com.example.maimaibackend.ticketsource.order.provider.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface V11OrderMapper {
    List<V11OrderSkuContext> selectSkuContexts(@Param("projectId") Long projectId,
                                                @Param("sessionId") Long sessionId,
                                                @Param("skuIds") List<Long> skuIds);

    int insertQuote(V11OrderQuoteRecord quote);
    V11OrderQuoteRecord selectQuoteForUpdate(@Param("quoteId") String quoteId,
                                              @Param("userId") Long userId);
    int markQuoteUsed(@Param("quoteId") String quoteId,
                      @Param("orderId") Long orderId);

    int insertOrderAudience(@Param("orderId") Long orderId,
                            @Param("orderItemId") Long orderItemId,
                            @Param("clientTicketNo") String clientTicketNo,
                            @Param("audience") OrderAudienceSnapshotDTO audience);

    int insertBridge(V11OrderBridgeInsert bridge);
    int insertBridgeItem(V11OrderItemBridgeInsert item);

    int markBridgeReserved(@Param("orderId") Long orderId,
                           @Param("providerOrderId") String providerOrderId,
                           @Param("providerOrderNo") String providerOrderNo,
                           @Param("providerOrderStatus") String providerOrderStatus,
                           @Param("reservationExpireTime") LocalDateTime reservationExpireTime,
                           @Param("providerCreateTime") LocalDateTime providerCreateTime,
                           @Param("responseSnapshot") String responseSnapshot);

    int markCreateUnknownResult(@Param("orderId") Long orderId,
                                @Param("errorCode") String errorCode,
                                @Param("errorMessage") String errorMessage,
                                @Param("retryable") boolean retryable);

    int markCreateRecoveryFailure(@Param("orderId") Long orderId,
                                  @Param("errorCode") String errorCode,
                                  @Param("errorMessage") String errorMessage,
                                  @Param("retryable") boolean retryable,
                                  @Param("manualReviewThreshold") int manualReviewThreshold);

    int markCreateFailed(@Param("orderId") Long orderId,
                         @Param("errorCode") String errorCode,
                         @Param("errorMessage") String errorMessage,
                         @Param("retryable") boolean retryable);

    int cancelLocalAfterCreateFailure(@Param("orderId") Long orderId,
                                      @Param("cancelTime") LocalDateTime cancelTime);

    int markRecoveredProviderTerminal(@Param("orderId") Long orderId,
                                      @Param("bridgeStatus") String bridgeStatus,
                                      @Param("providerOrderId") String providerOrderId,
                                      @Param("providerOrderNo") String providerOrderNo,
                                      @Param("providerOrderStatus") String providerOrderStatus,
                                      @Param("providerCreateTime") LocalDateTime providerCreateTime,
                                      @Param("responseSnapshot") String responseSnapshot);

    int markRecoveredSubmitIdempotencyFailed(@Param("orderId") Long orderId,
                                             @Param("errorCode") String errorCode,
                                             @Param("errorMessage") String errorMessage);

    int markBridgeManualReview(@Param("orderId") Long orderId,
                               @Param("operation") String operation,
                               @Param("providerOrderStatus") String providerOrderStatus,
                               @Param("errorCode") String errorCode,
                               @Param("errorMessage") String errorMessage,
                               @Param("responseSnapshot") String responseSnapshot);

    Map<String, Object> selectCreateRecoveryBySubmitNo(@Param("clientSubmitNo") String clientSubmitNo);

    V11LocalOrderContext selectOrderContextForRecovery(@Param("orderId") Long orderId);

    List<Long> selectUnknownCreateOrderIds(@Param("limit") int limit);

    int markRecoveredSubmitIdempotencySuccess(@Param("orderId") Long orderId);

    int markRecoveredSubmitIdempotencyManualReview(@Param("orderId") Long orderId,
                                                    @Param("errorCode") String errorCode,
                                                    @Param("errorMessage") String errorMessage);

    V11LocalOrderContext selectOrderContextForUpdate(@Param("orderId") Long orderId,
                                                     @Param("userId") Long userId);

    List<V11OrderItemBridgeInsert> selectBridgeItems(@Param("bridgeId") Long bridgeId);

    int markPaymentConfirming(@Param("orderId") Long orderId);
    int restoreReservedAfterPaymentFailure(@Param("orderId") Long orderId,
                                           @Param("errorCode") String errorCode,
                                           @Param("errorMessage") String errorMessage,
                                           @Param("retryable") boolean retryable);
    int markBridgePaid(@Param("orderId") Long orderId,
                       @Param("providerOrderStatus") String providerOrderStatus,
                       @Param("providerPayTime") LocalDateTime providerPayTime,
                       @Param("responseSnapshot") String responseSnapshot);
    int updateOrderPaid(@Param("orderId") Long orderId,
                        @Param("payMethod") String payMethod,
                        @Param("payTime") LocalDateTime payTime);
    int insertGeneratingTickets(@Param("orderId") Long orderId);
    int insertV12IssueTask(@Param("orderId") Long orderId);

    int markCanceling(@Param("orderId") Long orderId);
    int restoreReservedAfterCancelFailure(@Param("orderId") Long orderId,
                                          @Param("errorCode") String errorCode,
                                          @Param("errorMessage") String errorMessage,
                                          @Param("retryable") boolean retryable);
    int markBridgeCanceled(@Param("orderId") Long orderId,
                           @Param("bridgeStatus") String bridgeStatus,
                           @Param("providerOrderStatus") String providerOrderStatus,
                           @Param("providerCancelTime") LocalDateTime providerCancelTime,
                           @Param("responseSnapshot") String responseSnapshot);
    int updateOrderCanceled(@Param("orderId") Long orderId,
                            @Param("cancelTime") LocalDateTime cancelTime);

    int updateLocalSkuInventory(@Param("skuId") Long skuId,
                                @Param("availableStock") Integer availableStock,
                                @Param("sourceSaleStatus") String sourceSaleStatus);
    int updateSkuMappingInventory(@Param("skuMappingId") Long skuMappingId,
                                  @Param("availableStock") Integer availableStock,
                                  @Param("sourceSaleStatus") String sourceSaleStatus,
                                  @Param("syncTime") LocalDateTime syncTime);
    int updateLocalSkuSaleStatusKeepStock(@Param("skuId") Long skuId,
                                          @Param("sourceSaleStatus") String sourceSaleStatus);

    int countOrderItems(@Param("orderId") Long orderId);
    int countOrderTickets(@Param("orderId") Long orderId);

    List<V11OrderResourceEntry> selectResourceEntries(@Param("providerCode") String providerCode,
                                                       @Param("providerProjectId") String providerProjectId);
}
