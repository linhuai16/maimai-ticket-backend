package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminTicketStateDTO;
import com.example.maimaibackend.dto.ticket.IssueFailedRefundInsertDTO;
import com.example.maimaibackend.dto.ticket.IssueOrderBaseDTO;
import com.example.maimaibackend.vo.admin.AdminIssueOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminIssueOrderItemVO;
import com.example.maimaibackend.vo.admin.AdminTicketDetailVO;
import com.example.maimaibackend.vo.admin.AdminTicketItemVO;
import com.example.maimaibackend.vo.admin.AdminTicketVerifyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminTicketMapper {

    Integer countTicketList(@Param("keyword") String keyword,
                            @Param("orderId") Long orderId,
                            @Param("userId") Long userId,
                            @Param("projectId") Long projectId,
                            @Param("sessionId") Long sessionId,
                            @Param("ticketStatus") String ticketStatus,
                            @Param("dateFrom") String dateFrom,
                            @Param("dateTo") String dateTo);

    List<AdminTicketItemVO> selectTicketList(@Param("keyword") String keyword,
                                              @Param("orderId") Long orderId,
                                              @Param("userId") Long userId,
                                              @Param("projectId") Long projectId,
                                              @Param("sessionId") Long sessionId,
                                              @Param("ticketStatus") String ticketStatus,
                                              @Param("dateFrom") String dateFrom,
                                              @Param("dateTo") String dateTo,
                                              @Param("limit") Integer limit,
                                              @Param("offset") Integer offset);

    AdminTicketDetailVO selectTicketDetail(@Param("ticketId") Long ticketId);

    String selectOrderFulfillmentMode(@Param("orderId") Long orderId);

    AdminTicketStateDTO selectTicketStateForUpdate(@Param("ticketId") Long ticketId);

    Integer updateTicketSeat(@Param("ticketId") Long ticketId,
                             @Param("seatInfo") String seatInfo,
                             @Param("updateTime") LocalDateTime updateTime);

    Integer markTicketError(@Param("ticketId") Long ticketId,
                            @Param("abnormalReason") String abnormalReason,
                            @Param("updateTime") LocalDateTime updateTime);

    Integer retryTicket(@Param("ticketId") Long ticketId,
                        @Param("updateTime") LocalDateTime updateTime);

    Integer countIssueOrderList(@Param("keyword") String keyword,
                                @Param("projectId") Long projectId,
                                @Param("sessionId") Long sessionId,
                                @Param("issueStatus") String issueStatus,
                                @Param("dateFrom") String dateFrom,
                                @Param("dateTo") String dateTo);

    List<AdminIssueOrderItemVO> selectIssueOrderList(@Param("keyword") String keyword,
                                                      @Param("projectId") Long projectId,
                                                      @Param("sessionId") Long sessionId,
                                                      @Param("issueStatus") String issueStatus,
                                                      @Param("dateFrom") String dateFrom,
                                                      @Param("dateTo") String dateTo,
                                                      @Param("limit") Integer limit,
                                                      @Param("offset") Integer offset);

    AdminIssueOrderDetailVO selectIssueOrderSummary(@Param("orderId") Long orderId);

    List<AdminTicketItemVO> selectTicketsByOrder(@Param("orderId") Long orderId);

    IssueOrderBaseDTO selectIssueOrderForUpdate(@Param("orderId") Long orderId);

    Integer countIssueSuccessCandidates(@Param("orderId") Long orderId);

    Integer issueOrderTickets(@Param("orderId") Long orderId,
                              @Param("issueTime") LocalDateTime issueTime);

    Integer updateOrderTicketIssuedTime(@Param("orderId") Long orderId,
                                        @Param("issueTime") LocalDateTime issueTime);

    Integer markOrderTicketsError(@Param("orderId") Long orderId,
                                  @Param("abnormalReason") String abnormalReason,
                                  @Param("updateTime") LocalDateTime updateTime);

    Integer retryOrderTickets(@Param("orderId") Long orderId,
                              @Param("updateTime") LocalDateTime updateTime);

    Integer countCheckedTickets(@Param("orderId") Long orderId);

    Integer countErrorTicketsByOrderId(@Param("orderId") Long orderId);

    Integer countRefundRecordByOrderId(@Param("orderId") Long orderId);

    Integer expireOrderTicketsForRefund(@Param("orderId") Long orderId,
                                        @Param("abnormalReason") String abnormalReason,
                                        @Param("expireTime") LocalDateTime expireTime);

    Integer insertSystemRefundRecord(IssueFailedRefundInsertDTO refund);

    Integer updateOrderRefundSuccess(@Param("orderId") Long orderId,
                                     @Param("updateTime") LocalDateTime updateTime);

    AdminTicketVerifyVO selectTicketByCode(@Param("code") String code);

    AdminTicketVerifyVO selectTicketByCodeForUpdate(@Param("code") String code);

    Integer checkTicket(@Param("ticketId") Long ticketId,
                        @Param("checkTime") LocalDateTime checkTime);

    Integer finishOrderIfAllTicketsChecked(@Param("orderId") Long orderId,
                                           @Param("finishTime") LocalDateTime finishTime);

    Integer countCheckRecords(@Param("keyword") String keyword,
                              @Param("projectId") Long projectId,
                              @Param("sessionId") Long sessionId,
                              @Param("dateFrom") String dateFrom,
                              @Param("dateTo") String dateTo);

    List<AdminTicketItemVO> selectCheckRecords(@Param("keyword") String keyword,
                                                @Param("projectId") Long projectId,
                                                @Param("sessionId") Long sessionId,
                                                @Param("dateFrom") String dateFrom,
                                                @Param("dateTo") String dateTo,
                                                @Param("limit") Integer limit,
                                                @Param("offset") Integer offset);
}
