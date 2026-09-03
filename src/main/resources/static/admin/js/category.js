(function () {
    'use strict';

    const listBody = document.getElementById('categoryTableBody');
    const formPage = document.getElementById('categoryFormPage');
    if (listBody) initList();
    if (formPage) initForm();

    function usableImage(value) {
        return AdminUI.isImageUrl(value);
    }

    function iconMarkup(item) {
        const value = String(item.iconUrl || '').trim();
        const initial = String(item.categoryName || '分').substring(0, 1);
        if (usableImage(value)) {
            return '<span class="resource-image"><img src="' + AdminUI.escapeHtml(value) + '" alt="" onerror="this.remove()"><i>' + AdminUI.escapeHtml(initial) + '</i></span>';
        }
        return '<span class="resource-image"><i>' + AdminUI.escapeHtml(initial) + '</i></span>';
    }

    function initList() {
        const totalText = document.getElementById('categoryTotalText');

        async function load() {
            listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">正在加载分类数据...</div></td></tr>';
            try {
                const items = await AdminRequest.get('/api/admin/categories');
                renderRows(items || []);
                totalText.textContent = '共 ' + AdminUI.formatNumber((items || []).length) + ' 个固定分类';
            } catch (error) {
                listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
                totalText.textContent = '读取失败';
                AdminUI.toast(error.message, 'error');
            }
        }

        function renderRows(items) {
            if (!items.length) {
                listBody.innerHTML = '<tr><td colspan="5"><div class="table-empty">暂无分类数据</div></td></tr>';
                return;
            }
            listBody.innerHTML = items.map(function (item) {
                return '<tr>' +
                    '<td><div class="operation-resource-cell">' + iconMarkup(item) + '<div><strong>' + AdminUI.escapeHtml(item.categoryName || '-') + '</strong><small>ID：' + AdminUI.escapeHtml(item.categoryId) + '</small><small>' + AdminUI.escapeHtml(item.iconUrl || '-') + '</small></div></div></td>' +
                    '<td><code class="code-chip">' + AdminUI.escapeHtml(item.categoryCode || '-') + '</code></td>' +
                    '<td><strong>' + AdminUI.formatNumber(item.projectCount) + '</strong></td>' +
                    '<td><strong>' + AdminUI.escapeHtml(item.sortOrder == null ? '-' : item.sortOrder) + '</strong></td>' +
                    '<td class="align-right"><span class="table-meta">固定分类 · 只读</span></td>' +
                    '</tr>';
            }).join('');
        }

        document.getElementById('categoryRefreshButton').addEventListener('click', load);
        load();
    }

    function initForm() {
        const categoryId = formPage.dataset.categoryId;
        const form = document.getElementById('categoryForm');
        const code = document.getElementById('categoryCode');
        const name = document.getElementById('categoryName');
        const iconUrl = document.getElementById('categoryIconUrl');
        const sortOrder = document.getElementById('categorySortOrder');
        const preview = document.getElementById('categoryIconPreview');
        const submit = document.getElementById('categorySubmitButton');

        function updatePreview() {
            const value = iconUrl.value.trim();
            if (!value) {
                preview.innerHTML = '<span>暂无图标</span>';
                return;
            }
            if (usableImage(value)) {
                preview.innerHTML = '<img src="' + AdminUI.escapeHtml(value) + '" alt="分类图标" onerror="this.remove()">';
            } else {
                preview.innerHTML = '<strong>' + AdminUI.escapeHtml((name.value || '分').substring(0, 1)) + '</strong><span>' + AdminUI.escapeHtml(value) + '</span>';
            }
        }

        async function loadDetail() {
            try {
                const data = await AdminRequest.get('/api/admin/categories/' + categoryId);
                code.value = data.categoryCode || '';
                name.value = data.categoryName || '';
                iconUrl.value = data.iconUrl || '';
                iconUrl.dispatchEvent(new Event('input', { bubbles: true }));
                sortOrder.value = data.sortOrder == null ? 0 : data.sortOrder;
                updatePreview();
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            }
        }

        async function save(event) {
            event.preventDefault();
            if (!iconUrl.value.trim()) {
                AdminUI.toast('请选择或上传分类图标', 'warning');
                return;
            }
            const body = {
                categoryName: name.value.trim(),
                iconUrl: iconUrl.value.trim(),
                sortOrder: Number(sortOrder.value)
            };
            AdminUI.setButtonLoading(submit, true, '保存中...');
            try {
                await AdminRequest.put('/api/admin/categories/' + categoryId, body);
                AdminUI.toast('分类已保存', 'success');
                window.setTimeout(function () { window.location.href = '/admin/operation/categories'; }, 500);
            } catch (error) {
                AdminUI.toast(error.message, 'error');
            } finally {
                AdminUI.setButtonLoading(submit, false);
            }
        }

        iconUrl.addEventListener('input', updatePreview);
        name.addEventListener('input', updatePreview);
        form.addEventListener('submit', save);
        loadDetail();
    }
})();
