package com.example.maimaibackend.ticketsource.order.provider;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.OrderMapper;
import com.example.maimaibackend.mapper.ticketsource.V11OrderMapper;
import com.example.maimaibackend.ticketsource.fulfillment.shipment.V11ShipmentService;
import com.example.maimaibackend.ticketsource.order.provider.model.V11LocalOrderContext;
import com.example.maimaibackend.ticketsource.order.provider.model.V11OrderCreateResult;
import com.example.maimaibackend.ticketsource.resource.provider.V11ResourceAdapterInvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class V11OrderServiceTest {
    private V11OrderMapper mapper;
    private V11ResourceAdapterInvoker invoker;
    private V11OrderService service;

    @BeforeEach
    void setUp() {
        mapper = mock(V11OrderMapper.class);
        invoker = mock(V11ResourceAdapterInvoker.class);
        service = new V11OrderService(
                mock(OrderMapper.class), mapper, invoker, new ObjectMapper(),
                mock(V11ShipmentService.class), mock(PlatformTransactionManager.class));
    }

    @Test
    void returnsAlreadyReservedOrderWithoutCallingProviderAgain() {
        V11LocalOrderContext context = context("RESERVED");
        when(mapper.selectOrderContextForRecovery(11L)).thenReturn(context);
        when(mapper.countOrderItems(11L)).thenReturn(1);
        when(mapper.countOrderTickets(11L)).thenReturn(2);

        V11OrderCreateResult result = service.recoverUnknownCreate(11L);

        assertEquals(11L, result.orderId());
        assertEquals("provider-order-11", result.providerOrderId());
        assertEquals(2, result.ticketCount());
        verifyNoInteractions(invoker);
    }

    @Test
    void rejectsRecoveryOutsideUnknownOrManualReviewWithoutCallingProvider() {
        when(mapper.selectOrderContextForRecovery(11L)).thenReturn(context("FAILED"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.recoverUnknownCreate(11L));

        assertEquals("当前订单不是创建结果待补查状态", error.getMessage());
        verifyNoInteractions(invoker);
    }

    private V11LocalOrderContext context(String bridgeStatus) {
        V11LocalOrderContext value = new V11LocalOrderContext();
        value.setOrderId(11L);
        value.setOrderNo("order-11");
        value.setOrderStatus("WAIT_PAY");
        value.setPaymentStatus("UNPAID");
        value.setPayAmount(new BigDecimal("288.00"));
        value.setPayExpireTime(LocalDateTime.of(2026, 9, 3, 12, 30));
        value.setProviderCode("LOCAL_MOCK");
        value.setProviderOrderId("provider-order-11");
        value.setProviderOrderStatus("RESERVED");
        value.setBridgeStatus(bridgeStatus);
        return value;
    }
}
