package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.purchase.V13PurchaseService;
import com.example.maimaibackend.ticketsource.purchase.model.V13PayRequest;
import com.example.maimaibackend.ticketsource.purchase.model.V13PurchaseInitView;
import com.example.maimaibackend.ticketsource.purchase.model.V13SubmitOrderRequest;
import com.example.maimaibackend.ticketsource.purchase.model.V13SubmitOrderView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** V1.3 真实购票 App 用户侧安全 Facade。 */
@RestController
@RequestMapping("/api/ticket-source/purchase/v13")
public class V13PurchaseController {
    private final V13PurchaseService service;

    public V13PurchaseController(V13PurchaseService service) {
        this.service = service;
    }

    @GetMapping("/init")
    public Result<V13PurchaseInitView> init(@RequestParam(required = false) Long userId,
                                            @RequestParam Long projectId,
                                            @RequestParam Long sessionId,
                                            @RequestParam Long skuId,
                                            @RequestParam(defaultValue = "1") Integer quantity) {
        return Result.success(service.init(userId, projectId, sessionId, skuId, quantity));
    }

    @PostMapping("/submit")
    public Result<V13SubmitOrderView> submit(@RequestBody V13SubmitOrderRequest request) {
        return Result.success(service.submit(request));
    }

    @PostMapping("/orders/{orderId}/pay")
    public Result<V13SubmitOrderView> pay(@PathVariable Long orderId,
                                          @RequestBody V13PayRequest request) {
        return Result.success(service.pay(orderId, request));
    }
}
