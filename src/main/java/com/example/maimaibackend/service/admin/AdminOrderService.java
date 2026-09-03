package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.order.OrderItemReleaseDTO;
import com.example.maimaibackend.mapper.admin.AdminOrderMapper;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminOrderDetailVO;
import com.example.maimaibackend.vo.admin.AdminOrderItemVO;
import com.example.maimaibackend.vo.admin.AdminOrderListPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminOrderService {

    private static final Set<String> ORDER_STATUS_SET = new HashSet<>(Arrays.asList(
            "WAIT_PAY", "WAIT_USE", "FINISHED", "REFUNDING", "REFUND_SUCCESS", "CANCELED"
    ));

    private final AdminOrderMapper adminOrderMapper;

    public AdminOrderService(AdminOrderMapper adminOrderMapper) {
        this.adminOrderMapper = adminOrderMapper;
    }

    public AdminOrderListPageVO getOrderList(String keyword, Long userId, Long projectId, String orderStatus,
                                             String dateFrom, String dateTo, Integer pageNo, Integer pageSize) {
        validateOptionalId(userId, "userId");
        validateOptionalId(projectId, "projectId");
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        String safeKeyword = trimToNull(keyword);
        String safeStatus = trimToNull(orderStatus);
        if (safeStatus != null && !ORDER_STATUS_SET.contains(safeStatus)) {
            throw new BusinessException("订单状态不合法");
        }
        DateRange range = normalizeDateRange(dateFrom, dateTo);
        int offset = (safePageNo - 1) * safePageSize;
        Integer total = adminOrderMapper.countOrderList(safeKeyword, userId, projectId, safeStatus,
                range.dateFrom, range.dateTo);
        List<AdminOrderItemVO> items = total == null || total == 0
                ? Collections.emptyList()
                : adminOrderMapper.selectOrderList(safeKeyword, userId, projectId, safeStatus,
                range.dateFrom, range.dateTo, safePageSize, offset);
        AdminOrderListPageVO vo = new AdminOrderListPageVO();
        vo.setTotal(total == null ? 0 : total);
        vo.setPageNo(safePageNo);
        vo.setPageSize(safePageSize);
        vo.setItems(items);
        return vo;
    }

    public AdminOrderDetailVO getOrderDetail(Long orderId) {
        validateId(orderId, "orderId");
        AdminOrderDetailVO vo = adminOrderMapper.selectOrderDetail(orderId);
        if (vo == null) {
            throw new BusinessException("订单不存在");
        }
        vo.setItems(adminOrderMapper.selectOrderItems(orderId));
        vo.setAudiences(adminOrderMapper.selectOrderAudiences(orderId));
        vo.setAddress(adminOrderMapper.selectOrderAddress(orderId));
        vo.setTickets(adminOrderMapper.selectOrderTickets(orderId));
        vo.setRefunds(adminOrderMapper.selectOrderRefunds(orderId));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperateResponse cancelOrder(Long orderId) {
        validateId(orderId, "orderId");
        String currentStatus = adminOrderMapper.selectOrderStatusForUpdate(orderId);
        if (currentStatus == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"WAIT_PAY".equals(currentStatus)) {
            throw new BusinessException("当前订单状态不可取消");
        }
        List<OrderItemReleaseDTO> items = adminOrderMapper.selectOrderItemsForRelease(orderId);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单缺少票档明细，不能取消");
        }
        for (OrderItemReleaseDTO item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("订单票档明细异常，不能取消");
            }
            int released = adminOrderMapper.releaseSkuLockedStock(item.getSkuId(), item.getQuantity());
            if (released != 1) {
                throw new BusinessException("票档锁定库存已变化，取消订单失败");
            }
        }
        int updated = adminOrderMapper.updateOrderCanceled(orderId);
        if (updated != 1) {
            throw new BusinessException("订单状态已变化，请刷新后重试");
        }
        return new AdminOperateResponse(true, "订单已取消，锁定库存已释放");
    }

    private DateRange normalizeDateRange(String dateFrom, String dateTo) {
        String safeFrom = normalizeDate(dateFrom, "dateFrom");
        String safeTo = normalizeDate(dateTo, "dateTo");
        if (safeFrom != null && safeTo != null && LocalDate.parse(safeFrom).isAfter(LocalDate.parse(safeTo))) {
            throw new BusinessException("dateFrom 不能晚于 dateTo");
        }
        return new DateRange(safeFrom, safeTo);
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

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }

    private void validateOptionalId(Long id, String name) {
        if (id != null && id <= 0) {
            throw new BusinessException(name + " 必须为正整数");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class DateRange {
        private final String dateFrom;
        private final String dateTo;

        private DateRange(String dateFrom, String dateTo) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }
    }
}
