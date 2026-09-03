package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.admin.TicketOperationContext;
import com.example.maimaibackend.dto.admin.TicketOperationLogDTO;
import com.example.maimaibackend.mapper.admin.AdminTicketLogMapper;
import com.example.maimaibackend.vo.admin.AdminTicketLogItemVO;
import com.example.maimaibackend.vo.admin.AdminTicketLogPageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdminTicketLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminTicketLogService.class);
    private static final Set<String> BUSINESS_TYPES = new HashSet<>(Arrays.asList("TICKET", "ISSUE", "CHECK", "QR", "REFUND"));
    private static final Set<String> OPERATOR_TYPES = new HashSet<>(Arrays.asList("SYSTEM", "ADMIN"));
    private static final Set<String> RESULT_STATUSES = new HashSet<>(Arrays.asList("SUCCESS", "FAILED"));
    private final AdminTicketLogMapper adminTicketLogMapper;

    public AdminTicketLogService(AdminTicketLogMapper adminTicketLogMapper) {
        this.adminTicketLogMapper = adminTicketLogMapper;
    }

    public void recordSuccess(TicketOperationContext context, String businessType, String actionType,
                              String targetType, Long targetId, Long orderId, Long ticketId,
                              String beforeStatus, String afterStatus, String description, String detailJson) {
        TicketOperationLogDTO log = buildLog(context, businessType, actionType, targetType, targetId,
                orderId, ticketId, beforeStatus, afterStatus, "SUCCESS", description, detailJson);
        if (adminTicketLogMapper.insertLog(log) != 1) {
            throw new BusinessException("票务日志写入失败");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailed(TicketOperationContext context, String businessType, String actionType,
                             String targetType, Long targetId, Long orderId, Long ticketId,
                             String beforeStatus, String afterStatus, String description, String detailJson) {
        try {
            TicketOperationLogDTO log = buildLog(context, businessType, actionType, targetType, targetId,
                    orderId, ticketId, beforeStatus, afterStatus, "FAILED", description, detailJson);
            if (adminTicketLogMapper.insertLog(log) != 1) {
                LOGGER.error("Failed ticket operation log was not inserted: businessType={}, actionType={}, targetId={}",
                        businessType, actionType, targetId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to persist ticket operation failure log: businessType={}, actionType={}, targetId={}",
                    businessType, actionType, targetId, e);
        }
    }

    public AdminTicketLogPageVO getLogs(String keyword, String businessType, String operatorType,
                                        String actionType, String resultStatus, Long ticketId, Long orderId,
                                        Long operatorId, String dateFrom, String dateTo,
                                        Integer pageNo, Integer pageSize) {
        validateOptionalId(ticketId, "ticketId");
        validateOptionalId(orderId, "orderId");
        validateOptionalId(operatorId, "operatorId");
        String safeBusinessType = normalizeEnum(businessType, BUSINESS_TYPES, "businessType");
        String safeOperatorType = normalizeEnum(operatorType, OPERATOR_TYPES, "operatorType");
        String safeResultStatus = normalizeEnum(resultStatus, RESULT_STATUSES, "resultStatus");
        String safeActionType = trimToNull(actionType);
        if (safeActionType != null && safeActionType.length() > 64) {
            throw new BusinessException("actionType 长度不能超过 64");
        }
        String safeFrom = normalizeDate(dateFrom, "dateFrom");
        String safeTo = normalizeDate(dateTo, "dateTo");
        if (safeFrom != null && safeTo != null && LocalDate.parse(safeFrom).isAfter(LocalDate.parse(safeTo))) {
            throw new BusinessException("dateFrom 不能晚于 dateTo");
        }
        int safePageNo = pageNo == null ? 1 : pageNo;
        int safePageSize = pageSize == null ? 20 : pageSize;
        if (safePageNo < 1) {
            throw new BusinessException("pageNo 必须大于 0");
        }
        if (safePageSize < 1 || safePageSize > 100) {
            throw new BusinessException("pageSize 必须在 1 到 100 之间");
        }
        int offset = (safePageNo - 1) * safePageSize;
        String safeKeyword = trimToNull(keyword);
        Integer total = adminTicketLogMapper.countLogs(safeKeyword, safeBusinessType, safeOperatorType,
                safeActionType, safeResultStatus, ticketId, orderId, operatorId, safeFrom, safeTo);
        List<AdminTicketLogItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminTicketLogMapper.selectLogs(safeKeyword, safeBusinessType, safeOperatorType,
                safeActionType, safeResultStatus, ticketId, orderId, operatorId, safeFrom, safeTo,
                safePageSize, offset);
        AdminTicketLogPageVO page = new AdminTicketLogPageVO();
        page.setTotal(total == null ? 0 : total);
        page.setPageNo(safePageNo);
        page.setPageSize(safePageSize);
        page.setItems(items);
        return page;
    }

    public AdminTicketLogItemVO getLogDetail(Long logId) {
        if (logId == null || logId <= 0) {
            throw new BusinessException("logId 必须为正整数");
        }
        AdminTicketLogItemVO detail = adminTicketLogMapper.selectLogDetail(logId);
        if (detail == null) {
            throw new BusinessException("票务日志不存在");
        }
        return detail;
    }

    private TicketOperationLogDTO buildLog(TicketOperationContext context, String businessType, String actionType,
                                            String targetType, Long targetId, Long orderId, Long ticketId,
                                            String beforeStatus, String afterStatus, String resultStatus,
                                            String description, String detailJson) {
        TicketOperationContext realContext = context == null ? TicketOperationContext.system("SYSTEM") : context;
        TicketOperationLogDTO log = new TicketOperationLogDTO();
        log.setLogNo(generateLogNo());
        log.setBusinessType(businessType);
        log.setActionType(actionType);
        log.setOperatorType(realContext.getOperatorType());
        log.setOperatorId(realContext.getOperatorId());
        log.setOperatorName(limit(realContext.getOperatorName(), 100));
        log.setSourceIp(limit(realContext.getSourceIp(), 64));
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOrderId(orderId);
        log.setTicketId(ticketId);
        log.setBeforeStatus(limit(beforeStatus, 64));
        log.setAfterStatus(limit(afterStatus, 64));
        log.setResultStatus(resultStatus);
        String safeDescription = limit(description, 500);
        log.setActionDescription(safeDescription == null ? "未提供操作说明" : safeDescription);
        log.setDetailJson(detailJson);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }

    private String generateLogNo() {
        return "TL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private void validateOptionalId(Long id, String name) {
        if (id != null && id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }

    private String normalizeEnum(String value, Set<String> allowed, String name) {
        String safe = trimToNull(value);
        if (safe == null) {
            return null;
        }
        safe = safe.toUpperCase();
        if (!allowed.contains(safe)) {
            throw new BusinessException(name + " 不合法");
        }
        return safe;
    }

    private String normalizeDate(String value, String name) {
        String safe = trimToNull(value);
        if (safe == null) {
            return null;
        }
        try {
            return LocalDate.parse(safe).toString();
        } catch (DateTimeParseException e) {
            throw new BusinessException(name + " 格式必须为 yyyy-MM-dd");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String limit(String value, int max) {
        String safe = trimToNull(value);
        if (safe == null || safe.length() <= max) {
            return safe;
        }
        return safe.substring(0, max);
    }
}
