package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.vo.admin.AdminAccountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminAuthMapper {
    AdminAccountVO selectByUsername(@Param("username") String username);

    int updateLastLoginTime(@Param("adminId") Long adminId);
}
