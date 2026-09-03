(function () {
    'use strict';

    const page = document.getElementById('skuManagementPage');
    if (!page) return;

    const state = {
        projectId: page.dataset.selectedProjectId ? Number(page.dataset.selectedProjectId) : null,
        sessionId: page.dataset.selectedSessionId ? Number(page.dataset.selectedSessionId) : null,
        project: null,
        sessions: [],
        session: null,
        skus: [],
        editingSkuId: null,
        stockSku: null,
        platformPriceSku: null,
        loading: false
    };

    const projectSelect = document.getElementById('skuProjectSelect');
    const sessionSelect = document.getElementById('skuSessionSelect');
    const tableBody = document.getElementById('skuTableBody');
    const totalText = document.getElementById('skuTotalText');
    const createButton = document.getElementById('createSkuButton');
    const refreshButton = document.getElementById('refreshSkuButton');
    const skuModal = document.getElementById('skuModal');
    const skuForm = document.getElementById('skuForm');
    const stockModal = document.getElementById('stockModal');
    const platformPriceModal = document.getElementById('platformPriceModal');
    const platformPriceForm = document.getElementById('platformPriceForm');
    const stockForm = document.getElementById('stockForm');
    const detailModal = document.getElementById('skuDetailModal');
    const detailContent = document.getElementById('skuDetailContent');

    function projectOption(item) {
        const option = document.createElement('option');
        option.value = String(item.projectId);
        option.textContent = (item.title || '未命名项目') + '（ID ' + item.projectId + '）';
        return option;
    }

    async function loadProjectOptions() {
        try {
            const items = [];
            let pageNo = 1;
            let total = null;
            do {
                const params = new URLSearchParams({ pageNo: String(pageNo), pageSize: '50' });
                const result = await AdminRequest.get('/api/admin/performances/projects?' + params.toString());
                const pageItems = Array.isArray(result.items) ? result.items : [];
                items.push.apply(items, pageItems);
                total = Number(result.total || items.length);
                pageNo += 1;
                if (!pageItems.length) break;
            } while (items.length < total && pageNo <= 21);

            projectSelect.innerHTML = '<option value="">请选择演出项目</option>';
            items.forEach(function (item) { projectSelect.appendChild(projectOption(item)); });
            if (state.projectId && !items.some(function (item) { return Number(item.projectId) === state.projectId; })) {
                try {
                    const detail = await AdminRequest.get('/api/admin/performances/projects/' + state.projectId);
                    projectSelect.appendChild(projectOption(detail));
                } catch (ignore) {
                    state.projectId = null;
                    state.sessionId = null;
                }
            }
            projectSelect.value = state.projectId ? String(state.projectId) : '';
            if (state.projectId) await selectProject(state.projectId, false);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function selectProject(projectId, updateUrl) {
        state.projectId = projectId ? Number(projectId) : null;
        state.project = null;
        state.sessions = [];
        state.session = null;
        state.skus = [];
        if (!state.projectId) state.sessionId = null;
        sessionSelect.disabled = true;
        sessionSelect.innerHTML = '<option value="">请先选择项目</option>';
        setSkuControls(false);
        document.getElementById('selectedSessionCard').hidden = true;
        document.getElementById('goSessionPageLink').hidden = true;
        tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">选择场次后显示票档</div></td></tr>';
        totalText.textContent = '请先选择演出项目和场次';
        if (!state.projectId) {
            updateAddressBar(updateUrl);
            return;
        }
        try {
            const results = await Promise.all([
                AdminRequest.get('/api/admin/performances/projects/' + state.projectId),
                AdminRequest.get('/api/admin/performances/projects/' + state.projectId + '/sessions')
            ]);
            state.project = results[0];
            state.sessions = results[1] || [];
            sessionSelect.innerHTML = '<option value="">请选择演出场次</option>';
            state.sessions.forEach(function (item) {
                const option = document.createElement('option');
                option.value = String(item.sessionId);
                option.textContent = (item.stationName || item.cityName || '未命名场次') + '｜' + AdminUI.formatDateTime(item.startTime);
                sessionSelect.appendChild(option);
            });
            sessionSelect.disabled = false;
            const goSession = document.getElementById('goSessionPageLink');
            goSession.href = '/admin/performances/sessions?projectId=' + state.projectId;
            goSession.hidden = false;
            if (state.sessionId && state.sessions.some(function (item) { return Number(item.sessionId) === state.sessionId; })) {
                sessionSelect.value = String(state.sessionId);
                await selectSession(state.sessionId, updateUrl);
            } else {
                state.sessionId = null;
                sessionSelect.value = '';
                updateAddressBar(updateUrl);
                if (!state.sessions.length) {
                    totalText.textContent = '该项目暂未创建场次';
                    tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">请先创建演出场次，再配置票档</div></td></tr>';
                }
            }
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function selectSession(sessionId, updateUrl) {
        state.sessionId = sessionId ? Number(sessionId) : null;
        state.session = state.sessions.find(function (item) { return Number(item.sessionId) === state.sessionId; }) || null;
        state.skus = [];
        setSkuControls(Boolean(state.sessionId));
        document.getElementById('selectedSessionCard').hidden = !state.sessionId;
        updateAddressBar(updateUrl);
        if (!state.sessionId) {
            totalText.textContent = '请选择演出场次';
            tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">选择场次后显示票档</div></td></tr>';
            return;
        }
        renderSessionContext();
        await loadSkus();
    }

    function updateAddressBar(updateUrl) {
        if (updateUrl === false) return;
        const params = new URLSearchParams();
        if (state.projectId) params.set('projectId', String(state.projectId));
        if (state.sessionId) params.set('sessionId', String(state.sessionId));
        const query = params.toString();
        window.history.replaceState({}, '', '/admin/performances/skus' + (query ? '?' + query : ''));
    }

    function setSkuControls(enabled) {
        createButton.disabled = !enabled;
        refreshButton.disabled = !enabled;
    }

    function renderSessionContext() {
        if (!state.session) return;
        document.getElementById('skuContextProjectTitle').textContent = state.project ? state.project.title : '当前项目';
        document.getElementById('skuContextSessionTitle').textContent = state.session.stationName || state.session.cityName || '-';
        document.getElementById('skuContextMeta').innerHTML =
            AdminUI.statusBadge(state.session.sessionStatus) +
            '<span>' + AdminUI.escapeHtml(state.session.cityName || '-') + ' · ' + AdminUI.escapeHtml(state.session.venueName || '-') + '</span>' +
            '<span>' + AdminUI.formatDateTime(state.session.startTime) + '</span>';
    }

    async function loadSkus() {
        if (!state.sessionId || state.loading) return;
        state.loading = true;
        tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">正在加载票档数据...</div></td></tr>';
        try {
            state.skus = await AdminRequest.get('/api/admin/performances/sessions/' + state.sessionId + '/skus') || [];
            renderSkus();
            updateSkuContextStats();
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div></td></tr>';
            totalText.textContent = '读取失败';
            AdminUI.toast(error.message, 'error');
        } finally {
            state.loading = false;
        }
    }

    function filteredSkus() {
        const keyword = document.getElementById('skuKeyword').value.trim().toLowerCase();
        const status = document.getElementById('skuStatusFilter').value;
        return state.skus.filter(function (item) {
            const text = [item.skuName, item.skuDesc].join(' ').toLowerCase();
            return (!keyword || text.includes(keyword)) && (!status || item.skuStatus === status);
        });
    }

    function renderSkus() {
        const items = filteredSkus();
        totalText.textContent = '共 ' + state.skus.length + ' 个票档，当前显示 ' + items.length + ' 个';
        if (!items.length) {
            tableBody.innerHTML = '<tr><td colspan="8"><div class="table-empty">' + (state.skus.length ? '没有符合筛选条件的票档' : '该场次暂未创建票档') + '</div></td></tr>';
            return;
        }
        tableBody.innerHTML = items.map(function (item) {
            const sourceManaged = item.sourceManaged === true;
            const nextStatuses = sourceManaged ? '' : ['PRESALE', 'ON_SALE', 'SOLD_OUT', 'OFFLINE']
                .filter(function (status) { return status !== item.skuStatus; })
                .map(function (status) {
                    const label = { PRESALE: '设为预售', ON_SALE: '设为在售', SOLD_OUT: '设为已售罄', OFFLINE: '下架票档' }[status];
                    const disabled = (status === 'PRESALE' || status === 'ON_SALE') && Number(item.stockAvailable || 0) <= 0;
                    return '<button type="button" data-action="status" data-id="' + item.skuId + '" data-status="' + status + '"' + (disabled ? ' disabled title="库存为 0"' : '') + '>' + label + '</button>';
                }).join('');
            const sourceMeta = sourceManaged
                ? '<small>'+AdminUI.escapeHtml(item.providerCode||'-')+' / '+AdminUI.escapeHtml(item.providerSkuId||'-')+'</small>'
                : '<small>麦麦本地票档</small>';
            const priceMeta = sourceManaged
                ? '<div class="table-meta">Provider '+AdminUI.formatMoney(item.providerSalePrice)+' · 结算 '+AdminUI.formatMoney(item.settlementPrice)+'<br>'+
                  (item.priceMode==='FIXED'?'固定麦麦售价':'跟随 Provider')+'</div>'
                : '';
            const stockText = sourceManaged && item.availableStockSnapshot == null ? '未知' : AdminUI.formatNumber(sourceManaged ? item.availableStockSnapshot : item.stockAvailable);
            const actions = sourceManaged
                ? '<button class="action-link" type="button" data-action="platform-price" data-id="'+item.skuId+'">平台售价</button>'
                : '<button class="action-link" type="button" data-action="edit" data-id="'+item.skuId+'">编辑</button><button class="action-link" type="button" data-action="stock" data-id="'+item.skuId+'">库存</button><details class="action-menu"><summary class="button button-ghost button-small">更多 ▾</summary><div class="action-menu-popover">'+nextStatuses+'<button class="danger-menu-item" type="button" data-action="delete" data-id="'+item.skuId+'">删除票档</button></div></details>';
            return '<tr>' +
                '<td><div class="resource-title-cell"><strong>' + AdminUI.escapeHtml(item.skuName || '-') + '</strong><small>' + AdminUI.escapeHtml(item.skuDesc || '无描述') + '</small><small>local #'+item.skuId+'</small>'+sourceMeta+'</div></td>' +
                '<td><span class="price price-large">' + AdminUI.formatMoney(item.price) + '</span>'+priceMeta+'</td>' +
                '<td><strong>' + stockText + '</strong>'+(sourceManaged?'<div class="table-meta">'+AdminUI.escapeHtml(item.inventoryMode||'-')+'</div>':'')+'</td>' +
                '<td><div class="table-meta">锁定 <b>' + AdminUI.formatNumber(item.stockLocked) + '</b><br>已售 <b>' + AdminUI.formatNumber(item.soldCount) + '</b></div></td>' +
                '<td>' + AdminUI.statusBadge(item.skuStatus) + (sourceManaged?'<div class="table-meta">source '+AdminUI.escapeHtml(item.sourceSaleStatus||'-')+'</div>':'') + '</td>' +
                '<td><div class="table-meta">排序 <b>' + AdminUI.formatNumber(item.sortOrder) + '</b><br>version <b>' + AdminUI.formatNumber(item.version) + '</b></div></td>' +
                '<td><span class="table-meta">' + AdminUI.formatDateTime(sourceManaged?item.sourceLastSyncTime:item.updateTime) + '</span></td>' +
                '<td class="align-right"><div class="action-row action-row-wrap"><button class="action-link" type="button" data-action="detail" data-id="' + item.skuId + '">详情</button>' + actions + '</div></td></tr>';
        }).join('');
    }

    function updateSkuContextStats() {
        document.getElementById('skuContextCount').textContent = AdminUI.formatNumber(state.skus.length);
        const available = state.skus.reduce(function (sum, item) { return sum + Number(item.stockAvailable || 0); }, 0);
        const locked = state.skus.reduce(function (sum, item) { return sum + Number(item.stockLocked || 0); }, 0);
        const sold = state.skus.reduce(function (sum, item) { return sum + Number(item.soldCount || 0); }, 0);
        document.getElementById('skuContextAvailable').textContent = AdminUI.formatNumber(available);
        document.getElementById('skuContextLockedSold').textContent = AdminUI.formatNumber(locked) + ' / ' + AdminUI.formatNumber(sold);
    }

    function resetSkuForm() {
        skuForm.reset();
        state.editingSkuId = null;
        document.getElementById('skuModalTitle').textContent = '新建票档';
        document.getElementById('skuStatus').value = 'PRESALE';
        document.getElementById('skuSortOrder').value = '0';
        document.getElementById('skuStockAvailable').value = '100';
        document.getElementById('createStockField').hidden = false;
        document.getElementById('skuStockAvailable').required = true;
    }

    function openCreateModal() {
        if (!state.sessionId) return;
        resetSkuForm();
        AdminUI.openModal(skuModal);
    }

    async function openEditModal(skuId) {
        resetSkuForm();
        state.editingSkuId = Number(skuId);
        document.getElementById('skuModalTitle').textContent = '编辑票档';
        document.getElementById('createStockField').hidden = true;
        document.getElementById('skuStockAvailable').required = false;
        AdminUI.openModal(skuModal);
        try {
            const item = await AdminRequest.get('/api/admin/performances/skus/' + skuId);
            document.getElementById('skuName').value = item.skuName || '';
            document.getElementById('skuPrice').value = item.price == null ? '' : item.price;
            document.getElementById('skuStatus').value = item.skuStatus || 'PRESALE';
            document.getElementById('skuSortOrder').value = item.sortOrder == null ? '0' : item.sortOrder;
            document.getElementById('skuDesc').value = item.skuDesc || '';
        } catch (error) {
            AdminUI.toast(error.message, 'error');
            AdminUI.closeModal(skuModal);
        }
    }

    function buildSkuRequest() {
        const request = {
            skuName: document.getElementById('skuName').value.trim(),
            skuDesc: AdminUI.nullableText(document.getElementById('skuDesc').value),
            price: Number(document.getElementById('skuPrice').value),
            skuStatus: document.getElementById('skuStatus').value,
            sortOrder: Number(document.getElementById('skuSortOrder').value || 0)
        };
        if (!state.editingSkuId) request.stockAvailable = Number(document.getElementById('skuStockAvailable').value);
        return request;
    }

    async function saveSku(event) {
        event.preventDefault();
        if (!skuForm.reportValidity()) return;
        const request = buildSkuRequest();
        const button = document.getElementById('saveSkuButton');
        AdminUI.setButtonLoading(button, true, '正在保存...');
        try {
            if (state.editingSkuId) {
                await AdminRequest.put('/api/admin/performances/skus/' + state.editingSkuId, request);
                AdminUI.toast('票档已更新，场次与项目价格区间已重新计算', 'success');
            } else {
                await AdminRequest.post('/api/admin/performances/sessions/' + state.sessionId + '/skus', request);
                AdminUI.toast('票档已创建，价格区间已自动更新', 'success');
            }
            AdminUI.closeModal(skuModal);
            await loadSkus();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(button, false);
        }
    }

    async function openPlatformPriceModal(skuId) {
        try {
            state.platformPriceSku = await AdminRequest.get('/api/admin/performances/skus/' + skuId);
            if (!state.platformPriceSku.sourceManaged) { AdminUI.toast('仅第三方票源票档使用平台售价策略', 'warning'); return; }
            document.getElementById('platformPriceSkuName').textContent=state.platformPriceSku.skuName||'-';
            document.getElementById('platformProviderSalePrice').textContent=AdminUI.formatMoney(state.platformPriceSku.providerSalePrice);
            document.getElementById('platformSettlementPrice').textContent=AdminUI.formatMoney(state.platformPriceSku.settlementPrice);
            document.getElementById('platformPriceMode').value=state.platformPriceSku.priceMode==='FIXED'?'FIXED':'FOLLOW_PROVIDER';
            document.getElementById('fixedPlatformPrice').value=state.platformPriceSku.price==null?'':state.platformPriceSku.price;
            document.getElementById('fixedPlatformPriceField').hidden=document.getElementById('platformPriceMode').value!=='FIXED';
            AdminUI.openModal(platformPriceModal);
        } catch(error){ AdminUI.toast(error.message,'error'); }
    }
    async function savePlatformPrice(event){
        event.preventDefault(); if(!state.platformPriceSku) return;
        const mode=document.getElementById('platformPriceMode').value;
        const body={priceMode:mode,platformPrice:mode==='FIXED'?Number(document.getElementById('fixedPlatformPrice').value):null};
        if(mode==='FIXED' && (!body.platformPrice || body.platformPrice<=0)){AdminUI.toast('请输入有效的平台售价','warning');return;}
        try{
            await AdminRequest.put('/api/admin/performances/skus/'+state.platformPriceSku.skuId+'/platform-price',body);
            AdminUI.toast(mode==='FIXED'?'麦麦平台售价已保存':'已恢复跟随 Provider 销售价','success');
            AdminUI.closeModal(platformPriceModal);state.platformPriceSku=null;await loadSkus();
        }catch(error){AdminUI.toast(error.message,'error');}
    }
    function closePlatformPriceModal(){AdminUI.closeModal(platformPriceModal);state.platformPriceSku=null;}

    async function openStockModal(skuId) {
        try {
            state.stockSku = await AdminRequest.get('/api/admin/performances/skus/' + skuId);
            document.getElementById('stockSkuName').textContent = state.stockSku.skuName || '-';
            document.getElementById('stockLockedText').textContent = AdminUI.formatNumber(state.stockSku.stockLocked);
            document.getElementById('stockSoldText').textContent = AdminUI.formatNumber(state.stockSku.soldCount);
            document.getElementById('stockVersionText').textContent = state.stockSku.version == null ? '0' : state.stockSku.version;
            document.getElementById('newStockAvailable').value = state.stockSku.stockAvailable == null ? '0' : state.stockSku.stockAvailable;
            AdminUI.openModal(stockModal);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function saveStock(event) {
        event.preventDefault();
        if (!stockForm.reportValidity() || !state.stockSku) return;
        const button = document.getElementById('saveStockButton');
        AdminUI.setButtonLoading(button, true, '正在保存...');
        try {
            await AdminRequest.put('/api/admin/performances/skus/' + state.stockSku.skuId + '/stock', {
                stockAvailable: Number(document.getElementById('newStockAvailable').value),
                version: Number(state.stockSku.version)
            });
            AdminUI.toast('可售库存已更新', 'success');
            AdminUI.closeModal(stockModal);
            state.stockSku = null;
            await loadSkus();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            AdminUI.setButtonLoading(button, false);
        }
    }

    async function updateStatus(skuId, status) {
        const label = { PRESALE: '预售', ON_SALE: '在售', SOLD_OUT: '已售罄', OFFLINE: '已下架' }[status] || status;
        if (!await AdminUI.confirm('确认将票档状态调整为“' + label + '”吗？', '调整票档状态')) return;
        try {
            await AdminRequest.put('/api/admin/performances/skus/' + skuId + '/status', { skuStatus: status });
            AdminUI.toast('票档状态已更新', 'success');
            await loadSkus();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function deleteSku(skuId) {
        if (!await AdminUI.confirm('删除票档不可恢复。已有订单、锁定库存或已售数量的票档不能删除，可改为下架。', '删除票档')) return;
        try {
            await AdminRequest.delete('/api/admin/performances/skus/' + skuId);
            AdminUI.toast('票档已删除，价格区间已重新计算', 'success');
            await loadSkus();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function showDetail(skuId) {
        AdminUI.openModal(detailModal);
        detailContent.innerHTML = '<div class="table-empty">正在读取票档详情...</div>';
        try {
            const item = await AdminRequest.get('/api/admin/performances/skus/' + skuId);
            detailContent.innerHTML = '<div class="detail-grid">' +
                detailItem('票档 ID', item.skuId) +
                detailItem('状态', AdminUI.statusBadge(item.skuStatus), true) +
                (item.sourceManaged ? detailItem('Provider 映射', AdminUI.escapeHtml((item.providerCode||'-')+' / '+(item.providerSkuId||'-')+' / '+(item.mappingStatus||'-')), true, true) : '') +
                (item.sourceManaged ? detailItem('价格边界', '麦麦 '+AdminUI.formatMoney(item.price)+'（'+(item.priceMode==='FIXED'?'固定':'跟随Provider')+'） / Provider '+AdminUI.formatMoney(item.providerSalePrice)+' / 结算 '+AdminUI.formatMoney(item.settlementPrice), true, true) : '') +
                detailItem('票档名称', item.skuName, false, true) +
                detailItem('票价', AdminUI.formatMoney(item.price)) +
                detailItem('排序值', item.sortOrder) +
                detailItem('可售库存', AdminUI.formatNumber(item.stockAvailable)) +
                detailItem('锁定库存', AdminUI.formatNumber(item.stockLocked)) +
                detailItem('已售数量', AdminUI.formatNumber(item.soldCount)) +
                detailItem('库存版本', item.version) +
                detailItem('票档描述', item.skuDesc || '-', false, true) +
                detailItem('创建时间', AdminUI.formatDateTime(item.createTime)) +
                detailItem('更新时间', AdminUI.formatDateTime(item.updateTime)) +
                '</div>';
        } catch (error) {
            detailContent.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
        }
    }

    function detailItem(label, value, raw, wide) {
        return '<div class="detail-item' + (wide ? ' wide' : '') + '"><span>' + AdminUI.escapeHtml(label) + '</span><strong>' + (raw ? value : AdminUI.escapeHtml(value == null ? '-' : value)) + '</strong></div>';
    }

    function closeSkuModal() { AdminUI.closeModal(skuModal); }
    function closeStockModal() { AdminUI.closeModal(stockModal); state.stockSku = null; }
    function closeDetailModal() { AdminUI.closeModal(detailModal); }

    projectSelect.addEventListener('change', function () {
        state.sessionId = null;
        selectProject(projectSelect.value, true);
    });
    sessionSelect.addEventListener('change', function () { selectSession(sessionSelect.value, true); });
    createButton.addEventListener('click', openCreateModal);
    refreshButton.addEventListener('click', loadSkus);
    document.getElementById('skuFilterForm').addEventListener('submit', function (event) { event.preventDefault(); renderSkus(); });
    document.getElementById('resetSkuFilterButton').addEventListener('click', function () {
        document.getElementById('skuFilterForm').reset();
        renderSkus();
    });
    skuForm.addEventListener('submit', saveSku);
    document.getElementById('skuModalClose').addEventListener('click', closeSkuModal);
    document.getElementById('skuModalCancel').addEventListener('click', closeSkuModal);
    skuModal.addEventListener('click', function (event) { if (event.target === skuModal) closeSkuModal(); });
    stockForm.addEventListener('submit', saveStock);
    platformPriceForm.addEventListener('submit', savePlatformPrice);
    document.getElementById('platformPriceMode').addEventListener('change',function(){document.getElementById('fixedPlatformPriceField').hidden=this.value!=='FIXED';});
    document.getElementById('platformPriceModalClose').addEventListener('click',closePlatformPriceModal);
    document.getElementById('platformPriceModalCancel').addEventListener('click',closePlatformPriceModal);
    platformPriceModal.addEventListener('click',function(event){if(event.target===platformPriceModal)closePlatformPriceModal();});
    document.getElementById('stockModalClose').addEventListener('click', closeStockModal);
    document.getElementById('stockModalCancel').addEventListener('click', closeStockModal);
    stockModal.addEventListener('click', function (event) { if (event.target === stockModal) closeStockModal(); });
    document.getElementById('skuDetailClose').addEventListener('click', closeDetailModal);
    document.getElementById('skuDetailCloseButton').addEventListener('click', closeDetailModal);
    detailModal.addEventListener('click', function (event) { if (event.target === detailModal) closeDetailModal(); });
    tableBody.addEventListener('click', function (event) {
        const target = event.target.closest('[data-action]');
        if (!target || target.disabled) return;
        const action = target.dataset.action;
        const id = target.dataset.id;
        if (action === 'detail') showDetail(id);
        if (action === 'edit') openEditModal(id);
        if (action === 'platform-price') openPlatformPriceModal(id);
        if (action === 'stock') openStockModal(id);
        if (action === 'status') updateStatus(id, target.dataset.status);
        if (action === 'delete') deleteSku(id);
        const details = target.closest('details');
        if (details) details.removeAttribute('open');
    });

    loadProjectOptions();
})();
