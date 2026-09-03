package com.example.maimaibackend.mapper;


import com.example.maimaibackend.vo.performance.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface PerformanceMapper {

    List<PerformanceCardVO> selectCategoryPerformanceList(
            @Param("cityName") String cityName,
            @Param("categoryId") Long categoryId,
            @Param("sortType") String sortType,
            @Param("filterTime") String filterTime,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    Integer countCategoryPerformanceList(
            @Param("cityName") String cityName,
            @Param("categoryId") Long categoryId,
            @Param("filterTime") String filterTime
    );

    List<PerformanceCardVO> selectSearchPerformanceList(
            @Param("cityName") String cityName,
            @Param("keyword") String keyword,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    Integer countSearchPerformanceList(@Param("cityName") String cityName, @Param("keyword") String keyword);

    List<VenueVO> selectVenueLookupList();

    ProjectDetailVO selectProjectDetail(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    List<SessionItemVO> selectSessionList(@Param("projectId") Long projectId);

    SessionItemVO selectSessionById(
            @Param("projectId") Long projectId,
            @Param("sessionId") Long sessionId
    );

    List<ServiceTagVO> selectManualServiceTags(@Param("projectId") Long projectId);

    List<ServiceTagVO> selectRefundServiceTags(@Param("projectId") Long projectId);

    List<String> selectRefundServiceTagDetailItems(@Param("projectId") Long projectId);

    Integer countProjectNotice(@Param("projectId") Long projectId);

    List<NoticeItemVO> selectProjectNotices(@Param("projectId") Long projectId);


    List<TicketSkuVO> selectTicketSkus(@Param("sessionId") Long sessionId);
}
