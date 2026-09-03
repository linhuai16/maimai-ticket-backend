(function () {
    'use strict';

    function buildPageNumbers(current, totalPages) {
        if (totalPages <= 7) {
            return Array.from({ length: totalPages }, function (_, index) { return index + 1; });
        }
        const pages = [1];
        const start = Math.max(2, current - 1);
        const end = Math.min(totalPages - 1, current + 1);
        if (start > 2) pages.push('...');
        for (let page = start; page <= end; page += 1) pages.push(page);
        if (end < totalPages - 1) pages.push('...');
        pages.push(totalPages);
        return pages;
    }

    function render(container, options) {
        if (!container) return;
        const current = Math.max(1, Number(options.pageNo || 1));
        const total = Math.max(0, Number(options.total || 0));
        const pageSize = Math.max(1, Number(options.pageSize || 10));
        const totalPages = Math.max(1, Math.ceil(total / pageSize));
        const onChange = typeof options.onChange === 'function' ? options.onChange : function () {};
        container.innerHTML = '';

        function addButton(label, page, disabled, active) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'page-button' + (active ? ' active' : '');
            button.textContent = label;
            button.disabled = disabled;
            if (!disabled) button.addEventListener('click', function () { onChange(page); });
            container.appendChild(button);
        }

        addButton('‹', current - 1, current <= 1, false);
        buildPageNumbers(current, totalPages).forEach(function (page) {
            if (page === '...') {
                const span = document.createElement('span');
                span.className = 'page-ellipsis';
                span.textContent = '…';
                container.appendChild(span);
            } else {
                addButton(String(page), page, false, page === current);
            }
        });
        addButton('›', current + 1, current >= totalPages, false);
    }

    window.AdminPagination = { render: render };
})();
