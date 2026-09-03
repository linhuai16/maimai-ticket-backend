package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminSaveCategoryRequest;
import com.example.maimaibackend.vo.admin.AdminCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminCategoryMapper {
    List<AdminCategoryVO> selectCategoryList();

    AdminCategoryVO selectCategoryById(@Param("categoryId") Long categoryId);

    Integer countCategoryById(@Param("categoryId") Long categoryId);

    Integer countDuplicateName(@Param("categoryId") Long categoryId,
                               @Param("categoryName") String categoryName);

    Integer updateCategory(@Param("categoryId") Long categoryId,
                           @Param("request") AdminSaveCategoryRequest request);
}
