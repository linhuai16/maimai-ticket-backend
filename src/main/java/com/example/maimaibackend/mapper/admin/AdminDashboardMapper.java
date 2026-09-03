package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.vo.admin.AdminDashboardVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDashboardMapper {
    AdminDashboardVO selectDashboardSummary();
}
