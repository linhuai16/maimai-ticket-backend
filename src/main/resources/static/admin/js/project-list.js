(function () {
    'use strict';

    const state = { pageNo: 1, pageSize: 10, total: 0, loading: false };
    const form = document.getElementById('projectFilterForm');
    const tableBody = document.getElementById('projectTableBody');
    const totalText = document.getElementById('projectTotalText');
    const pagination = document.getElementById('projectPagination');
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const detailModal = document.getElementById('projectDetailModal');
    const detailContent = document.getElementById('projectDetailContent');
    const detailEditLink = document.getElementById('detailEditProjectLink');
    const detailSessionLink = document.getElementById('detailManageSessionLink');


    const providerForm = document.getElementById('providerProjectFilterForm');
    const providerBody = document.getElementById('providerProjectTableBody');
    const providerSummary = document.getElementById('providerProjectSummary');
    const providerCode = 'MOCK_DAMAI';
    const providerState = { pageNo: 1, pageSize: 10, total: 0 };
    const providerPagination = document.getElementById('providerProjectPagination');
    const providerPageSizeSelect = document.getElementById('providerPageSizeSelect');

    async function loadProviderProjects() {
        if (!providerBody) return;
        providerBody.innerHTML = '<tr><td colspan="6"><div class="table-empty">正在读取 MOCK_DAMAI 项目...</div></td></tr>';
        try {
            const params = new URLSearchParams();
            const keyword = document.getElementById('providerKeyword').value.trim();
            const city = document.getElementById('providerCity').value.trim();
            if (keyword) params.set('keyword', keyword);
            if (city) params.set('cityName', city);
            params.set('pageNo', String(providerState.pageNo));
            params.set('pageSize', String(providerState.pageSize));
            const call = await AdminRequest.get('/api/admin/ticket-source-gateway/' + providerCode + '/projects?' + params.toString());
            if (call.success === false) throw new Error(call.message || 'Provider 项目查询失败');
            const page = call.data || {};
            const records = page.records || [];
            providerState.total = Number(page.total || 0);
            providerState.pageNo = Number(page.pageNo || providerState.pageNo);
            providerState.pageSize = Number(page.pageSize || providerState.pageSize);
            if (providerPageSizeSelect) providerPageSizeSelect.value = String(providerState.pageSize);
            const mappingParams = new URLSearchParams();
            mappingParams.set('providerCode', providerCode);
            records.forEach(function (item) {
                if (item.providerProjectId) mappingParams.append('providerProjectId', String(item.providerProjectId));
            });
            const mappings = records.length
                ? await AdminRequest.get('/api/admin/ticket-source-console/mappings/by-projects?' + mappingParams.toString())
                : [];
            const mappingByRemote = {};
            (mappings || []).forEach(function (m) { if (m.providerProjectId) mappingByRemote[String(m.providerProjectId)] = m; });
            providerSummary.textContent = 'Provider 共 ' + AdminUI.formatNumber(providerState.total) + ' 个项目；当前第 ' + providerState.pageNo + ' 页。';
            if (providerPagination) {
                AdminPagination.render(providerPagination, {
                    pageNo: providerState.pageNo,
                    pageSize: providerState.pageSize,
                    total: providerState.total,
                    onChange: function (pageNo) {
                        providerState.pageNo = pageNo;
                        loadProviderProjects();
                    }
                });
            }
            if (!records.length) {
                providerBody.innerHTML = '<tr><td colspan="6"><div class="table-empty">没有符合条件的第三方项目</div></td></tr>';
                return;
            }
            providerBody.innerHTML = records.map(function (item) {
                const remoteId = String(item.providerProjectId || '');
                const mapping = mappingByRemote[remoteId];
                const price = item.minPrice == null ? '-' : (Number(item.minPrice) === Number(item.maxPrice) ? AdminUI.formatMoney(item.minPrice) : AdminUI.formatMoney(item.minPrice) + ' - ' + AdminUI.formatMoney(item.maxPrice));
                const action = mapping && mapping.projectId
                    ? '<a class="action-link" href="/admin/performances/projects/' + mapping.projectId + '/edit">已映射 · 管理</a><button class="action-link" type="button" data-provider-action="sync" data-provider-project="' + AdminUI.escapeHtml(remoteId) + '">重新同步</button>'
                    : '<button class="action-link" type="button" data-provider-action="sync" data-provider-project="' + AdminUI.escapeHtml(remoteId) + '">同步到麦麦</button>';
                return '<tr><td><strong>' + AdminUI.escapeHtml(item.projectName || '-') + '</strong><div class="table-meta">' + AdminUI.escapeHtml(remoteId) + '</div></td>' +
                    '<td>' + AdminUI.escapeHtml(item.cityName || '-') + '<div class="table-meta">' + AdminUI.escapeHtml(item.venueName || '-') + '</div></td>' +
                    '<td>' + AdminUI.statusBadge(item.saleStatus || 'UNKNOWN') + '</td>' +
                    '<td><span class="price">' + price + '</span></td>' +
                    '<td><span class="table-meta">' + AdminUI.formatDateTime(item.updateTime) + '</span></td>' +
                    '<td class="align-right"><div class="action-row action-row-wrap">' + action + '</div></td></tr>';
            }).join('');
        } catch (error) {
            providerBody.innerHTML = '<tr><td colspan="6"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            providerSummary.textContent = 'Provider 项目读取失败';
            AdminUI.toast(error.message, 'error');
        }
    }

    async function syncProviderProject(providerProjectId) {
        const syncMessage = '确认同步 MOCK_DAMAI 项目 ' + providerProjectId + '？\n\n' +
            '会更新：标题、分类、海报、简介/详情、场次、票档、库存、Provider 价格、限购、观演须知和退款规则。\n\n' +
            '不会覆盖：首页排序、首页推荐、麦麦上下架状态、管理员服务标签和管理员项目须知。';
        if (!await AdminUI.confirm(syncMessage, '同步第三方项目')) return;
        try {
            const result = await AdminRequest.post('/api/admin/ticket-source-v11-sync/' + providerCode + '/projects/' + encodeURIComponent(providerProjectId) + '/sync', {syncInventory: true, syncCampaignAssets: false});
            AdminUI.toast('第三方项目同步完成', 'success');
            await Promise.all([loadProviderProjects(), loadProjects()]);
            if (result && result.projectId) window.location.href = '/admin/performances/projects/' + result.projectId + '/edit';
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    function queryString() {
        const params = new URLSearchParams();
        const keyword = document.getElementById('keyword').value.trim();
        const categoryId = document.getElementById('categoryId').value;
        const projectStatus = document.getElementById('projectStatus').value;
        if (keyword) params.set('keyword', keyword);
        if (categoryId) params.set('categoryId', categoryId);
        if (projectStatus) params.set('projectStatus', projectStatus);
        params.set('pageNo', String(state.pageNo));
        params.set('pageSize', String(state.pageSize));
        return params.toString();
    }

    async function loadProjects() {
        if (state.loading) return;
        state.loading = true;
        tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">正在加载项目数据...</div></td></tr>';
        try {
            const page = await AdminRequest.get('/api/admin/performances/projects?' + queryString());
            state.total = Number(page.total || 0);
            state.pageNo = Number(page.pageNo || state.pageNo);
            state.pageSize = Number(page.pageSize || state.pageSize);
            pageSizeSelect.value = String(state.pageSize);
            renderRows(page.items || []);
            totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 个演出项目';
            AdminPagination.render(pagination, {
                pageNo: state.pageNo,
                pageSize: state.pageSize,
                total: state.total,
                onChange: function (pageNo) {
                    state.pageNo = pageNo;
                    loadProjects();
                }
            });
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            totalText.textContent = '读取失败';
            AdminUI.toast(error.message, 'error');
        } finally {
            state.loading = false;
        }
    }

    function posterMarkup(item) {
        const title = item.title || '';
        const firstChar = title ? title.substring(0, 1) : '演';
        const url = String(item.posterUrl || '');
        if (AdminUI.isImageUrl(url)) {
            return '<span class="project-cover"><img src="' + AdminUI.escapeHtml(url) + '" alt="" onerror="this.remove()"><i>' + AdminUI.escapeHtml(firstChar) + '</i></span>';
        }
        return '<span class="project-cover">' + AdminUI.escapeHtml(firstChar) + '</span>';
    }

    function renderRows(items) {
        if (!items.length) {
            tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">没有符合条件的演出项目</div></td></tr>';
            return;
        }
        tableBody.innerHTML = items.map(function (item) {
            const title = AdminUI.escapeHtml(item.title);
            const priceText = item.minPrice == null
                ? '-'
                : (Number(item.minPrice) === Number(item.maxPrice)
                    ? AdminUI.formatMoney(item.minPrice)
                    : AdminUI.formatMoney(item.minPrice) + ' - ' + AdminUI.formatMoney(item.maxPrice));
            const nextStatusButtons = [
                ['ON_SALE', '上架为在售'],
                ['COMING_SOON', '上架为即将开售'],
                ['SOLD_OUT', '标记已售罄'],
                ['OFFLINE', '下架项目']
            ].filter(function (entry) { return entry[0] !== item.projectStatus; }).map(function (entry) {
                return '<button type="button" data-action="status" data-id="' + item.projectId + '" data-status="' + entry[0] + '">' + entry[1] + '</button>';
            }).join('');
            const recommendText = Number(item.recommendFlag) === 1 ? '取消首页推荐' : '设为首页推荐';
            const recommendFlag = Number(item.recommendFlag) === 1 ? 0 : 1;
            const recommendDisabled = item.projectStatus === 'OFFLINE' && recommendFlag === 1;
            return '<tr>' +
                '<td><div class="project-cell">' + posterMarkup(item) + '<div class="project-copy"><strong title="' + title + '">' + title + '</strong><small>ID：' + item.projectId + (Number(item.recommendFlag) === 1 ? ' · 首页推荐' : '') + '</small></div></div></td>' +
                '<td>' + AdminUI.escapeHtml(item.categoryName || '-') + '</td>' +
                '<td><span class="price">' + priceText + '</span></td>' +
                '<td><div class="table-meta">场次 <b>' + AdminUI.formatNumber(item.sessionCount) + '</b> / 在售 <b>' + AdminUI.formatNumber(item.onSaleSessionCount) + '</b><br>可售 <b>' + AdminUI.formatNumber(item.stockAvailableCount) + '</b> / 已售 <b>' + AdminUI.formatNumber(item.soldCount) + '</b></div></td>' +
                '<td><div class="table-meta">热度 <b>' + AdminUI.formatNumber(item.hotScore) + '</b><br>想看 <b>' + AdminUI.formatNumber(item.wantCount) + '</b></div></td>' +
                '<td>' + AdminUI.statusBadge(item.projectStatus) + '</td>' +
                '<td><span class="table-meta">' + AdminUI.formatDateTime(item.updateTime) + '</span></td>' +
                '<td class="align-right"><div class="action-row action-row-wrap">' +
                '<button class="action-link" type="button" data-action="detail" data-id="' + item.projectId + '">详情</button>' +
                '<a class="action-link" href="/admin/performances/projects/' + item.projectId + '/edit">编辑</a>' +
                '<a class="action-link" href="/admin/performances/sessions?projectId=' + item.projectId + '">场次</a>' +
                '<details class="action-menu"><summary class="button button-ghost button-small">更多 ▾</summary><div class="action-menu-popover">' +
                '<button type="button" data-action="recommend" data-id="' + item.projectId + '" data-recommend="' + recommendFlag + '"' + (recommendDisabled ? ' disabled title="已下架项目不能推荐"' : '') + '>' + recommendText + '</button>' +
                nextStatusButtons + '</div></details></div></td>' +
                '</tr>';
        }).join('');
    }

    async function showDetail(projectId) {
        AdminUI.openModal(detailModal);
        detailEditLink.href = '/admin/performances/projects/' + projectId + '/edit';
        detailSessionLink.href = '/admin/performances/sessions?projectId=' + projectId;
        detailContent.innerHTML = '<div class="table-empty">正在读取演出项目详情...</div>';
        try {
            const results = await Promise.all([
                AdminRequest.get('/api/admin/performances/projects/' + projectId),
                AdminRequest.get('/api/admin/performances/projects/' + projectId + '/sessions')
            ]);
            const item = results[0];
            const sessions = results[1] || [];
            detailContent.innerHTML = '<div class="detail-grid">' +
                detailItem('项目 ID', item.projectId) +
                detailItem('项目状态', AdminUI.statusBadge(item.projectStatus), true) +
                detailItem('演出标题', AdminUI.escapeHtml(item.title), true, true) +
                detailItem('演出分类', AdminUI.escapeHtml(item.categoryName || item.categoryId || '-'), true) +
                detailItem('价格区间', item.minPrice == null ? '-' : AdminUI.formatMoney(item.minPrice) + ' - ' + AdminUI.formatMoney(item.maxPrice), true) +
                detailItem('热度 / 想看', AdminUI.formatNumber(item.hotScore) + ' / ' + AdminUI.formatNumber(item.wantCount), true) +
                detailItem('首页推荐', Number(item.recommendFlag) === 1 ? '是' : '否', true) +
                detailItem('发布时间', AdminUI.formatDateTime(item.publishTime), true) +
                detailItem('更新时间', AdminUI.formatDateTime(item.updateTime), true) +
                detailItem('海报地址', AdminUI.escapeHtml(item.posterUrl || '-'), true, true) +
                detailItem('生效服务标签', AdminUI.escapeHtml((item.serviceTags || []).join('、') || '-'), true, true) +
                detailItem('生效观演须知', AdminUI.escapeHtml((item.noticeTitles || []).join('、') || '-'), true, true) +
                detailItem('详情内容', item.detailContent ? '<div class="rich-text-preview">' + AdminRichTextEditor.sanitizePreviewHtml(item.detailContent) + '</div>' : '<span class="table-meta">-</span>', true, true) +
                '</div>' +
                '<section class="detail-section"><h4>场次概览（' + sessions.length + '）</h4>' +
                (sessions.length ? '<div class="mini-list">' + sessions.slice(0, 10).map(function (session) {
                    return '<div class="mini-list-item"><div><strong>' + AdminUI.escapeHtml(session.stationName || session.cityName || '未命名场次') + '</strong><small> ' + AdminUI.escapeHtml(session.venueName || '') + '</small></div><div>' + AdminUI.statusBadge(session.sessionStatus) + '<small> ' + AdminUI.formatDateTime(session.startTime) + '</small></div></div>';
                }).join('') + '</div>' : '<div class="table-empty">该项目暂未创建场次</div>') + '</section>';
        } catch (error) {
            detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
            AdminUI.toast(error.message, 'error');
        }
    }

    function detailItem(label, value, raw, wide) {
        return '<div class="detail-item' + (wide ? ' wide' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + (raw ? value : AdminUI.escapeHtml(value)) + '</strong></div>';
    }

    function closeDetail() { AdminUI.closeModal(detailModal); }

    async function updateStatus(projectId, status) {
        const label = { ON_SALE: '在售', COMING_SOON: '即将开售', SOLD_OUT: '已售罄', OFFLINE: '已下架' }[status] || status;
        const publishTip = status === 'ON_SALE' || status === 'COMING_SOON'
            ? '系统会校验项目下已有可展示场次、可用票档和库存。确认上架为“' + label + '”吗？'
            : '确认将项目状态调整为“' + label + '”吗？';
        const confirmed = await AdminUI.confirm(publishTip, status === 'OFFLINE' ? '下架项目' : '调整项目状态');
        if (!confirmed) return;
        try {
            await AdminRequest.put('/api/admin/performances/projects/' + projectId + '/status', { projectStatus: status });
            AdminUI.toast('项目状态已更新', 'success');
            loadProjects();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function updateRecommend(projectId, recommendFlag) {
        const label = Number(recommendFlag) === 1 ? '设为首页推荐' : '取消首页推荐';
        const confirmed = await AdminUI.confirm('确认' + label + '吗？', '调整推荐状态');
        if (!confirmed) return;
        try {
            await AdminRequest.put('/api/admin/performances/projects/' + projectId + '/status', { recommendFlag: Number(recommendFlag) });
            AdminUI.toast('推荐状态已更新', 'success');
            loadProjects();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadProjects();
    });
    document.getElementById('resetFilterButton').addEventListener('click', function () {
        form.reset();
        state.pageNo = 1;
        loadProjects();
    });
    document.getElementById('refreshProjectButton').addEventListener('click', loadProjects);
    pageSizeSelect.addEventListener('change', function () {
        state.pageSize = Number(pageSizeSelect.value);
        state.pageNo = 1;
        loadProjects();
    });
    tableBody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action]');
        if (!button || button.disabled) return;
        const action = button.dataset.action;
        const projectId = button.dataset.id;
        if (action === 'detail') showDetail(projectId);
        if (action === 'status') updateStatus(projectId, button.dataset.status);
        if (action === 'recommend') updateRecommend(projectId, button.dataset.recommend);
        const details = button.closest('details');
        if (details) details.removeAttribute('open');
    });
    document.getElementById('projectDetailClose').addEventListener('click', closeDetail);
    document.getElementById('projectDetailCloseButton').addEventListener('click', closeDetail);
    detailModal.addEventListener('click', function (event) { if (event.target === detailModal) closeDetail(); });

    if (providerForm) providerForm.addEventListener('submit', function (event) { event.preventDefault(); providerState.pageNo = 1; loadProviderProjects(); });
    const providerRefresh = document.getElementById('refreshProviderProjectsButton');
    if (providerRefresh) providerRefresh.addEventListener('click', loadProviderProjects);
    if (providerPageSizeSelect) providerPageSizeSelect.addEventListener('change', function () { providerState.pageSize = Number(providerPageSizeSelect.value || 10); providerState.pageNo = 1; loadProviderProjects(); });
    if (providerBody) providerBody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-provider-action="sync"]');
        if (button) syncProviderProject(button.dataset.providerProject);
    });

    loadProviderProjects();
    loadProjects();
})();
