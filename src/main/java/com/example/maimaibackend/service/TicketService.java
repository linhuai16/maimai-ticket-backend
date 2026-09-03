package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.TicketMapper;
import com.example.maimaibackend.vo.ticket.ElectronicTicketVO;
import com.example.maimaibackend.vo.ticket.TicketDetailPageVO;
import com.example.maimaibackend.vo.ticket.TicketFolderItemVO;
import com.example.maimaibackend.vo.ticket.TicketFolderPageVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private static final String CUSTOMER_SERVICE_PHONE = "400-000-0000";

    private final TicketMapper ticketMapper;

    public TicketService(TicketMapper ticketMapper) {
        this.ticketMapper = ticketMapper;
    }

    public TicketFolderPageVO getTicketFolder(Long userId) {
        validateId(userId, "userId");
        if (ticketMapper.countUser(userId) <= 0) {
            throw new BusinessException("用户不存在");
        }

        List<TicketFolderItemVO> items = ticketMapper.selectTicketFolderItems(userId);
        TicketFolderPageVO vo = new TicketFolderPageVO();
        vo.setUserId(userId);
        vo.setTotal(items.size());
        vo.setItems(items);
        return vo;
    }

    public TicketDetailPageVO getTicketDetail(Long userId, Long orderId, Long ticketId) {
        validateId(userId, "userId");
        validateId(orderId, "orderId");

        TicketDetailPageVO detail = ticketMapper.selectTicketDetailBase(orderId, userId);
        if (detail == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }

        List<ElectronicTicketVO> tickets = ticketMapper.selectElectronicTickets(orderId);
        detail.setCustomerServicePhone(CUSTOMER_SERVICE_PHONE);

        if ("PAPER_TICKET".equals(detail.getDeliveryType())) {
            detail.setDefaultTicketId(null);
            detail.setTickets(List.of());
            return detail;
        }
        if (tickets.isEmpty()) {
            throw new BusinessException("当前订单暂无电子票");
        }

        Long defaultTicketId = detail.getDefaultTicketId();
        if (ticketId != null) {
            validateId(ticketId, "ticketId");
            boolean exists = false;
            for (ElectronicTicketVO ticket : tickets) {
                if (ticketId.equals(ticket.getTicketId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                throw new BusinessException("指定电子票不属于当前订单");
            }
            defaultTicketId = ticketId;
        }

        detail.setDefaultTicketId(defaultTicketId);
        detail.setTickets(tickets);
        return detail;
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException("请求参数无效：" + name);
        }
    }
}
