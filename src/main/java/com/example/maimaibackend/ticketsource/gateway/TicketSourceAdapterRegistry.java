package com.example.maimaibackend.ticketsource.gateway;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 适配器注册中心。数据库只保存 adapter_code，不保存 Java 类名。
 */
@Component
public class TicketSourceAdapterRegistry {
    private final Map<String, TicketSourceAdapter> adapters;

    public TicketSourceAdapterRegistry(List<TicketSourceAdapter> adapterList) {
        Map<String, TicketSourceAdapter> registered = new LinkedHashMap<>();
        for (TicketSourceAdapter adapter : adapterList) {
            String code = normalize(adapter.adapterCode());
            if (code == null) {
                throw new IllegalStateException("票源适配器编码不能为空: " + adapter.getClass().getName());
            }
            TicketSourceAdapter previous = registered.putIfAbsent(code, adapter);
            if (previous != null) {
                throw new IllegalStateException(
                        "票源适配器编码重复: " + code + ", "
                                + previous.getClass().getName() + " / " + adapter.getClass().getName()
                );
            }
        }
        this.adapters = Collections.unmodifiableMap(registered);
    }

    public Optional<TicketSourceAdapter> find(String adapterCode) {
        String code = normalize(adapterCode);
        return code == null ? Optional.empty() : Optional.ofNullable(adapters.get(code));
    }

    public Map<String, TicketSourceAdapter> all() {
        return adapters;
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
