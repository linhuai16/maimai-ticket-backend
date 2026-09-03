package com.example.maimaibackend.mapper.admin;

import com.example.maimaibackend.dto.admin.AdminProjectSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSessionSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSkuSaveDTO;
import com.example.maimaibackend.vo.admin.AdminProjectDetailVO;
import com.example.maimaibackend.vo.admin.AdminProjectItemVO;
import com.example.maimaibackend.vo.admin.AdminSessionItemVO;
import com.example.maimaibackend.vo.admin.AdminSkuItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminPerformanceMapper {
    Integer countProjectList(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("projectStatus") String projectStatus);

    List<AdminProjectItemVO> selectProjectList(@Param("keyword") String keyword,
                                               @Param("categoryId") Long categoryId,
                                               @Param("projectStatus") String projectStatus,
                                               @Param("limit") Integer limit,
                                               @Param("offset") Integer offset);

    AdminProjectDetailVO selectProjectDetail(@Param("projectId") Long projectId);

    List<String> selectEffectiveProjectServiceTagNames(@Param("projectId") Long projectId);

    List<String> selectEffectiveProjectNoticeTitles(@Param("projectId") Long projectId);

    Integer countProjectById(@Param("projectId") Long projectId);

    Integer countTicketSourceProjectMapping(@Param("projectId") Long projectId);

    Integer countTicketSourceSessionMapping(@Param("sessionId") Long sessionId);

    Integer countTicketSourceSkuMapping(@Param("skuId") Long skuId);

    Integer countCategoryById(@Param("categoryId") Long categoryId);

    Integer insertProject(AdminProjectSaveDTO dto);

    Integer updateProject(AdminProjectSaveDTO dto);

    Integer updateSourceManagedProjectOperation(AdminProjectSaveDTO dto);

    Integer updateProjectStatus(@Param("projectId") Long projectId,
                                @Param("projectStatus") String projectStatus,
                                @Param("recommendFlag") Integer recommendFlag);

    Integer countProjectReadySessions(@Param("projectId") Long projectId,
                                      @Param("projectStatus") String projectStatus);

    List<AdminSessionItemVO> selectSessionsByProjectId(@Param("projectId") Long projectId);

    AdminSessionItemVO selectSessionDetail(@Param("sessionId") Long sessionId);

    Integer countSessionById(@Param("sessionId") Long sessionId);

    Long selectProjectIdBySessionId(@Param("sessionId") Long sessionId);

    Integer countVenueById(@Param("venueId") Long venueId);

    String selectVenueCityName(@Param("venueId") Long venueId);

    Integer countDuplicateSession(@Param("projectId") Long projectId,
                                  @Param("venueId") Long venueId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("excludeSessionId") Long excludeSessionId);

    Integer insertSession(AdminSessionSaveDTO dto);

    Integer updateSession(AdminSessionSaveDTO dto);

    Integer updateSessionStatus(@Param("sessionId") Long sessionId,
                                @Param("sessionStatus") String sessionStatus);

    Integer countSessionReadySkus(@Param("sessionId") Long sessionId,
                                  @Param("sessionStatus") String sessionStatus);

    Integer countOrdersBySessionId(@Param("sessionId") Long sessionId);

    Integer countSkusBySessionId(@Param("sessionId") Long sessionId);

    Integer countBannersBySessionId(@Param("sessionId") Long sessionId);

    Integer deleteSession(@Param("sessionId") Long sessionId);

    List<AdminSkuItemVO> selectSkusBySessionId(@Param("sessionId") Long sessionId);

    AdminSkuItemVO selectSkuDetail(@Param("skuId") Long skuId);

    Integer countSkuById(@Param("skuId") Long skuId);

    Integer countDuplicateSkuName(@Param("sessionId") Long sessionId,
                                  @Param("skuName") String skuName,
                                  @Param("excludeSkuId") Long excludeSkuId);

    Integer insertSku(AdminSkuSaveDTO dto);

    Integer updateSku(AdminSkuSaveDTO dto);

    Integer updateSkuStatus(@Param("skuId") Long skuId,
                            @Param("skuStatus") String skuStatus);

    Integer updateSkuStock(@Param("skuId") Long skuId,
                           @Param("stockAvailable") Integer stockAvailable,
                           @Param("version") Integer version,
                           @Param("skuStatus") String skuStatus);

    BigDecimal selectTicketSourceSalePrice(@Param("skuId") Long skuId);

    Integer updateSourceSkuPlatformPrice(@Param("skuId") Long skuId,
                                         @Param("price") BigDecimal price,
                                         @Param("priceMode") String priceMode);

    Integer countOrderItemsBySkuId(@Param("skuId") Long skuId);

    Integer deleteSku(@Param("skuId") Long skuId);

    Integer refreshSessionPrice(@Param("sessionId") Long sessionId);

    Integer refreshProjectPrice(@Param("projectId") Long projectId);
}
