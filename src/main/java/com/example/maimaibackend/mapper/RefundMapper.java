package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.refund.MockRefundBaseDTO;
import com.example.maimaibackend.dto.refund.RefundApplyBaseDTO;
import com.example.maimaibackend.dto.refund.RefundRecordInsertDTO;
import com.example.maimaibackend.vo.refund.RefundProgressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RefundMapper {

    RefundApplyBaseDTO selectRefundApplyBase(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    int countRefundByOrderId(@Param("orderId") Long orderId);

    RefundProgressVO selectRefundProgressByOrder(@Param("orderId") Long orderId, @Param("userId") Long userId);

    int insertRefundRecord(RefundRecordInsertDTO dto);

    int updateOrderToRefunding(
            @Param("orderId") Long orderId,
            @Param("now") LocalDateTime now
    );

    int expireTicketsByOrderId(
            @Param("orderId") Long orderId,
            @Param("now") LocalDateTime now,
            @Param("reason") String reason
    );

    MockRefundBaseDTO selectMockRefundBase(@Param("refundId") Long refundId);

    int updateRefundSuccess(
            @Param("refundId") Long refundId,
            @Param("refundTime") LocalDateTime refundTime
    );

    int updateOrderRefundSuccess(
            @Param("orderId") Long orderId,
            @Param("now") LocalDateTime now
    );

    int ensureTicketsExpired(
            @Param("orderId") Long orderId,
            @Param("now") LocalDateTime now
    );
}
