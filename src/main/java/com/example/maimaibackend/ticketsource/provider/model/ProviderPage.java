package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;

public record ProviderPage<T>(List<T> records, long total, int pageNo, int pageSize) {
    public ProviderPage {
        records = ModelSupport.list(records);
        if (total < 0 || pageNo < 1 || pageSize < 1) throw new IllegalArgumentException("分页参数不合法");
    }
}
