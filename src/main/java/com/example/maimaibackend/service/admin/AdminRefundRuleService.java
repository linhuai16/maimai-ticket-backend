package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.admin.AdminRefundRuleMapper;
import com.example.maimaibackend.vo.admin.AdminRefundRuleDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundRuleItemVO;
import com.example.maimaibackend.vo.admin.AdminRefundRuleListPageVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminRefundRuleService {

    private static final Set<String> REFUND_TYPE_SET = new HashSet<>(Arrays.asList("CONDITIONAL_REFUND", "NO_REFUND", "MISSING"));

    private final AdminRefundRuleMapper adminRefundRuleMapper;

    public AdminRefundRuleService(AdminRefundRuleMapper adminRefundRuleMapper) {
        this.adminRefundRuleMapper = adminRefundRuleMapper;
    }

    public AdminRefundRuleListPageVO getRefundRuleList(String keyword, String refundType, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeRefundType = normalizeRefundType(refundType);
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminRefundRuleMapper.countRefundRuleList(safeKeyword, safeRefundType);
        List<AdminRefundRuleItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminRefundRuleMapper.selectRefundRuleList(safeKeyword, safeRefundType, safePageSize, offset);
        AdminRefundRuleListPageVO vo = new AdminRefundRuleListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminRefundRuleDetailVO getProjectRefundRule(Long projectId) {
        requireProject(projectId);
        AdminRefundRuleDetailVO vo = adminRefundRuleMapper.selectRefundRuleDetailByProjectId(projectId);
        if (vo == null) throw new BusinessException("演出项目不存在");
        if (vo.getRefundRuleId() == null) vo.setStages(Collections.emptyList());
        else vo.setStages(adminRefundRuleMapper.selectRefundRuleStages(vo.getRefundRuleId()));
        return vo;
    }

    private String normalizeRefundType(String value) {
        String type = trimToNull(value);
        if (type == null) return null;
        type = type.toUpperCase();
        if (!REFUND_TYPE_SET.contains(type)) {
            throw new BusinessException("refundType 仅支持 CONDITIONAL_REFUND、NO_REFUND 或 MISSING");
        }
        return type;
    }

    private void requireProject(Long projectId) {
        if (projectId == null || projectId <= 0) throw new BusinessException("projectId 不合法");
        if (adminRefundRuleMapper.countProjectById(projectId) == 0) throw new BusinessException("演出项目不存在");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
