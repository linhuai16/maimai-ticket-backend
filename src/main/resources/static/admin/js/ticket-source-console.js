(function () {
    'use strict';

    const page = document.querySelector('[data-ts-page]');
    if (!page) return;
    const mode = page.dataset.tsPage;
    const R = window.AdminRequest;
    const UI = window.AdminUI;

    const esc = UI.escapeHtml;
    const dt = UI.formatDateTime;
    const money = UI.formatMoney;
    const badge = UI.statusBadge;

    function value(v, fallback) { return v == null || v === '' ? (fallback == null ? '-' : fallback) : v; }
    function boolText(v) { return Number(v) === 1 || v === true ? '是' : '否'; }
    function json(el, data) { if (el) el.textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2); }
    function empty(body, colspan, text) { body.innerHTML = '<tr><td colspan="' + colspan + '"><div class="table-empty">' + esc(text || '暂无数据') + '</div></td></tr>'; }
    function qs(params) {
        const out = [];
        Object.keys(params || {}).forEach(function (k) {
            const v = params[k];
            if (v !== undefined && v !== null && String(v).trim() !== '') out.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
        });
        return out.length ? '?' + out.join('&') : '';
    }
    function callData(call) {
        if (!call) throw new Error('Provider 未返回调用结果');
        if (call.success === false) throw new Error(call.message || call.providerErrorCode || call.errorCode || 'Provider 调用失败');
        return call.data;
    }
    async function confirmRun(message, fn) {
        const ok = await UI.confirm(message, '第三方票源操作确认');
        if (!ok) return;
        return fn();
    }
    async function providers(selectIds, includeAll) {
        const list = await R.get('/api/admin/ticket-source-gateway/providers');
        (selectIds || []).forEach(function (id) {
            const sel = document.getElementById(id);
            if (!sel) return;
            const current = sel.value;
            let html = includeAll ? '<option value="">全部 Provider</option>' : '';
            list.forEach(function (p) {
                html += '<option value="' + esc(p.providerCode) + '">' + esc(p.providerName || p.providerCode) + ' (' + esc(p.providerCode) + ')</option>';
            });
            sel.innerHTML = html;
            if (current) sel.value = current;
        });
        return list;
    }
    function errorToast(err) { UI.toast(err && err.message ? err.message : '操作失败', 'danger'); }

    // ---------------------------------------------------------------------
    // Overview
    // ---------------------------------------------------------------------
    async function loadOverview() {
        try {
            const result = await Promise.all([
                R.get('/api/admin/ticket-source-console/summary'),
                R.get('/api/admin/ticket-source-console/providers')
            ]);
            const s = result[0], ps = result[1];
            const metrics = [
                ['Provider', s.enabledProviderCount + ' / ' + s.providerCount, '启用 / 全部'],
                ['项目映射', s.projectMappingCount, 'BOUND 项目'],
                ['SKU 映射', s.skuMappingCount, 'BOUND 票档'],
                ['订单桥接', s.orderBridgeCount, '累计 TICKET_SOURCE'],
                ['创建结果待确认', s.unknownResultOrderCount, 'UNKNOWN_RESULT'],
                ['订单人工复核', s.manualReviewOrderCount, 'MANUAL_REVIEW'],
                ['待履约', s.pendingIssueCount, '含失败/重试/人工'],
                ['待物流同步', s.pendingShipmentCount, '纸质快递'],
                ['待售后协同', s.pendingRefundCount, '退票 / 退款协同'],
                ['活动待审核', s.pendingCampaignReviewCount, 'Provider素材'],
                ['待回调处理', s.pendingCallbackCount, 'PENDING / FAILED'],
                ['对账差异', s.reconciliationDifferenceCount, 'DIFFERENCE / ERROR'],
                ['结算草稿', s.draftSettlementCount, 'DRAFT'],
                ['待付款账单', s.confirmedSettlementCount, 'CONFIRMED'],
                ['24h 网关失败', s.gatewayFailure24h, 'Provider 调用异常']
            ];
            document.getElementById('tsOverviewMetrics').innerHTML = metrics.map(function (m) {
                return '<div class="metric-card"><p>' + esc(m[0]) + '</p><strong>' + esc(m[1]) + '</strong><small>' + esc(m[2]) + '</small></div>';
            }).join('');
            const configBody=document.getElementById('tsProviderConfigBody');
            if(configBody){
                if(!ps.length) empty(configBody,8,'尚未配置 Provider');
                else configBody.innerHTML=ps.map(function(p){return '<tr data-provider-config="'+esc(p.providerId)+'"><td><strong>'+esc(p.providerName||p.providerCode)+'</strong><div class="table-meta">'+esc(p.providerCode)+' · '+esc(value(p.adapterCode))+'</div></td><td><select class="ts-cell-input" data-p-field="providerStatus"><option '+(p.providerStatus==='ENABLED'?'selected':'')+'>ENABLED</option><option '+(p.providerStatus==='DISABLED'?'selected':'')+'>DISABLED</option></select></td><td><input class="ts-cell-input" type="number" min="0" data-p-field="priority" value="'+esc(value(p.priority,100))+'"></td><td><input class="ts-cell-input ts-cell-wide" data-p-field="baseUrl" value="'+esc(value(p.baseUrl,''))+'"></td><td><input class="ts-cell-input" data-p-field="credentialRef" value="'+esc(value(p.credentialRef,''))+'" placeholder="仅引用"></td><td><input class="ts-cell-input" type="number" min="100" data-p-field="connectTimeoutMs" value="'+esc(value(p.connectTimeoutMs,3000))+'"><div class="table-meta">connect ms</div><input class="ts-cell-input" type="number" min="100" data-p-field="readTimeoutMs" value="'+esc(value(p.readTimeoutMs,10000))+'"><div class="table-meta">read ms</div></td><td><input class="ts-cell-input ts-cell-wide" data-p-field="remark" value="'+esc(value(p.remark,''))+'"></td><td class="align-right"><button class="button button-small button-secondary" data-p-action="save" data-provider-id="'+esc(p.providerId)+'">保存</button></td></tr>';}).join('');
            }
            const health = document.getElementById('tsProviderHealth');
            if (!ps.length) { health.innerHTML='<div class="table-empty">尚未配置 Provider</div>'; return; }
            health.innerHTML='<div class="ts-health-list">' + ps.map(function(p){
                return '<div class="ts-health-row" data-provider-health="' + esc(p.providerCode) + '"><div><strong>' + esc(p.providerName || p.providerCode) + '</strong><small>' + esc(p.providerCode) + ' · ' + esc(p.adapterCode || '-') + '</small></div><span class="status-badge">检查中</span></div>';
            }).join('') + '</div>';
            await Promise.all(ps.map(async function(p){
                const row = health.querySelector('[data-provider-health="'+CSS.escape(p.providerCode)+'"]');
                try {
                    const call=await R.get('/api/admin/ticket-source-gateway/'+encodeURIComponent(p.providerCode)+'/health');
                    const d=callData(call);
                    row.querySelector('.status-badge').outerHTML = (d && d.available === false) ? '<span class="status-badge danger">不可用</span>' : '<span class="status-badge success">可用</span>';
                } catch(e) {
                    row.querySelector('.status-badge').outerHTML = '<span class="status-badge danger">不可用</span>';
                }
            }));
        } catch (e) { errorToast(e); }
    }

    async function saveProviderConfig(id){
        const row=document.querySelector('[data-provider-config="'+CSS.escape(String(id))+'"]');
        if(!row) return;
        function f(name){return row.querySelector('[data-p-field="'+name+'"]');}
        const body={
            providerStatus:f('providerStatus').value,
            priority:Number(f('priority').value||100),
            baseUrl:f('baseUrl').value.trim()||null,
            credentialRef:f('credentialRef').value.trim()||null,
            connectTimeoutMs:Number(f('connectTimeoutMs').value||3000),
            readTimeoutMs:Number(f('readTimeoutMs').value||10000),
            remark:f('remark').value.trim()||null
        };
        try{
            await R.put('/api/admin/ticket-source-console/providers/'+id,body);
            UI.toast('Provider 配置已保存','success');
            await loadOverview();
        }catch(e){errorToast(e);}
    }

    // ---------------------------------------------------------------------
    // Resources
    // ---------------------------------------------------------------------
    async function loadMappings() {
        const body=document.getElementById('tsMappingsBody');
        try {
            const rows=await R.get('/api/admin/ticket-source-console/mappings'+qs({providerCode:document.getElementById('tsMappingProvider').value,keyword:document.getElementById('tsMappingKeyword').value,limit:document.getElementById('tsMappingLimit').value}));
            if(!rows.length){empty(body,7,'没有映射数据');return;}
            body.innerHTML=rows.map(function(x){
                const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';
                return '<tr><td><strong>'+esc(x.providerCode)+'</strong></td><td><strong>'+esc(value(x.providerProjectName))+'</strong><div class="table-meta">'+esc(x.providerProjectId)+'</div></td><td><strong>'+esc(value(x.localProjectTitle))+'</strong><div class="table-meta">local #'+esc(value(x.projectId))+'</div></td><td>'+badge(x.mappingStatus)+'<div class="table-meta">source '+esc(value(x.sourceSaleStatus))+'</div></td><td><div class="table-meta"><b>'+esc(x.sessionCount)+'</b> 场次 · <b>'+esc(x.skuCount)+'</b> SKU</div></td><td>'+badge(x.lastSyncStatus)+'<div class="table-meta">'+esc(dt(x.lastSyncTime))+'</div></td><td>'+err+'</td></tr>';
            }).join('');
        } catch(e){empty(body,7,e.message);errorToast(e);}
    }
    async function loadRemoteProjects() {
        const body=document.getElementById('tsRemoteProjectsBody');
        const provider=document.getElementById('tsResourceProvider').value;
        if(!provider){empty(body,5,'请选择 Provider');return;}
        try {
            const call=await R.get('/api/admin/ticket-source-gateway/'+encodeURIComponent(provider)+'/projects'+qs({keyword:document.getElementById('tsResourceKeyword').value,cityName:document.getElementById('tsResourceCity').value,pageNo:document.getElementById('tsResourcePageNo').value,pageSize:20}));
            const d=callData(call)||{}; const rows=d.records||[];
            document.getElementById('tsRemoteProjectsText').textContent='共 '+value(d.total,rows.length)+' 条；当前第 '+value(d.pageNo,1)+' 页';
            if(!rows.length){empty(body,5,'Provider 当前没有匹配项目');return;}
            body.innerHTML=rows.map(function(x){
                const range=(x.minPrice==null&&x.maxPrice==null)?'-':money(x.minPrice)+(x.maxPrice!=null&&String(x.maxPrice)!==String(x.minPrice)?' ~ '+money(x.maxPrice):'');
                return '<tr><td><strong>'+esc(value(x.projectName))+'</strong><div class="table-meta">'+esc(x.providerProjectId)+'</div></td><td>'+badge(x.saleStatus)+'<div class="table-meta">'+esc(value(x.cityName))+' · '+esc(value(x.venueName))+'</div></td><td>'+esc(range)+'</td><td>'+esc(dt(x.updateTime))+'</td><td class="align-right"><div class="action-row"><button class="action-link" data-r-action="preview" data-provider="'+esc(provider)+'" data-project="'+esc(x.providerProjectId)+'">Preview</button><button class="action-link" data-r-action="mapping" data-provider="'+esc(provider)+'" data-project="'+esc(x.providerProjectId)+'">Mapping</button><button class="action-link" data-r-action="sync" data-provider="'+esc(provider)+'" data-project="'+esc(x.providerProjectId)+'">Sync</button></div></td></tr>';
            }).join('');
        } catch(e){empty(body,5,e.message);errorToast(e);}
    }
    function renderResourceDetails(rows){
        const panel=document.getElementById('tsResourceDetailPanel');
        const body=document.getElementById('tsResourceDetailBody');
        if(!panel||!body)return; panel.hidden=false;
        rows=rows||[];
        if(!rows.length){empty(body,8,'暂无项目/场次/SKU映射明细');return;}
        body.innerHTML=rows.map(function(x){
            const stock=x.availableStockSnapshot==null?'NULL · 未知库存':(Number(x.availableStockSnapshot)===0?'0 · 明确售罄':String(x.availableStockSnapshot)+' · 有库存');
            const platformPrice=x.platformPrice!=null?x.platformPrice:x.localPrice;
            return '<tr>'+
                '<td><strong>local project #'+esc(x.projectId)+'</strong><div class="table-meta">'+esc(value(x.providerProjectId))+'</div><div class="table-meta">'+badge(x.projectMappingStatus)+'</div></td>'+
                '<td><strong>local session #'+esc(x.sessionId)+'</strong><div class="table-meta">'+esc(value(x.providerSessionId))+'</div><div class="table-meta">'+badge(x.sessionMappingStatus)+'</div></td>'+
                '<td><strong>local SKU #'+esc(x.skuId)+' · '+esc(value(x.skuName))+'</strong><div class="table-meta">'+esc(value(x.providerSkuId))+' · '+esc(value(x.providerSkuName,''))+'</div><div class="table-meta">'+badge(x.skuMappingStatus)+'</div></td>'+
                '<td>'+badge(x.sourceSaleStatus)+'<div class="table-meta">local '+esc(value(x.localSkuStatus))+'</div></td>'+
                '<td><strong>'+esc(stock)+'</strong><div class="table-meta">'+esc(value(x.inventoryMode))+'</div></td>'+
                '<td><div class="table-meta">票面 '+money(x.providerFacePrice)+'<br>销售 '+money(x.providerSalePrice)+'<br>结算 '+money(x.settlementPrice)+'</div></td>'+
                '<td><strong>'+money(platformPrice)+'</strong><div class="table-meta">'+esc(x.priceMode==='FIXED'?'FIXED · 固定麦麦售价':(x.priceMode==='FOLLOW_PROVIDER'?'FOLLOW_PROVIDER · 跟随Provider':value(x.priceMode)))+'</div></td>'+
                '<td><strong>'+esc(({ETICKET:'电子票',PAPER_TICKET:'纸质票',MIXED:'混合'}[x.deliveryType]||value(x.deliveryType)))+'</strong><div class="table-meta">'+badge(x.lastSyncStatus)+' · '+esc(dt(x.lastSyncTime))+'</div></td></tr>';
        }).join('');
    }
    async function loadResourceDetails(provider,pid){
        const panel=document.getElementById('tsResourceDetailPanel');
        const body=document.getElementById('tsResourceDetailBody');
        if(!panel||!body)return; panel.hidden=false;
        try{
            const rows=await R.get('/api/admin/ticket-source-v11-orders/resources/'+encodeURIComponent(provider)+'/'+encodeURIComponent(pid));
            renderResourceDetails(rows);
        }catch(e){empty(body,8,e.message);errorToast(e);}
    }

    async function resourceAction(action,provider,pid){
        const result=document.getElementById('tsResourceResult');
        try {
            let data;
            if(action==='preview') data=await R.get('/api/admin/ticket-source-v11-sync/'+encodeURIComponent(provider)+'/projects/'+encodeURIComponent(pid)+'/preview');
            if(action==='mapping'){
                data=await R.get('/api/admin/ticket-source-v11-sync/'+encodeURIComponent(provider)+'/projects/'+encodeURIComponent(pid)+'/mapping');
                if(data && Array.isArray(data.details)) renderResourceDetails(data.details); else await loadResourceDetails(provider,pid);
            }
            if(action==='sync') {
                return confirmRun('确认同步 '+provider+' / '+pid+'？同步会写入本地项目、场次、票档、价格、库存快照和映射状态。', async function(){
                    const d=await R.post('/api/admin/ticket-source-v11-sync/'+encodeURIComponent(provider)+'/projects/'+encodeURIComponent(pid)+'/sync');
                    json(result,d); UI.toast('资源同步完成','success'); await loadMappings(); await loadRemoteProjects(); await loadResourceDetails(provider,pid);
                });
            }
            json(result,data);
        } catch(e){json(result,{error:e.message});errorToast(e);}
    }

    // ---------------------------------------------------------------------
    // Orders
    // ---------------------------------------------------------------------
    const orderPageState={pageNo:1,pageSize:20,total:0};
    async function loadOrders(){
        const body=document.getElementById('tsOrdersBody');
        const pagination=document.getElementById('tsOrderPagination');
        try{
            const data=await R.get('/api/admin/ticket-source-console/orders-page'+qs({
                providerCode:document.getElementById('tsOrderProvider').value,
                bridgeStatus:document.getElementById('tsOrderStatus').value,
                keyword:document.getElementById('tsOrderKeyword').value,
                pageNo:orderPageState.pageNo,
                pageSize:orderPageState.pageSize
            }));
            const rows=Array.isArray(data&&data.items)?data.items:[];
            orderPageState.pageNo=Number((data&&data.pageNo)||orderPageState.pageNo||1);
            orderPageState.pageSize=Number((data&&data.pageSize)||orderPageState.pageSize||20);
            orderPageState.total=Number((data&&data.total)||0);
            const pageCount=Math.max(1,Math.ceil(orderPageState.total/orderPageState.pageSize));
            document.getElementById('tsOrdersText').textContent='共 '+orderPageState.total+' 条 · 第 '+orderPageState.pageNo+' / '+pageCount+' 页';
            if(!rows.length){empty(body,9,'没有订单桥接数据');if(pagination) pagination.innerHTML='';return;}
            body.innerHTML=rows.map(function(x){
                const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta ts-error-message">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';
                const recoveryAttempts=Number(x.createRecoveryAttempts||0);
                const createRecovery=(x.bridgeStatus==='UNKNOWN_RESULT')||(x.bridgeStatus==='MANUAL_REVIEW'&&(x.lastOperation==='CREATE_ORDER_LOOKUP'||recoveryAttempts>0||!!x.unknownResultSince));
                const recoveryMeta=createRecovery
                    ?'<div class="table-meta">补查 '+esc(value(x.createRecoveryAttempts,0))+' 次 · '+esc(dt(x.lastRecoveryTime||x.unknownResultSince))+'</div>':'';
                let actions='<button class="action-link" data-o-action="detail" data-order="'+esc(x.orderId)+'">详情</button>';
                if(x.bridgeStatus==='UNKNOWN_RESULT'){
                    actions+='<button class="action-link warning" data-o-action="recoverUnknown" data-order="'+esc(x.orderId)+'">补查创建结果</button>';
                }else if(x.bridgeStatus==='MANUAL_REVIEW'&&createRecovery){
                    actions+='<button class="action-link danger" data-o-action="recoverUnknown" data-order="'+esc(x.orderId)+'">人工补查创建结果</button>';
                }else if(x.localOrderStatus==='WAIT_PAY'&&x.bridgeStatus!=='MANUAL_REVIEW'){
                    actions+='<button class="action-link danger" data-o-action="expire" data-order="'+esc(x.orderId)+'">超时取消</button>';
                }
                return '<tr><td><strong>#'+esc(x.orderId)+' · '+esc(value(x.orderNo))+'</strong><div class="table-meta">user '+esc(x.userId)+' · '+esc(value(x.projectTitle))+'</div></td><td><strong>'+esc(x.providerCode)+'</strong><div class="table-meta">SKU '+esc(value(x.providerSkuId))+'</div></td><td><strong>'+esc(value(x.providerOrderNo))+'</strong><div class="table-meta">'+esc(value(x.providerOrderId))+'</div></td><td>'+money(x.payAmount)+'<div class="table-meta">× '+esc(value(x.quantity,0))+' '+esc(value(x.currencyCode,'CNY'))+'</div></td><td>'+badge(x.localOrderStatus)+'<div class="table-meta">'+esc(value(x.paymentStatus))+'</div></td><td>'+badge(x.bridgeStatus)+'<div class="table-meta">provider '+esc(value(x.providerOrderStatus))+'</div>'+recoveryMeta+'</td><td>'+esc(value(x.lastOperation))+'<div class="table-meta">'+esc(dt(x.updateTime))+'</div></td><td class="ts-error-cell">'+err+'</td><td class="align-right"><div class="action-row ts-order-actions">'+actions+'</div></td></tr>';
            }).join('');
            if(window.AdminPagination&&pagination){
                window.AdminPagination.render(pagination,{
                    pageNo:orderPageState.pageNo,
                    pageSize:orderPageState.pageSize,
                    total:orderPageState.total,
                    onChange:function(next){
                        if(next===orderPageState.pageNo) return;
                        orderPageState.pageNo=next;
                        loadOrders();
                    }
                });
            }
        }catch(e){empty(body,9,e.message);if(pagination) pagination.innerHTML='';errorToast(e);}
    }
    async function orderAction(action,id){
        const out=document.getElementById('tsOrderResult');
        try{
            if(action==='detail'){json(out,await R.get('/api/admin/ticket-source-orders/'+id));return;}
            if(action==='recoverUnknown') return confirmRun('只会按商户订单号/创建幂等键查询 Provider 已有订单，绝不会再次 createOrder。确认补查订单 '+id+' 的创建结果？',async function(){const d=await R.post('/api/admin/ticket-source-v11-orders/'+id+'/recover-unknown');json(out,d);UI.toast('创建结果补查完成','success');await loadOrders();});
            if(action==='expire') return confirmRun('只应对确实已超过支付截止时间且创建结果已确定的第三方 WAIT_PAY 执行。确认处理订单 '+id+'？',async function(){const d=await R.post('/api/admin/ticket-source-orders/'+id+'/expire');json(out,d);UI.toast('超时处理完成','success');await loadOrders();});
        }catch(e){json(out,{error:e.message});errorToast(e);}
    }

    // ---------------------------------------------------------------------
    // Fulfillment & shipment
    // ---------------------------------------------------------------------
    function issueStateText(x){
        if(x.taskStatus==='STOPPED') return '履约已停止';
        if(x.orderStatus!=='WAIT_USE'){
            if(x.orderStatus==='REFUNDING') return '订单退款中，停止履约';
            if(x.orderStatus==='REFUND_SUCCESS') return '订单已退款，停止履约';
            if(x.orderStatus==='CANCELED') return '订单已取消';
            if(x.orderStatus==='FINISHED') return '订单已完成';
            return '当前订单状态不允许履约';
        }
        if(x.paymentStatus!=='PROVIDER_CONFIRMED') return '等待第三方支付确认';
        if(x.taskStatus==='SUCCESS') return '履约完成';
        if(x.taskStatus==='PENDING' && !x.requestSent) return '等待系统自动履约';
        if(x.taskStatus==='WAIT_PROVIDER' || x.taskStatus==='PROCESSING') return '等待供应商，系统将自动补查';
        if(['FAILED','RETRY_WAIT','MANUAL_REVIEW','PARTIAL'].indexOf(x.taskStatus)>=0) return '异常任务，需要补偿/复核';
        return '系统自动处理';
    }
    function issueActions(x){
        if(x.orderStatus!=='WAIT_USE' || x.paymentStatus!=='PROVIDER_CONFIRMED' || x.taskStatus==='SUCCESS'){
            return '<span class="table-meta">'+esc(issueStateText(x))+'</span>';
        }
        const actions=[];
        const providerRequestKnown=(x.requestSent===true || Number(x.requestSent)===1 || x.orderModel==='SINGLE_SKU');
        if(providerRequestKnown && ['WAIT_PROVIDER','PROCESSING','PARTIAL','RETRY_WAIT','MANUAL_REVIEW','FAILED'].indexOf(x.taskStatus)>=0){
            actions.push('<button class="action-link" data-i-action="sync" data-order="'+esc(x.orderId)+'">同步供应商状态</button>');
        }
        if(['FAILED','RETRY_WAIT','MANUAL_REVIEW','PARTIAL'].indexOf(x.taskStatus)>=0){
            actions.push('<button class="action-link" data-i-action="retry" data-order="'+esc(x.orderId)+'">重试异常任务</button>');
        }
        return actions.length?'<div class="action-row ts-action-stack">'+actions.join('')+'</div>':'<span class="table-meta">'+esc(issueStateText(x))+'</span>';
    }
    async function loadIssues(){
        const body=document.getElementById('tsIssuesBody');
        try{
            const rows=await R.get('/api/admin/ticket-source-console/issues'+qs({status:document.getElementById('tsIssueStatus').value,limit:document.getElementById('tsIssueLimit').value}));
            if(!rows.length){empty(body,8,'暂无履约任务');return;}
            body.innerHTML=rows.map(function(x){
                const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta ts-error-message">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';
                return '<tr><td><strong>#'+esc(x.orderId)+' · '+esc(value(x.orderNo))+'</strong><div class="table-meta">订单 '+esc(value(x.orderStatus))+' / 支付 '+esc(value(x.paymentStatus))+'</div><div class="table-meta">'+esc(value(x.orderModel,''))+' · providerOrder '+esc(value(x.providerOrderId))+'</div></td><td>'+esc(x.providerCode)+'</td><td>'+badge(x.taskStatus)+'<div class="table-meta">'+esc(value(x.providerDeliveryStatus))+'</div><div class="table-meta">'+esc(issueStateText(x))+'</div></td><td><div class="table-meta"><b>'+esc(value(x.issuedCount,0))+'</b> issued / <b>'+esc(value(x.failedCount,0))+'</b> failed / '+esc(value(x.expectedTicketCount,0))+' expected</div></td><td>'+esc(value(x.retryCount,0))+' / '+esc(value(x.maxRetryCount,0))+'<div class="table-meta">manual '+esc(boolText(x.manualHold))+' · requestSent '+esc(boolText(x.requestSent))+'</div></td><td>'+esc(dt(x.nextAttemptTime))+'</td><td class="ts-error-cell">'+err+'</td><td class="align-right">'+issueActions(x)+'</td></tr>';
            }).join('');
        }catch(e){empty(body,8,e.message);errorToast(e);}
    }
    async function loadShipments(){
        const body=document.getElementById('tsShipmentsBody');
        try{const rows=await R.get('/api/admin/ticket-source-console/shipments'+qs({status:document.getElementById('tsShipmentStatus').value,limit:document.getElementById('tsShipmentLimit').value}));if(!rows.length){empty(body,9,'暂无物流记录');return;}body.innerHTML=rows.map(function(x){const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta ts-error-message">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';return '<tr><td><strong>#'+esc(x.orderId)+' · '+esc(value(x.orderNo))+'</strong><div class="table-meta">'+esc(value(x.providerOrderId))+'</div></td><td>'+esc(x.providerCode)+'</td><td>'+badge(x.shipmentStatus)+'</td><td>'+esc(value(x.carrierName))+'<div class="table-meta">'+esc(value(x.carrierCode,''))+'</div></td><td>'+esc(value(x.waybillNo))+'</td><td><div class="table-meta">发 '+esc(dt(x.shippedTime))+'<br>签 '+esc(dt(x.signedTime))+'</div></td><td>'+badge(x.lastSyncStatus)+'<div class="table-meta">'+esc(dt(x.lastSyncTime))+'</div></td><td class="ts-error-cell">'+err+'</td><td class="align-right">'+(x.shipmentStatus==='NOT_REQUIRED'?'<span class="table-meta">物流已关闭</span>':(['DELIVERED','RETURNED'].indexOf(x.shipmentStatus)>=0?'<span class="table-meta">物流终态</span>':'<button class="action-link" data-s-action="sync" data-order="'+esc(x.orderId)+'">同步物流</button>'))+'</td></tr>';}).join('');}catch(e){empty(body,9,e.message);errorToast(e);}
    }
    async function fulfillmentAction(type,id){
        const out=document.getElementById('tsFulfillmentResult');
        try{
            if(type==='sync'){
                const d=await R.post('/api/admin/ticket-source-issues/'+id+'/sync-status');
                json(out,d);UI.toast('供应商履约状态已同步','success');await loadIssues();return;
            }
            if(type==='retry'){
                return confirmRun('仅对异常履约任务执行重试。该动作可能按既有幂等键重新触发供应商履约；正常订单无需管理员操作。确认继续？',async function(){
                    const d=await R.post('/api/admin/ticket-source-issues/'+id+'/retry');
                    json(out,d);UI.toast('异常履约任务已重试','success');await loadIssues();await loadShipments();
                });
            }
            if(type==='shipment-sync'){
                const d=await R.post('/api/admin/ticket-source-v11-shipments/orders/'+id+'/sync');
                json(out,d);UI.toast('物流已同步','success');await loadShipments();
            }
        }catch(e){json(out,{error:e.message});errorToast(e);}
    }

    // ---------------------------------------------------------------------
    // Refunds
    // ---------------------------------------------------------------------
    function refundStateText(x){
        if(x.bridgeStatus==='PENDING_REVIEW') return '等待麦麦退款审核';
        if(x.bridgeStatus==='SUCCESS') return '供应商售后协同已完成';
        if(x.bridgeStatus==='REJECTED') return '退款已驳回，未向供应商发起';
        if(x.bridgeStatus==='REQUESTING' && !x.providerRefundId) return '系统正在发起供应商售后协同';
        if((x.bridgeStatus==='PROCESSING' || x.bridgeStatus==='REQUESTING') && x.providerRefundId) return '供应商处理中，系统将自动补查';
        if(['RETRY_WAIT','MANUAL_REVIEW','FAILED'].indexOf(x.bridgeStatus)>=0) return '售后协同异常，需要补偿/复核';
        return '系统自动处理';
    }
    function refundActions(x){
        const actions=['<button class="action-link" data-f-action="detail" data-refund="'+esc(x.refundId)+'">详情</button>'];
        if(x.bridgeStatus==='PENDING_REVIEW'){
            actions.push('<a class="action-link" href="/admin/refunds">去退款审核</a>');
        } else if(x.providerRefundId && ['REQUESTING','PROCESSING','RETRY_WAIT','MANUAL_REVIEW','FAILED'].indexOf(x.bridgeStatus)>=0){
            actions.push('<button class="action-link" data-f-action="sync" data-refund="'+esc(x.refundId)+'">同步供应商售后状态</button>');
        }
        if(['RETRY_WAIT','MANUAL_REVIEW','FAILED'].indexOf(x.bridgeStatus)>=0){
            actions.push('<button class="action-link" data-f-action="retry" data-refund="'+esc(x.refundId)+'">重试异常协同</button>');
        }
        return '<div class="action-row ts-action-stack">'+actions.join('')+'</div>';
    }
    async function loadRefunds(){
        const body=document.getElementById('tsRefundsBody');
        try{
            const rows=await R.get('/api/admin/ticket-source-console/refunds'+qs({status:document.getElementById('tsRefundStatus').value,limit:document.getElementById('tsRefundLimit').value}));
            if(!rows.length){empty(body,9,'暂无退票 / 退款协同记录');return;}
            body.innerHTML=rows.map(function(x){
                const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta ts-error-message">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';
                return '<tr><td><strong>#'+esc(x.refundId)+' · '+esc(value(x.refundNo))+'</strong><div class="table-meta">order #'+esc(x.orderId)+' · '+esc(value(x.orderNo))+' · '+esc(value(x.orderModel,''))+'</div><div class="table-meta">订单 '+esc(value(x.orderStatus))+' / 支付 '+esc(value(x.paymentStatus))+'</div></td><td>'+esc(x.providerCode)+'<div class="table-meta">'+esc(value(x.providerOrderId))+'</div></td><td>'+esc(value(x.providerRefundId))+'<div class="table-meta">'+esc(value(x.providerRefundStatus))+'</div></td><td>'+money(x.refundAmount)+'<div class="table-meta">用户退款 · 手续费 '+money(x.feeAmount)+'<br>Provider结算基准 '+money(x.providerSettlementBaseAmount)+'</div></td><td>'+badge(x.localRefundStatus)+'</td><td>'+badge(x.bridgeStatus)+'<div class="table-meta">'+esc(value(x.lastSyncStatus))+'</div><div class="table-meta">'+esc(refundStateText(x))+'</div></td><td>'+esc(value(x.retryCount,0))+' / '+esc(value(x.maxRetryCount,0))+'<div class="table-meta">manual '+esc(boolText(x.manualHold))+'</div></td><td class="ts-error-cell">'+err+'</td><td class="align-right">'+refundActions(x)+'</td></tr>';
            }).join('');
        }catch(e){empty(body,9,e.message);errorToast(e);}
    }
    async function refundAction(action,id){
        const out=document.getElementById('tsRefundResult');
        try{
            if(action==='detail'){json(out,await R.get('/api/admin/ticket-source-refunds/'+id));return;}
            if(action==='sync'){
                const d=await R.post('/api/admin/ticket-source-refunds/'+id+'/sync-status');
                json(out,d);UI.toast('供应商售后状态已同步','success');await loadRefunds();return;
            }
            if(action==='retry'){
                return confirmRun('仅对 RETRY_WAIT / MANUAL_REVIEW / FAILED 异常协同重试。可能使用原幂等键重新发起供应商售后请求；PENDING_REVIEW 必须回到退款审核页处理。确认继续？',async function(){
                    const d=await R.post('/api/admin/ticket-source-refunds/'+id+'/retry');
                    json(out,d);UI.toast('异常售后协同已重试','success');await loadRefunds();
                });
            }
        }catch(e){json(out,{error:e.message});errorToast(e);}
    }

    // ---------------------------------------------------------------------
    // Campaign assets & promotions
    // ---------------------------------------------------------------------
    let campaignAssetCache = {};
    async function loadCampaignAssets(){
        const body=document.getElementById('tsCampaignAssetsBody');
        try{
            const rows=await R.get('/api/admin/ticket-source-console/campaign-assets'+qs({providerCode:document.getElementById('tsCampaignProvider').value,reviewStatus:document.getElementById('tsCampaignReviewStatus').value,limit:document.getElementById('tsCampaignLimit').value}));
            campaignAssetCache={}; rows.forEach(function(x){campaignAssetCache[String(x.assetId)]=x;});
            if(!rows.length){empty(body,8,'暂无第三方活动素材');return;}
            body.innerHTML=rows.map(function(x){
                const img=x.mobileImageUrl||x.imageUrl;
                const preview=img?'<a class="ts-asset-preview" href="'+esc(img)+'" target="_blank" rel="noopener"><img src="'+esc(img)+'" alt="" onerror="this.remove()"><span>'+esc(value(x.title))+'</span></a>':'<strong>'+esc(value(x.title))+'</strong>';
                const target=[x.targetType,x.providerTargetId].filter(Boolean).join(' · ');
                const mapped=x.mappedProjectId?('<div class="table-meta">本地 #'+esc(x.mappedProjectId)+' '+esc(value(x.mappedProjectTitle,''))+(x.mappedSessionId?' · 场次 #'+esc(x.mappedSessionId):'')+'</div>'):'';
                let actions='<div class="action-row">';
                if(x.reviewStatus==='PENDING') actions+='<button class="action-link" data-ca-action="approve" data-asset="'+esc(x.assetId)+'">通过</button><button class="action-link danger" data-ca-action="reject" data-asset="'+esc(x.assetId)+'">拒绝</button>';
                if(x.reviewStatus==='APPROVED' && x.assetType==='BANNER' && !x.bannerId) actions+='<button class="action-link" data-ca-action="publish" data-asset="'+esc(x.assetId)+'">生成本地Banner</button>';
                if(x.bannerId) actions+='<a class="action-link" href="/admin/operation/banners/'+esc(x.bannerId)+'/edit">编辑Banner</a>';
                actions+='</div>';
                return '<tr><td>'+preview+'<div class="table-meta">asset '+esc(value(x.providerAssetId))+'</div></td><td>'+esc(x.providerCode)+'</td><td>'+badge(x.assetType)+'<div class="table-meta">'+esc(value(x.positionCode))+'</div></td><td>'+esc(value(target))+mapped+'</td><td>'+esc(dt(x.startTime))+'<div class="table-meta">至 '+esc(dt(x.endTime))+'</div></td><td>'+badge(x.reviewStatus)+'<div class="table-meta">'+esc(value(x.reviewRemark,''))+'</div></td><td>'+(x.bannerId?'<strong>#'+esc(x.bannerId)+'</strong>':'-')+'</td><td class="align-right">'+actions+'</td></tr>';
            }).join('');
        }catch(e){empty(body,8,e.message);errorToast(e);}
    }
    async function loadPromotions(){
        const body=document.getElementById('tsPromotionsBody');
        try{
            const rows=await R.get('/api/admin/ticket-source-console/promotions'+qs({providerCode:document.getElementById('tsPromotionProvider').value,status:document.getElementById('tsPromotionStatus').value,limit:document.getElementById('tsPromotionLimit').value}));
            if(!rows.length){empty(body,8,'暂无第三方优惠规则');return;}
            body.innerHTML=rows.map(function(x){return '<tr><td>'+esc(x.providerCode)+'</td><td><strong>'+esc(value(x.projectTitle,x.providerProjectName))+'</strong><div class="table-meta">'+esc(value(x.providerProjectId))+'</div></td><td><strong>'+esc(value(x.promotionTitle))+'</strong><div class="table-meta">'+esc(value(x.providerPromotionId))+'</div></td><td>'+esc(value(x.promotionType))+'</td><td>'+esc(dt(x.startTime))+'<div class="table-meta">至 '+esc(dt(x.endTime))+'</div></td><td>'+esc(boolText(x.stackable))+'</td><td>'+badge(x.promotionStatus)+'</td><td><div class="table-meta ts-truncate" title="'+esc(value(x.promotionDescription,''))+'">'+esc(value(x.promotionDescription,''))+'</div></td></tr>';}).join('');
        }catch(e){empty(body,8,e.message);errorToast(e);}
    }
    async function reviewCampaignAsset(id,status){
        return confirmRun((status==='APPROVED'?'确认审核通过':'确认拒绝')+'第三方活动素材 #'+id+'？审核通过不会直接投放首页。',async function(){
            try{const d=await R.post('/api/admin/ticket-source-console/campaign-assets/'+id+'/review',{reviewStatus:status,reviewRemark:status==='APPROVED'?'后台审核通过':'后台审核拒绝'});json(document.getElementById('tsCampaignResult'),d);UI.toast('素材审核状态已更新','success');await loadCampaignAssets();}catch(e){errorToast(e);}
        });
    }
    async function loadCampaignPublishSessions(projectId,selectedSessionId){
        const sel=document.getElementById('tsCampaignPublishSessionId');
        if(!projectId){sel.disabled=true;sel.innerHTML='<option value="">不绑定具体场次</option>';return;}
        sel.disabled=true;sel.innerHTML='<option value="">正在加载场次...</option>';
        try{const rows=await R.get('/api/admin/performances/projects/'+projectId+'/sessions');sel.innerHTML='<option value="">不绑定具体场次</option>'+(rows||[]).map(function(x){return '<option value="'+esc(x.sessionId)+'">'+esc([x.cityName,x.stationName,x.venueName,dt(x.startTime),x.sessionStatus].filter(Boolean).join(' · '))+'</option>';}).join('');sel.disabled=false;if(selectedSessionId)sel.value=String(selectedSessionId);}catch(e){sel.innerHTML='<option value="">场次读取失败</option>';errorToast(e);}
    }
    async function prepareCampaignBanner(id){
        const x=campaignAssetCache[String(id)]; if(!x){UI.toast('素材数据已刷新，请重新选择','danger');return;}
        document.getElementById('tsCampaignPublishAssetId').value=x.assetId;
        document.getElementById('tsCampaignPublishAssetLabel').value=(x.providerCode||'')+' / '+(x.title||'')+' / '+(x.providerAssetId||'');
        document.getElementById('tsCampaignPublishTitle').value=x.title||'';
        document.getElementById('tsCampaignPublishProjectId').value=x.mappedProjectId||'';
        document.getElementById('tsCampaignPublishProjectTitle').value=x.mappedProjectTitle||'';
        document.getElementById('tsCampaignPublishStart').value=UI.toDateTimeLocal(x.startTime);
        document.getElementById('tsCampaignPublishEnd').value=UI.toDateTimeLocal(x.endTime);
        await loadCampaignPublishSessions(x.mappedProjectId,x.mappedSessionId);
        document.getElementById('tsCampaignPublishPanel').scrollIntoView({behavior:'smooth',block:'start'});
    }
    async function publishCampaignBanner(e){
        e.preventDefault();
        const id=Number(document.getElementById('tsCampaignPublishAssetId').value||0);
        if(!id){UI.toast('请先选择已审核通过的 BANNER 素材','danger');return;}
        const body={bannerTitle:document.getElementById('tsCampaignPublishTitle').value.trim(),imageUrl:document.getElementById('tsCampaignPublishImageUrl').value.trim(),targetProjectId:Number(document.getElementById('tsCampaignPublishProjectId').value||0)||null,targetSessionId:Number(document.getElementById('tsCampaignPublishSessionId').value||0)||null,enableStatus:document.getElementById('tsCampaignPublishEnable').value,sortOrder:Number(document.getElementById('tsCampaignPublishSort').value||0),startTime:document.getElementById('tsCampaignPublishStart').value||null,endTime:document.getElementById('tsCampaignPublishEnd').value||null};
        try{const d=await R.post('/api/admin/ticket-source-console/campaign-assets/'+id+'/publish-banner',body);json(document.getElementById('tsCampaignResult'),d);UI.toast('本地 Banner 已生成并绑定','success');await loadCampaignAssets();}catch(err){json(document.getElementById('tsCampaignResult'),{error:err.message});errorToast(err);}
    }

    // ---------------------------------------------------------------------
    // Settlement periods
    // ---------------------------------------------------------------------
    let currentSettlementPeriodId=null;
    async function loadSettlements(){
        const body=document.getElementById('tsSettlementsBody');
        try{
            const rows=await R.get('/api/admin/ticket-source-console/settlements'+qs({providerCode:document.getElementById('tsSettlementProvider').value,status:document.getElementById('tsSettlementStatus').value,limit:document.getElementById('tsSettlementLimit').value}));
            if(!rows.length){empty(body,9,'暂无账期结算单');return;}
            body.innerHTML=rows.map(function(x){let actions='<div class="action-row"><button class="action-link" data-set-action="detail" data-period="'+esc(x.periodId)+'">详情</button>';if(x.periodStatus==='DRAFT')actions+='<button class="action-link" data-set-action="regenerate" data-period="'+esc(x.periodId)+'">重新生成</button><button class="action-link" data-set-action="confirm" data-period="'+esc(x.periodId)+'">确认账单</button>';if(x.periodStatus==='CONFIRMED')actions+='<button class="action-link" data-set-action="paid" data-period="'+esc(x.periodId)+'">标记已付款</button><button class="action-link" data-set-action="carry" data-period="'+esc(x.periodId)+'">结转下期</button>';actions+='</div>';return '<tr><td><strong>#'+esc(x.periodId)+' · '+esc(value(x.settlementNo))+'</strong><div class="table-meta">'+esc(value(x.dateFrom))+' ~ '+esc(value(x.dateTo))+'</div></td><td>'+esc(x.providerCode)+'</td><td>'+badge(x.periodStatus)+'</td><td>'+money(x.saleSettlementAmount)+'<div class="table-meta">'+esc(value(x.saleOrderCount,0))+' 单</div></td><td>'+money(x.refundDeductionAmount)+'<div class="table-meta">'+esc(value(x.refundOrderCount,0))+' 单</div></td><td>'+money(x.adjustmentAmount)+'</td><td><strong>'+money(x.netPayableAmount)+'</strong></td><td>'+esc(value(x.closeMode))+'<div class="table-meta">'+esc(dt(x.closeTime))+'</div></td><td class="align-right">'+actions+'</td></tr>';}).join('');
        }catch(e){empty(body,9,e.message);errorToast(e);}
    }
    async function loadSettlementDetail(id){
        try{const d=await R.get('/api/admin/ticket-source-console/settlements/'+id);currentSettlementPeriodId=id;json(document.getElementById('tsSettlementResult'),d);document.getElementById('tsSettlementDetailTitle').textContent=(d.settlementNo||('#'+id))+' · '+d.providerCode+' · '+d.periodStatus+' · 净应付 '+money(d.netPayableAmount);const form=document.getElementById('tsSettlementAdjustmentForm');form.hidden=d.periodStatus!=='DRAFT';document.getElementById('tsSettlementAdjustmentPeriodId').value=id;const body=document.getElementById('tsSettlementDetailsBody');const rows=d.details||[];if(!rows.length){empty(body,7,'当前账期还没有明细');return;}body.innerHTML=rows.map(function(x){return '<tr><td>'+badge(x.detailType)+'</td><td>'+esc(dt(x.businessTime))+'</td><td>'+esc(value(x.referenceNo))+'<div class="table-meta">order '+esc(value(x.orderId,''))+(x.refundId?' · refund '+esc(x.refundId):'')+'</div></td><td>'+money(x.userAmount)+'</td><td>'+money(x.providerSettlementAmount)+'</td><td><strong>'+money(x.amountEffect)+'</strong></td><td>'+esc(value(x.remark))+'</td></tr>';}).join('');}catch(e){errorToast(e);}
    }
    async function settlementAction(action,id){
        if(action==='detail')return loadSettlementDetail(id);
        let message='';let call=null;
        if(action==='regenerate'){message='重新按当前数据库订单/退款事实生成 DRAFT 账单？人工调整项会保留。';call=function(){return R.post('/api/admin/ticket-source-console/settlements/'+id+'/regenerate');};}
        if(action==='confirm'){message='确认账单后将锁定明细，不能再重新生成或添加调整。确认继续？';call=function(){return R.post('/api/admin/ticket-source-console/settlements/'+id+'/confirm');};}
        if(action==='paid'){message='只有实际已向 Provider 完成账期付款后才能标记 PAID。确认已付款？';call=function(){return R.post('/api/admin/ticket-source-console/settlements/'+id+'/close?mode=PAID');};}
        if(action==='carry'){message='仅用于净应付为负的账期。确认将该负余额结转下期？';call=function(){return R.post('/api/admin/ticket-source-console/settlements/'+id+'/close?mode=CARRIED_FORWARD');};}
        if(!call)return;
        return confirmRun(message,async function(){try{const d=await call();json(document.getElementById('tsSettlementResult'),d);UI.toast('账期操作完成','success');await loadSettlements();await loadSettlementDetail(id);}catch(e){errorToast(e);}});
    }
    async function createSettlement(e){e.preventDefault();const body={providerCode:document.getElementById('tsSettlementCreateProvider').value,dateFrom:document.getElementById('tsSettlementDateFrom').value,dateTo:document.getElementById('tsSettlementDateTo').value,remark:document.getElementById('tsSettlementRemark').value.trim()||null};try{const d=await R.post('/api/admin/ticket-source-console/settlements',body);json(document.getElementById('tsSettlementResult'),d);UI.toast('DRAFT 账期已生成','success');await loadSettlements();await loadSettlementDetail(d.periodId);}catch(err){errorToast(err);}}
    async function addSettlementAdjustment(e){e.preventDefault();const id=Number(document.getElementById('tsSettlementAdjustmentPeriodId').value||0);const amount=Number(document.getElementById('tsSettlementAdjustmentAmount').value||0);const remark=document.getElementById('tsSettlementAdjustmentRemark').value.trim();if(!id||!amount||!remark){UI.toast('请选择 DRAFT 账期，并填写非0调整金额和原因','danger');return;}try{const d=await R.post('/api/admin/ticket-source-console/settlements/'+id+'/adjustments',{amount:amount,remark:remark});json(document.getElementById('tsSettlementResult'),d);document.getElementById('tsSettlementAdjustmentAmount').value='';document.getElementById('tsSettlementAdjustmentRemark').value='';UI.toast('调整项已加入','success');await loadSettlements();await loadSettlementDetail(id);}catch(err){errorToast(err);}}

    // ---------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------
    async function loadCallbacks(){const body=document.getElementById('tsCallbacksBody');try{const rows=await R.get('/api/admin/ticket-source-console/callbacks'+qs({status:document.getElementById('tsCallbackStatus').value,limit:100}));if(!rows.length){empty(body,7,'暂无回调事件');return;}body.innerHTML=rows.map(function(x){const err=x.lastErrorCode||x.lastErrorMessage?'<span class="danger-text">'+esc(value(x.lastErrorCode,''))+'</span><div class="table-meta ts-truncate">'+esc(value(x.lastErrorMessage,''))+'</div>':'-';return '<tr><td>'+esc(x.eventId)+'</td><td>'+esc(x.providerCode)+'</td><td><strong>'+esc(x.eventType)+'</strong><div class="table-meta">'+esc(value(x.providerEventId))+'</div></td><td>'+esc(value(x.resourceType))+'<div class="table-meta">'+esc(value(x.providerResourceId))+'</div></td><td>'+badge(x.processStatus)+'</td><td>'+err+'</td><td><button class="action-link" data-c-action="process" data-event="'+esc(x.eventId)+'">处理</button></td></tr>';}).join('');}catch(e){empty(body,7,e.message);errorToast(e);}}
    async function loadReconciliations(){const body=document.getElementById('tsReconciliationsBody');try{const rows=await R.get('/api/admin/ticket-source-console/reconciliations?limit=100');if(!rows.length){empty(body,9,'暂无对账批次');return;}body.innerHTML=rows.map(function(x){return '<tr><td><strong>#'+esc(x.batchId)+'</strong><div class="table-meta">'+esc(value(x.batchNo))+'</div></td><td>'+esc(x.providerCode)+'</td><td>'+badge(x.batchStatus)+'</td><td>'+esc(value(x.totalCount,0))+'</td><td>'+esc(value(x.matchedCount,0))+'</td><td>'+esc(value(x.differenceCount,0))+'</td><td>'+esc(value(x.errorCount,0))+'</td><td>'+esc(dt(x.startTime))+'<div class="table-meta">'+esc(dt(x.finishTime))+'</div></td><td><button class="action-link" data-rec-action="detail" data-batch="'+esc(x.batchId)+'">详情</button></td></tr>';}).join('');}catch(e){empty(body,9,e.message);errorToast(e);}}
    async function loadLogs(){const body=document.getElementById('tsGatewayLogsBody');try{const success=document.getElementById('tsLogSuccess').value;const rows=await R.get('/api/admin/ticket-source-gateway-logs'+qs({providerCode:document.getElementById('tsLogProvider').value,operationCode:document.getElementById('tsLogOperation').value,success:success===''?null:success,limit:100}));if(!rows.length){empty(body,8,'暂无网关日志');return;}body.innerHTML=rows.map(function(x){return '<tr><td>'+esc(dt(x.callTime))+'<div class="table-meta">'+esc(value(x.requestId))+'</div></td><td>'+esc(x.providerCode)+'<div class="table-meta">'+esc(value(x.adapterCode))+'</div></td><td>'+esc(value(x.operationCode))+'</td><td>'+(x.success?'<span class="status-badge success">成功</span>':'<span class="status-badge danger">失败</span>')+'</td><td>'+esc(value(x.gatewayErrorCode))+'<div class="table-meta">'+esc(value(x.providerErrorCode,''))+'</div></td><td>'+esc(boolText(x.retryable))+'</td><td>'+esc(value(x.elapsedMs,0))+' ms</td><td><div class="log-description">'+esc(value(x.message))+'</div></td></tr>';}).join('');}catch(e){empty(body,8,e.message);errorToast(e);}}
    async function processCallback(id){const out=document.getElementById('tsOperationsResult');try{const d=await R.post('/api/admin/ticket-source-v12/callbacks/'+id+':process');json(out,d);UI.toast('回调处理完成','success');await loadCallbacks();}catch(e){json(out,{error:e.message});errorToast(e);}}
    async function reconcile(){const provider=document.getElementById('tsReconcileProvider').value;const ids=(document.getElementById('tsReconcileOrderIds').value.match(/\d+/g)||[]).map(Number).filter(function(x){return x>0;});if(!provider||!ids.length){UI.toast('请选择 Provider 并至少填写一个 orderId','danger');return;}try{const d=await R.post('/api/admin/ticket-source-reconciliation/run',{providerCode:provider,orderIds:Array.from(new Set(ids))});json(document.getElementById('tsReconcileResult'),d);UI.toast('对账完成','success');await loadReconciliations();}catch(e){json(document.getElementById('tsReconcileResult'),{error:e.message});errorToast(e);}}
    async function reconciliationDetail(id){try{json(document.getElementById('tsOperationsResult'),await R.get('/api/admin/ticket-source-reconciliation/batches/'+id));}catch(e){errorToast(e);}}

    // ---------------------------------------------------------------------
    // Mock
    // ---------------------------------------------------------------------
    async function loadMock(){const body=document.getElementById('tsMockBehaviorsBody');try{const rows=await R.get('/api/admin/ticket-source-mock/v11/behaviors');if(!rows.length){empty(body,7,'没有行为配置');return;}body.innerHTML=rows.map(function(x){const op=x.operationCode||x.operation_code;const delay=x.delayMs!=null?x.delayMs:x.delay_ms;const code=x.forcedErrorCode!=null?x.forcedErrorCode:x.forced_error_code;const msg=x.forcedErrorMessage!=null?x.forcedErrorMessage:x.forced_error_message;const retry=x.retryable!=null?x.retryable:x.forced_retryable;return '<tr data-mock-op="'+esc(op)+'"><td><strong>'+esc(op)+'</strong></td><td><input type="checkbox" data-mock-field="enabled" '+(x.enabled?'checked':'')+'></td><td><input class="ts-cell-input" type="number" min="0" data-mock-field="delayMs" value="'+esc(value(delay,0))+'"></td><td><input class="ts-cell-input" data-mock-field="forcedErrorCode" value="'+esc(value(code,''))+'"></td><td><input class="ts-cell-input ts-cell-wide" data-mock-field="forcedErrorMessage" value="'+esc(value(msg,''))+'"></td><td><input type="checkbox" data-mock-field="retryable" '+(Number(retry)===1||retry===true?'checked':'')+'></td><td><button class="button button-small button-secondary" data-m-action="save" data-op="'+esc(op)+'">保存</button></td></tr>';}).join('');}catch(e){empty(body,7,e.message);errorToast(e);}}
    async function saveMock(op){const row=document.querySelector('[data-mock-op="'+CSS.escape(op)+'"]');function field(name){const e=row.querySelector('[data-mock-field="'+name+'"]');return e.type==='checkbox'?e.checked:e.value;}const body={enabled:field('enabled'),delayMs:Number(field('delayMs')||0),forcedErrorCode:field('forcedErrorCode')||null,forcedErrorMessage:field('forcedErrorMessage')||null,retryable:field('retryable')};try{const d=await R.put('/api/admin/ticket-source-mock/v11/behaviors/'+encodeURIComponent(op),body);json(document.getElementById('tsMockResult'),d);UI.toast(op+' 已更新','success');await loadMock();}catch(e){json(document.getElementById('tsMockResult'),{error:e.message});errorToast(e);}}
    async function updateMockPrice(){const id=document.getElementById('tsMockSkuId').value.trim();if(!id){UI.toast('providerSkuId 不能为空','danger');return;}const body={facePrice:Number(document.getElementById('tsMockFacePrice').value),salePrice:Number(document.getElementById('tsMockSalePrice').value),settlementPrice:Number(document.getElementById('tsMockSettlementPrice').value)};try{const d=await R.put('/api/admin/ticket-source-mock/v11/ticket-products/'+encodeURIComponent(id)+'/price',body);json(document.getElementById('tsMockResult'),d);UI.toast('模拟实时价格已更新','success');}catch(e){json(document.getElementById('tsMockResult'),{error:e.message});errorToast(e);}}

    // ---------------------------------------------------------------------
    // Events + init
    // ---------------------------------------------------------------------
    document.addEventListener('click', function (e) {
        const b=e.target.closest('button'); if(!b) return;
        if(b.dataset.pAction) saveProviderConfig(b.dataset.providerId);
        if(b.dataset.rAction) resourceAction(b.dataset.rAction,b.dataset.provider,b.dataset.project);
        if(b.dataset.oAction) orderAction(b.dataset.oAction,b.dataset.order);
        if(b.dataset.iAction) fulfillmentAction(b.dataset.iAction,b.dataset.order);
        if(b.dataset.sAction) fulfillmentAction('shipment-sync',b.dataset.order);
        if(b.dataset.fAction) refundAction(b.dataset.fAction,b.dataset.refund);
        if(b.dataset.caAction){if(b.dataset.caAction==='approve')reviewCampaignAsset(b.dataset.asset,'APPROVED');if(b.dataset.caAction==='reject')reviewCampaignAsset(b.dataset.asset,'REJECTED');if(b.dataset.caAction==='publish')prepareCampaignBanner(b.dataset.asset);}
        if(b.dataset.setAction) settlementAction(b.dataset.setAction,b.dataset.period);
        if(b.dataset.cAction) processCallback(b.dataset.event);
        if(b.dataset.recAction) reconciliationDetail(b.dataset.batch);
        if(b.dataset.mAction) saveMock(b.dataset.op);
    });

    async function init(){
        if(mode==='overview'){
            document.getElementById('tsOverviewRefresh').addEventListener('click',loadOverview); await loadOverview();
        }
        if(mode==='resources'){
            await providers(['tsResourceProvider','tsMappingProvider'],true);
            document.getElementById('tsResourceProvider').querySelector('option[value=""]').textContent='请选择 Provider';
            document.getElementById('tsResourceRemoteForm').addEventListener('submit',function(e){e.preventDefault();loadRemoteProjects();});
            document.getElementById('tsMappingFilterForm').addEventListener('submit',function(e){e.preventDefault();loadMappings();});
            await loadMappings();
        }
        if(mode==='campaigns'){
            await providers(['tsCampaignSyncProvider'],false);
            await providers(['tsCampaignProvider','tsPromotionProvider'],true);
            document.getElementById('tsCampaignSyncForm').addEventListener('submit',async function(e){e.preventDefault();const provider=document.getElementById('tsCampaignSyncProvider').value;if(!provider){UI.toast('请选择 Provider','danger');return;}try{const d=await R.post('/api/admin/ticket-source-v11-sync/'+encodeURIComponent(provider)+'/campaign-assets/sync'+qs({cityCode:document.getElementById('tsCampaignCityCode').value}));json(document.getElementById('tsCampaignResult'),d);UI.toast('活动素材同步完成','success');await loadCampaignAssets();}catch(err){errorToast(err);}});
            document.getElementById('tsCampaignFilterForm').addEventListener('submit',function(e){e.preventDefault();loadCampaignAssets();});
            document.getElementById('tsPromotionFilterForm').addEventListener('submit',function(e){e.preventDefault();loadPromotions();});
            document.getElementById('tsCampaignPublishForm').addEventListener('submit',publishCampaignBanner);
            document.getElementById('tsCampaignChooseProject').addEventListener('click',function(){AdminProjectSelector.open({title:'选择本地 Banner 跳转项目',confirmText:'使用此项目',selectedProjectId:document.getElementById('tsCampaignPublishProjectId').value,onSelect:async function(item){document.getElementById('tsCampaignPublishProjectId').value=item.projectId;document.getElementById('tsCampaignPublishProjectTitle').value=item.title||'';await loadCampaignPublishSessions(item.projectId,null);}});});
            await Promise.all([loadCampaignAssets(),loadPromotions()]);
        }
        if(mode==='settlements'){
            await providers(['tsSettlementCreateProvider'],false);
            await providers(['tsSettlementProvider'],true);
            document.getElementById('tsSettlementCreateForm').addEventListener('submit',createSettlement);
            document.getElementById('tsSettlementFilterForm').addEventListener('submit',function(e){e.preventDefault();loadSettlements();});
            document.getElementById('tsSettlementAdjustmentForm').addEventListener('submit',addSettlementAdjustment);
            const now=new Date();const first=new Date(now.getFullYear(),now.getMonth(),1);const iso=function(d){const y=d.getFullYear(),m=String(d.getMonth()+1).padStart(2,'0'),day=String(d.getDate()).padStart(2,'0');return y+'-'+m+'-'+day;};
            document.getElementById('tsSettlementDateFrom').value=iso(first);document.getElementById('tsSettlementDateTo').value=iso(now);
            await loadSettlements();
        }
        if(mode==='orders'){
            await providers(['tsOrderProvider'],true);
            const sizeSelect=document.getElementById('tsOrderPageSize');
            orderPageState.pageSize=Number(sizeSelect&&sizeSelect.value||20);
            document.getElementById('tsOrderFilterForm').addEventListener('submit',function(e){e.preventDefault();orderPageState.pageNo=1;loadOrders();});
            if(sizeSelect) sizeSelect.addEventListener('change',function(){orderPageState.pageSize=Number(this.value||20);orderPageState.pageNo=1;loadOrders();});
            document.getElementById('tsExpireDueBtn').addEventListener('click',function(){confirmRun('确认扫描并处理已经到期的第三方 WAIT_PAY？该动作会调用对应 Adapter 取消 Provider 预留。',async function(){const d=await R.post('/api/admin/ticket-source-orders/expire-due?limit=100');json(document.getElementById('tsOrderResult'),d);UI.toast('到期订单扫描完成','success');orderPageState.pageNo=1;await loadOrders();});});
            await loadOrders();
        }
        if(mode==='fulfillment'){
            document.getElementById('tsIssueFilterForm').addEventListener('submit',function(e){e.preventDefault();loadIssues();});
            document.getElementById('tsShipmentFilterForm').addEventListener('submit',function(e){e.preventDefault();loadShipments();});
            document.getElementById('tsProcessIssueDue').addEventListener('click',function(){confirmRun('手动触发一次自动履约任务扫描？系统平时会自动执行；本按钮只用于测试/运维，不表示管理员需要逐单履约。',async function(){try{const d=await R.post('/api/admin/ticket-source-issues/process-due?limit=100');json(document.getElementById('tsFulfillmentResult'),d);UI.toast('自动履约任务扫描完成','success');await loadIssues();}catch(e){errorToast(e);}});});
            document.getElementById('tsSyncShipmentPending').addEventListener('click',async function(){try{const d=await R.post('/api/admin/ticket-source-v11-shipments/sync-pending?limit=100&staleMinutes=5&deliveredStaleMinutes=1440');json(document.getElementById('tsFulfillmentResult'),d);UI.toast('物流批量同步完成','success');await loadShipments();}catch(e){errorToast(e);}});
            await Promise.all([loadIssues(),loadShipments()]);
        }
        if(mode==='refunds'){
            document.getElementById('tsRefundFilterForm').addEventListener('submit',function(e){e.preventDefault();loadRefunds();});
            document.getElementById('tsProcessRefundDue').addEventListener('click',function(){confirmRun('手动触发一次自动售后协同任务扫描？系统平时会自动处理已审核退款；PENDING_REVIEW 仍必须走退款审核。',async function(){try{const d=await R.post('/api/admin/ticket-source-refunds/process-due?limit=100');json(document.getElementById('tsRefundResult'),d);UI.toast('自动售后协同任务扫描完成','success');await loadRefunds();}catch(e){errorToast(e);}});});
            await loadRefunds();
        }
        if(mode==='operations'){
            await providers(['tsReconcileProvider'],false); await providers(['tsLogProvider'],true);
            document.getElementById('tsCallbackFilterForm').addEventListener('submit',function(e){e.preventDefault();loadCallbacks();});
            document.getElementById('tsReconcileForm').addEventListener('submit',function(e){e.preventDefault();reconcile();});
            document.getElementById('tsGatewayLogFilter').addEventListener('submit',function(e){e.preventDefault();loadLogs();});
            document.getElementById('tsProcessCallbacks').addEventListener('click',async function(){try{const d=await R.post('/api/admin/ticket-source-v12/callbacks/pending:process?limit=100');json(document.getElementById('tsOperationsResult'),d);UI.toast('回调补偿完成','success');await loadCallbacks();}catch(e){errorToast(e);}});
            document.getElementById('tsRefreshReconciliations').addEventListener('click',loadReconciliations);
            await Promise.all([loadCallbacks(),loadReconciliations(),loadLogs()]);
        }
        if(mode==='mock'){
            document.getElementById('tsMockRefresh').addEventListener('click',loadMock);
            document.getElementById('tsMockReset').addEventListener('click',function(){confirmRun('确认 Reset 所有 MOCK_DAMAI 行为？这会清除强制错误与延迟。',async function(){const d=await R.post('/api/admin/ticket-source-mock/v11/behaviors/reset');json(document.getElementById('tsMockResult'),d);UI.toast('MOCK_DAMAI 已恢复默认','success');await loadMock();});});
            document.getElementById('tsMockPriceForm').addEventListener('submit',function(e){e.preventDefault();updateMockPrice();});
            await loadMock();
        }
    }
    init().catch(errorToast);
})();
