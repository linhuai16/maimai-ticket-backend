package com.example.maimaibackend.mapper;

import com.example.maimaibackend.vo.ticket.ElectronicTicketVO;
import com.example.maimaibackend.vo.ticket.TicketDetailPageVO;
import com.example.maimaibackend.vo.ticket.TicketFolderItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper {

    int countUser(@Param("userId") Long userId);

    List<TicketFolderItemVO> selectTicketFolderItems(@Param("userId") Long userId);

    TicketDetailPageVO selectTicketDetailBase(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    List<ElectronicTicketVO> selectElectronicTickets(@Param("orderId") Long orderId);
}
