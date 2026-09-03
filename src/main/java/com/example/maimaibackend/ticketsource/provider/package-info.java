/**
 * 第三方票源统一接口契约 V1.1 的 Java 模型边界。
 *
 * <p>本包与现有 ticketsource.gateway 旧模型并行存在。第一批只建立统一模型、
 * Adapter 契约和兼容转换，不改变数据库结构，也不接管现有资源、订单、出票和退款流程。</p>
 *
 * <p>第三方平台原始 DTO 必须停留在平台专属 Adapter 包内，不得直接进入本包之外的
 * 核心业务、数据库实体或鸿蒙前端 VO。</p>
 */
package com.example.maimaibackend.ticketsource.provider;
