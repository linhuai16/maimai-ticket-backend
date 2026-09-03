package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.OrderQueryMapper;
import com.example.maimaibackend.vo.order.OrderDetailVO;
import com.example.maimaibackend.vo.order.OrderListItemVO;
import com.example.maimaibackend.vo.order.OrderListPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderQueryService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final String TAB_ALL = "ALL";
    private static final String TAB_WAIT_PAY = "WAIT_PAY";
    private static final String TAB_WAIT_USE = "WAIT_USE";
    private static final String TAB_FINISHED = "FINISHED";

    private final OrderQueryMapper orderQueryMapper;

    public OrderQueryService(OrderQueryMapper orderQueryMapper) {
        this.orderQueryMapper = orderQueryMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderListPageVO getOrderList(Long userId, String tab, Integer pageNo, Integer pageSize) {
        validateId(userId, "userId");
        String realTab = normalizeTab(tab);
        int realPageNo = normalizePageNo(pageNo);
        int realPageSize = normalizePageSize(pageSize);
        int offset = (realPageNo - 1) * realPageSize;


        int total = orderQueryMapper.countOrders(userId, realTab);
        List<OrderListItemVO> orders = orderQueryMapper.selectOrderList(userId, realTab, realPageSize, offset);

        OrderListPageVO vo = new OrderListPageVO();
        vo.setUserId(userId);
        vo.setTab(realTab);
        vo.setPageNo(realPageNo);
        vo.setPageSize(realPageSize);
        vo.setTotal(total);
        vo.setHasMore(offset + orders.size() < total);
        vo.setOrders(orders);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        validateId(userId, "userId");
        validateId(orderId, "orderId");


        OrderDetailVO detail = orderQueryMapper.selectOrderDetailBase(orderId, userId);
        if (detail == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }
        detail.setItems(orderQueryMapper.selectOrderDetailItems(orderId));
        detail.setAudiences(orderQueryMapper.selectOrderAudiences(orderId));
        detail.setAddress(orderQueryMapper.selectOrderAddress(orderId));
        detail.setTickets(orderQueryMapper.selectOrderTickets(orderId));
        detail.setRefundRecord(orderQueryMapper.selectOrderRefundRecord(orderId));
        return detail;
    }

    /**
     * 用户订单查询必须保持纯读取。V1.3 第三方订单超时释放由
     * TicketSourceOrderExpireScheduler / V11 订单取消链路处理，
     * 不能在列表/详情查询时直接修改本地 stock_locked。
     */

    private String normalizeTab(String tab) {
        if (tab == null || tab.trim().isEmpty()) {
            return TAB_ALL;
        }
        String upperTab = tab.trim().toUpperCase();
        if (TAB_ALL.equals(upperTab) || TAB_WAIT_PAY.equals(upperTab)
                || TAB_WAIT_USE.equals(upperTab) || TAB_FINISHED.equals(upperTab)) {
            return upperTab;
        }
        throw new BusinessException("订单列表 tab 参数无效");
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException("请求参数无效：" + name);
        }
    }
}
