(function () {
    'use strict';

    const state = { pageNo: 1, pageSize: 10, total: 0, editingVenueId: null, loading: false };
    const form = document.getElementById('venueFilterForm');
    const tableBody = document.getElementById('venueTableBody');
    const totalText = document.getElementById('venueTotalText');
    const pagination = document.getElementById('venuePagination');
    const pageSizeSelect = document.getElementById('venuePageSizeSelect');
    const venueModal = document.getElementById('venueModal');
    const venueForm = document.getElementById('venueForm');
    const detailModal = document.getElementById('venueDetailModal');
    const detailContent = document.getElementById('venueDetailContent');

    function buildQuery() {
        const params = new URLSearchParams();
        const keyword = document.getElementById('venueKeyword').value.trim();
        const cityName = document.getElementById('venueCityFilter').value.trim();
        if (keyword) params.set('keyword', keyword);
        if (cityName) params.set('cityName', cityName);
        params.set('pageNo', String(state.pageNo));
        params.set('pageSize', String(state.pageSize));
        return params.toString();
    }

    async function loadVenues() {
        if (state.loading) return;
        state.loading = true;
        tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">正在加载场馆数据...</div></td></tr>';
        try {
            const page = await AdminRequest.get('/api/admin/venues?' + buildQuery());
            state.total = Number(page.total || 0);
            state.pageNo = Number(page.pageNo || state.pageNo);
            state.pageSize = Number(page.pageSize || state.pageSize);
            pageSizeSelect.value = String(state.pageSize);
            renderRows(page.items || []);
            totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 个场馆';
            AdminPagination.render(pagination, {
                pageNo: state.pageNo,
                pageSize: state.pageSize,
                total: state.total,
                onChange: function (pageNo) {
                    state.pageNo = pageNo;
                    loadVenues();
                }
            });
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            totalText.textContent = '读取失败';
            AdminUI.toast(error.message, 'error');
        } finally {
            state.loading = false;
        }
    }

    function renderRows(items) {
        if (!items.length) {
            tableBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">没有符合条件的场馆</div></td></tr>';
            return;
        }
        tableBody.innerHTML = items.map(function (item) {
            const coordinate = item.longitude == null && item.latitude == null
                ? '-'
                : AdminUI.escapeHtml((item.longitude == null ? '-' : item.longitude) + ', ' + (item.latitude == null ? '-' : item.latitude));
            return '<tr>' +
                '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.venueName || '-') + '</strong><small>ID：' + item.venueId + '</small></div></td>' +
                '<td><span class="city-chip">' + AdminUI.escapeHtml(item.cityName || '-') + '</span></td>' +
                '<td><div class="address-cell" title="' + AdminUI.escapeHtml(item.address || '') + '">' + AdminUI.escapeHtml(item.address || '-') + '</div></td>' +
                '<td><span class="table-meta">' + coordinate + '</span></td>' +
                '<td><strong>' + AdminUI.formatNumber(item.sessionCount) + '</strong> 场</td>' +
                '<td><span class="table-meta">' + AdminUI.formatDateTime(item.updateTime) + '</span></td>' +
                '<td class="align-right"><div class="action-row">' +
                '<button class="action-link" type="button" data-action="detail" data-id="' + item.venueId + '">详情</button>' +
                '<button class="action-link" type="button" data-action="edit" data-id="' + item.venueId + '">编辑</button>' +
                '<button class="action-link danger" type="button" data-action="delete" data-id="' + item.venueId + '">删除</button>' +
                '</div></td></tr>';
        }).join('');
    }

    function resetVenueForm() {
        venueForm.reset();
        state.editingVenueId = null;
        document.getElementById('venueModalTitle').textContent = '新建场馆';
        document.getElementById('venueLockedTip').hidden = true;
        document.getElementById('venueCityName').disabled = false;
    }

    function openCreateModal() {
        resetVenueForm();
        AdminUI.openModal(venueModal);
    }

    async function openEditModal(venueId) {
        resetVenueForm();
        state.editingVenueId = Number(venueId);
        document.getElementById('venueModalTitle').textContent = '编辑场馆';
        AdminUI.openModal(venueModal);
        try {
            const item = await AdminRequest.get('/api/admin/venues/' + venueId);
            document.getElementById('venueName').value = item.venueName || '';
            document.getElementById('venueCityName').value = item.cityName || '';
            document.getElementById('venueAddress').value = item.address || '';
            document.getElementById('venueLongitude').value = item.longitude == null ? '' : item.longitude;
            document.getElementById('venueLatitude').value = item.latitude == null ? '' : item.latitude;
            if (Number(item.sessionCount || 0) > 0) {
                document.getElementById('venueLockedTip').hidden = false;
                document.getElementById('venueCityName').disabled = true;
                document.getElementById('venueCityName').dataset.lockedValue = item.cityName || '';
            }
        } catch (error) {
            AdminUI.toast(error.message, 'error');
            AdminUI.closeModal(venueModal);
        }
    }

    function buildRequest() {
        const cityInput = document.getElementById('venueCityName');
        return {
            venueName: document.getElementById('venueName').value.trim(),
            cityName: cityInput.disabled ? cityInput.dataset.lockedValue : cityInput.value.trim(),
            address: document.getElementById('venueAddress').value.trim(),
            longitude: AdminUI.nullableNumber(document.getElementById('venueLongitude').value),
            latitude: AdminUI.nullableNumber(document.getElementById('venueLatitude').value)
        };
    }

    async function saveVenue(event) {
        event.preventDefault();
        const cityInput = document.getElementById('venueCityName');
        const wasDisabled = cityInput.disabled;
        if (wasDisabled) cityInput.disabled = false;
        const valid = venueForm.reportValidity();
        if (wasDisabled) cityInput.disabled = true;
        if (!valid) return;
        const button = document.getElementById('saveVenueButton');
        AdminUI.setButtonLoading(button, true, '正在保存...');
        try {
            const request = buildRequest();
            if (state.editingVenueId) {
                await AdminRequest.put('/api/admin/venues/' + state.editingVenueId, request);
                AdminUI.toast('场馆已更新', 'success');
            } else {
                await AdminRequest.post('/api/admin/venues', request);
                AdminUI.toast('场馆已创建', 'success');
            }
            AdminUI.closeModal(venueModal);
            await loadVenues();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(button, false);
        }
    }

    async function deleteVenue(venueId) {
        if (!await AdminUI.confirm('删除场馆不可恢复。已关联演出场次的场馆不能删除。', '删除场馆')) return;
        try {
            await AdminRequest.delete('/api/admin/venues/' + venueId);
            AdminUI.toast('场馆已删除', 'success');
            if (state.pageNo > 1 && state.total % state.pageSize === 1) state.pageNo -= 1;
            await loadVenues();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function showDetail(venueId) {
        AdminUI.openModal(detailModal);
        detailContent.innerHTML = '<div class="table-empty">正在读取场馆详情...</div>';
        try {
            const item = await AdminRequest.get('/api/admin/venues/' + venueId);
            const coordinate = item.longitude == null && item.latitude == null
                ? '-'
                : (item.longitude == null ? '-' : item.longitude) + ', ' + (item.latitude == null ? '-' : item.latitude);
            detailContent.innerHTML = '<div class="detail-grid">' +
                detailItem('场馆 ID', item.venueId) +
                detailItem('所属城市', item.cityName) +
                detailItem('场馆名称', item.venueName, false, true) +
                detailItem('详细地址', item.address, false, true) +
                detailItem('经纬度', coordinate) +
                detailItem('关联场次', AdminUI.formatNumber(item.sessionCount) + ' 场') +
                detailItem('创建时间', AdminUI.formatDateTime(item.createTime)) +
                detailItem('更新时间', AdminUI.formatDateTime(item.updateTime)) +
                '</div>';
        } catch (error) {
            detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
        }
    }

    function detailItem(label, value, raw, wide) {
        return '<div class="detail-item' + (wide ? ' wide' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + (raw ? value : AdminUI.escapeHtml(value == null ? '-' : value)) + '</strong></div>';
    }

    function closeVenueModal() { AdminUI.closeModal(venueModal); }
    function closeDetailModal() { AdminUI.closeModal(detailModal); }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadVenues();
    });
    document.getElementById('resetVenueFilterButton').addEventListener('click', function () {
        form.reset();
        state.pageNo = 1;
        loadVenues();
    });
    document.getElementById('refreshVenueButton').addEventListener('click', loadVenues);
    pageSizeSelect.addEventListener('change', function () {
        state.pageSize = Number(pageSizeSelect.value);
        state.pageNo = 1;
        loadVenues();
    });
    document.getElementById('createVenueButton').addEventListener('click', openCreateModal);
    venueForm.addEventListener('submit', saveVenue);
    document.getElementById('venueModalClose').addEventListener('click', closeVenueModal);
    document.getElementById('venueModalCancel').addEventListener('click', closeVenueModal);
    venueModal.addEventListener('click', function (event) { if (event.target === venueModal) closeVenueModal(); });
    document.getElementById('venueDetailClose').addEventListener('click', closeDetailModal);
    document.getElementById('venueDetailCloseButton').addEventListener('click', closeDetailModal);
    detailModal.addEventListener('click', function (event) { if (event.target === detailModal) closeDetailModal(); });
    tableBody.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        if (target.dataset.action === 'detail') showDetail(target.dataset.id);
        if (target.dataset.action === 'edit') openEditModal(target.dataset.id);
        if (target.dataset.action === 'delete') deleteVenue(target.dataset.id);
    });

    loadVenues();
})();
