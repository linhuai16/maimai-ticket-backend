package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminRelationSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSaveNoticeRequest;
import com.example.maimaibackend.vo.admin.AdminNoticeVO;
import com.example.maimaibackend.vo.admin.AdminProjectNoticeConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminNoticeMapper {
    List<AdminNoticeVO> selectNoticeList(@Param("keyword") String keyword);

    AdminNoticeVO selectNoticeById(@Param("noticeId") Long noticeId);

    Integer countNoticeById(@Param("noticeId") Long noticeId);

    Integer countDuplicateTitle(@Param("noticeId") Long noticeId,
                                @Param("title") String title);

    Integer insertNotice(AdminSaveNoticeRequest request);

    Integer updateNotice(AdminSaveNoticeRequest request);

    Integer countProjectRelations(@Param("noticeId") Long noticeId);

    Integer deleteNotice(@Param("noticeId") Long noticeId);

    Integer countProjectById(@Param("projectId") Long projectId);

    AdminProjectNoticeConfigVO selectProjectNoticeBase(@Param("projectId") Long projectId);

    List<AdminNoticeVO> selectProjectNotices(@Param("projectId") Long projectId);

    List<AdminNoticeVO> selectProviderProjectNotices(@Param("projectId") Long projectId);

    Integer countProviderProjectNoticeRelationsByIds(@Param("projectId") Long projectId,
                                                     @Param("noticeIds") List<Long> noticeIds);

    Integer deleteProjectNoticeRelations(@Param("projectId") Long projectId);

    Integer insertProjectNoticeRelations(@Param("projectId") Long projectId,
                                          @Param("items") List<AdminRelationSaveDTO> items);

    Integer countNoticesByIds(@Param("noticeIds") List<Long> noticeIds);
}
