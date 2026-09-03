(function () {
    'use strict';

    const page = document.getElementById('userManagementPage');
    if (!page) return;

    const elements = {
        form: document.getElementById('userFilterForm'),
        keyword: document.getElementById('userKeyword'),
        status: document.getElementById('userStatusFilter'),
        reset: document.getElementById('resetUserFilterButton'),
        refresh: document.getElementById('refreshUserButton'),
        body: document.getElementById('userTableBody'),
        total: document.getElementById('userTotalText'),
        summary: document.getElementById('userSummaryChips'),
        pageSize: document.getElementById('userPageSizeSelect'),
        pagination: document.getElementById('userPagination'),
        detailModal: document.getElementById('userDetailModal'),
        detailContent: document.getElementById('userDetailContent'),
        detailClose: document.getElementById('userDetailClose'),
        detailCloseButton: document.getElementById('userDetailCloseButton'),
        detailOrderLink: document.getElementById('userDetailOrderLink')
    };

    const state = { pageNo: 1, pageSize: 10 };
    const accountStatusMap = { NORMAL: '正常', DISABLED: '已禁用' };

    function text(value, fallback) {
        if (value == null || value === '') return fallback == null ? '-' : fallback;
        return String(value);
    }

    function readInitialQuery() {
        const params = new URLSearchParams(window.location.search);
        elements.keyword.value = params.get('keyword') || '';
        elements.status.value = params.get('accountStatus') || '';
        const size = Number(params.get('pageSize'));
        if ([10, 20, 50].includes(size)) {
            state.pageSize = size;
            elements.pageSize.value = String(size);
        }
    }

    function buildQuery() {
        const params = new URLSearchParams();
        const keyword = AdminUI.nullableText(elements.keyword.value);
        const status = AdminUI.nullableText(elements.status.value);
        if (keyword != null) params.set('keyword', keyword);
        if (status != null) params.set('accountStatus', status);
        params.set('pageNo', state.pageNo);
        params.set('pageSize', state.pageSize);
        return params;
    }

    function updateBrowserQuery(params) {
        const next = new URLSearchParams(params);
        next.delete('pageNo');
        const detailUserId = new URLSearchParams(window.location.search).get('detailUserId');
        if (detailUserId) next.set('detailUserId', detailUserId);
        const url = window.location.pathname + (next.toString() ? '?' + next.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function loadUsers() {
        elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">正在加载用户数据...</div></td></tr>';
        const params = buildQuery();
        try {
            const data = await AdminRequest.get('/api/admin/users?' + params.toString());
            renderUsers(data || {});
            updateBrowserQuery(params);
        } catch (error) {
            elements.body.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            elements.total.textContent = '读取失败';
            elements.summary.innerHTML = '';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderUsers(data) {
        const items = Array.isArray(data.items) ? data.items : [];
        const total = Number(data.total || 0);
        const disabledCount = items.filter(function (item) { return item.accountStatus === 'DISABLED'; }).length;
        elements.total.textContent = '共 ' + AdminUI.formatNumber(total) + ' 位用户';
        elements.summary.innerHTML = '<span>当前页 ' + items.length + ' 位</span><span class="danger-chip">已禁用 ' + disabledCount + ' 位</span>';
        elements.body.innerHTML = items.length ? items.map(renderUserRow).join('') : '<tr><td colspan="8"><div class="table-empty">没有符合条件的用户</div></td></tr>';
        AdminPagination.render(elements.pagination, {
            total: total,
            pageNo: Number(data.pageNo || state.pageNo),
            pageSize: Number(data.pageSize || state.pageSize),
            onChange: function (pageNo) {
                state.pageNo = pageNo;
                loadUsers();
            }
        });
    }

    function renderUserRow(item) {
        const nickname = text(item.nickname, '未设置昵称');
        const targetStatus = item.accountStatus === 'DISABLED' ? 'NORMAL' : 'DISABLED';
        const actionText = targetStatus === 'NORMAL' ? '恢复账号' : '禁用账号';
        return '<tr>' +
            '<td><div class="user-cell"><div class="user-list-avatar">' + renderAvatar(item.avatarUrl, nickname) + '</div><div><strong>' + AdminUI.escapeHtml(nickname) + '</strong><small>' + AdminUI.escapeHtml(text(item.phone)) + ' · ID ' + AdminUI.escapeHtml(text(item.userId)) + '</small></div></div></td>' +
            '<td><div class="resource-count-grid"><span>全部订单 <b>' + AdminUI.formatNumber(item.orderCount) + '</b></span><span>待支付 <b>' + AdminUI.formatNumber(item.waitPayOrderCount) + '</b></span><span>已支付 <b>' + AdminUI.formatNumber(item.paidOrderCount) + '</b></span></div></td>' +
            '<td><div class="amount-stack"><strong>' + AdminUI.formatMoney(item.totalPayAmount) + '</strong><small>累计实付</small></div></td>' +
            '<td><div class="resource-count-grid"><span>观演人 <b>' + AdminUI.formatNumber(item.audienceCount) + '</b></span><span>地址 <b>' + AdminUI.formatNumber(item.addressCount) + '</b></span></div></td>' +
            '<td><strong>' + AdminUI.formatNumber(item.wantCount) + '</strong><div class="table-meta">条想看记录</div></td>' +
            '<td>' + AdminUI.statusBadge(item.accountStatus) + '</td>' +
            '<td><div class="table-meta">' + AdminUI.formatDateTime(item.createTime) + '<br><span>更新 ' + AdminUI.formatDateTime(item.updateTime) + '</span></div></td>' +
            '<td class="align-right"><div class="action-row action-row-wrap">' +
            '<button class="action-link" type="button" data-action="detail" data-id="' + item.userId + '">详情</button>' +
            '<a class="action-link" href="/admin/orders?userId=' + item.userId + '">订单</a>' +
            '<button class="action-link ' + (targetStatus === 'DISABLED' ? 'danger' : '') + '" type="button" data-action="status" data-id="' + item.userId + '" data-name="' + AdminUI.escapeHtml(nickname) + '" data-current="' + AdminUI.escapeHtml(text(item.accountStatus)) + '" data-target="' + targetStatus + '">' + actionText + '</button>' +
            '</div></td></tr>';
    }

    function renderAvatar(url, nickname) {
        const source = String(url || '').trim();
        const usable = AdminUI.isImageUrl(source);
        if (usable) return '<img src="' + AdminUI.escapeHtml(source) + '" alt="用户头像">';
        return '<span>' + AdminUI.escapeHtml(nickname.substring(0, 1)) + '</span>';
    }

    function usableImage(value) {
        return AdminUI.isImageUrl(value);
    }

    function updateDetailQuery(value) {
        const params = new URLSearchParams(window.location.search);
        if (value == null || value === '') params.delete('detailUserId');
        else params.set('detailUserId', value);
        const url = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.replaceState(null, '', url);
    }

    async function openUserDetail(userId) {
        updateDetailQuery(userId);
        elements.detailContent.innerHTML = '<div class="table-empty">正在读取用户详情...</div>';
        AdminUI.openModal(elements.detailModal);
        try {
            const data = await AdminRequest.get('/api/admin/users/' + userId);
            renderUserDetail(data || {});
        } catch (error) {
            elements.detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderUserDetail(data) {
        const nickname = text(data.nickname, '未设置昵称');
        const wants = Array.isArray(data.wants) ? data.wants : [];
        elements.detailOrderLink.href = '/admin/orders?userId=' + encodeURIComponent(data.userId || '');
        elements.detailContent.innerHTML =
            '<div class="user-profile-hero">' +
            '<div class="user-profile-avatar">' + renderAvatar(data.avatarUrl, nickname) + '</div>' +
            '<div class="user-profile-copy"><div class="detail-title-row"><h3>' + AdminUI.escapeHtml(nickname) + '</h3>' + AdminUI.statusBadge(data.accountStatus) + '</div>' +
            '<p>' + AdminUI.escapeHtml(text(data.phone)) + '</p><small>用户 ID ' + AdminUI.escapeHtml(text(data.userId)) + ' · 注册于 ' + AdminUI.formatDateTime(data.createTime) + '</small></div></div>' +
            '<div class="inline-alert">为保护用户隐私，后台不展示用户观演人、证件号码和收货地址明细。</div>' +
            '<div class="user-stat-grid">' +
            statCard('全部订单', data.orderCount) + statCard('已支付订单', data.paidOrderCount) + statCard('累计实付', AdminUI.formatMoney(data.totalPayAmount), true) +
            statCard('想看记录', wants.length) +
            '</div>' +
            detailSection('想看记录', wants.length ? '<div class="want-detail-grid">' + wants.map(function (want) {
                return '<a class="want-detail-card" href="/admin/performances/projects/' + encodeURIComponent(want.projectId || '') + '/edit"><div class="want-cover">' + (usableImage(want.posterUrl) ? '<img src="' + AdminUI.escapeHtml(want.posterUrl) + '" alt="演出海报">' : '<span>演</span>') + '</div><div><strong>' + AdminUI.escapeHtml(text(want.projectTitle)) + '</strong><small>项目 ID ' + AdminUI.escapeHtml(text(want.projectId)) + ' · ' + AdminUI.formatDateTime(want.wantTime) + '</small></div></a>';
            }).join('') + '</div>' : '<div class="detail-empty-inline">用户暂无想看记录</div>');
    }

    function statCard(label, value, primary) {
        return '<div class="user-stat-card' + (primary ? ' primary' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + AdminUI.escapeHtml(text(value, '0')) + '</strong></div>';
    }

    function detailSection(title, html) {
        return '<section class="detail-section"><h4>' + AdminUI.escapeHtml(title) + '</h4>' + html + '</section>';
    }

    async function updateUserStatus(userId, name, currentStatus, targetStatus) {
        const targetText = accountStatusMap[targetStatus] || targetStatus;
        const confirmed = await AdminUI.confirm(
            '确认将用户“' + name + '”（ID ' + userId + '）设置为“' + targetText + '”吗？' + (targetStatus === 'DISABLED' ? '禁用后该账号将无法登录 APP。' : ''),
            targetStatus === 'DISABLED' ? '确认禁用用户' : '确认恢复用户'
        );
        if (!confirmed) return;
        try {
            await AdminRequest.put('/api/admin/users/' + userId + '/status', { accountStatus: targetStatus });
            AdminUI.toast(targetStatus === 'DISABLED' ? '用户已禁用' : '用户已恢复', 'success');
            await loadUsers();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    elements.form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadUsers();
    });
    elements.reset.addEventListener('click', function () {
        elements.form.reset();
        state.pageNo = 1;
        state.pageSize = 10;
        elements.pageSize.value = '10';
        loadUsers();
    });
    elements.refresh.addEventListener('click', loadUsers);
    elements.pageSize.addEventListener('change', function () {
        state.pageSize = Number(elements.pageSize.value || 10);
        state.pageNo = 1;
        loadUsers();
    });
    elements.body.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        if (target.dataset.action === 'detail') openUserDetail(target.dataset.id);
        if (target.dataset.action === 'status') updateUserStatus(target.dataset.id, target.dataset.name, target.dataset.current, target.dataset.target);
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
    loadUsers().then(function () {
        const detailUserId = new URLSearchParams(window.location.search).get('detailUserId');
        if (detailUserId) openUserDetail(detailUserId);
    });
})();
