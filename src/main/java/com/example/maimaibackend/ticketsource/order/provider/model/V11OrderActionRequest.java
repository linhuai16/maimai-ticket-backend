package com.example.maimaibackend.ticketsource.order.provider.model;

/** V1.1 支付或取消操作请求。 */
public record V11OrderActionRequest(Long userId, String payMethod, String reason) {}
