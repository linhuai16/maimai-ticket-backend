package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.status.V13LocalStatusRecalculateResult;
import com.example.maimaibackend.ticketsource.status.V13LocalStatusRecalculateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 本地展示状态重算维护入口。 */
@RestController
@RequestMapping("/api/ticket-source/status/v13")
public class V13LocalStatusController {
    private final V13LocalStatusRecalculateService service;

    public V13LocalStatusController(V13LocalStatusRecalculateService service) {
        this.service = service;
    }

    @PostMapping("/recalculate")
    public Result<V13LocalStatusRecalculateResult> recalculate() {
        return Result.success(service.recalculateAll());
    }
}
