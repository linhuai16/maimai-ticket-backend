package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProjectMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSessionMapping;
import com.example.maimaibackend.ticketsource.domain.model.TicketSourceSkuMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方资源映射的只读入口。同步写入逻辑在第三批实现，避免第一批提前改变现有业务行为。
 */
@Mapper
public interface TicketSourceMappingMapper {
    TicketSourceProjectMapping selectProjectMapping(@Param("providerId") Long providerId,
                                                     @Param("projectId") Long projectId);

    TicketSourceSessionMapping selectSessionMapping(@Param("providerId") Long providerId,
                                                     @Param("sessionId") Long sessionId);

    TicketSourceSkuMapping selectSkuMapping(@Param("providerId") Long providerId,
                                             @Param("skuId") Long skuId);

    List<TicketSourceSkuMapping> selectBoundSkuMappingsBySkuId(@Param("skuId") Long skuId);
}
