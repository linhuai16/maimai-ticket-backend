package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.TicketOperationLogDTO;
import com.example.maimaibackend.vo.admin.AdminTicketLogItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminTicketLogMapper {
    Integer insertLog(TicketOperationLogDTO log);

    Integer countLogs(@Param("keyword") String keyword,
                      @Param("businessType") String businessType,
                      @Param("operatorType") String operatorType,
                      @Param("actionType") String actionType,
                      @Param("resultStatus") String resultStatus,
                      @Param("ticketId") Long ticketId,
                      @Param("orderId") Long orderId,
                      @Param("operatorId") Long operatorId,
                      @Param("dateFrom") String dateFrom,
                      @Param("dateTo") String dateTo);

    List<AdminTicketLogItemVO> selectLogs(@Param("keyword") String keyword,
                                          @Param("businessType") String businessType,
                                          @Param("operatorType") String operatorType,
                                          @Param("actionType") String actionType,
                                          @Param("resultStatus") String resultStatus,
                                          @Param("ticketId") Long ticketId,
                                          @Param("orderId") Long orderId,
                                          @Param("operatorId") Long operatorId,
                                          @Param("dateFrom") String dateFrom,
                                          @Param("dateTo") String dateTo,
                                          @Param("limit") Integer limit,
                                          @Param("offset") Integer offset);

    AdminTicketLogItemVO selectLogDetail(@Param("logId") Long logId);
}
