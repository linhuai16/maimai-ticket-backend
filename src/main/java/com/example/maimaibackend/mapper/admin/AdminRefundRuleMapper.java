package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.vo.admin.AdminRefundRuleDetailVO;
import com.example.maimaibackend.vo.admin.AdminRefundRuleItemVO;
import com.example.maimaibackend.vo.admin.AdminRefundRuleStageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRefundRuleMapper {
    Integer countRefundRuleList(@Param("keyword") String keyword,
                                @Param("refundType") String refundType);

    List<AdminRefundRuleItemVO> selectRefundRuleList(@Param("keyword") String keyword,
                                                     @Param("refundType") String refundType,
                                                     @Param("limit") Integer limit,
                                                     @Param("offset") Integer offset);

    Integer countProjectById(@Param("projectId") Long projectId);

    AdminRefundRuleDetailVO selectRefundRuleDetailByProjectId(@Param("projectId") Long projectId);

    List<AdminRefundRuleStageVO> selectRefundRuleStages(@Param("refundRuleId") Long refundRuleId);
}
