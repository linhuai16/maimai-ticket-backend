package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceBehavior;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceOrder;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceOrderSku;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceRefund;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceRefundPlan;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceDelivery;
import com.example.maimaibackend.ticketsource.mock.model.MockTicketSourceCredential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LocalMockTicketSourceMapper {
    int countProjects(@Param("keyword") String keyword, @Param("cityName") String cityName);

    List<TicketSourceProject> selectProjects(
            @Param("keyword") String keyword,
            @Param("cityName") String cityName,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    TicketSourceProject selectProjectById(@Param("providerProjectId") String providerProjectId);

    List<TicketSourceSession> selectSessionsByProjectId(
            @Param("providerProjectId") String providerProjectId
    );

    TicketSourceSession selectSessionById(@Param("providerSessionId") String providerSessionId);

    int countSessionById(@Param("providerSessionId") String providerSessionId);

    List<TicketSourceSku> selectSkusBySessionId(
            @Param("providerSessionId") String providerSessionId
    );

    TicketSourceInventory selectInventoryBySkuId(@Param("providerSkuId") String providerSkuId);

    int countAllProjects();

    int countAllSessions();

    int countAllSkus();

    MockTicketSourceBehavior selectBehavior(@Param("operationCode") String operationCode);

    List<MockTicketSourceBehavior> selectBehaviors();

    int updateBehavior(
            @Param("operationCode") String operationCode,
            @Param("enabled") boolean enabled,
            @Param("delayMs") int delayMs,
            @Param("forcedErrorCode") String forcedErrorCode,
            @Param("forcedErrorMessage") String forcedErrorMessage
    );

    int resetBehaviors();

    int updateProjectSaleStatus(
            @Param("providerProjectId") String providerProjectId,
            @Param("saleStatus") String saleStatus,
            @Param("dataVersion") String dataVersion
    );

    int updateSessionSaleStatus(
            @Param("providerSessionId") String providerSessionId,
            @Param("saleStatus") String saleStatus,
            @Param("dataVersion") String dataVersion
    );

    int updateSkuInventory(
            @Param("providerSkuId") String providerSkuId,
            @Param("inventoryMode") String inventoryMode,
            @Param("availableStock") Integer availableStock,
            @Param("saleStatus") String saleStatus,
            @Param("dataVersion") String dataVersion
    );

    MockTicketSourceOrderSku selectOrderSkuById(@Param("providerSkuId") String providerSkuId);

    MockTicketSourceOrder selectOrderByProviderOrderId(@Param("providerOrderId") String providerOrderId);

    MockTicketSourceOrder selectOrderByCreateIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    int reserveSkuStock(
            @Param("providerSkuId") String providerSkuId,
            @Param("quantity") Integer quantity,
            @Param("dataVersion") String dataVersion
    );

    int restoreSkuStock(
            @Param("providerSkuId") String providerSkuId,
            @Param("quantity") Integer quantity,
            @Param("dataVersion") String dataVersion
    );

    int insertMockOrder(MockTicketSourceOrder order);

    int markMockOrderPaid(
            @Param("providerOrderId") String providerOrderId,
            @Param("paymentIdempotencyKey") String paymentIdempotencyKey,
            @Param("payTime") LocalDateTime payTime,
            @Param("dataVersion") String dataVersion
    );

    int markMockOrderCanceled(
            @Param("providerOrderId") String providerOrderId,
            @Param("cancelIdempotencyKey") String cancelIdempotencyKey,
            @Param("cancelTime") LocalDateTime cancelTime,
            @Param("cancelReason") String cancelReason,
            @Param("orderStatus") String orderStatus,
            @Param("dataVersion") String dataVersion
    );

    MockTicketSourceDelivery selectDelivery(@Param("providerOrderId") String providerOrderId);

    List<MockTicketSourceCredential> selectCredentials(@Param("providerOrderId") String providerOrderId);

    int insertDefaultDelivery(MockTicketSourceDelivery delivery);

    int upsertDeliveryPlan(MockTicketSourceDelivery delivery);

    int bindDeliveryRequest(
            @Param("providerOrderId") String providerOrderId,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("dataVersion") String dataVersion
    );

    int upsertCredential(MockTicketSourceCredential credential);

    int updateDeliveryResult(
            @Param("providerOrderId") String providerOrderId,
            @Param("deliveryStatus") String deliveryStatus,
            @Param("issuedCount") Integer issuedCount,
            @Param("failedCount") Integer failedCount,
            @Param("lastErrorCode") String lastErrorCode,
            @Param("lastErrorMessage") String lastErrorMessage,
            @Param("dataVersion") String dataVersion
    );


    MockTicketSourceRefund selectRefundByProviderRefundId(@Param("providerRefundId") String providerRefundId);

    MockTicketSourceRefund selectRefundByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    MockTicketSourceRefund selectRefundByProviderOrderId(@Param("providerOrderId") String providerOrderId);

    MockTicketSourceRefundPlan selectRefundPlan(@Param("providerOrderId") String providerOrderId);

    int upsertRefundPlan(
            @Param("providerOrderId") String providerOrderId,
            @Param("refundMode") String refundMode,
            @Param("availableTime") LocalDateTime availableTime,
            @Param("dataVersion") String dataVersion
    );

    int insertMockRefund(MockTicketSourceRefund refund);

    int markMockOrderRefunding(
            @Param("providerOrderId") String providerOrderId,
            @Param("dataVersion") String dataVersion
    );

    int markMockOrderRefunded(
            @Param("providerOrderId") String providerOrderId,
            @Param("dataVersion") String dataVersion
    );

    int markMockRefundSuccess(
            @Param("providerRefundId") String providerRefundId,
            @Param("refundTime") LocalDateTime refundTime,
            @Param("dataVersion") String dataVersion
    );

    int markMockRefundFailed(
            @Param("providerRefundId") String providerRefundId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage,
            @Param("dataVersion") String dataVersion
    );

    int markMockRefundInventoryRestored(@Param("providerRefundId") String providerRefundId);

    int makeMockRefundAvailableNow(@Param("providerRefundId") String providerRefundId,
                                   @Param("availableTime") LocalDateTime availableTime,
                                   @Param("dataVersion") String dataVersion);

    int voidCredentialsByOrderId(
            @Param("providerOrderId") String providerOrderId,
            @Param("refundTime") LocalDateTime refundTime,
            @Param("dataVersion") String dataVersion
    );

    int markDeliveryRefunded(
            @Param("providerOrderId") String providerOrderId,
            @Param("dataVersion") String dataVersion
    );

    int expireMockOrder(
            @Param("providerOrderId") String providerOrderId,
            @Param("cancelTime") LocalDateTime cancelTime,
            @Param("dataVersion") String dataVersion
    );
}
