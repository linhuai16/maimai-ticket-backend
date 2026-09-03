(function () {
    'use strict';

    const listBody = document.getElementById('refundRuleTableBody');
    const detailPage = document.getElementById('refundRuleDetailPage');
    if (listBody) initList();
    if (detailPage) initDetail();

    function refundTypeBadge(value) {
        if (value === 'CONDITIONAL_REFUND') return '<span class="status-badge success">条件退</span>';
        if (value === 'NO_REFUND') return '<span class="status-badge danger">不可退</span>';
        return '<span class="status-badge warning">待同步</span>';
    }

    function percent(value) {
        if (value == null || value === '') return '-';
        const number = Number(value) * 100;
        return Number.isFinite(number) ? number.toLocaleString('zh-CN', { maximumFractionDigits: 2 }) + '%' : '-';
    }

    function duration(minutes) {
        const value = Number(minutes || 0);
        if (value % 1440 === 0) return (value / 1440) + '天';
        if (value % 60 === 0) return (value / 60) + '小时';
        return value + '分钟';
    }

    function stageText(stage) {
        let range = '';
        if (stage.maxBeforeStartMinutes == null) range = '演出开始前' + duration(stage.minBeforeStartMinutes) + '及以上';
        else if (Number(stage.minBeforeStartMinutes) === 0) range = '演出开始前' + duration(stage.maxBeforeStartMinutes) + '内';
        else range = '演出开始前' + duration(stage.minBeforeStartMinutes) + '至' + duration(stage.maxBeforeStartMinutes);
        let result = '以项目规则为准';
        if (stage.stageResult === 'FREE') result = '免费退票';
        if (stage.stageResult === 'NOT_ALLOWED') result = '不可退票';
        if (stage.stageResult === 'FEE_PERCENT') result = '收取' + percent(stage.feeRate) + '手续费';
        if (stage.stageResult === 'FEE_FIXED') result = '收取￥' + AdminUI.formatMoney(stage.fixedFeeAmount || 0) + '手续费';
        return range + '：' + result;
    }

    function initList() {
        const state = { pageNo: 1, pageSize: 10, total: 0 };
        const form = document.getElementById('refundRuleFilterForm');
        const keyword = document.getElementById('refundRuleKeyword');
        const type = document.getElementById('refundRuleTypeFilter');
        const totalText = document.getElementById('refundRuleTotalText');
        const pageSize = document.getElementById('refundRulePageSize');
        const pagination = document.getElementById('refundRulePagination');

        async function load() {
            listBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">正在加载退款规则...</div></td></tr>';
            const params = new URLSearchParams({ pageNo: state.pageNo, pageSize: state.pageSize });
            if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
            if (type.value) params.set('refundType', type.value);
            try {
                const data = await AdminRequest.get('/api/admin/refund-rules?' + params.toString());
                state.total = Number(data.total || 0);
                state.pageNo = Number(data.pageNo || state.pageNo);
                state.pageSize = Number(data.pageSize || state.pageSize);
                renderRows(data.items || []);
                totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 个演出项目';
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
                listBody.innerHTML = '<tr><td colspan="7"><div class="table-empty">没有符合条件的退款规则</div></td></tr>';
                return;
            }
            listBody.innerHTML = items.map(function (item) {
                let feeText = '-';
                if (item.refundType === 'NO_REFUND') feeText = '不适用';
                if (item.refundType === 'CONDITIONAL_REFUND' && item.minFeeRate != null) {
                    feeText = Number(item.minFeeRate) === Number(item.maxFeeRate) ? percent(item.minFeeRate) : percent(item.minFeeRate) + ' - ' + percent(item.maxFeeRate);
                }
                return '<tr>' +
                    '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.projectTitle || '-') + '</strong><small>项目 ID：' + AdminUI.escapeHtml(item.projectId) + ' · 规则 ID：' + AdminUI.escapeHtml(item.refundRuleId || '-') + '</small></div></td>' +
                    '<td>' + AdminUI.statusBadge(item.projectStatus) + '</td>' +
                    '<td>' + refundTypeBadge(item.refundType) + '</td>' +
                    '<td><strong>' + AdminUI.formatNumber(item.stageCount) + '</strong></td>' +
                    '<td><strong>' + AdminUI.escapeHtml(feeText) + '</strong></td>' +
                    '<td><span class="table-meta">' + AdminUI.formatDateTime(item.updateTime) + '</span></td>' +
                    '<td class="align-right"><a class="action-link" href="/admin/operation/refund-rules/' + item.projectId + '">查看</a></td>' +
                    '</tr>';
            }).join('');
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            state.pageNo = 1;
            load();
        });
        document.getElementById('refundRuleResetButton').addEventListener('click', function () {
            form.reset();
            state.pageNo = 1;
            load();
        });
        document.getElementById('refundRuleRefreshButton').addEventListener('click', load);
        pageSize.addEventListener('change', function () {
            state.pageSize = Number(pageSize.value || 10);
            state.pageNo = 1;
            load();
        });
        load();
    }

    function initDetail() {
        const projectId = detailPage.dataset.projectId;
        const content = document.getElementById('refundRuleDetailContent');

        async function load() {
            try {
                const data = await AdminRequest.get('/api/admin/projects/' + projectId + '/refund-rule');
                if (!data.refundRuleId) {
                    content.innerHTML = '<div class="table-empty">该项目尚未同步退款规则。Provider 项目在进入可交易状态前必须补齐明确退款政策。</div>';
                    return;
                }
                const stages = data.stages || [];
                content.innerHTML = '<div class="detail-grid">' +
                    '<div><span>演出项目</span><strong>' + AdminUI.escapeHtml(data.projectTitle || '-') + '</strong></div>' +
                    '<div><span>项目状态</span><strong>' + AdminUI.escapeHtml(data.projectStatus || '-') + '</strong></div>' +
                    '<div><span>退款类型</span><strong>' + (data.refundType === 'CONDITIONAL_REFUND' ? '条件退' : '不可退') + '</strong></div>' +
                    '<div><span>Provider Rule ID</span><strong>' + AdminUI.escapeHtml(data.providerRuleId || '-') + '</strong></div>' +
                    '<div><span>用户退款入口</span><strong>' + (data.consumerEntryEnabled ? '开放' : '关闭') + '</strong></div>' +
                    '<div><span>配送费可退</span><strong>' + (data.deliveryFeeRefundable ? '是' : '否') + '</strong></div>' +
                    '</div>' +
                    '<div class="inline-note"><strong>规则摘要</strong><p>' + AdminUI.escapeHtml(data.ruleDescription || 'Provider 未提供摘要') + '</p></div>' +
                    '<div class="mini-list">' + (stages.length ? stages.map(function (stage, index) {
                        return '<div class="mini-list-item"><div><strong>阶段 ' + (index + 1) + '</strong></div><div class="table-meta">' + AdminUI.escapeHtml(stageText(stage)) + '</div></div>';
                    }).join('') : '<div class="table-empty">该规则没有退款阶梯</div>') + '</div>';
            } catch (error) {
                content.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
                AdminUI.toast(error.message, 'error');
            }
        }

        load();
    }
})();
