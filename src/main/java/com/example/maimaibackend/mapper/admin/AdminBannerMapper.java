package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminSaveBannerRequest;
import com.example.maimaibackend.vo.admin.AdminBannerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminBannerMapper {
    Integer countBannerList(@Param("keyword") String keyword,
                            @Param("enableStatus") String enableStatus);

    List<AdminBannerVO> selectBannerList(@Param("keyword") String keyword,
                                         @Param("enableStatus") String enableStatus,
                                         @Param("limit") Integer limit,
                                         @Param("offset") Integer offset);

    AdminBannerVO selectBannerById(@Param("bannerId") Long bannerId);

    Integer countBannerById(@Param("bannerId") Long bannerId);

    Integer countProjectById(@Param("projectId") Long projectId);

    String selectProjectStatus(@Param("projectId") Long projectId);

    Integer countSessionBelongProject(@Param("sessionId") Long sessionId,
                                      @Param("projectId") Long projectId);

    String selectSessionStatusBelongProject(@Param("sessionId") Long sessionId,
                                             @Param("projectId") Long projectId);

    Integer insertBanner(AdminSaveBannerRequest request);

    Integer updateBanner(AdminSaveBannerRequest request);

    Integer updateBannerStatus(@Param("bannerId") Long bannerId,
                               @Param("enableStatus") String enableStatus);

    Integer deleteBanner(@Param("bannerId") Long bannerId);
}
