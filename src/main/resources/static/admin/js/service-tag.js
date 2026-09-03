(function () {
    'use strict';

    const listBody = document.getElementById('serviceTagTableBody');
    const formPage = document.getElementById('serviceTagFormPage');
    if (listBody) initList();
    if (formPage) initForm();

    function initList() {
        const form = document.getElementById('serviceTagFilterForm');
        const keyword = document.getElementById('serviceTagKeyword');
        const totalText = document.getElementById('serviceTagTotalText');

        async function load() {
            listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">正在加载服务标签...</div></td></tr>';
            const params = new URLSearchParams();
            if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
            try {
                const items = await AdminRequest.get('/api/admin/service-tags' + (params.toString() ? '?' + params.toString() : ''));
                renderRows(items || []);
                totalText.textContent = '共 ' + AdminUI.formatNumber((items || []).length) + ' 个服务标签';
            } catch (error) {
                listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
                totalText.textContent = '读取失败';
                AdminUI.toast(error.message, 'error');
            }
        }

        function renderRows(items) {
            if (!items.length) {
                listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">没有符合条件的服务标签</div></td></tr>';
                return;
            }
            listBody.innerHTML = items.map(function (item) {
                const systemTag = item.systemRefundTag === true;
                const sourceText = systemTag ? '退款规则自动生成' : (Number(item.automaticProjectCount || 0) > 0 ? 'Provider 能力映射' : '运营标签');
                return '<tr>' +
                    '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.tagName || '-') + '</strong><small>ID：' + AdminUI.escapeHtml(item.tagId) + '</small></div></td>' +
                    '<td><p class="table-description" title="' + AdminUI.escapeHtml(item.description || '') + '">' + AdminUI.escapeHtml(item.description || '-') + '</p></td>' +
                    '<td><strong>' + AdminUI.formatNumber(item.projectCount) + '</strong></td>' +
                    '<td>' + (systemTag ? '<span class="status-badge warning">' + sourceText + '</span>' : '<span class="status-badge">' + sourceText + '</span>') + '</td>' +
                    '<td class="align-right"><div class="action-row"><a class="action-link" href="/admin/operation/service-tags/' + item.tagId + '/edit">编辑</a>' + (systemTag ? '' : '<button class="action-link danger-text" type="button" data-action="delete" data-id="' + item.tagId + '" data-name="' + AdminUI.escapeHtml(item.tagName || '') + '">删除</button>') + '</div></td>' +
                    '</tr>';
            }).join('');
        }

        async function remove(button) {
            const confirmed = await AdminUI.confirm('确认删除服务标签“' + button.dataset.name + '”吗？仍被能力映射或项目使用时后端会阻止删除。', '删除服务标签');
            if (!confirmed) return;
            try {
                await AdminRequest.delete('/api/admin/service-tags/' + button.dataset.id);
                AdminUI.toast('服务标签已删除', 'success');
                load();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            load();
        });
        document.getElementById('serviceTagResetButton').addEventListener('click', function () {
            form.reset();
            load();
        });
        document.getElementById('serviceTagRefreshButton').addEventListener('click', load);
        listBody.addEventListener('click', function (event) {
            const button = event.target.closest('[data-action="delete"]');
            if (button) remove(button);
        });
        load();
    }

    function initForm() {
        const mode = formPage.dataset.mode || 'create';
        const tagId = formPage.dataset.tagId || '';
        const form = document.getElementById('serviceTagForm');
        const name = document.getElementById('serviceTagName');
        const description = document.getElementById('serviceTagDescription');
        const submit = document.getElementById('serviceTagSubmitButton');
        const systemNotice = document.getElementById('systemTagNotice');

        async function loadDetail() {
            try {
                const data = await AdminRequest.get('/api/admin/service-tags/' + tagId);
                name.value = data.tagName || '';
                description.value = data.description || '';
                if (data.systemRefundTag === true) {
                    name.readOnly = true;
                    systemNotice.hidden = false;
                }
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        async function save(event) {
            event.preventDefault();
            const body = {
                tagName: name.value.trim(),
                description: description.value.trim()
            };
            AdminUI.setButtonLoading(submit, true, '保存中...');
            try {
                if (mode === 'edit') await AdminRequest.put('/api/admin/service-tags/' + tagId, body);
                else await AdminRequest.post('/api/admin/service-tags', body);
                AdminUI.toast('服务标签已保存', 'success');
                window.setTimeout(function () { window.location.href = '/admin/operation/service-tags'; }, 500);
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            } finally {
                AdminUI.setButtonLoading(submit, false);
            }
        }

        form.addEventListener('submit', save);
        if (mode === 'edit' && tagId) loadDetail();
    }
})();
