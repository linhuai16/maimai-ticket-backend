package com.example.maimaibackend.ticketsource.provider.model;

import java.util.List;
import java.util.Map;

final class ModelSupport {
    private ModelSupport() {}

    static <T> List<T> list(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    static <K, V> Map<K, V> map(Map<K, V> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }

    static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "不能为空");
        }
        return value.trim();
    }
}
