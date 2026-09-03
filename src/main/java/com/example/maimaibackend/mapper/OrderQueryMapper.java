package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.order.OrderAddressVO;
import com.example.maimaibackend.vo.order.OrderAudienceVO;
import com.example.maimaibackend.vo.order.OrderDetailItemVO;
import com.example.maimaibackend.vo.order.OrderDetailVO;
import com.example.maimaibackend.vo.order.OrderListItemVO;
import com.example.maimaibackend.vo.order.OrderRefundRecordVO;
import com.example.maimaibackend.vo.order.OrderTicketVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderQueryMapper {

    int countOrders(
            @Param("userId") Long userId,
            @Param("tab") String tab
    );

    List<OrderListItemVO> selectOrderList(
            @Param("userId") Long userId,
            @Param("tab") String tab,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    OrderDetailVO selectOrderDetailBase(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    List<OrderDetailItemVO> selectOrderDetailItems(@Param("orderId") Long orderId);

    List<OrderAudienceVO> selectOrderAudiences(@Param("orderId") Long orderId);

    OrderAddressVO selectOrderAddress(@Param("orderId") Long orderId);

    List<OrderTicketVO> selectOrderTickets(@Param("orderId") Long orderId);

    OrderRefundRecordVO selectOrderRefundRecord(@Param("orderId") Long orderId);
}
