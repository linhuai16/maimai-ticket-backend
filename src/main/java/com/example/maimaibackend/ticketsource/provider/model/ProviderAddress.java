package com.example.maimaibackend.ticketsource.provider.model;

/** V1.2 统一地址：只承载行政区编码与详细地址；收件人联系方式由 ProviderContact 单独承载。 */
public record ProviderAddress(
        String countryCode,
        String provinceCode,
        String cityCode,
        String areaCode,
        String detailAddress
) {
    public ProviderAddress {
        countryCode = ModelSupport.required(countryCode, "countryCode");
        provinceCode = ModelSupport.required(provinceCode, "provinceCode");
        cityCode = ModelSupport.required(cityCode, "cityCode");
        areaCode = ModelSupport.required(areaCode, "areaCode");
        detailAddress = ModelSupport.required(detailAddress, "detailAddress");
    }
}
