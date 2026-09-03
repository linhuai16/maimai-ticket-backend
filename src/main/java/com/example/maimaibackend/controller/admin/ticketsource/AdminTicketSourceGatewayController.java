package com.example.maimaibackend.controller.admin.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.mapper.ticketsource.TicketSourceProviderMapper;
import com.example.maimaibackend.ticketsource.gateway.TicketSourceGateway;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceCallResult;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceHealth;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceDelivery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceInventory;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourcePage;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProject;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProjectQuery;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderView;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceProviderOrder;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSession;
import com.example.maimaibackend.ticketsource.gateway.model.TicketSourceSku;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-source-gateway")
public class AdminTicketSourceGatewayController {
    private final TicketSourceGateway gateway;
    private final TicketSourceProviderMapper providerMapper;

    public AdminTicketSourceGatewayController(
            TicketSourceGateway gateway,
            TicketSourceProviderMapper providerMapper
    ) {
        this.gateway = gateway;
        this.providerMapper = providerMapper;
    }

    @GetMapping("/providers")
    public Result<List<TicketSourceProviderView>> providers() {
        List<TicketSourceProviderView> views = providerMapper.selectAllProviders()
                .stream()
                .map(TicketSourceProviderView::from)
                .toList();
        return Result.success(views);
    }

    @GetMapping("/{providerCode}/health")
    public Result<TicketSourceCallResult<TicketSourceHealth>> health(
            @PathVariable String providerCode
    ) {
        return Result.success(gateway.health(providerCode));
    }

    @GetMapping("/{providerCode}/projects")
    public Result<TicketSourceCallResult<TicketSourcePage<TicketSourceProject>>> projects(
            @PathVariable String providerCode,
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
        return Result.success(gateway.queryProjects(providerCode, query));
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}")
    public Result<TicketSourceCallResult<TicketSourceProject>> project(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId
    ) {
        return Result.success(gateway.getProject(providerCode, providerProjectId));
    }

    @GetMapping("/{providerCode}/projects/{providerProjectId}/sessions")
    public Result<TicketSourceCallResult<List<TicketSourceSession>>> sessions(
            @PathVariable String providerCode,
            @PathVariable String providerProjectId
    ) {
        return Result.success(gateway.querySessions(providerCode, providerProjectId));
    }

    @GetMapping("/{providerCode}/sessions/{providerSessionId}/skus")
    public Result<TicketSourceCallResult<List<TicketSourceSku>>> skus(
            @PathVariable String providerCode,
            @PathVariable String providerSessionId
    ) {
        return Result.success(gateway.querySkus(providerCode, providerSessionId));
    }

    @GetMapping("/{providerCode}/skus/{providerSkuId}/inventory")
    public Result<TicketSourceCallResult<TicketSourceInventory>> inventory(
            @PathVariable String providerCode,
            @PathVariable String providerSkuId
    ) {
        return Result.success(gateway.queryInventory(providerCode, providerSkuId));
    }

    @GetMapping("/{providerCode}/orders/{providerOrderId}")
    public Result<TicketSourceCallResult<TicketSourceProviderOrder>> order(
            @PathVariable String providerCode,
            @PathVariable String providerOrderId
    ) {
        return Result.success(gateway.getOrder(providerCode, providerOrderId));
    }

    @GetMapping("/{providerCode}/orders/{providerOrderId}/tickets")
    public Result<TicketSourceCallResult<TicketSourceDelivery>> tickets(
            @PathVariable String providerCode,
            @PathVariable String providerOrderId
    ) {
        return Result.success(gateway.getTickets(providerCode, providerOrderId));
    }
}

