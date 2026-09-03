package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminCreateSkuRequest;
import com.example.maimaibackend.dto.admin.AdminProjectSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSaveProjectRequest;
import com.example.maimaibackend.dto.admin.AdminSaveSessionRequest;
import com.example.maimaibackend.dto.admin.AdminSessionSaveDTO;
import com.example.maimaibackend.dto.admin.AdminSkuSaveDTO;
import com.example.maimaibackend.dto.admin.AdminUpdateSkuRequest;
import com.example.maimaibackend.dto.admin.AdminUpdateSkuStockRequest;
import com.example.maimaibackend.dto.admin.AdminUpdatePlatformPriceRequest;
import com.example.maimaibackend.dto.admin.UpdateProjectStatusRequest;
import com.example.maimaibackend.dto.admin.UpdateSessionStatusRequest;
import com.example.maimaibackend.dto.admin.UpdateSkuStatusRequest;
import com.example.maimaibackend.mapper.admin.AdminPerformanceMapper;
import com.example.maimaibackend.media.MediaBusinessType;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminProjectDetailVO;
import com.example.maimaibackend.vo.admin.AdminProjectItemVO;
import com.example.maimaibackend.vo.admin.AdminProjectListPageVO;
import com.example.maimaibackend.vo.admin.AdminSessionItemVO;
import com.example.maimaibackend.vo.admin.AdminSkuItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminPerformanceService {

    private static final Set<String> PROJECT_STATUS_SET = new HashSet<>(Arrays.asList(
            "ON_SALE", "COMING_SOON", "SOLD_OUT", "OFFLINE"
    ));

    private static final Set<String> SESSION_STATUS_SET = new HashSet<>(Arrays.asList(
            "ON_SALE", "PRESALE", "SOLD_OUT", "ENDED", "OFFLINE"
    ));

    private static final Set<String> SKU_STATUS_SET = new HashSet<>(Arrays.asList(
            "ON_SALE", "PRESALE", "SOLD_OUT", "OFFLINE"
    ));

    private static final Set<String> DELIVERY_TYPE_SET = new HashSet<>(Arrays.asList(
            "ETICKET", "PAPER_TICKET"
    ));

    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
    private static final BigDecimal MAX_HOT_SCORE = new BigDecimal("99999999.99");

    private final AdminPerformanceMapper adminPerformanceMapper;
    private final AdminMediaService adminMediaService;
    private final AdminRichTextService adminRichTextService;

    public AdminPerformanceService(
            AdminPerformanceMapper adminPerformanceMapper,
            AdminMediaService adminMediaService,
            AdminRichTextService adminRichTextService
    ) {
        this.adminPerformanceMapper = adminPerformanceMapper;
        this.adminMediaService = adminMediaService;
        this.adminRichTextService = adminRichTextService;
    }

    public AdminProjectListPageVO getProjectList(String keyword, Long categoryId, String projectStatus,
                                                  Integer pageNo, Integer pageSize) {
        Integer safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        Integer safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeStatus = trimToNull(projectStatus);
        if (safeStatus != null && !PROJECT_STATUS_SET.contains(safeStatus)) {
            throw new BusinessException("项目状态不合法");
        }
        if (categoryId != null && adminPerformanceMapper.countCategoryById(categoryId) == 0) {
            throw new BusinessException("演出分类不存在");
        }
        Integer offset = (safePageNo - 1) * safePageSize;
        Integer total = adminPerformanceMapper.countProjectList(safeKeyword, categoryId, safeStatus);
        List<AdminProjectItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminPerformanceMapper.selectProjectList(safeKeyword, categoryId, safeStatus, safePageSize, offset);

        AdminProjectListPageVO vo = new AdminProjectListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminProjectDetailVO getProjectDetail(Long projectId) {
        requireProject(projectId);
        return loadProjectDetail(projectId);
    }

    @Transactional
    public AdminProjectDetailVO createProject(AdminSaveProjectRequest request) {
        AdminProjectSaveDTO dto = validateAndBuildProject(null, request, true, null);
        adminPerformanceMapper.insertProject(dto);
        return loadProjectDetail(dto.getProjectId());
    }

    @Transactional
    public AdminProjectDetailVO updateProject(Long projectId, AdminSaveProjectRequest request) {
        requireProject(projectId);
        AdminProjectDetailVO current = loadProjectDetail(projectId);
        if (Boolean.TRUE.equals(current.getSourceManaged())) {
            AdminProjectSaveDTO dto = validateAndBuildSourceManagedOperation(projectId, request, current);
            if (adminPerformanceMapper.updateSourceManagedProjectOperation(dto) != 1) {
                throw new BusinessException(409, "第三方项目运营配置更新失败，请刷新后重试");
            }
            return loadProjectDetail(projectId);
        }
        AdminProjectSaveDTO dto = validateAndBuildProject(projectId, request, false, current);
        adminPerformanceMapper.updateProject(dto);
        return loadProjectDetail(projectId);
    }

    @Transactional
    public AdminOperateResponse updateProjectStatus(Long projectId, UpdateProjectStatusRequest request) {
        requireProject(projectId);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String projectStatus = trimToNull(request.getProjectStatus());
        Integer recommendFlag = request.getRecommendFlag();
        if (projectStatus == null && recommendFlag == null) {
            throw new BusinessException("至少需要传入 projectStatus 或 recommendFlag");
        }
        if (projectStatus != null && !PROJECT_STATUS_SET.contains(projectStatus)) {
            throw new BusinessException("项目状态不合法");
        }
        validateFlag(recommendFlag, "recommendFlag");

        AdminProjectDetailVO current = loadProjectDetail(projectId);
        String targetStatus = projectStatus == null ? current.getProjectStatus() : projectStatus;
        Integer targetRecommend = recommendFlag == null ? current.getRecommendFlag() : recommendFlag;
        if ("OFFLINE".equals(targetStatus) && recommendFlag != null && recommendFlag == 1) {
            throw new BusinessException("已下架项目不能设为首页推荐");
        }
        if ("OFFLINE".equals(targetStatus)) {
            targetRecommend = 0;
        } else if (projectStatus != null) {
            validateProjectReadyForPublish(projectId, targetStatus);
        }
        if (targetRecommend != null && targetRecommend == 1) {
            validateProjectReadyForPublish(projectId, targetStatus);
        }
        int updated = safeCount(adminPerformanceMapper.updateProjectStatus(
                projectId, projectStatus, targetRecommend));
        if (updated != 1) {
            throw new BusinessException(409, "项目状态更新失败，请刷新后重试");
        }
        String message;
        if (projectStatus == null && recommendFlag != null) {
            message = recommendFlag == 1 ? "已设为首页推荐" : "已取消首页推荐";
        } else {
            message = "OFFLINE".equals(targetStatus) ? "项目已下架" : "项目状态已更新";
        }
        return new AdminOperateResponse(true, message);
    }

    public List<AdminSessionItemVO> getProjectSessions(Long projectId) {
        requireProject(projectId);
        return adminPerformanceMapper.selectSessionsByProjectId(projectId);
    }

    public AdminSessionItemVO getSessionDetail(Long sessionId) {
        requireSession(sessionId);
        return adminPerformanceMapper.selectSessionDetail(sessionId);
    }

    @Transactional
    public AdminSessionItemVO createSession(Long projectId, AdminSaveSessionRequest request) {
        requireProject(projectId);
        requireLocalManagedProject(projectId);
        AdminSessionSaveDTO dto = validateAndBuildSession(null, projectId, request, true, null);
        adminPerformanceMapper.insertSession(dto);
        return adminPerformanceMapper.selectSessionDetail(dto.getSessionId());
    }

    @Transactional
    public AdminSessionItemVO updateSession(Long sessionId, AdminSaveSessionRequest request) {
        requireSession(sessionId);
        requireLocalManagedSession(sessionId);
        if (adminPerformanceMapper.countOrdersBySessionId(sessionId) > 0) {
            throw new BusinessException("该场次已产生订单，不能修改基础信息；请使用状态接口调整场次状态");
        }
        Long projectId = adminPerformanceMapper.selectProjectIdBySessionId(sessionId);
        AdminSessionItemVO current = adminPerformanceMapper.selectSessionDetail(sessionId);
        AdminSessionSaveDTO dto = validateAndBuildSession(
                sessionId, projectId, request, false, current.getSessionStatus());
        adminPerformanceMapper.updateSession(dto);
        adminPerformanceMapper.refreshProjectPrice(projectId);
        return adminPerformanceMapper.selectSessionDetail(sessionId);
    }

    @Transactional
    public AdminOperateResponse deleteSession(Long sessionId) {
        requireSession(sessionId);
        requireLocalManagedSession(sessionId);
        if (adminPerformanceMapper.countOrdersBySessionId(sessionId) > 0) {
            throw new BusinessException("该场次已产生订单，不能删除");
        }
        if (adminPerformanceMapper.countSkusBySessionId(sessionId) > 0) {
            throw new BusinessException("请先删除该场次下的全部票档");
        }
        if (adminPerformanceMapper.countBannersBySessionId(sessionId) > 0) {
            throw new BusinessException("该场次仍被 Banner 引用，请先修改或删除关联 Banner");
        }
        adminPerformanceMapper.deleteSession(sessionId);
        return new AdminOperateResponse(true, "场次已删除");
    }

    @Transactional
    public AdminOperateResponse updateSessionStatus(Long sessionId, UpdateSessionStatusRequest request) {
        requireSession(sessionId);
        requireLocalManagedSession(sessionId);
        if (request == null || trimToNull(request.getSessionStatus()) == null) {
            throw new BusinessException("sessionStatus 不能为空");
        }
        String sessionStatus = trimToNull(request.getSessionStatus());
        if (!SESSION_STATUS_SET.contains(sessionStatus)) {
            throw new BusinessException("场次状态不合法");
        }
        if ("ON_SALE".equals(sessionStatus)
                || "PRESALE".equals(sessionStatus)
                || "SOLD_OUT".equals(sessionStatus)) {
            validateSessionReadyForPublish(sessionId, sessionStatus);
        }
        Long projectId = adminPerformanceMapper.selectProjectIdBySessionId(sessionId);
        int updated = safeCount(adminPerformanceMapper.updateSessionStatus(sessionId, sessionStatus));
        if (updated != 1) {
            throw new BusinessException(409, "场次状态更新失败，请刷新后重试");
        }
        adminPerformanceMapper.refreshProjectPrice(projectId);
        return new AdminOperateResponse(true,
                "OFFLINE".equals(sessionStatus) ? "场次已下架" : "场次状态已更新");
    }

    public List<AdminSkuItemVO> getSessionSkus(Long sessionId) {
        requireSession(sessionId);
        return adminPerformanceMapper.selectSkusBySessionId(sessionId);
    }

    public AdminSkuItemVO getSkuDetail(Long skuId) {
        return requireSku(skuId);
    }

    @Transactional
    public AdminSkuItemVO createSku(Long sessionId, AdminCreateSkuRequest request) {
        requireSession(sessionId);
        requireLocalManagedSession(sessionId);
        Long projectId = adminPerformanceMapper.selectProjectIdBySessionId(sessionId);
        AdminSkuSaveDTO dto = validateAndBuildCreateSku(projectId, sessionId, request);
        adminPerformanceMapper.insertSku(dto);
        refreshPrice(projectId, sessionId);
        return adminPerformanceMapper.selectSkuDetail(dto.getSkuId());
    }

    @Transactional
    public AdminSkuItemVO updateSku(Long skuId, AdminUpdateSkuRequest request) {
        AdminSkuItemVO current = requireSku(skuId);
        requireLocalManagedSku(skuId);
        AdminSkuSaveDTO dto = validateAndBuildUpdateSku(current, request);
        adminPerformanceMapper.updateSku(dto);
        refreshPrice(current.getProjectId(), current.getSessionId());
        return adminPerformanceMapper.selectSkuDetail(skuId);
    }

    @Transactional
    public AdminSkuItemVO updateSkuStock(Long skuId, AdminUpdateSkuStockRequest request) {
        AdminSkuItemVO current = requireSku(skuId);
        requireLocalManagedSku(skuId);
        if (request == null || request.getStockAvailable() == null) {
            throw new BusinessException("stockAvailable 不能为空");
        }
        if (request.getVersion() == null || request.getVersion() < 0) {
            throw new BusinessException("version 不能为空且不能小于 0");
        }
        if (request.getStockAvailable() < 0) {
            throw new BusinessException("可售库存不能小于 0");
        }
        String targetStatus = null;
        if (request.getStockAvailable() == 0 && !"OFFLINE".equals(current.getSkuStatus())) {
            targetStatus = "SOLD_OUT";
        }
        int changed = adminPerformanceMapper.updateSkuStock(
                skuId, request.getStockAvailable(), request.getVersion(), targetStatus
        );
        if (changed == 0) {
            throw new BusinessException("库存版本已变化，请刷新票档后重试");
        }
        return adminPerformanceMapper.selectSkuDetail(skuId);
    }

    @Transactional
    public AdminOperateResponse updateSkuStatus(Long skuId, UpdateSkuStatusRequest request) {
        AdminSkuItemVO current = requireSku(skuId);
        requireLocalManagedSku(skuId);
        if (request == null || trimToNull(request.getSkuStatus()) == null) {
            throw new BusinessException("skuStatus 不能为空");
        }
        String skuStatus = trimToNull(request.getSkuStatus());
        if (!SKU_STATUS_SET.contains(skuStatus)) {
            throw new BusinessException("票档状态不合法");
        }
        if (("ON_SALE".equals(skuStatus) || "PRESALE".equals(skuStatus))
                && (current.getStockAvailable() == null || current.getStockAvailable() <= 0)) {
            throw new BusinessException("可售库存为 0，不能设置为可售或预售");
        }
        adminPerformanceMapper.updateSkuStatus(skuId, skuStatus);
        refreshPrice(current.getProjectId(), current.getSessionId());
        return new AdminOperateResponse(true, "票档状态已更新");
    }

    @Transactional
    public AdminOperateResponse deleteSku(Long skuId) {
        AdminSkuItemVO current = requireSku(skuId);
        requireLocalManagedSku(skuId);
        if (adminPerformanceMapper.countOrderItemsBySkuId(skuId) > 0) {
            throw new BusinessException("该票档已有订单记录，不能删除；可改为 OFFLINE");
        }
        if ((current.getStockLocked() != null && current.getStockLocked() > 0)
                || (current.getSoldCount() != null && current.getSoldCount() > 0)) {
            throw new BusinessException("该票档存在锁定库存或已售数量，不能删除");
        }
        adminPerformanceMapper.deleteSku(skuId);
        refreshPrice(current.getProjectId(), current.getSessionId());
        return new AdminOperateResponse(true, "票档已删除");
    }

    @Transactional
    public AdminSkuItemVO updateSourceSkuPlatformPrice(Long skuId, AdminUpdatePlatformPriceRequest request) {
        AdminSkuItemVO current = requireSku(skuId);
        if (!Boolean.TRUE.equals(current.getSourceManaged())) {
            throw new BusinessException("该票档不是第三方票源票档，请使用普通票档编辑接口");
        }
        if (request == null) throw new BusinessException("请求参数不能为空");
        String mode = trimToNull(request.getPriceMode());
        if (!("FIXED".equals(mode) || "FOLLOW_PROVIDER".equals(mode))) {
            throw new BusinessException("priceMode 仅支持 FIXED 或 FOLLOW_PROVIDER");
        }
        BigDecimal targetPrice;
        if ("FIXED".equals(mode)) {
            targetPrice = request.getPlatformPrice();
            if (targetPrice == null || targetPrice.signum() <= 0) {
                throw new BusinessException("固定平台售价必须大于 0");
            }
            validateScale(targetPrice, 2, "平台售价");
        } else {
            targetPrice = adminPerformanceMapper.selectTicketSourceSalePrice(skuId);
            if (targetPrice == null || targetPrice.signum() <= 0) {
                throw new BusinessException("Provider 当前没有可用销售价，无法切换为跟随票源价");
            }
        }
        if (adminPerformanceMapper.updateSourceSkuPlatformPrice(skuId, targetPrice, mode) != 1) {
            throw new BusinessException("平台售价更新失败，请刷新后重试");
        }
        refreshPrice(current.getProjectId(), current.getSessionId());
        return adminPerformanceMapper.selectSkuDetail(skuId);
    }

    private void validateProjectReadyForPublish(Long projectId, String targetStatus) {
        int readyCount = safeCount(adminPerformanceMapper.countProjectReadySessions(projectId, targetStatus));
        if (readyCount > 0) {
            return;
        }
        if ("ON_SALE".equals(targetStatus)) {
            throw new BusinessException("项目上架为在售前，至少需要一个在售场次和有库存的在售票档");
        }
        if ("COMING_SOON".equals(targetStatus)) {
            throw new BusinessException("项目上架为即将开售前，至少需要一个预售/在售场次和有库存的预售/在售票档");
        }
        if ("SOLD_OUT".equals(targetStatus)) {
            throw new BusinessException("项目设为已售罄前，至少需要一个已配置票档的非下架场次");
        }
    }

    private void validateSessionReadyForPublish(Long sessionId, String targetStatus) {
        int readyCount = safeCount(adminPerformanceMapper.countSessionReadySkus(sessionId, targetStatus));
        if (readyCount > 0) {
            return;
        }
        if ("ON_SALE".equals(targetStatus)) {
            throw new BusinessException("场次上架为在售前，至少需要一个有库存的在售票档");
        }
        if ("PRESALE".equals(targetStatus)) {
            throw new BusinessException("场次上架为预售前，至少需要一个有库存的预售或在售票档");
        }
        throw new BusinessException("场次设为已售罄前，至少需要一个非下架票档");
    }

    private AdminProjectSaveDTO validateAndBuildSourceManagedOperation(Long projectId,
                                                                       AdminSaveProjectRequest request,
                                                                       AdminProjectDetailVO current) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (request.getTitle() != null
                || request.getCategoryId() != null
                || request.getPosterUrl() != null
                || request.getDetailContent() != null) {
            throw new BusinessException(409, "第三方项目的标题、分类、海报和简介/详情属于 Provider 权威事实数据，只能通过 Provider 重新同步更新");
        }
        if (request.getProjectStatus() != null || request.getRecommendFlag() != null) {
            throw new BusinessException("第三方项目的上下架和首页推荐请使用项目状态接口维护");
        }
        BigDecimal hotScore = request.getHotScore() == null ? current.getHotScore() : request.getHotScore();
        if (hotScore == null) {
            hotScore = BigDecimal.ZERO;
        }
        if (hotScore.compareTo(BigDecimal.ZERO) < 0 || hotScore.compareTo(MAX_HOT_SCORE) > 0) {
            throw new BusinessException("hotScore 必须在 0 到 99999999.99 之间");
        }
        validateScale(hotScore, 2, "hotScore");
        LocalDateTime publishTime = request.getPublishTime() == null ? current.getPublishTime() : request.getPublishTime();

        AdminProjectSaveDTO dto = new AdminProjectSaveDTO();
        dto.setProjectId(projectId);
        dto.setHotScore(hotScore);
        dto.setPublishTime(publishTime);
        return dto;
    }

    private AdminProjectSaveDTO validateAndBuildProject(Long projectId, AdminSaveProjectRequest request,
                                                        boolean create, AdminProjectDetailVO current) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String title = requireText(request.getTitle(), "演出标题", 200);
        Long categoryId = request.getCategoryId();
        if (categoryId == null || adminPerformanceMapper.countCategoryById(categoryId) == 0) {
            throw new BusinessException("演出分类不存在");
        }
        String posterUrl = adminMediaService.requireImageReference(
                request.getPosterUrl(), MediaBusinessType.PROJECT_POSTER, "演出海报"
        );
        String detailContent = adminRichTextService.sanitizeProjectDetail(request.getDetailContent());
        BigDecimal hotScore = request.getHotScore() == null ? BigDecimal.ZERO : request.getHotScore();
        if (hotScore.compareTo(BigDecimal.ZERO) < 0 || hotScore.compareTo(MAX_HOT_SCORE) > 0) {
            throw new BusinessException("hotScore 必须在 0 到 99999999.99 之间");
        }
        validateScale(hotScore, 2, "hotScore");
        String projectStatus = create ? "OFFLINE" : current.getProjectStatus();
        Integer recommendFlag = create ? 0 : current.getRecommendFlag();
        LocalDateTime publishTime = request.getPublishTime();
        if (!create && publishTime == null) {
            publishTime = current.getPublishTime();
        }

        AdminProjectSaveDTO dto = new AdminProjectSaveDTO();
        dto.setProjectId(projectId);
        dto.setTitle(title);
        dto.setCategoryId(categoryId);
        dto.setPosterUrl(posterUrl);
        dto.setDetailContent(detailContent);
        dto.setHotScore(hotScore);
        dto.setProjectStatus(projectStatus);
        dto.setRecommendFlag(recommendFlag);
        dto.setPublishTime(publishTime);
        return dto;
    }

    private AdminSessionSaveDTO validateAndBuildSession(Long sessionId, Long projectId,
                                                        AdminSaveSessionRequest request,
                                                        boolean create, String currentStatus) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String requestedCityName = requireText(request.getCityName(), "城市名称", 50);
        String stationName = requireText(request.getStationName(), "站点名称", 100);
        Long venueId = request.getVenueId();
        if (venueId == null || adminPerformanceMapper.countVenueById(venueId) == 0) {
            throw new BusinessException("场馆不存在");
        }
        String venueCity = adminPerformanceMapper.selectVenueCityName(venueId);
        if (!sameCity(requestedCityName, venueCity)) {
            throw new BusinessException("场次城市必须与场馆所属城市一致，请重新选择同城场馆");
        }
        String cityName = venueCity;
        LocalDateTime startTime = request.getStartTime();
        if (startTime == null) {
            throw new BusinessException("演出开始时间不能为空");
        }
        LocalDateTime endTime = request.getEndTime();
        if (endTime != null && !endTime.isAfter(startTime)) {
            throw new BusinessException("演出结束时间必须晚于开始时间");
        }
        LocalDateTime saleStartTime = request.getSaleStartTime();
        LocalDateTime saleEndTime = request.getSaleEndTime();
        if (saleStartTime != null && saleEndTime != null && !saleEndTime.isAfter(saleStartTime)) {
            throw new BusinessException("停售时间必须晚于开售时间");
        }
        if (saleStartTime != null && !saleStartTime.isBefore(startTime)) {
            throw new BusinessException("开售时间必须早于演出开始时间");
        }
        if (saleEndTime != null && saleEndTime.isAfter(startTime)) {
            throw new BusinessException("停售时间不能晚于演出开始时间");
        }
        Integer issueOffsetHours = request.getIssueOffsetHours();
        if (issueOffsetHours != null && (issueOffsetHours < 0 || issueOffsetHours > 8760)) {
            throw new BusinessException("出票提前小时数必须在 0 到 8760 之间");
        }
        String sessionStatus = create ? "OFFLINE" : currentStatus;
        if (sessionStatus == null || !SESSION_STATUS_SET.contains(sessionStatus)) {
            throw new BusinessException("场次当前状态不合法");
        }
        Integer limitPerOrder = request.getLimitPerOrder();
        if (limitPerOrder == null || limitPerOrder < 1 || limitPerOrder > 99) {
            throw new BusinessException("每单限购必须在 1 到 99 之间");
        }
        String deliveryType = trimToNull(request.getDeliveryType());
        if (deliveryType == null || !DELIVERY_TYPE_SET.contains(deliveryType)) {
            throw new BusinessException("配送方式不合法");
        }
        if (adminPerformanceMapper.countDuplicateSession(projectId, venueId, startTime, sessionId) > 0) {
            throw new BusinessException("同一项目、场馆和开始时间下已存在场次");
        }

        AdminSessionSaveDTO dto = new AdminSessionSaveDTO();
        dto.setSessionId(sessionId);
        dto.setProjectId(projectId);
        dto.setCityName(cityName);
        dto.setStationName(stationName);
        dto.setVenueId(venueId);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setSaleStartTime(saleStartTime);
        dto.setSaleEndTime(saleEndTime);
        dto.setIssueOffsetHours(issueOffsetHours);
        dto.setSessionStatus(sessionStatus);
        dto.setLimitPerOrder(limitPerOrder);
        dto.setStationDetailContent(adminRichTextService.sanitizeSessionDetail(request.getStationDetailContent()));
        dto.setDeliveryType(deliveryType);
        return dto;
    }

    private AdminSkuSaveDTO validateAndBuildCreateSku(Long projectId, Long sessionId,
                                                      AdminCreateSkuRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String skuName = requireText(request.getSkuName(), "票档名称", 100);
        String skuDesc = trimToNull(request.getSkuDesc());
        if (skuDesc != null && skuDesc.length() > 200) {
            throw new BusinessException("票档描述长度不能超过 200");
        }
        BigDecimal price = validatePrice(request.getPrice());
        Integer stockAvailable = request.getStockAvailable();
        if (stockAvailable == null || stockAvailable < 0) {
            throw new BusinessException("可售库存不能为空且不能小于 0");
        }
        String skuStatus = trimToNull(request.getSkuStatus());
        if (skuStatus == null || !SKU_STATUS_SET.contains(skuStatus)) {
            throw new BusinessException("票档状态不合法");
        }
        validateStockAndStatus(stockAvailable, skuStatus);
        Integer sortOrder = request.getSortOrder() == null ? 0 : request.getSortOrder();
        if (sortOrder < 0) {
            throw new BusinessException("sortOrder 不能小于 0");
        }
        if (adminPerformanceMapper.countDuplicateSkuName(sessionId, skuName, null) > 0) {
            throw new BusinessException("当前场次已存在同名票档");
        }

        AdminSkuSaveDTO dto = new AdminSkuSaveDTO();
        dto.setProjectId(projectId);
        dto.setSessionId(sessionId);
        dto.setSkuName(skuName);
        dto.setSkuDesc(skuDesc);
        dto.setPrice(price);
        dto.setStockAvailable(stockAvailable);
        dto.setSkuStatus(skuStatus);
        dto.setSortOrder(sortOrder);
        return dto;
    }

    private AdminSkuSaveDTO validateAndBuildUpdateSku(AdminSkuItemVO current,
                                                      AdminUpdateSkuRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String skuName = requireText(request.getSkuName(), "票档名称", 100);
        String skuDesc = trimToNull(request.getSkuDesc());
        if (skuDesc != null && skuDesc.length() > 200) {
            throw new BusinessException("票档描述长度不能超过 200");
        }
        BigDecimal price = validatePrice(request.getPrice());
        String skuStatus = trimToNull(request.getSkuStatus());
        if (skuStatus == null || !SKU_STATUS_SET.contains(skuStatus)) {
            throw new BusinessException("票档状态不合法");
        }
        validateStockAndStatus(current.getStockAvailable(), skuStatus);
        Integer sortOrder = request.getSortOrder() == null ? 0 : request.getSortOrder();
        if (sortOrder < 0) {
            throw new BusinessException("sortOrder 不能小于 0");
        }
        if (adminPerformanceMapper.countDuplicateSkuName(
                current.getSessionId(), skuName, current.getSkuId()) > 0) {
            throw new BusinessException("当前场次已存在同名票档");
        }

        AdminSkuSaveDTO dto = new AdminSkuSaveDTO();
        dto.setSkuId(current.getSkuId());
        dto.setProjectId(current.getProjectId());
        dto.setSessionId(current.getSessionId());
        dto.setSkuName(skuName);
        dto.setSkuDesc(skuDesc);
        dto.setPrice(price);
        dto.setSkuStatus(skuStatus);
        dto.setSortOrder(sortOrder);
        return dto;
    }

    private void validateStockAndStatus(Integer stockAvailable, String skuStatus) {
        if (("ON_SALE".equals(skuStatus) || "PRESALE".equals(skuStatus))
                && (stockAvailable == null || stockAvailable <= 0)) {
            throw new BusinessException("可售库存为 0 时，票档状态只能为 SOLD_OUT 或 OFFLINE");
        }
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(MAX_PRICE) > 0) {
            throw new BusinessException("票价必须大于 0 且不超过 99999999.99");
        }
        validateScale(price, 2, "票价");
        return price;
    }

    private void validateScale(BigDecimal value, int maxScale, String fieldName) {
        if (value != null && Math.max(value.stripTrailingZeros().scale(), 0) > maxScale) {
            throw new BusinessException(fieldName + " 最多保留 " + maxScale + " 位小数");
        }
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }

    private void refreshPrice(Long projectId, Long sessionId) {
        adminPerformanceMapper.refreshSessionPrice(sessionId);
        adminPerformanceMapper.refreshProjectPrice(projectId);
    }

    private AdminProjectDetailVO loadProjectDetail(Long projectId) {
        AdminProjectDetailVO vo = adminPerformanceMapper.selectProjectDetail(projectId);
        if (vo == null) {
            throw new BusinessException("演出项目不存在");
        }
        vo.setServiceTags(adminPerformanceMapper.selectEffectiveProjectServiceTagNames(projectId));
        vo.setNoticeTitles(adminPerformanceMapper.selectEffectiveProjectNoticeTitles(projectId));
        return vo;
    }

    private void requireLocalManagedProject(Long projectId) {
        if (safeCount(adminPerformanceMapper.countTicketSourceProjectMapping(projectId)) > 0) {
            throw new BusinessException("第三方票源项目由资源同步和自动上架策略维护，不能通过本地演出接口直接修改");
        }
    }

    private void requireLocalManagedSession(Long sessionId) {
        if (safeCount(adminPerformanceMapper.countTicketSourceSessionMapping(sessionId)) > 0) {
            throw new BusinessException("第三方票源场次由资源同步维护，不能通过本地演出接口直接修改");
        }
    }

    private void requireLocalManagedSku(Long skuId) {
        if (safeCount(adminPerformanceMapper.countTicketSourceSkuMapping(skuId)) > 0) {
            throw new BusinessException("第三方票源票档及库存由票源同步维护，不能通过本地演出接口直接修改");
        }
    }

    private void requireProject(Long projectId) {
        if (projectId == null) {
            throw new BusinessException("projectId 不能为空");
        }
        if (adminPerformanceMapper.countProjectById(projectId) == 0) {
            throw new BusinessException("演出项目不存在");
        }
    }

    private void requireSession(Long sessionId) {
        if (sessionId == null) {
            throw new BusinessException("sessionId 不能为空");
        }
        if (adminPerformanceMapper.countSessionById(sessionId) == 0) {
            throw new BusinessException("场次不存在");
        }
    }

    private AdminSkuItemVO requireSku(Long skuId) {
        if (skuId == null) {
            throw new BusinessException("skuId 不能为空");
        }
        AdminSkuItemVO vo = adminPerformanceMapper.selectSkuDetail(skuId);
        if (vo == null) {
            throw new BusinessException("票档不存在");
        }
        return vo;
    }

    private String requireText(String value, String fieldName, int maxLength) {
        String text = trimToNull(value);
        if (text == null) {
            throw new BusinessException(fieldName + "不能为空");
        }
        if (text.length() > maxLength) {
            throw new BusinessException(fieldName + "长度不能超过 " + maxLength);
        }
        return text;
    }

    private void validateFlag(Integer value, String fieldName) {
        if (value != null && value != 0 && value != 1) {
            throw new BusinessException(fieldName + " 只能为 0 或 1");
        }
    }

    private boolean sameCity(String left, String right) {
        String normalizedLeft = normalizeCity(left);
        String normalizedRight = normalizeCity(right);
        return normalizedLeft != null && normalizedLeft.equals(normalizedRight);
    }

    private String normalizeCity(String value) {
        String text = trimToNull(value);
        if (text != null && text.length() > 1 && text.endsWith("市")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
