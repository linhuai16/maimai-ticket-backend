package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.payment.PayOrderAudienceDTO;
import com.example.maimaibackend.dto.payment.PayOrderBaseDTO;
import com.example.maimaibackend.dto.payment.PayOrderItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentMapper {

    PayOrderBaseDTO selectOrderForPay(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    List<PayOrderItemDTO> selectOrderItemsForPay(@Param("orderId") Long orderId);

    List<PayOrderAudienceDTO> selectOrderAudiencesForTicket(@Param("orderId") Long orderId);

    int countElectronicTicketsByOrderId(@Param("orderId") Long orderId);

    int reduceLockedStockToSold(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    int updateOrderPaid(
            @Param("orderId") Long orderId,
            @Param("payMethod") String payMethod,
            @Param("payTime") LocalDateTime payTime
    );

    int insertElectronicTicket(
            @Param("ticketNo") String ticketNo,
            @Param("orderId") Long orderId,
            @Param("orderItemId") Long orderItemId,
            @Param("orderAudienceId") Long orderAudienceId,
            @Param("ticketStatus") String ticketStatus,
            @Param("now") LocalDateTime now
    );
}
