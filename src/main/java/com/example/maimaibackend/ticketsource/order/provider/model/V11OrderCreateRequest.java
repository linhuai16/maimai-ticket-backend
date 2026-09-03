package com.example.maimaibackend.ticketsource.order.provider.model;

/** 使用服务端保存的计价单创建订单，客户端不能自行覆盖金额。 */
public record V11OrderCreateRequest(Long userId, String quoteId) {}
