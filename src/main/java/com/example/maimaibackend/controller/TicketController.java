package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.TicketService;
import com.example.maimaibackend.vo.ticket.TicketDetailPageVO;
import com.example.maimaibackend.vo.ticket.TicketFolderPageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/users/{userId}/ticket-folder")
    public Result<TicketFolderPageVO> getTicketFolder(@PathVariable Long userId) {
        return Result.success(ticketService.getTicketFolder(userId));
    }

    @GetMapping("/orders/{orderId}/tickets")
    public Result<TicketDetailPageVO> getTicketDetail(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long ticketId
    ) {
        return Result.success(ticketService.getTicketDetail(userId, orderId, ticketId));
    }
}
