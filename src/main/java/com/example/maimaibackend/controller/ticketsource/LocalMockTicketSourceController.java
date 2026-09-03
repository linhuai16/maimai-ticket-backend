package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.ticketsource.gateway.TicketSourceAdapterException;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCancelOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceConfirmPaymentRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCreateOrderRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceHealth;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceIssueRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourcePage;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProjectQuery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefund;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceRefundRequest;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import com.example.maimaibackend.ticketsource.mock.LocalMockTicketSourceService;
import com.example.maimaibackend.ticketsource.mock.dto.MockProviderResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 可被 Postman/HTTP Client 当作独立第三方平台调用的本地模拟接口。
 * 当前适配器为避免同进程 HTTP 自调用，直接复用同一个模拟服务；未来可拆分为独立进程。
 */
@RestController
@RequestMapping("/mock-ticket-source/api/v1")
@ConditionalOnProperty(
        prefix = "maimai.ticket-source.mock",
        name = "api-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LocalMockTicketSourceController {
    private final LocalMockTicketSourceService mockService;

    public LocalMockTicketSourceController(LocalMockTicketSourceService mockService) {
        this.mockService = mockService;
    }

    @GetMapping("/health")
    public MockProviderResponse<TicketSourceHealth> health(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        return execute(requestId, mockService::health);
    }

    @GetMapping("/projects")
    public MockProviderResponse<TicketSourcePage<TicketSourceProject>> projects(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cityName,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        TicketSourceProjectQuery query = new TicketSourceProjectQuery();
        query.setKeyword(keyword);
        query.setCityName(cityName);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return execute(requestId, () -> mockService.queryProjects(query));
    }

    @GetMapping("/projects/{providerProjectId}")
    public MockProviderResponse<TicketSourceProject> project(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerProjectId
    ) {
        return execute(requestId, () -> mockService.getProject(providerProjectId));
    }

    @GetMapping("/projects/{providerProjectId}/sessions")
    public MockProviderResponse<List<TicketSourceSession>> sessions(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerProjectId
    ) {
        return execute(requestId, () -> mockService.querySessions(providerProjectId));
    }

    @GetMapping("/sessions/{providerSessionId}/skus")
    public MockProviderResponse<List<TicketSourceSku>> skus(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerSessionId
    ) {
        return execute(requestId, () -> mockService.querySkus(providerSessionId));
    }

    @GetMapping("/skus/{providerSkuId}/inventory")
    public MockProviderResponse<TicketSourceInventory> inventory(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerSkuId
    ) {
        return execute(requestId, () -> mockService.queryInventory(providerSkuId));
    }

    @PostMapping("/orders")
    public MockProviderResponse<TicketSourceProviderOrder> createOrder(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestBody TicketSourceCreateOrderRequest request
    ) {
        return execute(requestId, () -> mockService.createOrder(request));
    }

    @PostMapping("/orders/{providerOrderId}/confirm-payment")
    public MockProviderResponse<TicketSourceProviderOrder> confirmPayment(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId,
            @RequestBody TicketSourceConfirmPaymentRequest request
    ) {
        return execute(requestId, () -> mockService.confirmPayment(providerOrderId, request));
    }

    @PostMapping("/orders/{providerOrderId}/cancel")
    public MockProviderResponse<TicketSourceProviderOrder> cancelOrder(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId,
            @RequestBody TicketSourceCancelOrderRequest request
    ) {
        return execute(requestId, () -> mockService.cancelOrder(providerOrderId, request));
    }

    @GetMapping("/orders/{providerOrderId}")
    public MockProviderResponse<TicketSourceProviderOrder> order(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId
    ) {
        return execute(requestId, () -> mockService.getOrder(providerOrderId));
    }

    @PostMapping("/orders/{providerOrderId}/tickets/request")
    public MockProviderResponse<TicketSourceDelivery> requestTickets(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId,
            @RequestBody TicketSourceIssueRequest request
    ) {
        return execute(requestId, () -> mockService.requestTickets(providerOrderId, request));
    }

    @GetMapping("/orders/{providerOrderId}/tickets")
    public MockProviderResponse<TicketSourceDelivery> tickets(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId
    ) {
        return execute(requestId, () -> mockService.getTickets(providerOrderId));
    }

    @PostMapping("/orders/{providerOrderId}/refunds")
    public MockProviderResponse<TicketSourceRefund> requestRefund(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerOrderId,
            @RequestBody TicketSourceRefundRequest request
    ) {
        return execute(requestId, () -> mockService.requestRefund(providerOrderId, request));
    }

    @GetMapping("/refunds/{providerRefundId}")
    public MockProviderResponse<TicketSourceRefund> refund(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @PathVariable String providerRefundId
    ) {
        return execute(requestId, () -> mockService.getRefund(providerRefundId));
    }

    private <T> MockProviderResponse<T> execute(String rawRequestId, Supplier<T> supplier) {
        String requestId = rawRequestId == null || rawRequestId.isBlank()
                ? UUID.randomUUID().toString().replace("-", "")
                : rawRequestId.trim();
        try {
            return MockProviderResponse.success(requestId, supplier.get());
        } catch (TicketSourceAdapterException e) {
            String providerCode = e.getProviderErrorCode() == null
                    ? "MOCK_REMOTE_ERROR"
                    : e.getProviderErrorCode();
            return MockProviderResponse.failure(requestId, providerCode, e.getMessage());
        } catch (Exception e) {
            return MockProviderResponse.failure(requestId, "MOCK_INTERNAL_ERROR", "模拟票源系统异常");
        }
    }
}
