package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.vo.admin.AdminOrderTicketVO;
import com.example.maimaibackend.vo.admin.AdminRefundDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRefundMapper {
    Integer countRefundList(@Param("keyword") String keyword,
                            @Param("refundStatus") String refundStatus,
                            @Param("userId") Long userId,
                            @Param("orderId") Long orderId,
                            @Param("dateFrom") String dateFrom,
                            @Param("dateTo") String dateTo);

    List<AdminRefundItemVO> selectRefundList(@Param("keyword") String keyword,
                                             @Param("refundStatus") String refundStatus,
                                             @Param("userId") Long userId,
                                             @Param("orderId") Long orderId,
                                             @Param("dateFrom") String dateFrom,
                                             @Param("dateTo") String dateTo,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    AdminRefundDetailVO selectRefundDetail(@Param("refundId") Long refundId);

    List<AdminOrderTicketVO> selectRefundOrderTickets(@Param("orderId") Long orderId);

    Integer updateRefundStatusIfCurrent(@Param("refundId") Long refundId,
                                        @Param("currentStatus") String currentStatus,
                                        @Param("targetStatus") String targetStatus,
                                        @Param("failReason") String failReason);

    Integer updateOrderStatusIfCurrent(@Param("orderId") Long orderId,
                                       @Param("currentStatus") String currentStatus,
                                       @Param("targetStatus") String targetStatus);

    Integer finalizeTicketsAfterRefundApproved(@Param("orderId") Long orderId);

    Integer countRefundHeldTickets(@Param("orderId") Long orderId);

    Integer restoreTicketsAfterRefundRejected(@Param("orderId") Long orderId);
}
