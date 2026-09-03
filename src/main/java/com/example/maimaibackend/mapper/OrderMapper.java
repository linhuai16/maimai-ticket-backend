package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.order.CancelOrderBaseDTO;
import com.example.maimaibackend.dto.order.OrderAddressSnapshotDTO;
import com.example.maimaibackend.dto.order.OrderAudienceSnapshotDTO;
import com.example.maimaibackend.dto.order.OrderConfirmBaseDTO;
import com.example.maimaibackend.dto.order.OrderItemInsertDTO;
import com.example.maimaibackend.dto.order.OrderItemReleaseDTO;
import com.example.maimaibackend.dto.order.TicketOrderInsertDTO;
import com.example.maimaibackend.vo.performance.ServiceTagVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    OrderConfirmBaseDTO selectOrderConfirmBase(
            @Param("projectId") Long projectId,
            @Param("sessionId") Long sessionId,
            @Param("skuId") Long skuId
    );

    OrderConfirmBaseDTO selectOrderConfirmBaseForUpdate(
            @Param("projectId") Long projectId,
            @Param("sessionId") Long sessionId,
            @Param("skuId") Long skuId
    );

    List<ServiceTagVO> selectOrderServiceTags(@Param("projectId") Long projectId);

    List<String> selectOrderRefundServiceTagDetailItems(@Param("projectId") Long projectId);

    int countUserById(@Param("userId") Long userId);

    List<OrderAudienceSnapshotDTO> selectAudienceSnapshots(
            @Param("userId") Long userId,
            @Param("audienceIds") List<Long> audienceIds
    );

    int countExistingAudienceBySessionAndCertHashes(
            @Param("sessionId") Long sessionId,
            @Param("certificateNoHashes") List<String> certificateNoHashes
    );

    OrderAddressSnapshotDTO selectAddressSnapshot(
            @Param("userId") Long userId,
            @Param("addressId") Long addressId
    );

    int lockSkuStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    int insertTicketOrder(TicketOrderInsertDTO order);

    int insertOrderItem(OrderItemInsertDTO orderItem);

    int insertOrderAudienceSnapshot(
            @Param("orderId") Long orderId,
            @Param("audience") OrderAudienceSnapshotDTO audience
    );

    int insertOrderAddressSnapshot(
            @Param("orderId") Long orderId,
            @Param("address") OrderAddressSnapshotDTO address
    );

    CancelOrderBaseDTO selectOrderForCancel(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    List<OrderItemReleaseDTO> selectOrderItemsForRelease(@Param("orderId") Long orderId);

    int releaseSkuLockedStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    int updateOrderCanceled(
            @Param("orderId") Long orderId,
            @Param("cancelTime") java.time.LocalDateTime cancelTime
    );

}
