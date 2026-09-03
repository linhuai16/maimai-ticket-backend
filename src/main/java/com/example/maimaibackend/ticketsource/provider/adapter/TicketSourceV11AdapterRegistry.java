package com.example.maimaibackend.ticketsource.provider.adapter;

import com.example.maimaibackend.ticketsource.provider.enums.ProviderCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 第一批独立注册表，暂不替换旧网关注册表。 */
@Component
public final class TicketSourceV11AdapterRegistry {
    private final Map<ProviderCode, TicketSourceV11Adapter> adapters;

    public TicketSourceV11AdapterRegistry(List<TicketSourceV11Adapter> adapterList) {
        EnumMap<ProviderCode, TicketSourceV11Adapter> map = new EnumMap<>(ProviderCode.class);
        if (adapterList != null) {
            for (TicketSourceV11Adapter adapter : adapterList) {
                if (adapter == null || adapter.providerCode() == null) continue;
                TicketSourceV11Adapter previous = map.put(adapter.providerCode(), adapter);
                if (previous != null) throw new IllegalStateException("V1.1适配器重复: " + adapter.providerCode());
            }
        }
        this.adapters = Map.copyOf(map);
    }

    public Optional<TicketSourceV11Adapter> find(ProviderCode providerCode) {
        return Optional.ofNullable(adapters.get(providerCode));
    }

    public boolean onlyContainsFrozenAdapters() {
        return adapters.keySet().stream().allMatch(code -> code == ProviderCode.MOCK_DAMAI);
    }
}
