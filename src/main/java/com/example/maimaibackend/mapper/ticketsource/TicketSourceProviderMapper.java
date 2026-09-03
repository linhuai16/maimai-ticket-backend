package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.domain.model.TicketSourceProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第一批只提供票源配置读取能力；第二批由统一票源网关消费这些配置。
 */
@Mapper
public interface TicketSourceProviderMapper {
    TicketSourceProvider selectById(@Param("providerId") Long providerId);

    TicketSourceProvider selectByCode(@Param("providerCode") String providerCode);

    List<TicketSourceProvider> selectEnabledProviders();

    List<TicketSourceProvider> selectAllProviders();
}
