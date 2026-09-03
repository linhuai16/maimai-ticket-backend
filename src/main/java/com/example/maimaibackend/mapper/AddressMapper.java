package com.example.maimaibackend.mapper;

import com.example.maimaibackend.dto.address.AddressSaveDTO;
import com.example.maimaibackend.vo.address.AddressDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {

    List<AddressDetailVO> selectAddressList(@Param("userId") Long userId);

    AddressDetailVO selectAddressDetail(@Param("userId") Long userId, @Param("addressId") Long addressId);

    int countByUserId(@Param("userId") Long userId);

    int countDefaultByUserId(@Param("userId") Long userId);

    int countDuplicateAddress(AddressSaveDTO address);

    int countDuplicateAddressExcludeSelf(AddressSaveDTO address);

    int clearDefault(@Param("userId") Long userId);

    int insertAddress(AddressSaveDTO address);

    int updateAddress(AddressSaveDTO address);

    int deleteAddress(@Param("userId") Long userId, @Param("addressId") Long addressId);

    int setDefault(@Param("userId") Long userId, @Param("addressId") Long addressId);

    Long selectFirstAddressId(@Param("userId") Long userId);
}
