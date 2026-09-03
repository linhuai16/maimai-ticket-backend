(function () {
    'use strict';

    const state = { projectId: null, projectTitle: '', allTags: [], selectedIds: [], providerTags: [], automaticTags: [] };
    const projectTitle = document.getElementById('serviceTagProjectTitle');
    const projectMeta = document.getElementById('serviceTagProjectMeta');
    const choiceKeyword = document.getElementById('serviceTagChoiceKeyword');
    const choiceList = document.getElementById('serviceTagChoiceList');
    const selectedList = document.getElementById('serviceTagSelectedList');
    const providerList = document.getElementById('serviceTagProviderList');
    const automaticList = document.getElementById('serviceTagAutomaticList');
    const saveButton = document.getElementById('serviceTagConfigSaveButton');

    function eligibleTags() {
        const providerIds = state.providerTags.map(function (tag) { return Number(tag.tagId); });
        return state.allTags.filter(function (tag) {
            return tag.systemRefundTag !== true && !providerIds.includes(Number(tag.tagId));
        });
    }

    function tagById(id) {
        return state.allTags.find(function (tag) { return Number(tag.tagId) === Number(id); });
    }

    function render() {
        const filter = choiceKeyword.value.trim().toLowerCase();
        const choices = eligibleTags().filter(function (tag) {
            return !filter || String(tag.tagName || '').toLowerCase().includes(filter) || String(tag.description || '').toLowerCase().includes(filter);
        });
        choiceList.innerHTML = choices.length ? choices.map(function (tag) {
            const checked = state.selectedIds.some(function (id) { return Number(id) === Number(tag.tagId); });
            return '<label class="choice-item"><input type="checkbox" data-tag-id="' + tag.tagId + '"' + (checked ? ' checked' : '') + '><span><strong>' + AdminUI.escapeHtml(tag.tagName || '-') + '</strong><small>' + AdminUI.escapeHtml(tag.description || '无说明') + '</small></span></label>';
        }).join('') : '<div class="table-empty">没有符合条件的标签</div>';

        selectedList.innerHTML = state.selectedIds.length ? state.selectedIds.map(function (id, index) {
            const tag = tagById(id);
            if (!tag) return '';
            return '<div class="ordered-item"><span class="order-index">' + (index + 1) + '</span><div><strong>' + AdminUI.escapeHtml(tag.tagName || '-') + '</strong><small>' + AdminUI.escapeHtml(tag.description || '') + '</small></div><div class="ordered-actions"><button type="button" data-move="up" data-index="' + index + '"' + (index === 0 ? ' disabled' : '') + '>↑</button><button type="button" data-move="down" data-index="' + index + '"' + (index === state.selectedIds.length - 1 ? ' disabled' : '') + '>↓</button><button type="button" data-remove-index="' + index + '">移除</button></div></div>';
        }).join('') : '<div class="table-empty">暂未选择手工标签</div>';

        providerList.innerHTML = state.providerTags.length ? state.providerTags.map(function (tag) {
            return '<span class="tag-pill">' + AdminUI.escapeHtml(tag.tagName || '-') + '</span>';
        }).join('') : '<span class="table-meta">暂无 Provider 同步标签</span>';
        automaticList.innerHTML = state.automaticTags.length ? state.automaticTags.map(function (tag) {
            return '<span class="tag-pill warning-pill">' + AdminUI.escapeHtml(tag.tagName || '-') + '</span>';
        }).join('') : '<span class="table-meta">暂无自动退款标签</span>';
        saveButton.disabled = !state.projectId;
    }

    async function loadAllTags() {
        state.allTags = await AdminRequest.get('/api/admin/service-tags') || [];
    }

    async function loadProject(projectIdValue) {
        try {
            const data = await AdminRequest.get('/api/admin/projects/' + projectIdValue + '/service-tags');
            state.projectId = Number(data.projectId);
            state.projectTitle = data.projectTitle || '';
            state.selectedIds = (data.manualTags || []).map(function (tag) { return Number(tag.tagId); });
            state.providerTags = data.providerTags || [];
            state.automaticTags = data.automaticRefundTags || [];
            projectTitle.textContent = state.projectTitle || ('项目 ' + state.projectId);
            projectMeta.textContent = '项目 ID：' + state.projectId + ' · 手工标签 ' + state.selectedIds.length + ' 个 · Provider 标签 ' + state.providerTags.length + ' 个';
            render();
            const url = new URL(window.location.href);
            url.searchParams.set('projectId', state.projectId);
            window.history.replaceState(null, '', url.toString());
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    function chooseProject() {
        AdminProjectSelector.open({
            title: '选择需要配置服务标签的项目',
            confirmText: '确认项目',
            selectedProjectId: state.projectId,
            onSelect: function (item) { return loadProject(item.projectId); }
        });
    }

    function toggleTag(tagId, checked) {
        const numericId = Number(tagId);
        const index = state.selectedIds.findIndex(function (id) { return Number(id) === numericId; });
        if (checked && index < 0) state.selectedIds.push(numericId);
        if (!checked && index >= 0) state.selectedIds.splice(index, 1);
        render();
    }

    function move(index, direction) {
        const target = direction === 'up' ? index - 1 : index + 1;
        if (target < 0 || target >= state.selectedIds.length) return;
        const temp = state.selectedIds[index];
        state.selectedIds[index] = state.selectedIds[target];
        state.selectedIds[target] = temp;
        render();
    }

    async function save() {
        if (!state.projectId) return;
        AdminUI.setButtonLoading(saveButton, true, '保存中...');
        try {
            const data = await AdminRequest.put('/api/admin/projects/' + state.projectId + '/service-tags', { tagIds: state.selectedIds });
            state.selectedIds = (data.manualTags || []).map(function (tag) { return Number(tag.tagId); });
            state.providerTags = data.providerTags || [];
            state.automaticTags = data.automaticRefundTags || [];
            AdminUI.toast('项目服务标签已保存', 'success');
            render();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(saveButton, false);
        }
    }

    document.getElementById('serviceTagChooseProjectButton').addEventListener('click', chooseProject);
    choiceKeyword.addEventListener('input', render);
    choiceList.addEventListener('change', function (event) {
        const input = event.target.closest('[data-tag-id]');
        if (input) toggleTag(input.dataset.tagId, input.checked);
    });
    selectedList.addEventListener('click', function (event) {
        const moveButton = event.target.closest('[data-move]');
        if (moveButton) move(Number(moveButton.dataset.index), moveButton.dataset.move);
        const removeButton = event.target.closest('[data-remove-index]');
        if (removeButton) {
            state.selectedIds.splice(Number(removeButton.dataset.removeIndex), 1);
            render();
        }
    });
    saveButton.addEventListener('click', save);

    loadAllTags().then(function () {
        render();
        const projectIdValue = new URLSearchParams(window.location.search).get('projectId');
        if (projectIdValue) loadProject(projectIdValue);
    }).catch(function (error) {
        AdminUI.toast(error.message, 'error');
    });
})();
