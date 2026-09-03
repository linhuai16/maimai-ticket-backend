package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.address.AddressSaveDTO;
import com.example.maimaibackend.dto.address.CreateAddressRequest;
import com.example.maimaibackend.dto.address.UpdateAddressRequest;
import com.example.maimaibackend.mapper.AddressMapper;
import com.example.maimaibackend.util.MaskUtil;
import com.example.maimaibackend.util.ValidateUtil;
import com.example.maimaibackend.vo.address.AddressDetailVO;
import com.example.maimaibackend.vo.address.AddressItemVO;
import com.example.maimaibackend.vo.address.AddressListPageVO;
import com.example.maimaibackend.vo.address.AddressOperateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;
    private final UserService userService;

    public AddressService(AddressMapper addressMapper, UserService userService) {
        this.addressMapper = addressMapper;
        this.userService = userService;
    }

    public AddressListPageVO getAddressList(Long userId) {
        userService.requireUser(userId);
        List<AddressDetailVO> records = addressMapper.selectAddressList(userId);
        List<AddressItemVO> items = new ArrayList<>();
        for (AddressDetailVO record : records) {
            AddressItemVO item = new AddressItemVO();
            item.setAddressId(record.getAddressId());
            item.setReceiverName(record.getReceiverName());
            item.setMaskedReceiverPhone(MaskUtil.maskPhone(record.getReceiverPhone()));
            item.setFullAddress(buildFullAddress(record));
            item.setIsDefault(record.getIsDefault());
            items.add(item);
        }
        AddressListPageVO vo = new AddressListPageVO();
        vo.setAddresses(items);
        return vo;
    }

    public AddressDetailVO getAddressDetail(Long userId, Long addressId) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(addressId, "地址ID");
        AddressDetailVO detail = addressMapper.selectAddressDetail(userId, addressId);
        if (detail == null) {
            throw new BusinessException("地址不存在或不属于当前用户");
        }
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressDetailVO createAddress(Long userId, CreateAddressRequest request) {
        userService.requireUser(userId);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        AddressSaveDTO save = buildSaveDTO(userId, null, request.getReceiverName(), request.getReceiverPhone(),
                request.getProvince(), request.getCity(), request.getDistrict(), request.getDetailAddress(), request.getIsDefault(),
                request.getCountryCode(), request.getProvinceCode(), request.getCityCode(), request.getAreaCode());

        if (addressMapper.countDuplicateAddress(save) > 0) {
            throw new BusinessException("当前用户下已存在相同收货地址");
        }

        int existingCount = addressMapper.countByUserId(userId);
        boolean shouldDefault = existingCount == 0 || Boolean.TRUE.equals(save.getIsDefault());
        save.setIsDefault(shouldDefault);
        if (shouldDefault) {
            addressMapper.clearDefault(userId);
        }

        addressMapper.insertAddress(save);
        return getAddressDetail(userId, save.getAddressId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressDetailVO updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(addressId, "地址ID");
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        AddressDetailVO old = getAddressDetail(userId, addressId);
        Boolean resolvedDefault = request.getIsDefault() == null ? old.getIsDefault() : request.getIsDefault();
        String[] updateCodes = resolveUpdateRegionCodes(request, old);
        AddressSaveDTO save = buildSaveDTO(userId, addressId, request.getReceiverName(), request.getReceiverPhone(),
                request.getProvince(), request.getCity(), request.getDistrict(), request.getDetailAddress(), resolvedDefault,
                updateCodes[0], updateCodes[1], updateCodes[2], updateCodes[3]);

        if (addressMapper.countDuplicateAddressExcludeSelf(save) > 0) {
            throw new BusinessException("当前用户下已存在其他相同收货地址");
        }

        if (Boolean.TRUE.equals(save.getIsDefault())) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.updateAddress(save);

        if (Boolean.TRUE.equals(old.getIsDefault()) && !Boolean.TRUE.equals(save.getIsDefault())) {
            ensureOneDefault(userId);
        }
        return getAddressDetail(userId, addressId);
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressOperateResponse deleteAddress(Long userId, Long addressId) {
        userService.requireUser(userId);
        AddressDetailVO old = getAddressDetail(userId, addressId);
        int rows = addressMapper.deleteAddress(userId, addressId);
        if (rows > 0 && Boolean.TRUE.equals(old.getIsDefault())) {
            ensureOneDefault(userId);
        }
        return new AddressOperateResponse(rows > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressOperateResponse setDefaultAddress(Long userId, Long addressId) {
        userService.requireUser(userId);
        getAddressDetail(userId, addressId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(userId, addressId);
        return new AddressOperateResponse(true);
    }

    private AddressSaveDTO buildSaveDTO(Long userId, Long addressId, String receiverName, String receiverPhone,
                                        String province, String city, String district, String detailAddress,
                                        Boolean isDefault, String countryCode, String provinceCode,
                                        String cityCode, String areaCode) {
        AddressSaveDTO save = new AddressSaveDTO();
        save.setUserId(userId);
        save.setAddressId(addressId);
        save.setReceiverName(ValidateUtil.requireText(receiverName, "收货人", 30));
        save.setReceiverPhone(ValidateUtil.requirePhone(receiverPhone));
        save.setProvince(ValidateUtil.requireText(province, "省份", 50));
        save.setCity(ValidateUtil.requireText(city, "城市", 50));
        save.setDistrict(ValidateUtil.requireText(district, "区县", 50));
        save.setDetailAddress(ValidateUtil.requireText(detailAddress, "详细地址", 255));
        String[] codes = normalizeRegionCodes(countryCode, provinceCode, cityCode, areaCode);
        save.setCountryCode(codes[0]);
        save.setProvinceCode(codes[1]);
        save.setCityCode(codes[2]);
        save.setAreaCode(codes[3]);
        save.setIsDefault(Boolean.TRUE.equals(isDefault));
        return save;
    }


    private String[] normalizeRegionCodes(String countryCode, String provinceCode, String cityCode, String areaCode) {
        String country = trimToNull(countryCode);
        String province = trimToNull(provinceCode);
        String city = trimToNull(cityCode);
        String area = trimToNull(areaCode);
        boolean any = country != null || province != null || city != null || area != null;
        if (!any) return new String[]{null, null, null, null};
        if (country == null || province == null || city == null || area == null) {
            throw new BusinessException("行政区编码必须同时提供countryCode/provinceCode/cityCode/areaCode");
        }
        country = country.toUpperCase(java.util.Locale.ROOT);
        if (!country.matches("[A-Z]{2}")) throw new BusinessException("countryCode格式不合法");
        if (!province.matches("\\d{6}") || !city.matches("\\d{6}") || !area.matches("\\d{6}")) {
            throw new BusinessException("provinceCode/cityCode/areaCode必须为6位行政区编码");
        }
        return new String[]{country, province, city, area};
    }

    private String[] resolveUpdateRegionCodes(UpdateAddressRequest request, AddressDetailVO old) {
        boolean anyCodeProvided = trimToNull(request.getCountryCode()) != null
                || trimToNull(request.getProvinceCode()) != null
                || trimToNull(request.getCityCode()) != null
                || trimToNull(request.getAreaCode()) != null;
        if (anyCodeProvided) {
            return new String[]{request.getCountryCode(), request.getProvinceCode(), request.getCityCode(), request.getAreaCode()};
        }

        boolean locationUnchanged = sameText(request.getProvince(), old.getProvince())
                && sameText(request.getCity(), old.getCity())
                && sameText(request.getDistrict(), old.getDistrict());
        if (locationUnchanged) {
            return new String[]{old.getCountryCode(), old.getProvinceCode(), old.getCityCode(), old.getAreaCode()};
        }

        // 旧版客户端只传省/市/区名称时，位置发生变化后不能继续沿用旧行政区编码。
        // 清空编码，等新版客户端重新保存标准 code；第三方 EXPRESS 计价会明确拒绝缺码地址。
        return new String[]{null, null, null, null};
    }

    private boolean sameText(String left, String right) {
        String a = trimToNull(left);
        String b = trimToNull(right);
        return java.util.Objects.equals(a, b);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String buildFullAddress(AddressDetailVO record) {
        return nullToEmpty(record.getProvince())
                + nullToEmpty(record.getCity())
                + nullToEmpty(record.getDistrict())
                + nullToEmpty(record.getDetailAddress());
    }

    private String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private void ensureOneDefault(Long userId) {
        if (addressMapper.countByUserId(userId) > 0 && addressMapper.countDefaultByUserId(userId) == 0) {
            Long firstId = addressMapper.selectFirstAddressId(userId);
            if (firstId != null) {
                addressMapper.setDefault(userId, firstId);
            }
        }
    }
}
