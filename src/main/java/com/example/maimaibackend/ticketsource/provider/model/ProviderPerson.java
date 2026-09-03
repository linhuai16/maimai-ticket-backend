package com.example.maimaibackend.ticketsource.provider.model;

public record ProviderPerson(
        String name,
        String certificateType,
        String certificateNo,
        String phone
) {
    public ProviderPerson {
        name = ModelSupport.required(name, "name");
        certificateType = ModelSupport.required(certificateType, "certificateType");
        certificateNo = ModelSupport.required(certificateNo, "certificateNo");
    }
}
