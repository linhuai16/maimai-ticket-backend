package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.issue.model.TicketSourceIssueTask;
import com.example.maimaibackend.ticketsource.issue.model.TicketSourceLocalTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TicketSourceIssueMapper {
    int insertIssueTask(@Param("orderId") Long orderId,
                        @Param("bridgeId") Long bridgeId,
                        @Param("providerId") Long providerId,
                        @Param("providerOrderId") String providerOrderId,
                        @Param("expectedCount") Integer expectedCount,
                        @Param("idempotencyKey") String idempotencyKey,
                        @Param("maxRetryCount") Integer maxRetryCount,
                        @Param("nextAttemptTime") LocalDateTime nextAttemptTime,
                        @Param("now") LocalDateTime now);

    TicketSourceIssueTask selectTaskByOrderId(@Param("orderId") Long orderId);
    TicketSourceIssueTask selectTaskForUpdate(@Param("orderId") Long orderId);
    List<Long> selectDueOrderIds(@Param("now") LocalDateTime now,
                                 @Param("processingCutoff") LocalDateTime processingCutoff,
                                 @Param("limit") Integer limit);
    int markProcessing(@Param("orderId") Long orderId, @Param("now") LocalDateTime now,
                       @Param("processingCutoff") LocalDateTime processingCutoff);
    List<TicketSourceLocalTicket> selectLocalTickets(@Param("orderId") Long orderId);

    int applyIssuedCredential(@Param("ticketId") Long ticketId,
                              @Param("providerId") Long providerId,
                              @Param("providerOrderId") String providerOrderId,
                              @Param("providerTicketId") String providerTicketId,
                              @Param("credentialType") String credentialType,
                              @Param("credentialPayload") String credentialPayload,
                              @Param("credentialVersion") String credentialVersion,
                              @Param("seatInfo") String seatInfo,
                              @Param("seatZone") String seatZone,
                              @Param("seatRow") String seatRow,
                              @Param("seatNumber") String seatNumber,
                              @Param("entranceInfo") String entranceInfo,
                              @Param("issueTime") LocalDateTime issueTime,
                              @Param("expireTime") LocalDateTime expireTime,
                              @Param("syncTime") LocalDateTime syncTime);

    int applyFailedCredential(@Param("ticketId") Long ticketId,
                              @Param("providerId") Long providerId,
                              @Param("providerOrderId") String providerOrderId,
                              @Param("providerTicketId") String providerTicketId,
                              @Param("reason") String reason,
                              @Param("syncTime") LocalDateTime syncTime);

    int countTicketsByStatus(@Param("orderId") Long orderId, @Param("ticketStatus") String ticketStatus);
    int updateOrderIssuedTime(@Param("orderId") Long orderId, @Param("issueTime") LocalDateTime issueTime);

    int updateTaskResult(@Param("orderId") Long orderId,
                         @Param("taskStatus") String taskStatus,
                         @Param("providerDeliveryStatus") String providerDeliveryStatus,
                         @Param("issuedCount") Integer issuedCount,
                         @Param("failedCount") Integer failedCount,
                         @Param("retryCount") Integer retryCount,
                         @Param("nextAttemptTime") LocalDateTime nextAttemptTime,
                         @Param("completeTime") LocalDateTime completeTime,
                         @Param("providerDeliveryVersion") String providerDeliveryVersion,
                         @Param("lastOperation") String lastOperation,
                         @Param("lastErrorCode") String lastErrorCode,
                         @Param("lastErrorMessage") String lastErrorMessage,
                         @Param("lastErrorRetryable") boolean lastErrorRetryable,
                         @Param("requestSent") boolean requestSent,
                         @Param("manualHold") boolean manualHold,
                         @Param("now") LocalDateTime now);

    int markGeneratingTicketsError(@Param("orderId") Long orderId,
                                   @Param("reason") String reason,
                                   @Param("now") LocalDateTime now);
    int resetFailedTickets(@Param("orderId") Long orderId, @Param("now") LocalDateTime now);
    int resetTaskForRetry(@Param("orderId") Long orderId, @Param("nextAttemptTime") LocalDateTime nextAttemptTime,
                          @Param("now") LocalDateTime now);
}
