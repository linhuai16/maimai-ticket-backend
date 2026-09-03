(function () {
    'use strict';

    const page = document.getElementById('orderManagementPage');
    if (!page) return;

    const elements = {
        form: document.getElementById('orderFilterForm'),
        keyword: document.getElementById('orderKeyword'),
        status: document.getElementById('orderStatusFilter'),
        userId: document.getElementById('orderUserId'),
        projectId: document.getElementById('orderProjectId'),
        dateFrom: document.getElementById('orderDateFrom'),
        dateTo: document.getElementById('orderDateTo'),
        reset: document.getElementById('resetOrderFilterButton'),
        refresh: document.getElementById('refreshOrderButton'),
        body: document.getElementById('orderTableBody'),
        total: document.getElementById('orderTotalText'),
        summary: document.getElementById('orderSummaryChips'),
        pageSize: document.getElementById('orderPageSizeSelect'),
        pagination: document.getElementById('orderPagination'),
        detailModal: document.getElementById('orderDetailModal'),
        detailContent: document.getElementById('orderDetailContent'),
        detailClose: document.getElementById('orderDetailClose'),
        detailCloseButton: document.getElementById('orderDetailCloseButton'),
        detailUserLink: document.getElementById('orderDetailUserLink'),
        detailRefundLink: document.getElementById('orderDetailRefundLink'),
        detailCancelButton: document.getElementById('orderDetailCancelButton')
    };

    const state = { pageNo: 1, pageSize: 10, detailOrderId: null };
    const deliveryTypeMap = { ETICKET: '电子票', ELECTRONIC_TICKET: '电子票', PAPER_TICKET: '实体票' };
    const payMethodMap = { ALIPAY: '支付宝', WECHAT: '微信支付', BANK_CARD: '银行卡', MOCK: 'Mock 支付' };
    const statusTextMap = {
        WAIT_PAY: '待支付', WAIT_USE: '待使用', FINISHED: '已完成',
        REFUNDING: '退款中', REFUND_SUCCESS: '退款成功', CANCELED: '已取消'
    };

    function text(value, fallback) {
        if (value == null || value === '') return fallback == null ? '-' : fallback;
        return String(value);
    }

    function readInitialQuery() {
        const params = new URLSearchParams(window.location.search);
        elements.keyword.value = params.get('keyword') || '';
        elements.status.value = params.get('orderStatus') || '';
        elements.userId.value = params.get('userId') || '';
        elements.projectId.value = params.get('projectId') || '';
        elements.dateFrom.value = params.get('dateFrom') || '';
        elements.dateTo.value = params.get('dateTo') || '';
        const size = Number(params.get('pageSize'));
        if ([10, 20, 50].includes(size)) {
            state.pageSize = size;
            elements.pageSize.value = String(size);
        }
    }

    function validateDates() {
        if (elements.dateFrom.value && elements.dateTo.value && elements.dateFrom.value > elements.dateTo.value) {
            AdminUI.toast('下单开始日期不能晚于结束日期', 'warning');
            return false;
        }
        return true;
    }

    function buildQuery() {
        const params = new URLSearchParams();
        const values = {
            keyword: AdminUI.nullableText(elements.keyword.value),
            orderStatus: AdminUI.nullableText(elements.status.value),
            userId: AdminUI.nullableNumber(elements.userId.value),
            projectId: AdminUI.nullableNumber(elements.projectId.value),
            dateFrom: AdminUI.nullableText(elements.dateFrom.value),
            dateTo: AdminUI.nullableText(elements.dateTo.value)
        };
        Object.keys(values).forEach(function (key) {
            if (values[key] != null) params.set(key, values[key]);
        });
        params.set('pageNo', state.pageNo);
        params.set('pageSize', state.pageSize);
        return params;
    }

    function updateBrowserQuery(params) {
        const next = new URLSearchParams(params);
        next.delete('pageNo');
        const detailId = new URLSearchParams(window.location.search).get('detailOrderId');
        if (detailId) next.set('detailOrderId', detailId);
        const url = window.location.pathname + (next.toString() ? '?' + next.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function loadOrders() {
        if (!validateDates()) return;
        elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">正在加载订单数据...</div></td></tr>';
        const params = buildQuery();
        try {
            const data = await AdminRequest.get('/api/admin/orders?' + params.toString());
            renderOrders(data || {});
            updateBrowserQuery(params);
        } catch (error) {
            elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            elements.total.textContent = '读取失败';
            elements.summary.innerHTML = '';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderOrders(data) {
        const items = Array.isArray(data.items) ? data.items : [];
        const total = Number(data.total || 0);
        elements.total.textContent = '共 ' + AdminUI.formatNumber(total) + ' 条订单';
        elements.summary.innerHTML = '<span>当前页 ' + items.length + ' 条</span><span>第 ' + Number(data.pageNo || state.pageNo) + ' 页</span>';
        if (!items.length) {
            elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">没有符合条件的订单</div></td></tr>';
        } else {
            elements.body.innerHTML = items.map(renderOrderRow).join('');
        }
        AdminPagination.render(elements.pagination, {
            total: total,
            pageNo: Number(data.pageNo || state.pageNo),
            pageSize: Number(data.pageSize || state.pageSize),
            onChange: function (pageNo) {
                state.pageNo = pageNo;
                loadOrders();
            }
        });
    }

    function renderOrderRow(item) {
        const location = [item.cityName, item.stationName, item.venueName].filter(Boolean).join(' · ') || '-';
        const payMethod = payMethodMap[item.payMethod] || item.payMethod || '未支付';
        const deliveryType = deliveryTypeMap[item.deliveryType] || item.deliveryType || '-';
        return '<tr>' +
            '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(text(item.orderNo)) + '</strong>' +
            '<small>ID ' + AdminUI.escapeHtml(text(item.orderId)) + ' · ' + AdminUI.escapeHtml(deliveryType) + '</small></div></td>' +
            '<td><div class="table-meta"><b>' + AdminUI.escapeHtml(text(item.nickname, '未设置昵称')) + '</b><br>' +
            AdminUI.escapeHtml(text(item.userPhone)) + '<br>用户 ID ' + AdminUI.escapeHtml(text(item.userId)) + '</div></td>' +
            '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(text(item.projectTitle)) + '</strong>' +
            '<small>' + AdminUI.escapeHtml(location) + '</small></div></td>' +
            '<td><div class="amount-stack"><strong>' + AdminUI.formatMoney(item.payAmount) + '</strong><small>票款 ' + AdminUI.formatMoney(item.ticketAmount) + '</small><small>' + AdminUI.escapeHtml(payMethod) + '</small></div></td>' +
            '<td><div class="resource-count-grid"><span>票档 <b>' + AdminUI.formatNumber(item.itemCount) + '</b></span><span>观演人 <b>' + AdminUI.formatNumber(item.audienceCount) + '</b></span><span>电子票 <b>' + AdminUI.formatNumber(item.ticketCount) + '</b></span><span>退款 <b>' + AdminUI.formatNumber(item.refundCount) + '</b></span></div></td>' +
            '<td>' + AdminUI.statusBadge(item.orderStatus) + '</td>' +
            '<td><div class="table-meta">' + AdminUI.formatDateTime(item.createTime) + '<br><span>支付截止 ' + AdminUI.formatDateTime(item.payExpireTime) + '</span></div></td>' +
            '<td class="align-right"><div class="action-row action-row-wrap">' +
            '<button class="action-link" type="button" data-action="detail" data-id="' + item.orderId + '">详情</button>' +
            (item.orderStatus === 'WAIT_PAY' ? '<button class="action-link danger" type="button" data-action="cancel" data-id="' + item.orderId + '" data-no="' + AdminUI.escapeHtml(text(item.orderNo)) + '">取消订单</button>' : '') +
            (Number(item.refundCount || 0) > 0 ? '<a class="action-link" href="/admin/refunds?orderId=' + item.orderId + '">退款</a>' : '') +
            '</div></td></tr>';
    }

    function updateDetailQuery(value) {
        const params = new URLSearchParams(window.location.search);
        if (value == null || value === '') params.delete('detailOrderId');
        else params.set('detailOrderId', value);
        const url = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function openOrderDetail(orderId) {
        state.detailOrderId = Number(orderId);
        updateDetailQuery(orderId);
        elements.detailContent.innerHTML = '<div class="table-empty">正在读取订单详情...</div>';
        AdminUI.openModal(elements.detailModal);
        try {
            const data = await AdminRequest.get('/api/admin/orders/' + orderId);
            renderOrderDetail(data || {});
        } catch (error) {
            elements.detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderOrderDetail(data) {
        const deliveryType = deliveryTypeMap[data.deliveryType] || data.deliveryType || '-';
        const payMethod = payMethodMap[data.payMethod] || data.payMethod || '未支付';
        const items = Array.isArray(data.items) ? data.items : [];
        const audiences = Array.isArray(data.audiences) ? data.audiences : [];
        const tickets = Array.isArray(data.tickets) ? data.tickets : [];
        const refunds = Array.isArray(data.refunds) ? data.refunds : [];
        const venueLine = [data.cityName, data.stationName, data.venueName].filter(Boolean).join(' · ') || '-';

        state.detailOrderId = Number(data.orderId || 0);
        elements.detailCancelButton.hidden = data.orderStatus !== 'WAIT_PAY';
        elements.detailCancelButton.dataset.orderNo = data.orderNo || '';
        elements.detailUserLink.href = '/admin/users?detailUserId=' + encodeURIComponent(data.userId || '');
        if (refunds.length) {
            elements.detailRefundLink.hidden = false;
            elements.detailRefundLink.href = '/admin/refunds?orderId=' + encodeURIComponent(data.orderId || '');
        } else {
            elements.detailRefundLink.hidden = true;
        }

        const addressHtml = data.address ?
            '<div class="mini-list-item"><div><strong>' + AdminUI.escapeHtml(text(data.address.receiverName)) + ' ' + AdminUI.escapeHtml(text(data.address.receiverPhone)) + '</strong>' +
            '<small>' + AdminUI.escapeHtml([data.address.province, data.address.city, data.address.district, data.address.detailAddress].filter(Boolean).join(' ')) + '</small></div></div>' :
            '<div class="detail-empty-inline">电子票订单或未保存地址快照</div>';

        elements.detailContent.innerHTML =
            '<div class="detail-hero">' +
            '<div class="detail-poster">' + (usableImage(data.posterUrl) ? '<img src="' + AdminUI.escapeHtml(data.posterUrl) + '" alt="演出海报">' : '<span>演</span>') + '</div>' +
            '<div class="detail-hero-copy"><div class="detail-title-row"><h3>' + AdminUI.escapeHtml(text(data.projectTitle)) + '</h3>' + AdminUI.statusBadge(data.orderStatus) + '</div>' +
            '<p>' + AdminUI.escapeHtml(venueLine) + '</p><small>订单号 ' + AdminUI.escapeHtml(text(data.orderNo)) + ' · 订单 ID ' + AdminUI.escapeHtml(text(data.orderId)) + '</small></div></div>' +
            '<div class="detail-grid detail-grid-four">' +
            detailItem('用户', text(data.nickname, '未设置昵称') + ' / ' + text(data.userPhone)) +
            detailItem('配送方式', deliveryType) +
            detailItem('支付方式', payMethod) +
            detailItem('创建时间', AdminUI.formatDateTime(data.createTime)) +
            detailItem('支付截止', AdminUI.formatDateTime(data.payExpireTime)) +
            detailItem('支付时间', AdminUI.formatDateTime(data.payTime)) +
            detailItem('出票时间', AdminUI.formatDateTime(data.ticketIssuedTime)) +
            detailItem('完成 / 取消', AdminUI.formatDateTime(data.finishTime || data.cancelTime)) +
            '</div>' +
            '<div class="amount-card-grid">' +
            amountCard('票款', data.ticketAmount) + amountCard('服务费', data.serviceFeeAmount) + amountCard('配送费', data.deliveryFeeAmount) +
            amountCard('优惠', data.discountAmount) + amountCard('订单总额', data.totalAmount) + amountCard('实付金额', data.payAmount, true) +
            '</div>' +
            detailSection('票档明细', items.length ? '<div class="mini-list">' + items.map(function (item) {
                return '<div class="mini-list-item"><div><strong>' + AdminUI.escapeHtml(text(item.skuName)) + '</strong><small>SKU ' + AdminUI.escapeHtml(text(item.skuId)) + ' · ' + AdminUI.formatMoney(item.unitPrice) + ' × ' + AdminUI.formatNumber(item.quantity) + '</small></div><strong>' + AdminUI.formatMoney(item.subtotalAmount) + '</strong></div>';
            }).join('') + '</div>' : '<div class="detail-empty-inline">无订单项</div>') +
            detailSection('观演人快照', audiences.length ? '<div class="snapshot-grid">' + audiences.map(function (audience) {
                return '<article class="snapshot-card"><strong>' + AdminUI.escapeHtml(text(audience.realName)) + '</strong><span>' + AdminUI.escapeHtml(text(audience.certificateType)) + '：' + AdminUI.escapeHtml(text(audience.certificateNo)) + '</span><span>手机号：' + AdminUI.escapeHtml(text(audience.phone)) + '</span></article>';
            }).join('') + '</div>' : '<div class="detail-empty-inline">无观演人快照</div>') +
            detailSection('收货地址快照', addressHtml) +
            detailSection('电子票', tickets.length ? '<div class="ticket-detail-list">' + tickets.map(renderTicketDetail) + '</div>' : '<div class="detail-empty-inline">尚未生成电子票</div>') +
            detailSection('退款记录', refunds.length ? '<div class="mini-list">' + refunds.map(function (refund) {
                return '<div class="mini-list-item"><div><strong>' + AdminUI.escapeHtml(text(refund.refundNo)) + '</strong><small>' + AdminUI.formatDateTime(refund.applyTime) + ' · 原因：' + AdminUI.escapeHtml(text(refund.reason)) + '</small></div><div class="mini-list-side">' + AdminUI.statusBadge(refund.refundStatus) + '<strong>' + AdminUI.formatMoney(refund.refundAmount) + '</strong></div></div>';
            }).join('') + '</div>' : '<div class="detail-empty-inline">无退款记录</div>');
    }

    function renderTicketDetail(ticket) {
        const qr = ticket.qrCodeValue ? '<span title="' + AdminUI.escapeHtml(ticket.qrCodeValue) + '">二维码：' + AdminUI.escapeHtml(abbreviate(ticket.qrCodeValue, 44)) + '</span>' : '<span>二维码：未生成</span>';
        return '<article class="ticket-detail-card"><div><strong>' + AdminUI.escapeHtml(text(ticket.ticketNo, '未生成票号')) + '</strong>' +
            '<small>电子票 ID ' + AdminUI.escapeHtml(text(ticket.ticketId)) + ' · 座位 ' + AdminUI.escapeHtml(text(ticket.seatInfo, '未分配')) + '</small></div>' +
            '<div class="ticket-detail-meta">' + AdminUI.statusBadge(ticket.ticketStatus) + qr +
            (ticket.abnormalReason ? '<span class="danger-text">异常：' + AdminUI.escapeHtml(ticket.abnormalReason) + '</span>' : '') + '</div></article>';
    }

    async function cancelOrder(orderId, orderNo) {
        const confirmed = await AdminUI.confirm(
            '确认取消待支付订单“' + text(orderNo, orderId) + '”吗？取消后将释放全部锁定库存。',
            '确认取消订单'
        );
        if (!confirmed) return;
        try {
            const result = await AdminRequest.post('/api/admin/orders/' + orderId + '/cancel');
            AdminUI.toast(result.message || '订单已取消', 'success');
            if (state.detailOrderId === Number(orderId)) {
                AdminUI.closeModal(elements.detailModal);
                updateDetailQuery(null);
                state.detailOrderId = null;
            }
            await loadOrders();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    function detailItem(label, value) {
        return '<div class="detail-item"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + AdminUI.escapeHtml(text(value)) + '</strong></div>';
    }

    function amountCard(label, value, primary) {
        return '<div class="amount-card' + (primary ? ' primary' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + AdminUI.formatMoney(value) + '</strong></div>';
    }

    function detailSection(title, html) {
        return '<section class="detail-section"><h4>' + AdminUI.escapeHtml(title) + '</h4>' + html + '</section>';
    }

    function abbreviate(value, maxLength) {
        const stringValue = text(value, '');
        return stringValue.length > maxLength ? stringValue.substring(0, maxLength) + '…' : stringValue;
    }

    function usableImage(value) {
        return AdminUI.isImageUrl(value);
    }

    elements.form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadOrders();
    });
    elements.reset.addEventListener('click', function () {
        elements.form.reset();
        state.pageNo = 1;
        state.pageSize = 10;
        elements.pageSize.value = '10';
        loadOrders();
    });
    elements.refresh.addEventListener('click', loadOrders);
    elements.pageSize.addEventListener('change', function () {
        state.pageSize = Number(elements.pageSize.value || 10);
        state.pageNo = 1;
        loadOrders();
    });
    elements.body.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        if (target.dataset.action === 'detail') openOrderDetail(target.dataset.id);
        if (target.dataset.action === 'cancel') cancelOrder(target.dataset.id, target.dataset.no);
    });
    elements.detailCancelButton.addEventListener('click', function () {
        cancelOrder(state.detailOrderId, elements.detailCancelButton.dataset.orderNo);
    });
    [elements.detailClose, elements.detailCloseButton].forEach(function (button) {
        button.addEventListener('click', function () {
            AdminUI.closeModal(elements.detailModal);
            updateDetailQuery(null);
        });
    });
    elements.detailModal.addEventListener('click', function (event) {
        if (event.target === elements.detailModal) {
            AdminUI.closeModal(elements.detailModal);
            updateDetailQuery(null);
        }
    });
    readInitialQuery();
    loadOrders().then(function () {
        const detailOrderId = new URLSearchParams(window.location.search).get('detailOrderId');
        if (detailOrderId) openOrderDetail(detailOrderId);
    });
})();
