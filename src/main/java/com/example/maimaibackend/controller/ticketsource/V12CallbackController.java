package com.example.maimaibackend.controller.ticketsource;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.ticketsource.provider.model.ProviderCallbackEvent;
import com.example.maimaibackend.ticketsource.workflow.TicketSourceWorkflowService;
import com.example.maimaibackend.ticketsource.workflow.model.V12CallbackAck;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ticket-source/callbacks")
public class V12CallbackController {
    private final TicketSourceWorkflowService service;
    public V12CallbackController(TicketSourceWorkflowService service) { this.service = service; }

    @PostMapping("/{providerCode}/events")
    public Result<V12CallbackAck> receive(@PathVariable String providerCode,
                                          @RequestHeader(value="X-Timestamp", required=false) String timestamp,
                                          @RequestHeader(value="X-Nonce", required=false) String nonce,
                                          @RequestHeader(value="X-Signature", required=false) String signature,
                                          @RequestBody ProviderCallbackEvent event) {
        return Result.success(service.receiveCallback(providerCode, event, timestamp, nonce, signature));
    }
}
