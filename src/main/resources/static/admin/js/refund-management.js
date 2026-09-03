(function () {
    'use strict';

    const page = document.getElementById('refundManagementPage');
    if (!page) return;

    const elements = {
        form: document.getElementById('refundFilterForm'),
        keyword: document.getElementById('refundKeyword'),
        status: document.getElementById('refundStatusFilter'),
        userId: document.getElementById('refundUserId'),
        orderId: document.getElementById('refundOrderId'),
        dateFrom: document.getElementById('refundDateFrom'),
        dateTo: document.getElementById('refundDateTo'),
        reset: document.getElementById('resetRefundFilterButton'),
        refresh: document.getElementById('refreshRefundButton'),
        body: document.getElementById('refundTableBody'),
        total: document.getElementById('refundTotalText'),
        summary: document.getElementById('refundSummaryChips'),
        pageSize: document.getElementById('refundPageSizeSelect'),
        pagination: document.getElementById('refundPagination'),
        detailModal: document.getElementById('refundDetailModal'),
        detailContent: document.getElementById('refundDetailContent'),
        detailClose: document.getElementById('refundDetailClose'),
        detailCloseButton: document.getElementById('refundDetailCloseButton'),
        detailOrderLink: document.getElementById('refundDetailOrderLink'),
        detailUserLink: document.getElementById('refundDetailUserLink'),
        statusModal: document.getElementById('refundStatusModal'),
        statusForm: document.getElementById('refundStatusForm'),
        statusClose: document.getElementById('refundStatusClose'),
        statusCancel: document.getElementById('refundStatusCancel'),
        statusSubmit: document.getElementById('refundStatusSubmit'),
        statusRefundNo: document.getElementById('statusRefundNo'),
        statusCurrent: document.getElementById('statusCurrentRefundStatus'),
        statusTarget: document.getElementById('statusTargetRefundStatus'),
        failReasonField: document.getElementById('refundFailReasonField'),
        failReason: document.getElementById('refundFailReason')
    };

    const state = { pageNo: 1, pageSize: 10, currentRefund: null };
    const statusTextMap = { REFUNDING: '退款中', REFUND_SUCCESS: '退款成功', REFUND_FAILED: '退款失败' };
    const refundTypeMap = { NO_REFUND: '不可退', CONDITIONAL_REFUND: '条件退', FULL_REFUND: '全额退', SYSTEM_REFUND: '系统退款' };

    function text(value, fallback) {
        if (value == null || value === '') return fallback == null ? '-' : fallback;
        return String(value);
    }

    function readInitialQuery() {
        const params = new URLSearchParams(window.location.search);
        elements.keyword.value = params.get('keyword') || '';
        elements.status.value = params.get('refundStatus') || '';
        elements.userId.value = params.get('userId') || '';
        elements.orderId.value = params.get('orderId') || '';
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
            AdminUI.toast('申请开始日期不能晚于结束日期', 'warning');
            return false;
        }
        return true;
    }

    function buildQuery() {
        const params = new URLSearchParams();
        const values = {
            keyword: AdminUI.nullableText(elements.keyword.value),
            refundStatus: AdminUI.nullableText(elements.status.value),
            userId: AdminUI.nullableNumber(elements.userId.value),
            orderId: AdminUI.nullableNumber(elements.orderId.value),
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
        const detailId = new URLSearchParams(window.location.search).get('detailRefundId');
        if (detailId) next.set('detailRefundId', detailId);
        const url = window.location.pathname + (next.toString() ? '?' + next.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function loadRefunds() {
        if (!validateDates()) return;
        elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">正在加载退款数据...</div></td></tr>';
        const params = buildQuery();
        try {
            const data = await AdminRequest.get('/api/admin/refunds?' + params.toString());
            renderRefunds(data || {});
            updateBrowserQuery(params);
        } catch (error) {
            elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            elements.total.textContent = '读取失败';
            elements.summary.innerHTML = '';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderRefunds(data) {
        const items = Array.isArray(data.items) ? data.items : [];
        const total = Number(data.total || 0);
        const pendingCount = items.filter(function (item) { return item.refundStatus === 'REFUNDING'; }).length;
        elements.total.textContent = '共 ' + AdminUI.formatNumber(total) + ' 条退款记录';
        elements.summary.innerHTML = '<span>当前页 ' + items.length + ' 条</span><span class="warning-chip">待处理 ' + pendingCount + ' 条</span>';
        elements.body.innerHTML = items.length ? items.map(renderRefundRow).join('') : '<tr><td colspan="8"><div class="table-empty">没有符合条件的退款记录</div></td></tr>';
        AdminPagination.render(elements.pagination, {
            total: total,
            pageNo: Number(data.pageNo || state.pageNo),
            pageSize: Number(data.pageSize || state.pageSize),
            onChange: function (pageNo) {
                state.pageNo = pageNo;
                loadRefunds();
            }
        });
    }

    function renderRefundRow(item) {
        const location = [item.cityName, item.stationName].filter(Boolean).join(' · ') || '-';
        const typeText = refundTypeMap[item.refundTypeSnapshot] || item.refundTypeSnapshot || '-';
        const feeRate = item.feeRateSnapshot == null ? '-' : (Number(item.feeRateSnapshot) * 100).toFixed(2).replace(/\.00$/, '') + '%';
        return '<tr>' +
            '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(text(item.refundNo)) + '</strong><small>订单 ' + AdminUI.escapeHtml(text(item.orderNo)) + ' · ID ' + AdminUI.escapeHtml(text(item.refundId)) + '</small></div></td>' +
            '<td><div class="table-meta"><b>' + AdminUI.escapeHtml(text(item.nickname, '未设置昵称')) + '</b><br>' + AdminUI.escapeHtml(text(item.userPhone)) + '<br>用户 ID ' + AdminUI.escapeHtml(text(item.userId)) + '</div></td>' +
            '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(text(item.projectTitle)) + '</strong><small>' + AdminUI.escapeHtml(location) + '</small></div></td>' +
            '<td><div class="amount-stack"><strong>' + AdminUI.formatMoney(item.refundAmount) + '</strong><small>手续费 ' + AdminUI.formatMoney(item.feeAmount) + '</small></div></td>' +
            '<td><div class="table-meta"><b>' + AdminUI.escapeHtml(typeText) + '</b><br>费率 ' + AdminUI.escapeHtml(feeRate) + '</div></td>' +
            '<td>' + AdminUI.statusBadge(item.refundStatus) + (item.failReason ? '<div class="status-reason danger-text" title="' + AdminUI.escapeHtml(item.failReason) + '">' + AdminUI.escapeHtml(abbreviate(item.failReason, 18)) + '</div>' : '') + '</td>' +
            '<td><div class="table-meta">' + AdminUI.formatDateTime(item.applyTime) + '<br><span>完成 ' + AdminUI.formatDateTime(item.refundTime) + '</span></div></td>' +
            '<td class="align-right"><div class="action-row action-row-wrap">' +
            '<button class="action-link" type="button" data-action="detail" data-id="' + item.refundId + '">详情</button>' +
            '<a class="action-link" href="/admin/orders?detailOrderId=' + item.orderId + '">订单</a>' +
            (item.refundStatus === 'REFUNDING' ? '<button class="action-link danger" type="button" data-action="status" data-id="' + item.refundId + '" data-no="' + AdminUI.escapeHtml(text(item.refundNo)) + '" data-status="' + AdminUI.escapeHtml(text(item.refundStatus)) + '">审核</button>' : '') +
            '</div></td></tr>';
    }

    function updateDetailQuery(value) {
        const params = new URLSearchParams(window.location.search);
        if (value == null || value === '') params.delete('detailRefundId');
        else params.set('detailRefundId', value);
        const url = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function openRefundDetail(refundId) {
        updateDetailQuery(refundId);
        elements.detailContent.innerHTML = '<div class="table-empty">正在读取退款详情...</div>';
        AdminUI.openModal(elements.detailModal);
        try {
            const data = await AdminRequest.get('/api/admin/refunds/' + refundId);
            renderRefundDetail(data || {});
        } catch (error) {
            elements.detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderRefundDetail(data) {
        const tickets = Array.isArray(data.tickets) ? data.tickets : [];
        const typeText = refundTypeMap[data.refundTypeSnapshot] || data.refundTypeSnapshot || '-';
        const feeRate = data.feeRateSnapshot == null ? '-' : (Number(data.feeRateSnapshot) * 100).toFixed(2).replace(/\.00$/, '') + '%';
        const location = [data.cityName, data.stationName, data.venueName].filter(Boolean).join(' · ') || '-';
        elements.detailOrderLink.href = '/admin/orders?detailOrderId=' + encodeURIComponent(data.orderId || '');
        elements.detailUserLink.href = '/admin/users?detailUserId=' + encodeURIComponent(data.userId || '');
        elements.detailContent.innerHTML =
            '<div class="detail-hero">' +
            '<div class="detail-poster">' + (usableImage(data.posterUrl) ? '<img src="' + AdminUI.escapeHtml(data.posterUrl) + '" alt="演出海报">' : '<span>退</span>') + '</div>' +
            '<div class="detail-hero-copy"><div class="detail-title-row"><h3>' + AdminUI.escapeHtml(text(data.projectTitle)) + '</h3>' + AdminUI.statusBadge(data.refundStatus) + '</div>' +
            '<p>' + AdminUI.escapeHtml(location) + '</p><small>退款号 ' + AdminUI.escapeHtml(text(data.refundNo)) + ' · 订单号 ' + AdminUI.escapeHtml(text(data.orderNo)) + '</small></div></div>' +
            '<div class="detail-grid detail-grid-four">' +
            detailItem('用户', text(data.nickname, '未设置昵称') + ' / ' + text(data.userPhone)) +
            detailItem('订单状态', statusText(data.orderStatus)) +
            detailItem('退款类型', typeText) +
            detailItem('手续费率', feeRate) +
            detailItem('申请时间', AdminUI.formatDateTime(data.applyTime)) +
            detailItem('退款完成时间', AdminUI.formatDateTime(data.refundTime)) +
            detailItem('最后更新时间', AdminUI.formatDateTime(data.updateTime)) +
            detailItem('退款记录 ID', text(data.refundId)) +
            '</div>' +
            '<div class="amount-card-grid amount-card-grid-four">' +
            amountCard('订单实付', data.orderPayAmount) + amountCard('用户退款', data.refundAmount, true) + amountCard('手续费', data.feeAmount) + amountCard('退款加手续费', Number(data.refundAmount || 0) + Number(data.feeAmount || 0)) +
            '</div>' +
            detailSection('申请原因', '<div class="text-detail-card">' + AdminUI.escapeHtml(text(data.reason, '用户未填写原因')) + '</div>') +
            (data.failReason ? detailSection('失败原因', '<div class="text-detail-card danger-card">' + AdminUI.escapeHtml(data.failReason) + '</div>') : '') +
            detailSection('关联电子票', tickets.length ? '<div class="ticket-detail-list">' + tickets.map(function (ticket) {
                return '<article class="ticket-detail-card"><div><strong>' + AdminUI.escapeHtml(text(ticket.ticketNo, '未生成票号')) + '</strong><small>电子票 ID ' + AdminUI.escapeHtml(text(ticket.ticketId)) + ' · 座位 ' + AdminUI.escapeHtml(text(ticket.seatInfo, '未分配')) + '</small></div><div class="ticket-detail-meta">' + AdminUI.statusBadge(ticket.ticketStatus) + (ticket.abnormalReason ? '<span class="danger-text">' + AdminUI.escapeHtml(ticket.abnormalReason) + '</span>' : '') + '</div></article>';
            }).join('') + '</div>' : '<div class="detail-empty-inline">订单下没有电子票</div>');
    }

    function statusText(status) {
        const map = { WAIT_PAY: '待支付', WAIT_USE: '待使用', FINISHED: '已完成', REFUNDING: '退款中', REFUND_SUCCESS: '退款成功', CANCELED: '已取消' };
        return map[status] || status || '-';
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

    function openStatusModal(refundId, refundNo, currentStatus) {
        state.currentRefund = { refundId: Number(refundId), refundNo: refundNo, currentStatus: currentStatus };
        elements.statusRefundNo.textContent = refundNo || '-';
        elements.statusCurrent.textContent = statusTextMap[currentStatus] || currentStatus || '-';
        elements.statusTarget.value = 'REFUND_SUCCESS';
        elements.failReason.value = '';
        toggleFailReason();
        AdminUI.openModal(elements.statusModal);
    }

    function toggleFailReason() {
        const failed = elements.statusTarget.value === 'REFUND_FAILED';
        elements.failReasonField.hidden = !failed;
        elements.failReason.required = failed;
    }

    async function submitStatus(event) {
        event.preventDefault();
        if (!state.currentRefund) return;
        const target = elements.statusTarget.value;
        const failReason = AdminUI.nullableText(elements.failReason.value);
        if (target === 'REFUND_FAILED' && !failReason) {
            AdminUI.toast('退款失败时必须填写失败原因', 'warning');
            elements.failReason.focus();
            return;
        }
        const confirmed = await AdminUI.confirm(
            '确认将退款“' + state.currentRefund.refundNo + '”审核为“' + (statusTextMap[target] || target) + '”吗？该退款处理完成后不可再次审核。',
            '确认退款审核'
        );
        if (!confirmed) return;
        AdminUI.setButtonLoading(elements.statusSubmit, true, '提交中...');
        try {
            if (target === 'REFUND_SUCCESS') {
                await AdminRequest.post('/api/admin/refunds/' + state.currentRefund.refundId + '/approve');
            } else {
                await AdminRequest.post('/api/admin/refunds/' + state.currentRefund.refundId + '/reject', { reason: failReason });
            }
            AdminUI.closeModal(elements.statusModal);
            AdminUI.toast(target === 'REFUND_SUCCESS' ? '退款审核通过' : '退款已驳回', 'success');
            await loadRefunds();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(elements.statusSubmit, false);
        }
    }

    elements.form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadRefunds();
    });
    elements.reset.addEventListener('click', function () {
        elements.form.reset();
        state.pageNo = 1;
        state.pageSize = 10;
        elements.pageSize.value = '10';
        loadRefunds();
    });
    elements.refresh.addEventListener('click', loadRefunds);
    elements.pageSize.addEventListener('change', function () {
        state.pageSize = Number(elements.pageSize.value || 10);
        state.pageNo = 1;
        loadRefunds();
    });
    elements.body.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        if (target.dataset.action === 'detail') openRefundDetail(target.dataset.id);
        if (target.dataset.action === 'status') openStatusModal(target.dataset.id, target.dataset.no, target.dataset.status);
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
    [elements.statusClose, elements.statusCancel].forEach(function (button) {
        button.addEventListener('click', function () { AdminUI.closeModal(elements.statusModal); });
    });
    elements.statusModal.addEventListener('click', function (event) {
        if (event.target === elements.statusModal) AdminUI.closeModal(elements.statusModal);
    });
    elements.statusTarget.addEventListener('change', toggleFailReason);
    elements.statusForm.addEventListener('submit', submitStatus);

    readInitialQuery();
    loadRefunds().then(function () {
        const detailRefundId = new URLSearchParams(window.location.search).get('detailRefundId');
        if (detailRefundId) openRefundDetail(detailRefundId);
    });
})();
