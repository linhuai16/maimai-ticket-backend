(function () {
    'use strict';
    const state = { pageNo: 1, pageSize: 20, total: 0 };
    const body = document.getElementById('ticketTableBody');
    if (!body) return;

    function value(id) { return document.getElementById(id).value.trim(); }
    function query() {
        const params = new URLSearchParams({ pageNo: state.pageNo, pageSize: state.pageSize });
        [['keyword','ticketKeyword'],['ticketStatus','ticketStatus'],['orderId','ticketOrderId'],['userId','ticketUserId'],['projectId','ticketProjectId'],['sessionId','ticketSessionId'],['dateFrom','ticketDateFrom'],['dateTo','ticketDateTo']].forEach(function (item) {
            const v = value(item[1]); if (v) params.set(item[0], v);
        });
        return params.toString();
    }

    async function load() {
        body.innerHTML = '<tr><td colspan="7"><div class="table-empty">正在加载电子票...</div></td></tr>';
        try {
            const data = await AdminRequest.get('/api/admin/tickets?' + query());
            state.total = Number(data.total || 0);
            document.getElementById('ticketTotalText').textContent = '共 ' + state.total + ' 张电子票';
            render(data.items || []);
            AdminPagination.render(document.getElementById('ticketPagination'), { pageNo: state.pageNo, pageSize: state.pageSize, total: state.total, onChange: function (page) { state.pageNo = page; load(); } });
        } catch (e) {
            body.innerHTML = '<tr><td colspan="7"><div class="table-empty danger-text">' + AdminUI.escapeHtml(e.message) + '</div></td></tr>';
        }
    }

    function render(items) {
        if (!items.length) { body.innerHTML = '<tr><td colspan="7"><div class="table-empty">暂无符合条件的电子票</div></td></tr>'; return; }
        body.innerHTML = items.map(function (item) {
            const sourceManaged = item.fulfillmentMode === 'TICKET_SOURCE';
            const qr = item.ticketStatus === 'UNUSED' || item.ticketStatus === 'CHECKED' || item.ticketStatus === 'EXPIRED';
            const canError = !sourceManaged && (item.ticketStatus === 'GENERATING' || item.ticketStatus === 'UNUSED');
            const canRetry = !sourceManaged && item.ticketStatus === 'ERROR';
            return '<tr>' +
                '<td><strong>' + AdminUI.escapeHtml(item.ticketNo) + '</strong><div class="table-meta">ID ' + item.ticketId + '</div></td>' +
                '<td><strong>' + AdminUI.escapeHtml(item.orderNo) + '</strong><div class="table-meta">用户 #' + item.userId + ' · ' + AdminUI.escapeHtml(item.userPhone || item.nickname || '-') + '</div></td>' +
                '<td><strong>' + AdminUI.escapeHtml(item.projectTitle) + '</strong><div class="table-meta">' + AdminUI.escapeHtml(item.cityName || '') + ' ' + AdminUI.escapeHtml(item.stationName || '') + '<br>场次 #' + item.sessionId + '</div></td>' +
                '<td><strong>' + AdminUI.escapeHtml(item.audienceName || '-') + '</strong><div class="table-meta">' + AdminUI.escapeHtml(item.seatInfo || (sourceManaged ? '等待 Provider 座位事实' : '未分配座位')) + '</div></td>' +
                '<td>' + AdminUI.statusBadge(item.ticketStatus) + (item.abnormalReason ? '<div class="table-meta danger-text">' + AdminUI.escapeHtml(item.abnormalReason) + '</div>' : '') + '</td>' +
                '<td><div class="table-meta">生成：' + AdminUI.formatDateTime(item.generateTime) + '<br>更新：' + AdminUI.formatDateTime(item.updateTime) + '</div></td>' +
                '<td class="align-right"><div class="table-actions"><button class="button button-secondary button-small" data-action="detail" data-id="' + item.ticketId + '">详情</button>' +
                (qr ? '<a class="button button-secondary button-small" href="/admin/tickets/' + item.ticketId + '/qr">二维码</a>' : '') +
                (sourceManaged ? '<span class="table-meta">Provider 同步只读</span>' : '<button class="button button-secondary button-small" data-action="seat" data-id="' + item.ticketId + '" data-seat="' + AdminUI.escapeHtml(item.seatInfo || '') + '">座位</button>') +
                (canError ? '<button class="button button-secondary button-small" data-action="error" data-id="' + item.ticketId + '">标记异常</button>' : '') +
                (canRetry ? '<button class="button button-primary button-small" data-action="retry" data-id="' + item.ticketId + '">重新出票</button>' : '') + '</div></td></tr>';
        }).join('');
    }

    async function detail(id) {
        const modal = document.getElementById('ticketDetailModal');
        const content = document.getElementById('ticketDetailContent');
        content.innerHTML = '<div class="table-empty">正在读取详情...</div>'; AdminUI.openModal(modal);
        try {
            const d = await AdminRequest.get('/api/admin/tickets/' + id);
            content.innerHTML = '<div class="detail-grid"><div><span>票号</span><strong>' + AdminUI.escapeHtml(d.ticketNo) + '</strong></div><div><span>票状态</span><strong>' + AdminUI.statusBadge(d.ticketStatus) + '</strong></div><div><span>履约来源</span><strong>' + AdminUI.escapeHtml(d.fulfillmentMode === 'TICKET_SOURCE' ? ('Provider · ' + (d.sourceProviderCode || '-')) : '本地兼容') + '</strong></div><div><span>订单</span><strong>' + AdminUI.escapeHtml(d.orderNo) + '</strong></div><div><span>订单状态</span><strong>' + AdminUI.statusBadge(d.orderStatus) + '</strong></div><div><span>演出</span><strong>' + AdminUI.escapeHtml(d.projectTitle) + '</strong></div><div><span>场次</span><strong>' + AdminUI.formatDateTime(d.startTime) + '</strong></div><div><span>场馆</span><strong>' + AdminUI.escapeHtml(d.venueName || '-') + '</strong></div><div><span>观演人</span><strong>' + AdminUI.escapeHtml(d.audienceName || '-') + '</strong></div><div><span>票档</span><strong>' + AdminUI.escapeHtml(d.skuName || '-') + '</strong></div><div><span>座位</span><strong>' + AdminUI.escapeHtml(d.seatInfo || '-') + '</strong></div><div><span>生成时间</span><strong>' + AdminUI.formatDateTime(d.generateTime) + '</strong></div><div><span>检票时间</span><strong>' + AdminUI.formatDateTime(d.checkTime) + '</strong></div></div>' + (d.abnormalReason ? '<div class="inline-alert danger">异常原因：' + AdminUI.escapeHtml(d.abnormalReason) + '</div>' : '');
        } catch (e) { content.innerHTML = '<div class="table-empty danger-text">' + AdminUI.escapeHtml(e.message) + '</div>'; }
    }

    async function retry(id) {
        if (!await AdminUI.confirm('确认将该电子票重新放入出票队列吗？', '重新出票')) return;
        try { await AdminRequest.post('/api/admin/tickets/' + id + '/retry'); AdminUI.toast('电子票已重新进入出票队列', 'success'); load(); }
        catch (e) { AdminUI.toast(e.message, 'danger'); }
    }

    body.addEventListener('click', function (e) {
        const button = e.target.closest('[data-action]'); if (!button) return;
        const id = button.dataset.id;
        if (button.dataset.action === 'detail') detail(id);
        if (button.dataset.action === 'retry') retry(id);
        if (button.dataset.action === 'seat') { document.getElementById('seatTicketId').value = id; document.getElementById('seatInfo').value = button.dataset.seat || ''; AdminUI.openModal(document.getElementById('seatModal')); }
        if (button.dataset.action === 'error') { document.getElementById('errorTicketId').value = id; document.getElementById('ticketErrorReason').value = ''; AdminUI.openModal(document.getElementById('ticketErrorModal')); }
    });

    document.querySelectorAll('[data-close-modal]').forEach(function (b) { b.addEventListener('click', function () { AdminUI.closeModal(document.getElementById(b.dataset.closeModal)); }); });
    document.getElementById('seatForm').addEventListener('submit', async function (e) { e.preventDefault(); try { await AdminRequest.put('/api/admin/tickets/' + value('seatTicketId') + '/seat', { seatInfo: value('seatInfo') || null }); AdminUI.closeModal(document.getElementById('seatModal')); AdminUI.toast('座位信息已保存', 'success'); load(); } catch (err) { AdminUI.toast(err.message, 'danger'); } });
    document.getElementById('ticketErrorForm').addEventListener('submit', async function (e) { e.preventDefault(); try { await AdminRequest.post('/api/admin/tickets/' + value('errorTicketId') + '/mark-error', { abnormalReason: value('ticketErrorReason') }); AdminUI.closeModal(document.getElementById('ticketErrorModal')); AdminUI.toast('电子票已标记异常', 'success'); load(); } catch (err) { AdminUI.toast(err.message, 'danger'); } });
    document.getElementById('ticketFilterForm').addEventListener('submit', function (e) { e.preventDefault(); state.pageNo = 1; load(); });
    document.getElementById('resetTicketFilter').addEventListener('click', function () { document.getElementById('ticketFilterForm').reset(); state.pageNo = 1; load(); });
    document.getElementById('refreshTicketButton').addEventListener('click', load);
    document.getElementById('ticketPageSize').addEventListener('change', function () { state.pageSize = Number(this.value); state.pageNo = 1; load(); });
    const initial = new URLSearchParams(window.location.search);
    if (initial.get('keyword')) document.getElementById('ticketKeyword').value = initial.get('keyword');
    if (initial.get('ticketId')) document.getElementById('ticketKeyword').value = initial.get('ticketId');
    if (initial.get('orderId')) document.getElementById('ticketOrderId').value = initial.get('orderId');
    load();
})();
