package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.dto.admin.TicketOperationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TicketOperationExecutor {
    private final AdminTicketLogService adminTicketLogService;

    public TicketOperationExecutor(AdminTicketLogService adminTicketLogService) {
        this.adminTicketLogService = adminTicketLogService;
    }

    public <T> T execute(TicketOperationContext context,
                         String businessType,
                         String actionType,
                         String targetType,
                         Long targetId,
                         Long orderId,
                         Long ticketId,
                         Supplier<T> operation) {
        try {
            return operation.get();
        } catch (RuntimeException e) {
            adminTicketLogService.recordFailed(
                    context,
                    businessType,
                    actionType,
                    targetType,
                    targetId,
                    orderId,
                    ticketId,
                    null,
                    null,
                    "操作失败：" + failureMessage(e),
                    null
            );
            throw e;
        }
    }

    private String failureMessage(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.trim().isEmpty() ? e.getClass().getSimpleName() : message.trim();
    }
}
