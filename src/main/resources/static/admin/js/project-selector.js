(function () {
    'use strict';

    const modal = document.getElementById('projectSelectorModal');
    if (!modal) return;

    const state = {
        pageNo: 1,
        pageSize: 10,
        total: 0,
        items: [],
        selectedProjectId: null,
        selectedItem: null,
        onSelect: null,
        title: '选择演出项目',
        confirmText: '确认选择'
    };
    const form = document.getElementById('projectSelectorFilterForm');
    const keyword = document.getElementById('projectSelectorKeyword');
    const status = document.getElementById('projectSelectorStatus');
    const tableBody = document.getElementById('projectSelectorTableBody');
    const totalText = document.getElementById('projectSelectorTotalText');
    const pagination = document.getElementById('projectSelectorPagination');
    const title = document.getElementById('projectSelectorTitle');
    const selectedText = document.getElementById('projectSelectorSelectedText');
    const confirmButton = document.getElementById('projectSelectorConfirm');

    function sameProject(left, right) {
        return left != null && right != null && String(left) === String(right);
    }

    function updateSelectionSummary() {
        confirmButton.textContent = state.confirmText;
        if (!state.selectedItem) {
            selectedText.textContent = state.selectedProjectId ? '当前项目 ID：' + state.selectedProjectId + '，请在列表中重新确认' : '尚未选择项目';
            confirmButton.disabled = true;
            return;
        }
        selectedText.innerHTML = '已选择：<strong>' + AdminUI.escapeHtml(state.selectedItem.title || ('项目 ' + state.selectedItem.projectId)) + '</strong><span>项目 ID：' + AdminUI.escapeHtml(state.selectedItem.projectId) + '</span>';
        confirmButton.disabled = false;
    }

    async function loadProjects() {
        tableBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">正在加载演出项目...</div></td></tr>';
        const params = new URLSearchParams({ pageNo: state.pageNo, pageSize: state.pageSize });
        if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
        if (status.value) params.set('projectStatus', status.value);
        try {
            const data = await AdminRequest.get('/api/admin/performances/projects?' + params.toString());
            state.total = Number(data.total || 0);
            state.pageNo = Number(data.pageNo || state.pageNo);
            state.pageSize = Number(data.pageSize || state.pageSize);
            state.items = data.items || [];
            if (!state.selectedItem && state.selectedProjectId != null) {
                state.selectedItem = state.items.find(function (item) {
                    return sameProject(item.projectId, state.selectedProjectId);
                }) || null;
            }
            renderRows();
            updateSelectionSummary();
            totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 个项目';
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
            state.items = [];
            tableBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            totalText.textContent = '读取失败';
            AdminUI.toast(error.message, 'error');
        }
    }

    function renderRows() {
        if (!state.items.length) {
            tableBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">没有符合条件的演出项目</div></td></tr>';
            return;
        }
        tableBody.innerHTML = state.items.map(function (item) {
            const selected = state.selectedItem && sameProject(state.selectedItem.projectId, item.projectId);
            return '<tr class="' + (selected ? 'is-selected' : '') + '" data-project-row="' + item.projectId + '" aria-selected="' + selected + '">' +
                '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.title || '-') + '</strong><small>项目 ID：' + AdminUI.escapeHtml(item.projectId) + '</small></div></td>' +
                '<td>' + AdminUI.escapeHtml(item.categoryName || '-') + '</td>' +
                '<td>' + AdminUI.statusBadge(item.projectStatus) + '</td>' +
                '<td><span class="table-meta">共 ' + AdminUI.formatNumber(item.sessionCount) + ' 场 / 在售 ' + AdminUI.formatNumber(item.onSaleSessionCount) + ' 场</span></td>' +
                '<td class="align-right"><button class="button ' + (selected ? 'button-secondary' : 'button-primary') + ' button-small" type="button" data-select-project="' + item.projectId + '">' + (selected ? '已选中' : '选择此项目') + '</button></td>' +
                '</tr>';
        }).join('');
    }

    function selectProject(projectId) {
        const item = state.items.find(function (candidate) {
            return sameProject(candidate.projectId, projectId);
        });
        if (!item) return;
        state.selectedProjectId = item.projectId;
        state.selectedItem = item;
        renderRows();
        updateSelectionSummary();
    }

    async function confirmSelection() {
        if (!state.selectedItem) {
            AdminUI.toast('请先选择演出项目', 'warning');
            return;
        }
        AdminUI.setButtonLoading(confirmButton, true, '保存中...');
        try {
            if (typeof state.onSelect === 'function') {
                await Promise.resolve(state.onSelect(state.selectedItem));
            }
            close();
        } catch (error) {
            AdminUI.toast(error && error.message ? error.message : '保存项目选择失败', 'error');
        } finally {
            AdminUI.setButtonLoading(confirmButton, false);
            updateSelectionSummary();
        }
    }

    function open(options) {
        const config = options || {};
        state.pageNo = 1;
        state.pageSize = Number(config.pageSize || 10);
        state.selectedProjectId = config.selectedProjectId || null;
        state.selectedItem = config.selectedItem || null;
        state.onSelect = config.onSelect || null;
        state.title = config.title || '选择演出项目';
        state.confirmText = config.confirmText || '确认选择';
        state.items = [];
        title.textContent = state.title;
        keyword.value = config.keyword || '';
        status.value = config.projectStatus || '';
        updateSelectionSummary();
        AdminUI.openModal(modal);
        loadProjects();
    }

    function close() {
        AdminUI.closeModal(modal);
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        loadProjects();
    });
    document.getElementById('projectSelectorReset').addEventListener('click', function () {
        form.reset();
        state.pageNo = 1;
        loadProjects();
    });
    tableBody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-select-project]');
        const row = event.target.closest('[data-project-row]');
        const source = button || row;
        if (!source) return;
        selectProject(source.dataset.selectProject || source.dataset.projectRow);
    });
    tableBody.addEventListener('dblclick', function (event) {
        const row = event.target.closest('[data-project-row]');
        if (!row) return;
        selectProject(row.dataset.projectRow);
        confirmSelection();
    });
    confirmButton.addEventListener('click', confirmSelection);
    document.getElementById('projectSelectorClose').addEventListener('click', close);
    document.getElementById('projectSelectorCancel').addEventListener('click', close);
    modal.addEventListener('click', function (event) {
        if (event.target === modal) close();
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) close();
    });

    window.AdminProjectSelector = { open: open, close: close };
})();
