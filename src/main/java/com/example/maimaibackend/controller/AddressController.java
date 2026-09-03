package com.example.maimaibackend.controller;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.address.CreateAddressRequest;
import com.example.maimaibackend.dto.address.UpdateAddressRequest;
import com.example.maimaibackend.service.AddressService;
import com.example.maimaibackend.vo.address.AddressDetailVO;
import com.example.maimaibackend.vo.address.AddressListPageVO;
import com.example.maimaibackend.vo.address.AddressOperateResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Result<AddressListPageVO> getAddressList(@PathVariable Long userId) {
        return Result.success(addressService.getAddressList(userId));
    }

    @GetMapping("/{addressId}")
    public Result<AddressDetailVO> getAddressDetail(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return Result.success(addressService.getAddressDetail(userId, addressId));
    }

    @PostMapping
    public Result<AddressDetailVO> createAddress(
            @PathVariable Long userId,
            @RequestBody CreateAddressRequest request
    ) {
        return Result.success(addressService.createAddress(userId, request));
    }

    @PutMapping("/{addressId}")
    public Result<AddressDetailVO> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody UpdateAddressRequest request
    ) {
        return Result.success(addressService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public Result<AddressOperateResponse> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return Result.success(addressService.deleteAddress(userId, addressId));
    }

    @PutMapping("/{addressId}/default")
    public Result<AddressOperateResponse> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return Result.success(addressService.setDefaultAddress(userId, addressId));
    }
}
