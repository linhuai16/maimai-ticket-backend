package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.ticket.IssueFailedRefundInsertDTO;
import com.example.maimaibackend.dto.ticket.IssueOrderBaseDTO;
import com.example.maimaibackend.dto.ticket.TicketCheckBaseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface MockTicketMapper {

    IssueOrderBaseDTO selectIssueOrderForUpdate(@Param("orderId") Long orderId);

    int countGeneratingTickets(@Param("orderId") Long orderId);

    int issueGeneratingTickets(
            @Param("orderId") Long orderId,
            @Param("issueTime") LocalDateTime issueTime
    );

    int updateOrderTicketIssuedTime(
            @Param("orderId") Long orderId,
            @Param("issueTime") LocalDateTime issueTime
    );

    int countIssueFailedCandidateTickets(@Param("orderId") Long orderId);

    int expireTicketsForIssueFailed(
            @Param("orderId") Long orderId,
            @Param("abnormalReason") String abnormalReason,
            @Param("expireTime") LocalDateTime expireTime
    );

    int countRefundRecordByOrderId(@Param("orderId") Long orderId);

    int insertIssueFailedRefundRecord(IssueFailedRefundInsertDTO refund);

    int updateOrderRefundSuccess(
            @Param("orderId") Long orderId,
            @Param("updateTime") LocalDateTime updateTime
    );

    TicketCheckBaseDTO selectTicketForCheck(@Param("ticketId") Long ticketId);

    int checkTicket(
            @Param("ticketId") Long ticketId,
            @Param("checkTime") LocalDateTime checkTime
    );

    int finishOrderIfAllTicketsChecked(
            @Param("orderId") Long orderId,
            @Param("finishTime") LocalDateTime finishTime
    );
}
