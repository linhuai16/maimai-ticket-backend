(function () {
    'use strict';

    const body = document.body;

    window.addEventListener('pageshow', function (event) {
        if (event.persisted) {
            window.location.reload();
        }
    });
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            body.classList.toggle('sidebar-open');
        });
    }
    document.addEventListener('click', function (event) {
        if (!body.classList.contains('sidebar-open')) return;
        const sidebar = document.querySelector('.admin-sidebar');
        if (sidebar && !sidebar.contains(event.target) && sidebarToggle && !sidebarToggle.contains(event.target)) {
            body.classList.remove('sidebar-open');
        }
    });

    const escapeMap = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    function escapeHtml(value) {
        return String(value == null ? '' : value).replace(/[&<>"']/g, function (char) {
            return escapeMap[char];
        });
    }

    function formatMoney(value) {
        if (value == null || value === '') return '-';
        const number = Number(value);
        if (!Number.isFinite(number)) return '-';
        return '¥' + number.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function formatNumber(value) {
        return Number(value || 0).toLocaleString('zh-CN');
    }

    function formatDateTime(value) {
        if (!value) return '-';
        const text = String(value).replace('T', ' ');
        return text.length >= 19 ? text.substring(0, 19) : text;
    }

    function toDateTimeLocal(value) {
        if (!value) return '';
        const text = String(value).replace(' ', 'T');
        return text.length >= 19 ? text.substring(0, 19) : text;
    }

    function nullableText(value) {
        const text = String(value == null ? '' : value).trim();
        return text === '' ? null : text;
    }

    function nullableNumber(value) {
        if (value == null || String(value).trim() === '') return null;
        const number = Number(value);
        return Number.isFinite(number) ? number : null;
    }

    function isImageUrl(value) {
        return /^(https?:\/\/|data:image\/|\/)/i.test(String(value || '').trim());
    }

    const statusMap = {
        ON_SALE: ['在售', 'success'],
        COMING_SOON: ['即将开售', 'warning'],
        OFFLINE: ['已下架', 'danger'],
        PRESALE: ['预售', 'info'],
        SOLD_OUT: ['已售罄', 'warning'],
        ENDED: ['已结束', ''],
        WAIT_PAY: ['待支付', 'warning'],
        WAIT_USE: ['待使用', 'info'],
        FINISHED: ['已完成', 'success'],
        REFUNDING: ['退款中', 'warning'],
        REFUND_SUCCESS: ['退款成功', 'success'],
        REFUND_FAILED: ['退款失败', 'danger'],
        CANCELED: ['已取消', ''],
        NORMAL: ['正常', 'success'],
        ENABLED: ['已启用', 'success'],
        EFFECTIVE: ['生效中', 'success'],
        NOT_STARTED: ['未开始', 'info'],
        DISABLED: ['已禁用', 'danger'],
        GENERATING: ['生成中', 'info'],
        UNUSED: ['未使用', 'success'],
        CHECKED: ['已检票', ''],
        EXPIRED: ['已失效', 'danger'],
        ERROR: ['异常', 'danger'],
        PARTIAL: ['部分完成', 'warning'],
        ISSUED: ['已出票', 'success'],
        NO_TICKET: ['无电子票', 'danger'],
        SUCCESS: ['成功', 'success'],
        FAILED: ['失败', 'danger'],
        SYSTEM: ['系统', 'info'],
        ADMIN: ['管理员', 'warning'],
        RESERVED: ['已预留', 'info'],
        PAID: ['已支付', 'success'],
        INITIATING: ['创建中', 'info'],
        UNKNOWN_RESULT: ['创建结果待确认', 'warning'],
        PAYMENT_CONFIRMING: ['支付确认中', 'warning'],
        CANCELING: ['取消中', 'warning'],
        MANUAL_REVIEW: ['人工复核', 'danger'],
        RETRY_WAIT: ['等待重试', 'warning'],
        WAIT_PROVIDER: ['等待票源', 'warning'],
        PROCESSING: ['处理中', 'info'],
        PENDING: ['待处理', 'warning'],
        PENDING_REVIEW: ['待审核', 'warning'],
        REQUESTING: ['请求中', 'info'],
        REJECTED: ['已驳回', 'danger'],
        WAIT_SHIPMENT: ['待发货', 'warning'],
        SHIPPED: ['已发货', 'info'],
        IN_TRANSIT: ['运输中', 'info'],
        DELIVERED: ['已签收', 'success'],
        EXCEPTION: ['物流异常', 'danger'],
        RETURNED: ['已退回', 'warning'],
        NOT_REQUIRED: ['无需物流', ''],
        BOUND: ['已绑定', 'success'],
        MATCH: ['一致', 'success'],
        DIFFERENCE: ['有差异', 'danger'],
        PARTIAL_FAILED: ['部分异常', 'warning']
    };

    function statusBadge(status) {
        const config = statusMap[status] || [status || '-', ''];
        return '<span class="status-badge ' + config[1] + '">' + escapeHtml(config[0]) + '</span>';
    }

    function openModal(modal) {
        if (!modal) return;
        modal.hidden = false;
        body.classList.add('modal-open');
    }

    function closeModal(modal) {
        if (!modal) return;
        modal.hidden = true;
        if (!document.querySelector('.modal-backdrop:not([hidden])')) {
            body.classList.remove('modal-open');
        }
    }

    function toast(message, type) {
        const container = document.getElementById('toastContainer');
        if (!container) return;
        const item = document.createElement('div');
        item.className = 'toast ' + (type || '');
        item.textContent = message || '操作完成';
        container.appendChild(item);
        window.setTimeout(function () {
            item.remove();
        }, 3200);
    }

    function confirmAction(message, title) {
        const modal = document.getElementById('confirmModal');
        if (!modal) return Promise.resolve(window.confirm(message));
        const titleEl = document.getElementById('confirmTitle');
        const messageEl = document.getElementById('confirmMessage');
        const ok = document.getElementById('confirmOk');
        const cancel = document.getElementById('confirmCancel');
        const close = document.getElementById('confirmClose');
        titleEl.textContent = title || '请确认';
        messageEl.textContent = message || '确认执行此操作吗？';
        openModal(modal);

        return new Promise(function (resolve) {
            function finish(result) {
                closeModal(modal);
                ok.removeEventListener('click', onOk);
                cancel.removeEventListener('click', onCancel);
                close.removeEventListener('click', onCancel);
                modal.removeEventListener('click', onBackdrop);
                resolve(result);
            }
            function onOk() { finish(true); }
            function onCancel() { finish(false); }
            function onBackdrop(event) { if (event.target === modal) finish(false); }
            ok.addEventListener('click', onOk);
            cancel.addEventListener('click', onCancel);
            close.addEventListener('click', onCancel);
            modal.addEventListener('click', onBackdrop);
        });
    }

    function setButtonLoading(button, loading, loadingText) {
        if (!button) return;
        if (loading) {
            button.dataset.originalText = button.textContent;
            button.textContent = loadingText || '处理中...';
            button.disabled = true;
        } else {
            button.textContent = button.dataset.originalText || button.textContent;
            button.disabled = false;
            delete button.dataset.originalText;
        }
    }

    window.AdminUI = {
        escapeHtml: escapeHtml,
        formatMoney: formatMoney,
        formatNumber: formatNumber,
        formatDateTime: formatDateTime,
        toDateTimeLocal: toDateTimeLocal,
        nullableText: nullableText,
        nullableNumber: nullableNumber,
        isImageUrl: isImageUrl,
        statusBadge: statusBadge,
        toast: toast,
        confirm: confirmAction,
        openModal: openModal,
        closeModal: closeModal,
        setButtonLoading: setButtonLoading
    };
})();
