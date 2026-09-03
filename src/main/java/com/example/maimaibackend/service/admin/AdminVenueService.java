package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.AdminSaveVenueRequest;
import com.example.maimaibackend.dto.admin.AdminVenueSaveDTO;
import com.example.maimaibackend.mapper.admin.AdminVenueMapper;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminVenueListPageVO;
import com.example.maimaibackend.vo.admin.AdminVenueOptionListVO;
import com.example.maimaibackend.vo.admin.AdminVenueVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class AdminVenueService {

    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");

    private final AdminVenueMapper adminVenueMapper;

    public AdminVenueService(AdminVenueMapper adminVenueMapper) {
        this.adminVenueMapper = adminVenueMapper;
    }

    public AdminVenueListPageVO getVenueList(String keyword, String cityName,
                                              Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeCityName = trimToNull(cityName);
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminVenueMapper.countVenueList(safeKeyword, safeCityName);
        List<AdminVenueVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminVenueMapper.selectVenueList(
                        safeKeyword, safeCityName, safePageSize, offset
                );

        AdminVenueListPageVO vo = new AdminVenueListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminVenueOptionListVO getVenueOptions(String cityName, String stationName,
                                                   Integer limit) {
        String safeCityName = trimToNull(cityName);
        String safeStationName = trimToNull(stationName);
        if (safeCityName == null && safeStationName == null) {
            throw new BusinessException("请先填写城市名称或站点名称");
        }

        boolean inferredFromStation = false;
        String resolvedCityName;
        if (safeCityName != null) {
            resolvedCityName = adminVenueMapper.selectCanonicalCityName(safeCityName);
            if (safeStationName != null) {
                String stationCity = adminVenueMapper.selectMatchedCityNameByStationName(safeStationName);
                if (stationCity != null && !sameCity(safeCityName, stationCity)) {
                    throw new BusinessException("站点名称与城市名称不一致，请检查后重新查询场馆");
                }
            }
        } else {
            resolvedCityName = adminVenueMapper.selectMatchedCityNameByStationName(safeStationName);
            inferredFromStation = true;
        }

        int safeLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 100);
        List<AdminVenueVO> items = resolvedCityName == null
                ? Collections.emptyList()
                : adminVenueMapper.selectVenueOptions(resolvedCityName, safeLimit);

        AdminVenueOptionListVO vo = new AdminVenueOptionListVO();
        vo.setResolvedCityName(resolvedCityName);
        vo.setInferredFromStation(inferredFromStation);
        vo.setTotal(items.size());
        vo.setItems(items);
        return vo;
    }

    public AdminVenueVO getVenueDetail(Long venueId) {
        return requireVenue(venueId);
    }

    @Transactional
    public AdminVenueVO createVenue(AdminSaveVenueRequest request) {
        AdminVenueSaveDTO dto = validateAndBuild(null, request);
        adminVenueMapper.insertVenue(dto);
        return requireVenue(dto.getVenueId());
    }

    @Transactional
    public AdminVenueVO updateVenue(Long venueId, AdminSaveVenueRequest request) {
        AdminVenueVO current = requireVenue(venueId);
        AdminVenueSaveDTO dto = validateAndBuild(venueId, request);
        if (current.getSessionCount() != null && current.getSessionCount() > 0
                && !sameCity(current.getCityName(), dto.getCityName())) {
            throw new BusinessException("该场馆已关联场次，不能修改所属城市");
        }
        adminVenueMapper.updateVenue(dto);
        return requireVenue(venueId);
    }

    @Transactional
    public AdminOperateResponse deleteVenue(Long venueId) {
        requireVenue(venueId);
        if (adminVenueMapper.countSessionsByVenueId(venueId) > 0) {
            throw new BusinessException("该场馆已关联演出场次，不能删除");
        }
        adminVenueMapper.deleteVenue(venueId);
        return new AdminOperateResponse(true, "场馆已删除");
    }

    private AdminVenueSaveDTO validateAndBuild(Long venueId, AdminSaveVenueRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String venueName = requireText(request.getVenueName(), "场馆名称", 100);
        String cityName = requireText(request.getCityName(), "所属城市", 50);
        String address = requireText(request.getAddress(), "场馆地址", 255);
        BigDecimal longitude = request.getLongitude();
        BigDecimal latitude = request.getLatitude();
        if (longitude != null && (longitude.compareTo(MIN_LONGITUDE) < 0
                || longitude.compareTo(MAX_LONGITUDE) > 0)) {
            throw new BusinessException("经度必须在 -180 到 180 之间");
        }
        if (latitude != null && (latitude.compareTo(MIN_LATITUDE) < 0
                || latitude.compareTo(MAX_LATITUDE) > 0)) {
            throw new BusinessException("纬度必须在 -90 到 90 之间");
        }
        validateScale(longitude, 7, "经度");
        validateScale(latitude, 7, "纬度");
        if (adminVenueMapper.countDuplicateVenue(
                cityName, venueName, address, venueId) > 0) {
            throw new BusinessException("同一城市下已存在相同名称和地址的场馆");
        }

        AdminVenueSaveDTO dto = new AdminVenueSaveDTO();
        dto.setVenueId(venueId);
        dto.setVenueName(venueName);
        dto.setCityName(cityName);
        dto.setAddress(address);
        dto.setLongitude(longitude);
        dto.setLatitude(latitude);
        return dto;
    }

    private AdminVenueVO requireVenue(Long venueId) {
        if (venueId == null) {
            throw new BusinessException("venueId 不能为空");
        }
        AdminVenueVO vo = adminVenueMapper.selectVenueDetail(venueId);
        if (vo == null) {
            throw new BusinessException("场馆不存在");
        }
        return vo;
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

    private void validateScale(BigDecimal value, int maxScale, String fieldName) {
        if (value != null && Math.max(value.stripTrailingZeros().scale(), 0) > maxScale) {
            throw new BusinessException(fieldName + " 最多保留 " + maxScale + " 位小数");
        }
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
