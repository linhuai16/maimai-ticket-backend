package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.order.OrderItemReleaseDTO;
import com.example.maimaibackend.vo.admin.AdminOrderAddressVO;
import com.example.maimaibackend.vo.admin.AdminOrderAudienceVO;
import com.example.maimaibackend.vo.admin.AdminOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminOrderItemDetailVO;
import com.example.maimaibackend.vo.admin.AdminOrderItemVO;
import com.example.maimaibackend.vo.admin.AdminOrderTicketVO;
import com.example.maimaibackend.vo.admin.AdminRefundItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminOrderMapper {
    Integer countOrderList(@Param("keyword") String keyword,
                           @Param("userId") Long userId,
                           @Param("projectId") Long projectId,
                           @Param("orderStatus") String orderStatus,
                           @Param("dateFrom") String dateFrom,
                           @Param("dateTo") String dateTo);

    List<AdminOrderItemVO> selectOrderList(@Param("keyword") String keyword,
                                           @Param("userId") Long userId,
                                           @Param("projectId") Long projectId,
                                           @Param("orderStatus") String orderStatus,
                                           @Param("dateFrom") String dateFrom,
                                           @Param("dateTo") String dateTo,
                                           @Param("limit") Integer limit,
                                           @Param("offset") Integer offset);

    AdminOrderDetailVO selectOrderDetail(@Param("orderId") Long orderId);
    List<AdminOrderItemDetailVO> selectOrderItems(@Param("orderId") Long orderId);
    List<AdminOrderAudienceVO> selectOrderAudiences(@Param("orderId") Long orderId);
    AdminOrderAddressVO selectOrderAddress(@Param("orderId") Long orderId);
    List<AdminOrderTicketVO> selectOrderTickets(@Param("orderId") Long orderId);
    List<AdminRefundItemVO> selectOrderRefunds(@Param("orderId") Long orderId);

    String selectOrderStatusForUpdate(@Param("orderId") Long orderId);
    List<OrderItemReleaseDTO> selectOrderItemsForRelease(@Param("orderId") Long orderId);
    Integer releaseSkuLockedStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    Integer updateOrderCanceled(@Param("orderId") Long orderId);
}
