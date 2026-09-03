package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminRelationSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSaveServiceTagRequest;
import com.example.maimaibackend.vo.admin.AdminServiceTagVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminServiceTagMapper {
    List<AdminServiceTagVO> selectServiceTagList(@Param("keyword") String keyword);

    AdminServiceTagVO selectServiceTagById(@Param("tagId") Long tagId);

    Integer countDuplicateName(@Param("tagId") Long tagId,
                               @Param("tagName") String tagName);

    Integer insertServiceTag(AdminSaveServiceTagRequest request);

    Integer updateServiceTag(AdminSaveServiceTagRequest request);

    Integer countProjectRelations(@Param("tagId") Long tagId);

    Integer countCapabilityRelations(@Param("tagId") Long tagId);

    Integer deleteServiceTag(@Param("tagId") Long tagId);

    String selectProjectTitle(@Param("projectId") Long projectId);

    Integer countProjectById(@Param("projectId") Long projectId);

    List<AdminServiceTagVO> selectProjectManualTags(@Param("projectId") Long projectId);

    List<AdminServiceTagVO> selectProjectProviderTags(@Param("projectId") Long projectId);

    List<AdminServiceTagVO> selectProjectAutomaticRefundTags(@Param("projectId") Long projectId);

    Integer countTagsByIds(@Param("tagIds") List<Long> tagIds);

    Integer countSystemRefundTagsByIds(@Param("tagIds") List<Long> tagIds);

    Integer countProviderTagRelationsByIds(@Param("projectId") Long projectId,
                                           @Param("tagIds") List<Long> tagIds);

    Integer deleteProjectTagRelations(@Param("projectId") Long projectId);

    Integer insertProjectTagRelations(@Param("projectId") Long projectId,
                                      @Param("items") List<AdminRelationSaveDTO> items);
}
