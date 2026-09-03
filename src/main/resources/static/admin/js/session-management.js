(function () {
    'use strict';

    const page = document.getElementById('sessionManagementPage');
    if (!page) return;

    const state = {
        projectId: page.dataset.selectedProjectId ? Number(page.dataset.selectedProjectId) : null,
        project: null,
        sessions: [],
        editingSessionId: null,
        loading: false,
        venueLoadTimer: null,
        venueRequestVersion: 0
    };

    const projectSelect = document.getElementById('projectSelect');
    const tableBody = document.getElementById('sessionTableBody');
    const totalText = document.getElementById('sessionTotalText');
    const createButton = document.getElementById('createSessionButton');
    const refreshButton = document.getElementById('refreshSessionButton');
    const sessionModal = document.getElementById('sessionModal');
    const sessionForm = document.getElementById('sessionForm');
    const detailModal = document.getElementById('sessionDetailModal');
    const detailContent = document.getElementById('sessionDetailContent');
    const sessionCityInput = document.getElementById('sessionCityName');
    const stationNameInput = document.getElementById('stationName');
    const venueSelect = document.getElementById('sessionVenueId');
    const venueSelectTip = document.getElementById('venueSelectTip');

    function projectOption(item) {
        const option = document.createElement('option');
        option.value = String(item.projectId);
        option.textContent = (item.title || '未命名项目') + '（ID ' + item.projectId + '）';
        return option;
    }

    async function loadProjectOptions() {
        const params = new URLSearchParams({ pageNo: '1', pageSize: '100' });
        try {
            const result = await AdminRequest.get('/api/admin/performances/projects?' + params.toString());
            const items = result.items || [];
            projectSelect.innerHTML = '<option value="">请选择演出项目</option>';
            items.forEach(function (item) { projectSelect.appendChild(projectOption(item)); });
            if (state.projectId && !items.some(function (item) { return Number(item.projectId) === state.projectId; })) {
                try {
                    const detail = await AdminRequest.get('/api/admin/performances/projects/' + state.projectId);
                    projectSelect.appendChild(projectOption(detail));
                } catch (ignore) {
                    state.projectId = null;
                }
            }
            projectSelect.value = state.projectId ? String(state.projectId) : '';
            if (state.projectId) await selectProject(state.projectId, false);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function selectProject(projectId, updateUrl) {
        state.projectId = projectId ? Number(projectId) : null;
        state.project = null;
        state.sessions = [];
        createButton.disabled = !state.projectId;
        refreshButton.disabled = !state.projectId;
        document.getElementById('selectedProjectCard').hidden = true;
        document.getElementById('editSelectedProjectLink').hidden = true;
        document.getElementById('manageSelectedSkuLink').hidden = true;
        if (updateUrl !== false) {
            const url = state.projectId
                ? '/admin/performances/sessions?projectId=' + state.projectId
                : '/admin/performances/sessions';
            window.history.replaceState({}, '', url);
        }
        if (!state.projectId) {
            totalText.textContent = '请先选择演出项目';
            tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">选择演出项目后显示场次</div></td></tr>';
            return;
        }
        await loadSelectedProject();
        await loadSessions();
    }

    async function loadSelectedProject() {
        try {
            state.project = await AdminRequest.get('/api/admin/performances/projects/' + state.projectId);
            const card = document.getElementById('selectedProjectCard');
            card.hidden = false;
            document.getElementById('selectedProjectTitle').textContent = state.project.title || '-';
            document.getElementById('selectedProjectMeta').innerHTML =
                AdminUI.statusBadge(state.project.projectStatus) +
                '<span>' + AdminUI.escapeHtml(state.project.categoryName || '-') + '</span>' +
                '<span>项目 ID：' + state.project.projectId + '</span>' +
                (state.project.sourceManaged ? '<span>Provider：' + AdminUI.escapeHtml(state.project.providerCode || '-') + ' · 场次由同步维护</span>' : '');
            createButton.disabled = Boolean(state.project.sourceManaged);
            const poster = document.getElementById('selectedProjectPoster');
            const posterUrl = String(state.project.posterUrl || '');
            if (AdminUI.isImageUrl(posterUrl)) {
                poster.innerHTML = '<img src="' + AdminUI.escapeHtml(posterUrl) + '" alt="" onerror="this.remove()">';
            } else {
                poster.textContent = (state.project.title || '演').substring(0, 1);
            }
            const editLink = document.getElementById('editSelectedProjectLink');
            editLink.href = '/admin/performances/projects/' + state.projectId + '/edit';
            editLink.hidden = false;
            const skuLink = document.getElementById('manageSelectedSkuLink');
            skuLink.href = '/admin/performances/skus?projectId=' + state.projectId;
            skuLink.hidden = false;
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function loadSessions() {
        if (!state.projectId || state.loading) return;
        state.loading = true;
        tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">正在加载场次数据...</div></td></tr>';
        try {
            state.sessions = await AdminRequest.get('/api/admin/performances/projects/' + state.projectId + '/sessions') || [];
            renderSessions();
            updateContextStats();
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            totalText.textContent = '读取失败';
            AdminUI.toast(error.message, 'error');
        } finally {
            state.loading = false;
        }
    }

    function filteredSessions() {
        const status = document.getElementById('sessionStatusFilter').value;
        return state.sessions.filter(function (item) {
            return !status || item.sessionStatus === status;
        });
    }

    function priceText(item) {
        if (item.minPrice == null) return '暂无票档';
        return Number(item.minPrice) === Number(item.maxPrice)
            ? AdminUI.formatMoney(item.minPrice)
            : AdminUI.formatMoney(item.minPrice) + ' - ' + AdminUI.formatMoney(item.maxPrice);
    }

    function renderSessions() {
        const items = filteredSessions();
        totalText.textContent = '共 ' + state.sessions.length + ' 个场次，当前显示 ' + items.length + ' 个';
        if (!items.length) {
            tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">' + (state.sessions.length ? '没有符合筛选条件的场次' : '该项目暂未创建场次') + '</div></td></tr>';
            return;
        }
        tableBody.innerHTML = items.map(function (item) {
            const sourceManaged = item.sourceManaged === true;
            const nextStatuses = sourceManaged ? '' : ['PRESALE', 'ON_SALE', 'SOLD_OUT', 'ENDED', 'OFFLINE']
                .filter(function (status) { return status !== item.sessionStatus; })
                .map(function (status) {
                    const label = { PRESALE: '上架为预售', ON_SALE: '上架为在售', SOLD_OUT: '标记已售罄', ENDED: '标记已结束', OFFLINE: '下架场次' }[status];
                    return '<button type="button" data-action="status" data-id="' + item.sessionId + '" data-status="' + status + '">' + label + '</button>';
                }).join('');
            return '<tr>' +
                '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.stationName || '-') + '</strong><small>' + AdminUI.escapeHtml(item.cityName || '-') + ' · ' + AdminUI.escapeHtml(item.venueName || '-') + '</small><small>ID：' + item.sessionId + '</small></div></td>' +
                '<td><div class="table-meta"><b>' + AdminUI.formatDateTime(item.startTime) + '</b><br>结束：' + AdminUI.formatDateTime(item.endTime) + '</div></td>' +
                '<td><div class="table-meta">开售：' + AdminUI.formatDateTime(item.saleStartTime) + '<br>停售：' + AdminUI.formatDateTime(item.saleEndTime) + '</div></td>' +
                '<td><div class="table-meta">票档 <b>' + AdminUI.formatNumber(item.skuCount) + '</b> · ' + priceText(item) + '<br>库存 <b>' + AdminUI.formatNumber(item.stockAvailableCount) + '</b> / 已售 <b>' + AdminUI.formatNumber(item.soldCount) + '</b></div></td>' +
                '<td><div class="table-meta"><b>' + ({ETICKET:'电子票',PAPER_TICKET:'纸质票',MIXED:'电子票 + 纸质票'}[item.deliveryType] || item.deliveryType || '-') + '</b><br>每单限购 ' + AdminUI.formatNumber(item.limitPerOrder) + ' 张</div></td>' +
                '<td>' + AdminUI.statusBadge(item.sessionStatus) + '</td>' +
                '<td class="align-right"><div class="action-row action-row-wrap">' +
                '<button class="action-link" type="button" data-action="detail" data-id="' + item.sessionId + '">详情</button>' +
                (sourceManaged ? '<span class="table-meta">Provider 同步维护</span>' : '<button class="action-link" type="button" data-action="edit" data-id="' + item.sessionId + '">编辑</button>') +
                '<a class="action-link" href="/admin/performances/skus?projectId=' + state.projectId + '&sessionId=' + item.sessionId + '">票档</a>' +
                (sourceManaged ? '' : '<details class="action-menu"><summary class="button button-ghost button-small">更多 ▾</summary><div class="action-menu-popover">' + nextStatuses + '<button class="danger-menu-item" type="button" data-action="delete" data-id="' + item.sessionId + '">删除场次</button></div></details>') +
                '</div></td></tr>';
        }).join('');
    }

    function updateContextStats() {
        document.getElementById('contextSessionCount').textContent = AdminUI.formatNumber(state.sessions.length);
        const available = state.sessions.reduce(function (sum, item) { return sum + Number(item.stockAvailableCount || 0); }, 0);
        const sold = state.sessions.reduce(function (sum, item) { return sum + Number(item.soldCount || 0); }, 0);
        document.getElementById('contextStockCount').textContent = AdminUI.formatNumber(available);
        document.getElementById('contextSoldCount').textContent = AdminUI.formatNumber(sold);
    }

    function normalizeCityName(value) {
        const city = String(value || '').trim();
        return city.length > 1 && city.endsWith('市') ? city.substring(0, city.length - 1) : city;
    }

    function appendVenueOption(item, prefix) {
        const option = document.createElement('option');
        option.value = String(item.venueId);
        option.dataset.cityName = item.cityName || '';
        option.dataset.invalidCity = 'false';
        option.textContent = (prefix || '') + item.venueName + '｜' + item.cityName + '｜' + item.address;
        venueSelect.appendChild(option);
        return option;
    }

    function resetVenueOptions(message) {
        state.venueRequestVersion += 1;
        venueSelect.disabled = true;
        venueSelect.innerHTML = '<option value="">' + AdminUI.escapeHtml(message) + '</option>';
        venueSelectTip.textContent = message;
    }

    async function loadVenues(cityName, stationName, selectedVenueId) {
        const city = String(cityName || '').trim();
        const station = String(stationName || '').trim();
        if (!city && !station) {
            resetVenueOptions('请先填写城市名称或站点名称');
            return;
        }

        const requestVersion = ++state.venueRequestVersion;
        venueSelect.disabled = true;
        venueSelect.innerHTML = '<option value="">正在匹配同城场馆...</option>';
        venueSelectTip.textContent = '正在根据城市名称和站点名称匹配场馆';
        const params = new URLSearchParams({ limit: '50' });
        if (city) params.set('cityName', city);
        if (station) params.set('stationName', station);

        try {
            const result = await AdminRequest.get('/api/admin/venues/options?' + params.toString());
            if (requestVersion !== state.venueRequestVersion) return;
            const items = result.items || [];
            const resolvedCity = result.resolvedCityName || '';
            venueSelect.innerHTML = '<option value="">请选择场馆</option>';
            items.forEach(function (item) { appendVenueOption(item, ''); });

            let currentVenueInvalid = false;
            if (selectedVenueId && !items.some(function (item) {
                return Number(item.venueId) === Number(selectedVenueId);
            })) {
                try {
                    const venue = await AdminRequest.get('/api/admin/venues/' + selectedVenueId);
                    if (requestVersion !== state.venueRequestVersion) return;
                    const option = appendVenueOption(venue, '⚠ 当前场馆：');
                    currentVenueInvalid = Boolean(resolvedCity)
                        && normalizeCityName(venue.cityName) !== normalizeCityName(resolvedCity);
                    option.dataset.invalidCity = currentVenueInvalid ? 'true' : 'false';
                    if (currentVenueInvalid) {
                        option.textContent = '⚠ 当前场馆城市不一致：' + venue.venueName + '｜' + venue.cityName + '｜' + venue.address;
                    }
                } catch (ignore) {
                    currentVenueInvalid = true;
                }
            }

            if (resolvedCity && !city) {
                sessionCityInput.value = resolvedCity;
            }
            venueSelect.value = selectedVenueId ? String(selectedVenueId) : '';
            venueSelect.disabled = items.length === 0 && !selectedVenueId;

            if (currentVenueInvalid) {
                venueSelectTip.textContent = '当前场次的城市与场馆所属城市不一致，请重新选择“' + resolvedCity + '”的场馆后保存';
            } else if (items.length) {
                const sourceText = result.inferredFromStation ? '站点名称推断' : '城市名称匹配';
                venueSelectTip.textContent = '已通过' + sourceText + '到“' + resolvedCity + '”，共加载 ' + items.length + ' 个同城场馆';
            } else if (resolvedCity) {
                venueSelectTip.textContent = '“' + resolvedCity + '”暂无场馆，请先到场馆管理中新增';
            } else {
                venueSelectTip.textContent = '无法根据当前城市或站点识别场馆所属城市';
            }
        } catch (error) {
            if (requestVersion !== state.venueRequestVersion) return;
            venueSelect.innerHTML = '<option value="">场馆匹配失败</option>';
            venueSelect.disabled = true;
            venueSelectTip.textContent = error.message;
            AdminUI.toast(error.message, 'error');
        }
    }

    function scheduleVenueLoad() {
        window.clearTimeout(state.venueLoadTimer);
        venueSelect.value = '';
        venueSelect.disabled = true;
        venueSelect.innerHTML = '<option value="">等待匹配同城场馆</option>';
        venueSelectTip.textContent = '停止输入后将自动匹配场馆';
        state.venueLoadTimer = window.setTimeout(function () {
            loadVenues(sessionCityInput.value, stationNameInput.value, null);
        }, 400);
    }

    function resetSessionForm() {
        sessionForm.reset();
        AdminRichTextEditor.set('stationDetailContent', '');
        state.editingSessionId = null;
        document.getElementById('sessionModalTitle').textContent = '新建演出场次';
        document.getElementById('sessionCurrentStatusText').textContent = '新建场次保存后默认为已下架。请先完善场次并配置票档，再在场次列表中单独上架。';
        document.getElementById('deliveryType').value = 'ETICKET';
        document.getElementById('limitPerOrder').value = '4';
        document.getElementById('issueOffsetHours').value = '24';
        window.clearTimeout(state.venueLoadTimer);
        state.venueRequestVersion += 1;
        venueSelect.disabled = true;
        venueSelect.innerHTML = '<option value="">请先填写城市名称或站点名称</option>';
        venueSelectTip.textContent = '填写城市或“北京站”这类站点名称后自动加载同城场馆，最多 50 个';
    }

    function openCreateModal() {
        if (!state.projectId) return;
        resetSessionForm();
        AdminUI.openModal(sessionModal);
    }

    async function openEditModal(sessionId) {
        resetSessionForm();
        state.editingSessionId = Number(sessionId);
        document.getElementById('sessionModalTitle').textContent = '编辑演出场次';
        AdminUI.openModal(sessionModal);
        try {
            const item = await AdminRequest.get('/api/admin/performances/sessions/' + sessionId);
            document.getElementById('sessionCityName').value = item.cityName || '';
            document.getElementById('stationName').value = item.stationName || '';
            document.getElementById('sessionStartTime').value = AdminUI.toDateTimeLocal(item.startTime);
            document.getElementById('sessionEndTime').value = AdminUI.toDateTimeLocal(item.endTime);
            document.getElementById('saleStartTime').value = AdminUI.toDateTimeLocal(item.saleStartTime);
            document.getElementById('saleEndTime').value = AdminUI.toDateTimeLocal(item.saleEndTime);
            document.getElementById('issueOffsetHours').value = item.issueOffsetHours == null ? '' : item.issueOffsetHours;
            document.getElementById('sessionCurrentStatusText').textContent = '当前状态：' + ({ PRESALE: '预售', ON_SALE: '在售', SOLD_OUT: '已售罄', ENDED: '已结束', OFFLINE: '已下架' }[item.sessionStatus] || item.sessionStatus || '未知') + '。本页面只修改基础信息，发布状态请在列表中单独调整。';
            document.getElementById('limitPerOrder').value = item.limitPerOrder == null ? '4' : item.limitPerOrder;
            document.getElementById('deliveryType').value = item.deliveryType || 'ETICKET';
            AdminRichTextEditor.set('stationDetailContent', item.stationDetailContent || '');
            await loadVenues(item.cityName, item.stationName, item.venueId);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
            AdminUI.closeModal(sessionModal);
        }
    }

    function buildSessionRequest() {
        return {
            cityName: document.getElementById('sessionCityName').value.trim(),
            stationName: document.getElementById('stationName').value.trim(),
            venueId: Number(document.getElementById('sessionVenueId').value),
            startTime: AdminUI.nullableText(document.getElementById('sessionStartTime').value),
            endTime: AdminUI.nullableText(document.getElementById('sessionEndTime').value),
            saleStartTime: AdminUI.nullableText(document.getElementById('saleStartTime').value),
            saleEndTime: AdminUI.nullableText(document.getElementById('saleEndTime').value),
            issueOffsetHours: AdminUI.nullableNumber(document.getElementById('issueOffsetHours').value),
            limitPerOrder: Number(document.getElementById('limitPerOrder').value),
            stationDetailContent: AdminUI.nullableText(AdminRichTextEditor.get('stationDetailContent')),
            deliveryType: document.getElementById('deliveryType').value
        };
    }

    async function saveSession(event) {
        event.preventDefault();
        if (!sessionForm.reportValidity()) return;
        const request = buildSessionRequest();
        if (!request.venueId) {
            AdminUI.toast('请选择演出场馆', 'warning');
            return;
        }
        const selectedOption = venueSelect.selectedOptions[0];
        const venueCity = selectedOption ? String(selectedOption.dataset.cityName || '').trim() : '';
        if (selectedOption && selectedOption.dataset.invalidCity === 'true') {
            AdminUI.toast('当前场馆与场次城市不一致，请重新选择同城场馆', 'warning');
            return;
        }
        if (venueCity && normalizeCityName(request.cityName) !== normalizeCityName(venueCity)) {
            AdminUI.toast('场次城市必须与场馆所属城市一致，请重新匹配场馆', 'warning');
            await loadVenues(request.cityName, request.stationName, null);
            return;
        }
        if (venueCity) {
            request.cityName = venueCity;
            sessionCityInput.value = venueCity;
        }
        const button = document.getElementById('saveSessionButton');
        AdminUI.setButtonLoading(button, true, '正在保存...');
        try {
            if (state.editingSessionId) {
                await AdminRequest.put('/api/admin/performances/sessions/' + state.editingSessionId, request);
                AdminUI.toast('场次已更新', 'success');
            } else {
                await AdminRequest.post('/api/admin/performances/projects/' + state.projectId + '/sessions', request);
                AdminUI.toast('场次已创建并保持下架，请配置票档后再上架', 'success');
            }
            AdminUI.closeModal(sessionModal);
            await loadSessions();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(button, false);
        }
    }

    async function updateStatus(sessionId, status) {
        const label = { PRESALE: '预售', ON_SALE: '在售', SOLD_OUT: '已售罄', ENDED: '已结束', OFFLINE: '已下架' }[status] || status;
        const publishTip = status === 'PRESALE' || status === 'ON_SALE'
            ? '系统会校验该场次已配置可用票档和库存。确认上架为“' + label + '”吗？'
            : '确认将场次状态调整为“' + label + '”吗？';
        if (!await AdminUI.confirm(publishTip, status === 'OFFLINE' ? '下架场次' : '调整场次状态')) return;
        try {
            await AdminRequest.put('/api/admin/performances/sessions/' + sessionId + '/status', { sessionStatus: status });
            AdminUI.toast('场次状态已更新', 'success');
            await loadSessions();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function deleteSession(sessionId) {
        if (!await AdminUI.confirm('删除场次不可恢复。仅无订单且已删除全部票档的场次可以删除。', '删除演出场次')) return;
        try {
            await AdminRequest.delete('/api/admin/performances/sessions/' + sessionId);
            AdminUI.toast('场次已删除', 'success');
            await loadSessions();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function showDetail(sessionId) {
        AdminUI.openModal(detailModal);
        detailContent.innerHTML = '<div class="table-empty">正在读取场次详情...</div>';
        try {
            const item = await AdminRequest.get('/api/admin/performances/sessions/' + sessionId);
            detailContent.innerHTML = '<div class="detail-grid">' +
                detailItem('场次 ID', item.sessionId) +
                detailItem('状态', AdminUI.statusBadge(item.sessionStatus), true) +
                detailItem('城市站', AdminUI.escapeHtml((item.cityName || '-') + ' / ' + (item.stationName || '-')), true) +
                detailItem('演出场馆', AdminUI.escapeHtml(item.venueName || '-'), true) +
                detailItem('演出时间', AdminUI.formatDateTime(item.startTime) + ' 至 ' + AdminUI.formatDateTime(item.endTime), true, true) +
                detailItem('售卖时间', AdminUI.formatDateTime(item.saleStartTime) + ' 至 ' + AdminUI.formatDateTime(item.saleEndTime), true, true) +
                detailItem('场次票型聚合', ({ETICKET:'电子票',PAPER_TICKET:'纸质票',MIXED:'电子票 + 纸质票'}[item.deliveryType] || item.deliveryType || '-'), true) +
                detailItem('数据来源', item.sourceManaged ? ('Provider：'+(item.providerCode||'-')+' / '+(item.providerSessionId||'-')) : '麦麦本地', true) +
                detailItem('提前出票', item.issueOffsetHours == null ? '-' : item.issueOffsetHours + ' 小时', true) +
                detailItem('限购', '每单 ' + AdminUI.formatNumber(item.limitPerOrder) + ' 张', true) +
                detailItem('价格区间', priceText(item), true) +
                detailItem('票务资源', '票档 ' + AdminUI.formatNumber(item.skuCount) + ' / 库存 ' + AdminUI.formatNumber(item.stockAvailableCount) + ' / 已售 ' + AdminUI.formatNumber(item.soldCount), true, true) +
                detailItem('城市站详情', item.stationDetailContent ? '<div class="rich-text-preview">' + AdminRichTextEditor.sanitizePreviewHtml(item.stationDetailContent) + '</div>' : '<span class="table-meta">-</span>', true, true) +
                detailItem('更新时间', AdminUI.formatDateTime(item.updateTime), true, true) +
                '</div>';
        } catch (error) {
            detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
        }
    }

    function detailItem(label, value, raw, wide) {
        return '<div class="detail-item' + (wide ? ' wide' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + (raw ? value : AdminUI.escapeHtml(value)) + '</strong></div>';
    }

    function closeSessionModal() { AdminUI.closeModal(sessionModal); }
    function closeDetailModal() { AdminUI.closeModal(detailModal); }

    projectSelect.addEventListener('change', function () { selectProject(projectSelect.value, true); });
    createButton.addEventListener('click', openCreateModal);
    refreshButton.addEventListener('click', loadSessions);
    document.getElementById('sessionFilterForm').addEventListener('submit', function (event) { event.preventDefault(); renderSessions(); });
    document.getElementById('resetSessionFilterButton').addEventListener('click', function () {
        document.getElementById('sessionFilterForm').reset();
        renderSessions();
    });
    document.getElementById('loadVenueButton').addEventListener('click', function () {
        window.clearTimeout(state.venueLoadTimer);
        loadVenues(sessionCityInput.value, stationNameInput.value, null);
    });
    sessionCityInput.addEventListener('input', scheduleVenueLoad);
    stationNameInput.addEventListener('input', scheduleVenueLoad);
    venueSelect.addEventListener('change', function () {
        const option = venueSelect.selectedOptions[0];
        if (!option || !option.value) return;
        const venueCity = String(option.dataset.cityName || '').trim();
        if (venueCity) sessionCityInput.value = venueCity;
        if (option.dataset.invalidCity === 'true') {
            venueSelectTip.textContent = '该场馆与当前场次城市不一致，不能保存，请选择同城场馆';
        }
    });
    sessionForm.addEventListener('submit', saveSession);
    document.getElementById('sessionModalClose').addEventListener('click', closeSessionModal);
    document.getElementById('sessionModalCancel').addEventListener('click', closeSessionModal);
    sessionModal.addEventListener('click', function (event) { if (event.target === sessionModal) closeSessionModal(); });
    document.getElementById('sessionDetailClose').addEventListener('click', closeDetailModal);
    document.getElementById('sessionDetailCloseButton').addEventListener('click', closeDetailModal);
    detailModal.addEventListener('click', function (event) { if (event.target === detailModal) closeDetailModal(); });
    tableBody.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        const action = target.dataset.action;
        const id = target.dataset.id;
        if (action === 'detail') showDetail(id);
        if (action === 'edit') openEditModal(id);
        if (action === 'status') updateStatus(id, target.dataset.status);
        if (action === 'delete') deleteSession(id);
        const details = target.closest('details');
        if (details) details.removeAttribute('open');
    });

    loadProjectOptions('');
})();
