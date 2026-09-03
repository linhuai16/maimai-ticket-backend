package com.example.maimaibackend.vo.address;

public class AddressItemVO {
    private Long addressId;
    private String receiverName;
    private String maskedReceiverPhone;
    private String fullAddress;
    private Boolean isDefault;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getMaskedReceiverPhone() { return maskedReceiverPhone; }
    public void setMaskedReceiverPhone(String maskedReceiverPhone) { this.maskedReceiverPhone = maskedReceiverPhone; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
