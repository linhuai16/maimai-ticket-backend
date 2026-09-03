package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.PerformanceMapper;
import com.example.maimaibackend.ticketsource.purchase.options.V12PurchaseOptionService;

import com.example.maimaibackend.vo.performance.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceService {
    private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);

    private final PerformanceMapper performanceMapper;
    private final V12PurchaseOptionService purchaseOptionService;

    public PerformanceService(PerformanceMapper performanceMapper, V12PurchaseOptionService purchaseOptionService) {
        this.performanceMapper = performanceMapper;
        this.purchaseOptionService = purchaseOptionService;
    }

    public CategoryPageVO getCategoryPerformanceList(String cityName, Long categoryId, String sortType,
                                                     String filterTime, Integer limit, Integer offset) {
        String realCityName = normalizeCity(cityName);
        int realLimit = normalizeLimit(limit);
        int realOffset = normalizeOffset(offset);
        String realSortType = normalizeSortType(sortType);
        String realFilterTime = normalizeFilterTime(filterTime);

        List<PerformanceCardVO> rows = performanceMapper.selectCategoryPerformanceList(
                realCityName, categoryId, realSortType, realFilterTime, realLimit, realOffset
        );
        Integer total = performanceMapper.countCategoryPerformanceList(realCityName, categoryId, realFilterTime);

        CategoryPageVO vo = new CategoryPageVO();
        vo.setPerformances(rows);
        vo.setTotal(total == null ? 0 : total);
        vo.setLimit(realLimit);
        vo.setOffset(realOffset);
        log.info("[CategoryQuery] city={} category={} result={}", realCityName, categoryId, rows.size());
        return vo;
    }

    public SearchResultPageVO searchPerformances(String cityName, String keyword, Integer limit, Integer offset) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException("搜索关键词不能为空");
        }
        String realKeyword = keyword.trim();
        String realCityName = normalizeCity(cityName);
        int realLimit = normalizeLimit(limit);
        int realOffset = normalizeOffset(offset);

        List<PerformanceCardVO> rows = performanceMapper.selectSearchPerformanceList(
                realCityName, realKeyword, realLimit, realOffset);
        Integer total = performanceMapper.countSearchPerformanceList(realCityName, realKeyword);

        SearchResultPageVO vo = new SearchResultPageVO();
        vo.setKeyword(realKeyword);
        vo.setPerformances(rows);
        vo.setTotal(total == null ? 0 : total);
        vo.setLimit(realLimit);
        vo.setOffset(realOffset);
        return vo;
    }

    public List<VenueVO> getVenueLookupList() {
        return performanceMapper.selectVenueLookupList();
    }

    public PerformanceDetailVO getPerformanceDetail(Long projectId, String cityName, Long sessionId, Long userId) {
        if (projectId == null) {
            throw new BusinessException("演出项目ID不能为空");
        }
        ProjectDetailVO project = performanceMapper.selectProjectDetail(projectId, userId);
        if (project == null) {
            throw new BusinessException("演出项目不存在或不可展示");
        }

        List<SessionItemVO> sessions = performanceMapper.selectSessionList(projectId);
        if (sessions == null || sessions.isEmpty()) {
            throw new BusinessException("当前演出暂无可展示场次");
        }

        SessionItemVO selectedSession = selectDefaultSession(sessions, sessionId, normalizeCity(cityName));
        if (selectedSession == null) {
            throw new BusinessException("未找到可展示场次");
        }

        PerformanceDetailVO vo = new PerformanceDetailVO();
        vo.setProject(project);
        vo.setSessions(sessions);
        vo.setSelectedSession(selectedSession);
        vo.setVenue(toVenueVO(selectedSession));
        vo.setServiceTags(loadServiceTags(projectId));
        vo.setNoticeItems(loadNoticeItems(projectId, selectedSession.getLimitPerOrder()));
        return vo;
    }

    public TicketSelectPageVO getTicketSelect(Long projectId, Long sessionId) {
        if (projectId == null || sessionId == null) {
            throw new BusinessException("演出项目ID和场次ID不能为空");
        }
        ProjectDetailVO project = performanceMapper.selectProjectDetail(projectId, null);
        if (project == null) {
            throw new BusinessException("演出项目不存在或不可展示");
        }

        SessionItemVO session = performanceMapper.selectSessionById(projectId, sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在或不属于当前项目");
        }
        if (!"ON_SALE".equals(session.getSessionStatus()) && !"PRESALE".equals(session.getSessionStatus())) {
            throw new BusinessException("当前场次不可购买");
        }

        List<TicketSkuVO> skus = performanceMapper.selectTicketSkus(sessionId);
        refreshTicketSelectInventories(projectId, sessionId, skus);
        // Provider 库存刷新可能改变 stock_available / sku_status，必须重新查询后再返回鸿蒙。
        skus = performanceMapper.selectTicketSkus(sessionId);
        sanitizeUserFacingSkus(skus);

        TicketSelectPageVO vo = new TicketSelectPageVO();
        vo.setProject(project);
        vo.setSession(session);
        vo.setVenue(toVenueVO(session));
        vo.setTicketSkus(skus);
        return vo;
    }

    private void refreshTicketSelectInventories(Long projectId, Long sessionId, List<TicketSkuVO> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (TicketSkuVO sku : skus) {
            if (sku == null || sku.getSkuId() == null) {
                continue;
            }
            try {
                purchaseOptionService.refreshInventoryForDisplay(projectId, sessionId, sku.getSkuId());
            } catch (RuntimeException ignored) {
                // 票档选择页属于读页面：Provider 短暂不可用时沿用最后一次本地快照，
                // 真正提交订单仍会再次实时校验，不能因为刷新失败把整页打成 500。
            }
        }
    }

    private SessionItemVO selectDefaultSession(List<SessionItemVO> sessions, Long sessionId, String cityName) {
        if (sessionId != null) {
            for (SessionItemVO session : sessions) {
                if (sessionId.equals(session.getSessionId())) {
                    return session;
                }
            }
            // Provider 状态刚刷新时，客户端可能仍携带上一次的 sessionId。
            // 详情页是读取接口，旧 sessionId 不应导致整页 500；继续按城市/首场可展示场次兜底。
        }
        for (SessionItemVO session : sessions) {
            if (cityName.equals(session.getCityName())) {
                return session;
            }
        }
        return sessions.get(0);
    }

    private List<ServiceTagVO> loadServiceTags(Long projectId) {
        List<ServiceTagVO> manualTags = performanceMapper.selectManualServiceTags(projectId);
        List<ServiceTagVO> refundTags = performanceMapper.selectRefundServiceTags(projectId);
        List<String> refundDetailItems = performanceMapper.selectRefundServiceTagDetailItems(projectId);
        Map<Long, ServiceTagVO> map = new LinkedHashMap<>();
        if (refundTags != null) {
            for (ServiceTagVO tag : refundTags) {
                if (tag == null || tag.getTagId() == null) continue;
                tag.setDetailItems(refundDetailItems == null ? List.of() : refundDetailItems);
                map.putIfAbsent(tag.getTagId(), tag);
            }
        }
        if (manualTags != null) {
            for (ServiceTagVO tag : manualTags) {
                if (tag == null || tag.getTagId() == null) continue;
                map.putIfAbsent(tag.getTagId(), tag);
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<NoticeItemVO> loadNoticeItems(Long projectId, Integer limitPerOrder) {
        List<NoticeItemVO> projectNotices = performanceMapper.selectProjectNotices(projectId);
        if (projectNotices == null || projectNotices.isEmpty()) return List.of();
        if (limitPerOrder != null && limitPerOrder > 0) {
            for (NoticeItemVO notice : projectNotices) {
                if (notice != null && "LIMIT".equals(notice.getNoticeType())) {
                    notice.setDescription("立即购买每单最多" + limitPerOrder + "张，具体以提交订单页展示为准。");
                }
            }
        }
        return projectNotices;
    }

    private VenueVO toVenueVO(SessionItemVO session) {
        VenueVO venue = new VenueVO();
        venue.setVenueId(session.getVenueId());
        venue.setVenueName(session.getVenueName());
        venue.setCityName(session.getCityName());
        venue.setAddress(session.getVenueAddress());
        venue.setLongitude(session.getLongitude());
        venue.setLatitude(session.getLatitude());
        return venue;
    }


    private void sanitizeUserFacingSkus(List<TicketSkuVO> skus) {
        if (skus == null) return;
        for (TicketSkuVO sku : skus) {
            if (sku == null) continue;
            sku.setSkuDesc(sanitizeUserFacingText(sku.getSkuDesc()));
        }
    }

    private String sanitizeUserFacingText(String value) {
        if (value == null) return null;
        String text = value.trim();
        if (text.isEmpty()) return null;
        String upper = text.toUpperCase();
        if (text.contains("第三方票源")
                || text.contains("第三方模拟")
                || text.contains("本地模拟")
                || text.contains("票源模拟")
                || upper.contains("MOCK-SKU")
                || upper.contains("MOCK-PROJ")
                || upper.contains("PROVIDER")
                || upper.contains("V1.1")
                || upper.contains("V1.2")
                || upper.contains("V1.3")) {
            return null;
        }
        return text;
    }

    private String normalizeCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return "北京";
        }
        return cityName.trim();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private int normalizeOffset(Integer offset) {
        return offset == null || offset < 0 ? 0 : offset;
    }

    private String normalizeSortType(String sortType) {
        if (sortType == null || sortType.trim().isEmpty()) {
            return "HOT";
        }
        String value = sortType.trim().toUpperCase();
        if (!"HOT".equals(value) && !"NEW".equals(value)) {
            return "HOT";
        }
        return value;
    }

    private String normalizeFilterTime(String filterTime) {
        if (filterTime == null || filterTime.trim().isEmpty()) {
            return null;
        }
        return filterTime.trim().toUpperCase();
    }
}
