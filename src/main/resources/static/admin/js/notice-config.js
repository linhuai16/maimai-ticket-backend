(function () {
    'use strict';

    const state = {
        allNotices: [],
        projectId: null,
        projectTitle: '',
        projectCategoryName: '',
        projectSelectedIds: [],
        projectProviderNotices: []
    };
    const projectTitle = document.getElementById('noticeProjectTitle');
    const projectMeta = document.getElementById('noticeProjectMeta');
    const projectKeyword = document.getElementById('projectNoticeKeyword');
    const projectChoiceList = document.getElementById('projectNoticeChoiceList');
    const projectSelectedList = document.getElementById('projectNoticeSelectedList');
    const projectProviderList = document.getElementById('projectProviderNoticeList');
    const projectSaveButton = document.getElementById('projectNoticeSaveButton');

    function noticeById(id) {
        return state.allNotices.find(function (item) { return Number(item.noticeId) === Number(id); });
    }

    function renderChoice() {
        const filter = String(projectKeyword.value || '').trim().toLowerCase();
        const providerIds = state.projectProviderNotices.map(function (item) { return Number(item.noticeId); });
        const items = state.allNotices.filter(function (item) {
            const matches = !filter || String(item.title || '').toLowerCase().includes(filter) || String(item.description || '').toLowerCase().includes(filter);
            return matches && !providerIds.includes(Number(item.noticeId));
        });
        const disabled = !state.projectId;
        projectChoiceList.innerHTML = items.length ? items.map(function (item) {
            const checked = state.projectSelectedIds.some(function (id) { return Number(id) === Number(item.noticeId); });
            return '<label class="choice-item' + (disabled ? ' is-disabled' : '') + '"><input type="checkbox" data-notice-id="' + item.noticeId + '"' + (checked ? ' checked' : '') + (disabled ? ' disabled' : '') + '><span><strong>' + AdminUI.escapeHtml(item.title || '-') + '</strong><small>' + AdminUI.escapeHtml(item.description || '无说明') + '</small></span></label>';
        }).join('') : '<div class="table-empty">没有符合条件的须知</div>';
    }

    function renderSelected() {
        projectSelectedList.innerHTML = state.projectSelectedIds.length ? state.projectSelectedIds.map(function (id, index) {
            const item = noticeById(id);
            if (!item) return '';
            return '<div class="ordered-item"><span class="order-index">' + (index + 1) + '</span><div><strong>' + AdminUI.escapeHtml(item.title || '-') + '</strong><small>麦麦项目须知</small></div><div class="ordered-actions"><button type="button" data-move="up" data-index="' + index + '"' + (index === 0 ? ' disabled' : '') + '>↑</button><button type="button" data-move="down" data-index="' + index + '"' + (index === state.projectSelectedIds.length - 1 ? ' disabled' : '') + '>↓</button><button type="button" data-remove-index="' + index + '">移除</button></div></div>';
        }).join('') : '<div class="table-empty">暂未配置本地项目须知</div>';
    }

    function renderProvider() {
        projectProviderList.innerHTML = state.projectProviderNotices.length ? state.projectProviderNotices.map(function (item) {
            return '<div class="ordered-item"><span class="order-index">源</span><div><strong>' + AdminUI.escapeHtml(item.title || '-') + '</strong><small>' + AdminUI.escapeHtml(item.description || '') + '</small></div></div>';
        }).join('') : '<div class="table-empty">当前项目暂无 Provider 同步须知</div>';
    }

    function renderProject() {
        renderChoice();
        renderSelected();
        renderProvider();
        projectKeyword.disabled = !state.projectId;
        projectSaveButton.disabled = !state.projectId;
    }

    async function loadBaseData() {
        state.allNotices = await AdminRequest.get('/api/admin/notices') || [];
    }

    async function loadProject(projectIdValue) {
        try {
            const data = await AdminRequest.get('/api/admin/projects/' + projectIdValue + '/notices');
            state.projectId = Number(data.projectId);
            state.projectTitle = data.projectTitle || '';
            state.projectCategoryName = data.categoryName || '';
            state.projectSelectedIds = (data.projectNotices || []).map(function (item) { return Number(item.noticeId); });
            state.projectProviderNotices = data.providerNotices || [];
            projectTitle.textContent = state.projectTitle || ('项目 ' + state.projectId);
            projectMeta.textContent = '项目 ID：' + state.projectId + ' · 所属分类：' + (state.projectCategoryName || '-') + ' · Provider 须知 ' + state.projectProviderNotices.length + ' 条 · 本地须知 ' + state.projectSelectedIds.length + ' 条';
            renderProject();
            const url = new URL(window.location.href);
            url.searchParams.set('projectId', state.projectId);
            window.history.replaceState(null, '', url.toString());
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    function chooseProject() {
        AdminProjectSelector.open({
            title: '选择需要配置观演须知的项目',
            confirmText: '确认项目',
            selectedProjectId: state.projectId,
            onSelect: function (item) { return loadProject(item.projectId); }
        });
    }

    async function saveProject() {
        AdminUI.setButtonLoading(projectSaveButton, true, '保存中...');
        try {
            const data = await AdminRequest.put('/api/admin/projects/' + state.projectId + '/notices', { noticeIds: state.projectSelectedIds });
            state.projectSelectedIds = (data.projectNotices || []).map(function (item) { return Number(item.noticeId); });
            state.projectProviderNotices = data.providerNotices || [];
            AdminUI.toast('项目级观演须知已保存', 'success');
            renderProject();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(projectSaveButton, false);
        }
    }

    function toggle(id, checked) {
        const numericId = Number(id);
        const index = state.projectSelectedIds.findIndex(function (item) { return Number(item) === numericId; });
        if (checked && index < 0) state.projectSelectedIds.push(numericId);
        if (!checked && index >= 0) state.projectSelectedIds.splice(index, 1);
        renderProject();
    }

    function move(index, direction) {
        const target = direction === 'up' ? index - 1 : index + 1;
        if (target < 0 || target >= state.projectSelectedIds.length) return;
        const temp = state.projectSelectedIds[index];
        state.projectSelectedIds[index] = state.projectSelectedIds[target];
        state.projectSelectedIds[target] = temp;
        renderProject();
    }

    projectKeyword.addEventListener('input', renderChoice);
    document.getElementById('noticeChooseProjectButton').addEventListener('click', chooseProject);
    projectChoiceList.addEventListener('change', function (event) {
        const input = event.target.closest('[data-notice-id]');
        if (input) toggle(input.dataset.noticeId, input.checked);
    });
    projectSelectedList.addEventListener('click', function (event) {
        const moveButton = event.target.closest('[data-move]');
        if (moveButton) move(Number(moveButton.dataset.index), moveButton.dataset.move);
        const removeButton = event.target.closest('[data-remove-index]');
        if (removeButton) {
            state.projectSelectedIds.splice(Number(removeButton.dataset.removeIndex), 1);
            renderProject();
        }
    });
    projectSaveButton.addEventListener('click', saveProject);

    loadBaseData().then(function () {
        renderProject();
        const projectIdValue = new URLSearchParams(window.location.search).get('projectId');
        if (projectIdValue) loadProject(projectIdValue);
    }).catch(function (error) {
        AdminUI.toast(error.message, 'error');
    });
})();
