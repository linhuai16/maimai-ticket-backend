package com.example.maimaibackend.ticketsource.provider.model;

/** V1.2 联系人。证件信息属于 Person/holder，不应混入 contact。 */
public record ProviderContact(
        String name,
        String phoneCountryCode,
        String phone,
        String email
) {
    public ProviderContact {
        name = ModelSupport.required(name, "contact.name");
        phoneCountryCode = (phoneCountryCode == null || phoneCountryCode.isBlank()) ? "86" : phoneCountryCode.trim();
        phone = ModelSupport.required(phone, "contact.phone");
        email = email == null || email.isBlank() ? null : email.trim();
    }
}
