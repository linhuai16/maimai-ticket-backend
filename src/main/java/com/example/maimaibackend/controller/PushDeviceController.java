package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.notification.PushDeviceService;
import com.example.maimaibackend.notification.ServiceCardRegistration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/push/devices")
public class PushDeviceController {
    private final PushDeviceService pushDeviceService;

    public PushDeviceController(PushDeviceService pushDeviceService) {
        this.pushDeviceService = pushDeviceService;
    }

    @PostMapping("/bind")
    public Result<Map<String, Object>> bind(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @RequestBody PushDeviceRequest request) {
        return Result.success(pushDeviceService.bind(authorization, request.userId(), request.deviceId(), request.pushToken(),
                request.serviceCards()));
    }

    @PostMapping("/unbind")
    public Result<Map<String, Object>> unbind(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @RequestBody PushDeviceRequest request) {
        return Result.success(pushDeviceService.unbind(authorization, request.userId(), request.deviceId(), request.pushToken()));
    }

    public record PushDeviceRequest(Long userId, String deviceId, String pushToken,
                                    List<ServiceCardRegistration> serviceCards) {}
}
