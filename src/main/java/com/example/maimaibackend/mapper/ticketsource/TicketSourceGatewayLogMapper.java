package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.log.model.TicketSourceGatewayLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketSourceGatewayLogMapper {
    int insertLog(TicketSourceGatewayLog log);

    List<TicketSourceGatewayLog> selectLogs(@Param("providerCode") String providerCode,
                                             @Param("operationCode") String operationCode,
                                             @Param("success") Boolean success,
                                             @Param("limit") Integer limit);
}
