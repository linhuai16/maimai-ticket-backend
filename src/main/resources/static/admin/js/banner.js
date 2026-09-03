(function () {
    'use strict';

    const listBody = document.getElementById('bannerTableBody');
    const formPage = document.getElementById('bannerFormPage');
    if (listBody) initList();
    if (formPage) initForm();

    function usableImage(value) {
        return AdminUI.isImageUrl(value);
    }

    function renderImage(value, title) {
        const safeValue = String(value || '').trim();
        if (usableImage(safeValue)) {
            return '<span class="resource-image resource-image-wide"><img src="' + AdminUI.escapeHtml(safeValue) + '" alt="' + AdminUI.escapeHtml(title || 'Banner') + '" onerror="this.remove()"><i>B</i></span>';
        }
        return '<span class="resource-image resource-image-wide"><i>B</i></span>';
    }

    function effectiveBadge(value) {
        const map = {
            EFFECTIVE: ['生效中', 'success'],
            NOT_STARTED: ['未开始', 'info'],
            EXPIRED: ['已过期', 'warning'],
            DISABLED: ['已禁用', 'danger'],
            TARGET_PROJECT_MISSING: ['目标项目缺失', 'danger'],
            TARGET_SESSION_MISSING: ['目标场次缺失或错配', 'danger'],
            TARGET_PROJECT_OFFLINE: ['目标项目已下架', 'warning'],
            TARGET_SESSION_OFFLINE: ['目标场次已下架', 'warning']
        };
        const item = map[value] || [value || '-', ''];
        return '<span class="status-badge ' + item[1] + '">' + AdminUI.escapeHtml(item[0]) + '</span>';
    }

    function initList() {
        const state = { pageNo: 1, pageSize: 10, total: 0 };
        const filterForm = document.getElementById('bannerFilterForm');
        const keyword = document.getElementById('bannerKeyword');
        const enableStatus = document.getElementById('bannerEnableStatus');
        const totalText = document.getElementById('bannerTotalText');
        const pageSize = document.getElementById('bannerPageSize');
        const pagination = document.getElementById('bannerPagination');

        async function load() {
            listBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">正在加载 Banner 数据...</div></td></tr>';
            const params = new URLSearchParams({ pageNo: state.pageNo, pageSize: state.pageSize });
            if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
            if (enableStatus.value) params.set('enableStatus', enableStatus.value);
            try {
                const data = await AdminRequest.get('/api/admin/banners?' + params.toString());
                state.total = Number(data.total || 0);
                state.pageNo = Number(data.pageNo || state.pageNo);
                state.pageSize = Number(data.pageSize || state.pageSize);
                renderRows(data.items || []);
                totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 条 Banner';
                AdminPagination.render(pagination, {
                    pageNo: state.pageNo,
                    pageSize: state.pageSize,
                    total: state.total,
                    onChange: function (pageNo) {
                        state.pageNo = pageNo;
                        load();
                    }
                });
            } catch (error) {
                listBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
                totalText.textContent = '读取失败';
                AdminUI.toast(error.message, 'error');
            }
        }

        function renderRows(items) {
            if (!items.length) {
                listBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">没有符合条件的 Banner</div></td></tr>';
                return;
            }
            listBody.innerHTML = items.map(function (item) {
                const sessionLine = item.targetSessionId
                    ? [item.targetSessionCityName, item.targetSessionStationName, AdminUI.formatDateTime(item.targetSessionStartTime), item.targetSessionStatus].filter(Boolean).join(' · ')
                    : '未绑定具体场次';
                const projectStatusLine = item.targetProjectStatus ? '项目状态：' + item.targetProjectStatus : '目标项目不存在';
                const nextStatus = item.enableStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED';
                const nextStatusText = nextStatus === 'ENABLED' ? '启用' : '禁用';
                return '<tr>' +
                    '<td><div class="operation-resource-cell">' + renderImage(item.imageUrl, item.bannerTitle) + '<div><strong>' + AdminUI.escapeHtml(item.bannerTitle || '-') + '</strong><small>ID：' + AdminUI.escapeHtml(item.bannerId) + '</small><small title="' + AdminUI.escapeHtml(item.imageUrl || '') + '">' + AdminUI.escapeHtml(item.imageUrl || '-') + '</small></div></div></td>' +
                    '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.targetProjectTitle || '-') + '</strong><small>项目 ID：' + AdminUI.escapeHtml(item.targetProjectId || '-') + ' · ' + AdminUI.escapeHtml(projectStatusLine) + '</small><small>' + AdminUI.escapeHtml(sessionLine) + '</small></div></td>' +
                    '<td><div class="table-meta"><b>' + AdminUI.formatDateTime(item.startTime) + '</b><br>至 ' + AdminUI.formatDateTime(item.endTime) + '</div></td>' +
                    '<td><strong>' + AdminUI.escapeHtml(item.sortOrder == null ? '-' : item.sortOrder) + '</strong></td>' +
                    '<td>' + AdminUI.statusBadge(item.enableStatus) + '</td>' +
                    '<td>' + effectiveBadge(item.effectiveStatus) + '</td>' +
                    '<td class="align-right"><div class="action-row action-row-wrap"><a class="action-link" href="/admin/operation/banners/' + item.bannerId + '/edit">编辑</a><button class="action-link" type="button" data-action="status" data-id="' + item.bannerId + '" data-title="' + AdminUI.escapeHtml(item.bannerTitle || '') + '" data-status="' + nextStatus + '">' + nextStatusText + '</button><button class="action-link danger-text" type="button" data-action="delete" data-id="' + item.bannerId + '" data-title="' + AdminUI.escapeHtml(item.bannerTitle || '') + '">删除</button></div></td>' +
                    '</tr>';
            }).join('');
        }

        async function changeStatus(button) {
            const nextStatus = button.dataset.status;
            const confirmed = await AdminUI.confirm('确认' + (nextStatus === 'ENABLED' ? '启用' : '禁用') + ' Banner“' + button.dataset.title + '”吗？', '调整 Banner 状态');
            if (!confirmed) return;
            try {
                await AdminRequest.put('/api/admin/banners/' + button.dataset.id + '/status', { enableStatus: nextStatus });
                AdminUI.toast('Banner 状态已更新', 'success');
                load();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        async function remove(button) {
            const confirmed = await AdminUI.confirm('确认删除 Banner“' + button.dataset.title + '”吗？删除后无法恢复。', '删除 Banner');
            if (!confirmed) return;
            try {
                await AdminRequest.delete('/api/admin/banners/' + button.dataset.id);
                AdminUI.toast('Banner 已删除', 'success');
                if (listBody.querySelectorAll('tr').length === 1 && state.pageNo > 1) state.pageNo -= 1;
                load();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        filterForm.addEventListener('submit', function (event) {
            event.preventDefault();
            state.pageNo = 1;
            load();
        });
        document.getElementById('bannerResetButton').addEventListener('click', function () {
            filterForm.reset();
            state.pageNo = 1;
            load();
        });
        document.getElementById('bannerRefreshButton').addEventListener('click', load);
        pageSize.addEventListener('change', function () {
            state.pageSize = Number(pageSize.value || 10);
            state.pageNo = 1;
            load();
        });
        listBody.addEventListener('click', function (event) {
            const button = event.target.closest('[data-action]');
            if (!button) return;
            if (button.dataset.action === 'status') changeStatus(button);
            if (button.dataset.action === 'delete') remove(button);
        });
        load();
    }

    function initForm() {
        const mode = formPage.dataset.mode || 'create';
        const bannerId = formPage.dataset.bannerId || '';
        const form = document.getElementById('bannerForm');
        const title = document.getElementById('bannerTitle');
        const imageUrl = document.getElementById('bannerImageUrl');
        const enableStatus = document.getElementById('bannerEnableStatus');
        const sortOrder = document.getElementById('bannerSortOrder');
        const startTime = document.getElementById('bannerStartTime');
        const endTime = document.getElementById('bannerEndTime');
        const projectId = document.getElementById('bannerTargetProjectId');
        const projectTitle = document.getElementById('bannerTargetProjectTitle');
        const sessionId = document.getElementById('bannerTargetSessionId');
        const sessionHelp = document.getElementById('bannerSessionHelp');
        const preview = document.getElementById('bannerImagePreview');
        const submit = document.getElementById('bannerSubmitButton');

        function updatePreview() {
            const value = imageUrl.value.trim();
            if (!value) {
                preview.innerHTML = '<span>暂无图片</span>';
                return;
            }
            if (usableImage(value)) {
                preview.innerHTML = '<img src="' + AdminUI.escapeHtml(value) + '" alt="Banner 预览" onerror="this.remove()">';
            } else {
                preview.innerHTML = '<strong>B</strong><span>' + AdminUI.escapeHtml(value) + '</span>';
            }
        }

        async function loadSessions(targetProjectId, selectedSessionId) {
            sessionId.disabled = true;
            sessionId.innerHTML = '<option value="">正在加载场次...</option>';
            sessionHelp.textContent = '正在读取项目场次。';
            try {
                const items = await AdminRequest.get('/api/admin/performances/projects/' + targetProjectId + '/sessions');
                sessionId.innerHTML = '<option value="">不绑定具体场次</option>' + (items || []).map(function (item) {
                    const label = [item.cityName, item.stationName, item.venueName, AdminUI.formatDateTime(item.startTime)].filter(Boolean).join(' · ');
                    const offline = item.sessionStatus === 'OFFLINE';
                    const selected = selectedSessionId != null && String(selectedSessionId) === String(item.sessionId);
                    const optionLabel = (label || ('场次 ' + item.sessionId)) + (item.sessionStatus ? ' · ' + item.sessionStatus : '');
                    return '<option value="' + item.sessionId + '"' + (offline && !selected ? ' disabled' : '') + '>' + AdminUI.escapeHtml(optionLabel) + '</option>';
                }).join('');
                sessionId.value = selectedSessionId == null ? '' : String(selectedSessionId);
                sessionId.disabled = false;
                sessionHelp.textContent = items && items.length ? '共 ' + items.length + ' 个场次；已下架场次不可新选，Banner 启用时目标项目和场次必须未下架。' : '该项目暂无场次，可先只绑定项目。';
            } catch (error) {
                sessionId.innerHTML = '<option value="">场次读取失败</option>';
                sessionHelp.textContent = error.message;
                AdminUI.toast(error.message, 'error');
            }
        }

        function chooseProject() {
            AdminProjectSelector.open({
                title: '选择 Banner 跳转项目',
                confirmText: '保存跳转目标',
                selectedProjectId: projectId.value,
                onSelect: async function (item) {
                    const changed = String(projectId.value || '') !== String(item.projectId);
                    projectId.value = item.projectId;
                    projectTitle.value = item.title || '';
                    if (item.projectStatus === 'OFFLINE') AdminUI.toast('该项目已下架，只能保存为禁用 Banner', 'warning');
                    await loadSessions(item.projectId, changed ? null : sessionId.value);
                }
            });
        }

        async function loadDetail() {
            try {
                const data = await AdminRequest.get('/api/admin/banners/' + bannerId);
                title.value = data.bannerTitle || '';
                imageUrl.value = data.imageUrl || '';
                imageUrl.dispatchEvent(new Event('input', { bubbles: true }));
                enableStatus.value = data.enableStatus || 'ENABLED';
                sortOrder.value = data.sortOrder == null ? 0 : data.sortOrder;
                startTime.value = AdminUI.toDateTimeLocal(data.startTime);
                endTime.value = AdminUI.toDateTimeLocal(data.endTime);
                projectId.value = data.targetProjectId || '';
                projectTitle.value = data.targetProjectTitle || '';
                updatePreview();
                if (data.targetProjectId) await loadSessions(data.targetProjectId, data.targetSessionId);
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        async function save(event) {
            event.preventDefault();
            const start = startTime.value;
            const end = endTime.value;
            if (!imageUrl.value.trim()) {
                AdminUI.toast('请选择或上传 Banner 图片', 'warning');
                return;
            }
            if (!projectId.value) {
                AdminUI.toast('请选择演出项目', 'warning');
                return;
            }
            if (!start || !end || new Date(start).getTime() >= new Date(end).getTime()) {
                AdminUI.toast('结束时间必须晚于开始时间', 'warning');
                return;
            }
            const body = {
                bannerTitle: title.value.trim(),
                imageUrl: imageUrl.value.trim(),
                targetProjectId: Number(projectId.value),
                targetSessionId: AdminUI.nullableNumber(sessionId.value),
                enableStatus: enableStatus.value,
                sortOrder: Number(sortOrder.value),
                startTime: start,
                endTime: end
            };
            AdminUI.setButtonLoading(submit, true, '保存中...');
            try {
                if (mode === 'edit') {
                    await AdminRequest.put('/api/admin/banners/' + bannerId, body);
                } else {
                    await AdminRequest.post('/api/admin/banners', body);
                }
                AdminUI.toast('Banner 已保存', 'success');
                window.setTimeout(function () { window.location.href = '/admin/operation/banners'; }, 500);
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            } finally {
                AdminUI.setButtonLoading(submit, false);
            }
        }

        imageUrl.addEventListener('input', updatePreview);
        document.getElementById('bannerChooseProjectButton').addEventListener('click', chooseProject);
        form.addEventListener('submit', save);
        updatePreview();
        if (mode === 'edit' && bannerId) loadDetail();
    }
})();
