(function () {
    'use strict';

    const listBody = document.getElementById('noticeTableBody');
    const formPage = document.getElementById('noticeFormPage');
    if (listBody) initList();
    if (formPage) initForm();

    function usableImage(value) {
        return AdminUI.isImageUrl(value);
    }

    function iconMarkup(item) {
        const value = String(item.iconUrl || '').trim();
        const initial = String(item.title || '知').substring(0, 1);
        if (usableImage(value)) {
            return '<span class="resource-image"><img src="' + AdminUI.escapeHtml(value) + '" alt="" onerror="this.remove()"><i>' + AdminUI.escapeHtml(initial) + '</i></span>';
        }
        return '<span class="resource-image"><i>' + AdminUI.escapeHtml(initial) + '</i></span>';
    }

    function initList() {
        const form = document.getElementById('noticeFilterForm');
        const keyword = document.getElementById('noticeKeyword');
        const totalText = document.getElementById('noticeTotalText');

        async function load() {
            listBody.innerHTML = '<tr><td colspan="4"><div class="table-empty">正在加载观演须知...</div></td></tr>';
            const params = new URLSearchParams();
            if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
            try {
                const items = await AdminRequest.get('/api/admin/notices' + (params.toString() ? '?' + params.toString() : ''));
                renderRows(items || []);
                totalText.textContent = '共 ' + AdminUI.formatNumber((items || []).length) + ' 条观演须知';
            } catch (error) {
                listBody.innerHTML = '<tr><td colspan="4"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
                totalText.textContent = '读取失败';
                AdminUI.toast(error.message, 'error');
            }
        }

        function renderRows(items) {
            if (!items.length) {
                listBody.innerHTML = '<tr><td colspan="4"><div class="table-empty">没有符合条件的观演须知</div></td></tr>';
                return;
            }
            listBody.innerHTML = items.map(function (item) {
                return '<tr>' +
                    '<td><div class="operation-resource-cell">' + iconMarkup(item) + '<div><strong>' + AdminUI.escapeHtml(item.title || '-') + '</strong><small>ID：' + AdminUI.escapeHtml(item.noticeId) + '</small><small>图标：' + AdminUI.escapeHtml(item.iconUrl || '未配置') + '</small></div></div></td>' +
                    '<td><p class="table-description" title="' + AdminUI.escapeHtml(item.description || '') + '">' + AdminUI.escapeHtml(item.description || '-') + '</p></td>' +
                    '<td><strong>' + AdminUI.formatNumber(item.projectCount) + '</strong></td>' +
                    '<td class="align-right"><div class="action-row"><a class="action-link" href="/admin/operation/notices/' + item.noticeId + '/edit">编辑</a><button class="action-link danger-text" type="button" data-action="delete" data-id="' + item.noticeId + '" data-title="' + AdminUI.escapeHtml(item.title || '') + '">删除</button></div></td>' +
                    '</tr>';
            }).join('');
        }

        async function remove(button) {
            const confirmed = await AdminUI.confirm('确认删除观演须知“' + button.dataset.title + '”吗？仍被演出项目使用时后端会阻止删除。', '删除观演须知');
            if (!confirmed) return;
            try {
                await AdminRequest.delete('/api/admin/notices/' + button.dataset.id);
                AdminUI.toast('观演须知已删除', 'success');
                load();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            load();
        });
        document.getElementById('noticeResetButton').addEventListener('click', function () {
            form.reset();
            load();
        });
        document.getElementById('noticeRefreshButton').addEventListener('click', load);
        listBody.addEventListener('click', function (event) {
            const button = event.target.closest('[data-action="delete"]');
            if (button) remove(button);
        });
        load();
    }

    function initForm() {
        const mode = formPage.dataset.mode || 'create';
        const noticeId = formPage.dataset.noticeId || '';
        const form = document.getElementById('noticeForm');
        const title = document.getElementById('noticeTitle');
        const iconUrl = document.getElementById('noticeIconUrl');
        const description = document.getElementById('noticeDescription');
        const preview = document.getElementById('noticeIconPreview');
        const submit = document.getElementById('noticeSubmitButton');

        function updatePreview() {
            const value = iconUrl.value.trim();
            if (!value) {
                preview.innerHTML = '<span>暂无图标</span>';
                return;
            }
            if (usableImage(value)) {
                preview.innerHTML = '<img src="' + AdminUI.escapeHtml(value) + '" alt="须知图标" onerror="this.remove()">';
            } else {
                preview.innerHTML = '<strong>' + AdminUI.escapeHtml((title.value || '知').substring(0, 1)) + '</strong><span>' + AdminUI.escapeHtml(value) + '</span>';
            }
        }

        async function loadDetail() {
            try {
                const data = await AdminRequest.get('/api/admin/notices/' + noticeId);
                title.value = data.title || '';
                iconUrl.value = data.iconUrl || '';
                iconUrl.dispatchEvent(new Event('input', { bubbles: true }));
                description.value = data.description || '';
                updatePreview();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        async function save(event) {
            event.preventDefault();
            const iconValue = iconUrl.value.trim();
            if (!iconValue) {
                AdminUI.toast('请选择或上传观演须知图标', 'warning');
                return;
            }
            const body = {
                title: title.value.trim(),
                description: description.value.trim(),
                iconUrl: iconValue
            };
            AdminUI.setButtonLoading(submit, true, '保存中...');
            try {
                if (mode === 'edit') await AdminRequest.put('/api/admin/notices/' + noticeId, body);
                else await AdminRequest.post('/api/admin/notices', body);
                AdminUI.toast('观演须知已保存', 'success');
                window.setTimeout(function () { window.location.href = '/admin/operation/notices'; }, 500);
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            } finally {
                AdminUI.setButtonLoading(submit, false);
            }
        }

        iconUrl.addEventListener('input', updatePreview);
        title.addEventListener('input', updatePreview);
        form.addEventListener('submit', save);
        updatePreview();
        if (mode === 'edit' && noticeId) loadDetail();
    }
})();
