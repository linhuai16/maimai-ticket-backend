package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.mapper.admin.AdminDashboardMapper;
import com.example.maimaibackend.vo.admin.AdminDashboardVO;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public AdminDashboardService(AdminDashboardMapper adminDashboardMapper) {
        this.adminDashboardMapper = adminDashboardMapper;
    }

    public AdminDashboardVO getDashboardSummary() {
        return adminDashboardMapper.selectDashboardSummary();
    }
}
