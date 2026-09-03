(function () {
    'use strict';

    const page = document.getElementById('projectFormPage');
    const form = document.getElementById('projectForm');
    if (!page || !form) return;

    const mode = page.dataset.mode || 'create';
    const projectId = page.dataset.projectId ? Number(page.dataset.projectId) : null;
    const saveButton = document.getElementById('saveProjectButton');
    const posterInput = document.getElementById('posterUrl');
    const preview = document.getElementById('posterPreview');
    const previewImage = document.getElementById('posterPreviewImage');
    let currentProject = null;
    let allServiceTags = [];
    let serviceTagConfig = null;
    let allNotices = [];
    let noticeConfig = null;

    function updatePosterPreview() {
        const value = posterInput.value.trim();
        if (!AdminUI.isImageUrl(value)) {
            previewImage.hidden = true;
            preview.querySelector('span').hidden = false;
            preview.querySelector('span').textContent = value ? '当前图片值无法在后台预览' : '暂无可预览图片';
            return;
        }
        preview.querySelector('span').hidden = true;
        previewImage.hidden = false;
        previewImage.src = value;
    }

    previewImage.addEventListener('error', function () {
        previewImage.hidden = true;
        preview.querySelector('span').hidden = false;
        preview.querySelector('span').textContent = '图片加载失败，请检查海报地址';
    });

    function buildRequest() {
        const operation = {
            hotScore: Number(document.getElementById('hotScore').value || 0),
            publishTime: AdminUI.nullableText(document.getElementById('publishTime').value)
        };
        if (currentProject && currentProject.sourceManaged === true) {
            return operation;
        }
        return {
            title: document.getElementById('title').value.trim(),
            categoryId: Number(document.getElementById('categoryId').value),
            posterUrl: posterInput.value.trim(),
            detailContent: AdminUI.nullableText(AdminRichTextEditor.get('detailContent')),
            hotScore: operation.hotScore,
            publishTime: operation.publishTime
        };
    }

    function renderPublishState(item) {
        currentProject = item;
        const status = document.getElementById('projectCurrentStatus');
        const recommend = document.getElementById('projectCurrentRecommend');
        if (status) status.innerHTML = AdminUI.statusBadge(item.projectStatus || 'OFFLINE');
        if (recommend) recommend.textContent = Number(item.recommendFlag) === 1 ? '是' : '否';
        const toggleButton = document.getElementById('toggleRecommendButton');
        if (toggleButton) {
            toggleButton.textContent = Number(item.recommendFlag) === 1 ? '取消首页推荐' : '设为首页推荐';
            toggleButton.disabled = item.projectStatus === 'OFFLINE' && Number(item.recommendFlag) !== 1;
            toggleButton.title = toggleButton.disabled ? '请先上架项目' : '';
        }
    }

    function applySourceManagedMode(item) {
        const sourceManaged = item && item.sourceManaged === true;
        const notice = document.getElementById('sourceManagedProjectNotice');
        const providerSection = document.getElementById('providerSourceSection');
        const coreTitle = document.getElementById('projectCoreSectionTitle');
        const coreHint = document.getElementById('projectCoreSectionHint');
        const operationHint = document.getElementById('projectOperationSectionHint');
        const save = document.getElementById('saveProjectButton');
        if (notice) notice.hidden = !sourceManaged;
        if (providerSection) providerSection.hidden = !sourceManaged;
        if (coreTitle) coreTitle.textContent = sourceManaged ? 'Provider 权威事实数据（标准化后）' : '本地演出基础信息';
        if (coreHint) coreHint.textContent = sourceManaged
            ? '标题、分类、海报和简介/详情来自 Provider，同步写入 performance_project；这里只读，不能作为麦麦长期运营覆盖。'
            : '带 * 的字段为必填项。';
        if (operationHint) operationHint.textContent = sourceManaged
            ? '首页排序分和平台发布时间属于麦麦运营字段，Provider 重新同步不会覆盖。'
            : '本地项目的运营字段由麦麦后台维护。';
        if (save) save.textContent = sourceManaged ? '保存平台运营配置' : '保存演出信息';

        ['title', 'categoryId'].forEach(function (id) {
            const element = document.getElementById(id);
            if (element) element.disabled = sourceManaged;
        });
        const posterPicker = posterInput.closest('[data-media-picker]');
        if (posterPicker) {
            posterPicker.querySelectorAll('button').forEach(function (button) { button.disabled = sourceManaged; });
        }
        const richRoot = document.querySelector('[data-rich-text-editor][data-input-id="detailContent"]');
        if (richRoot) {
            const surface = richRoot.querySelector('.rich-text-surface');
            if (surface) {
                surface.setAttribute('contenteditable', sourceManaged ? 'false' : 'true');
                surface.classList.toggle('rich-text-surface-readonly', sourceManaged);
            }
            richRoot.querySelectorAll('.rich-text-toolbar button, .rich-text-toolbar select').forEach(function (control) {
                control.disabled = sourceManaged;
            });
        }
    }

    function fillProviderSource(item) {
        if (!item.sourceManaged) return;
        document.getElementById('providerCodeText').textContent = item.providerCode || '-';
        document.getElementById('providerProjectIdText').textContent = item.providerProjectId || '-';
        document.getElementById('providerMappingStatusText').textContent = (item.mappingStatus || '-') + ' / ' + (item.lastSyncStatus || '-');
        document.getElementById('providerSaleStatusText').textContent = (item.providerSaleStatus || '-') + (item.providerStatusText ? ' · ' + item.providerStatusText : '');
        document.getElementById('providerLastSyncText').textContent = AdminUI.formatDateTime(item.lastSyncTime);
        document.getElementById('providerSourceUpdatedText').textContent = AdminUI.formatDateTime(item.sourceUpdatedTime);
        document.getElementById('providerProjectNameText').textContent = item.providerProjectName || item.title || '-';
        document.getElementById('providerIntroductionText').textContent = item.providerIntroduction || item.providerSubtitle || 'Provider 未提供独立简介';
        const providerPosterText = document.getElementById('providerPosterText');
        if (providerPosterText) providerPosterText.textContent = item.providerPosterUrl || 'Provider 未提供海报';
        document.getElementById('providerDetailPreview').innerHTML = item.providerDetailContent
            ? AdminRichTextEditor.sanitizePreviewHtml(item.providerDetailContent)
            : '<span class="table-meta">Provider 未提供详情</span>';

    }

    function fillProject(item) {
        document.getElementById('title').value = item.title || '';
        document.getElementById('categoryId').value = item.categoryId == null ? '' : String(item.categoryId);
        posterInput.value = item.posterUrl || '';
        posterInput.dispatchEvent(new Event('input', { bubbles: true }));
        AdminRichTextEditor.set('detailContent', item.detailContent || '');
        document.getElementById('hotScore').value = item.hotScore == null ? '0' : item.hotScore;
        document.getElementById('publishTime').value = AdminUI.toDateTimeLocal(item.publishTime);
        document.getElementById('projectReadOnlyPanel').hidden = false;
        document.getElementById('readonlyProjectId').textContent = item.projectId == null ? '-' : item.projectId;
        document.getElementById('readonlyPrice').textContent = item.minPrice == null
            ? '暂无票档价格'
            : (Number(item.minPrice) === Number(item.maxPrice)
                ? AdminUI.formatMoney(item.minPrice)
                : AdminUI.formatMoney(item.minPrice) + ' - ' + AdminUI.formatMoney(item.maxPrice));
        document.getElementById('readonlyWantCount').textContent = AdminUI.formatNumber(item.wantCount);
        document.getElementById('readonlyCreateTime').textContent = AdminUI.formatDateTime(item.createTime);
        document.getElementById('readonlyUpdateTime').textContent = AdminUI.formatDateTime(item.updateTime);
        renderPublishState(item);
        applySourceManagedMode(item);
        fillProviderSource(item);
        updatePosterPreview();
    }

    function tagChip(tag, prefix) {
        return '<span class="status-badge">' + AdminUI.escapeHtml((prefix || '') + (tag.tagName || '-')) + '</span>';
    }

    function renderServiceTags() {
        const generatedBox = document.getElementById('providerServiceTagsBox');
        const manualBox = document.getElementById('manualServiceTagOptions');
        if (!generatedBox || !manualBox || !serviceTagConfig) return;
        const generated = (serviceTagConfig.providerTags || []).concat(serviceTagConfig.automaticRefundTags || []);
        generatedBox.innerHTML = generated.length ? generated.map(function (tag) { return tagChip(tag, ''); }).join('') : '<span class="table-meta">暂无系统生成标签</span>';
        const manualIds = {};
        (serviceTagConfig.manualTags || []).forEach(function (tag) { manualIds[String(tag.tagId)] = true; });
        const providerIds = {};
        (serviceTagConfig.providerTags || []).forEach(function (tag) { providerIds[String(tag.tagId)] = true; });
        const candidates = allServiceTags.filter(function (tag) {
            return !tag.systemRefundTag && !providerIds[String(tag.tagId)];
        });
        manualBox.innerHTML = candidates.length ? candidates.map(function (tag) {
            return '<label class="checkbox-option"><input type="checkbox" data-manual-tag-id="' + tag.tagId + '"' + (manualIds[String(tag.tagId)] ? ' checked' : '') + '> <span>' + AdminUI.escapeHtml(tag.tagName || '-') + '</span></label>';
        }).join('') : '<span class="table-meta">暂无可追加标签</span>';
    }

    async function loadServiceTags() {
        if (!projectId) return;
        try {
            const results = await Promise.all([
                AdminRequest.get('/api/admin/service-tags'),
                AdminRequest.get('/api/admin/projects/' + projectId + '/service-tags')
            ]);
            allServiceTags = results[0] || [];
            serviceTagConfig = results[1];
            renderServiceTags();
        } catch (error) {
            const generatedBox = document.getElementById('providerServiceTagsBox');
            const manualBox = document.getElementById('manualServiceTagOptions');
            if (generatedBox) generatedBox.textContent = error.message;
            if (manualBox) manualBox.textContent = error.message;
            AdminUI.toast(error.message, 'error');
        }
    }

    async function saveServiceTags() {
        const ids = Array.from(document.querySelectorAll('[data-manual-tag-id]:checked')).map(function (input) { return Number(input.dataset.manualTagId); });
        try {
            serviceTagConfig = await AdminRequest.put('/api/admin/projects/' + projectId + '/service-tags', {tagIds: ids});
            AdminUI.toast('服务标签调整已保存', 'success');
            renderServiceTags();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    function noticeCard(notice) {
        const icon = notice.iconUrl ? '<span class="table-meta">图标：' + AdminUI.escapeHtml(notice.iconUrl) + '</span>' : '';
        return '<div class="mini-list-item"><div><strong>' + AdminUI.escapeHtml(notice.title || '-') + '</strong></div><div class="table-meta">' + AdminUI.escapeHtml(notice.description || '-') + '<br>' + icon + '</div></div>';
    }

    function renderNotices() {
        const providerBox = document.getElementById('providerNoticesBox');
        const manualBox = document.getElementById('manualNoticeOptions');
        if (!providerBox || !manualBox || !noticeConfig) return;
        const providerNotices = noticeConfig.providerNotices || [];
        providerBox.innerHTML = providerNotices.length ? '<div class="mini-list">' + providerNotices.map(noticeCard).join('') + '</div>' : '<span class="table-meta">Provider 暂无项目级须知</span>';
        const selected = {};
        (noticeConfig.projectNotices || []).forEach(function (notice) { selected[String(notice.noticeId)] = true; });
        const providerIds = {};
        providerNotices.forEach(function (notice) { providerIds[String(notice.noticeId)] = true; });
        const candidates = allNotices.filter(function (notice) { return !providerIds[String(notice.noticeId)]; });
        manualBox.innerHTML = candidates.length ? candidates.map(function (notice) {
            return '<label class="checkbox-option checkbox-option-block"><input type="checkbox" data-manual-notice-id="' + notice.noticeId + '"' + (selected[String(notice.noticeId)] ? ' checked' : '') + '> <span><strong>' + AdminUI.escapeHtml(notice.title || '-') + '</strong><small>' + AdminUI.escapeHtml(notice.description || '') + '</small></span></label>';
        }).join('') : '<span class="table-meta">暂无可补充须知</span>';
    }

    async function loadNotices() {
        if (!projectId) return;
        try {
            const results = await Promise.all([
                AdminRequest.get('/api/admin/notices'),
                AdminRequest.get('/api/admin/projects/' + projectId + '/notices')
            ]);
            allNotices = results[0] || [];
            noticeConfig = results[1];
            renderNotices();
        } catch (error) {
            const box = document.getElementById('manualNoticeOptions');
            if (box) box.textContent = error.message;
        }
    }

    async function saveNotices() {
        const ids = Array.from(document.querySelectorAll('[data-manual-notice-id]:checked')).map(function (input) { return Number(input.dataset.manualNoticeId); });
        try {
            noticeConfig = await AdminRequest.put('/api/admin/projects/' + projectId + '/notices', {noticeIds: ids});
            AdminUI.toast('观演须知本地补充已保存', 'success');
            renderNotices();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function loadRefundRule() {
        if (!projectId) return;
        const box = document.getElementById('refundRuleBox');
        if (!box) return;
        try {
            const rule = await AdminRequest.get('/api/admin/projects/' + projectId + '/refund-rule');
            if (!rule || !rule.refundRuleId) {
                box.innerHTML = '<strong>暂无退款规则</strong><p>第三方项目应重新执行 Provider 同步；本地自营项目可以在退款规则页面配置。</p>';
                return;
            }
            const sourceText = rule.sourceProviderId ? 'Provider 同步 · 只读' : '麦麦本地规则';
            const stages = (rule.stages || []).map(function (stage) {
                const max = stage.maxBeforeStartMinutes == null ? '∞' : stage.maxBeforeStartMinutes;
                const fee = stage.feeRate == null ? '-' : (Number(stage.feeRate) * 100).toFixed(2) + '%';
                return '<li>' + stage.minBeforeStartMinutes + ' ～ ' + max + ' 分钟：手续费 ' + fee + '</li>';
            }).join('');
            box.innerHTML = '<strong>' + AdminUI.escapeHtml(rule.refundType || '-') + ' · ' + sourceText + '</strong>' +
                '<p>' + AdminUI.escapeHtml(rule.ruleDescription || '无退款规则摘要') + '</p>' +
                '<p>用户退款入口：' + (rule.consumerEntryEnabled ? '开启' : '关闭') + '；配送费可退：' + (rule.deliveryFeeRefundable ? '是' : '否') + '</p>' +
                (stages ? '<ul>' + stages + '</ul>' : '');
        } catch (error) {
            box.textContent = error.message;
        }
    }

    async function loadProject() {
        if (mode !== 'edit' || !projectId) return;
        let loaded = false;
        AdminUI.setButtonLoading(saveButton, true, '读取中...');
        try {
            const item = await AdminRequest.get('/api/admin/performances/projects/' + projectId);
            fillProject(item);
            loaded = true;
            await Promise.all([loadServiceTags(), loadNotices(), loadRefundRule()]);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
            form.querySelectorAll('input, select, textarea, button, [contenteditable="true"]').forEach(function (element) {
                element.disabled = true;
            });
        } finally {
            if (loaded) AdminUI.setButtonLoading(saveButton, false);
        }
    }

    async function updateProjectStatus(status) {
        const label = {COMING_SOON: '即将开售', ON_SALE: '在售', SOLD_OUT: '已售罄', OFFLINE: '已下架'}[status] || status;
        const message = status === 'OFFLINE'
            ? '确认下架该项目吗？用户端将不再展示该项目。'
            : '确认将项目上架为“' + label + '”吗？系统会检查场次、票档和库存是否完整。';
        if (!await AdminUI.confirm(message, status === 'OFFLINE' ? '下架项目' : '上架项目')) return;
        try {
            await AdminRequest.put('/api/admin/performances/projects/' + projectId + '/status', {projectStatus: status});
            AdminUI.toast(status === 'OFFLINE' ? '项目已下架' : '项目状态已更新', 'success');
            const item = await AdminRequest.get('/api/admin/performances/projects/' + projectId);
            fillProject(item);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function toggleRecommend() {
        if (!currentProject) return;
        const target = Number(currentProject.recommendFlag) === 1 ? 0 : 1;
        if (!await AdminUI.confirm(target === 1 ? '确认设为首页推荐吗？' : '确认取消首页推荐吗？', '推荐设置')) return;
        try {
            await AdminRequest.put('/api/admin/performances/projects/' + projectId + '/status', {recommendFlag: target});
            AdminUI.toast('推荐设置已更新', 'success');
            const item = await AdminRequest.get('/api/admin/performances/projects/' + projectId);
            fillProject(item);
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        }
    }

    async function providerOperation(type) {
        if (!currentProject || !currentProject.providerProjectId) return;
        const url = '/api/admin/ticket-source-v11-sync/' + encodeURIComponent(currentProject.providerCode || 'MOCK_DAMAI') + '/projects/' + encodeURIComponent(currentProject.providerProjectId);
        const output = document.getElementById('providerSyncResult');
        try {
            let result;
            if (type === 'preview') {
                result = await AdminRequest.get(url + '/preview');
                output.hidden = false;
                output.textContent = JSON.stringify(result, null, 2);
                return;
            }
            const syncMessage = '本次同步会更新：\n' +
                '• 标题、分类、海报、简介/详情\n' +
                '• 场次、开售时间、票档、Provider 售价、结算价、库存与限购\n' +
                '• Provider 观演须知与退款规则\n\n' +
                '不会覆盖：\n' +
                '• 首页排序、首页推荐、麦麦上下架状态、平台发布时间\n' +
                '• 管理员服务标签与管理员项目须知';
            if (!await AdminUI.confirm(syncMessage, '重新同步 Provider')) return;
            result = await AdminRequest.post(url + '/sync', {syncInventory: true, syncCampaignAssets: false});
            output.hidden = false;
            output.textContent = JSON.stringify(result, null, 2);
            AdminUI.toast('Provider 同步完成', 'success');
            await loadProject();
        } catch (error) {
            output.hidden = false;
            output.textContent = error.message;
            AdminUI.toast(error.message, 'error');
        }
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        if (!form.reportValidity()) return;
        const request = buildRequest();
        const sourceManaged = currentProject && currentProject.sourceManaged === true;
        if (!sourceManaged && !request.categoryId) {
            AdminUI.toast('请选择演出分类', 'warning');
            return;
        }
        if (!sourceManaged && !request.posterUrl) {
            AdminUI.toast('请选择或上传演出海报', 'warning');
            return;
        }
        AdminUI.setButtonLoading(saveButton, true, '正在保存...');
        try {
            const saved = mode === 'edit'
                ? await AdminRequest.put('/api/admin/performances/projects/' + projectId, request)
                : await AdminRequest.post('/api/admin/performances/projects', request);
            const savedMessage = mode === 'edit'
                ? (currentProject && currentProject.sourceManaged === true ? '平台运营配置已更新' : '演出信息已更新')
                : '演出项目已创建并保持下架';
            AdminUI.toast(savedMessage, 'success');
            if (mode === 'edit') {
                fillProject(saved);
                AdminUI.setButtonLoading(saveButton, false);
            } else {
                window.setTimeout(function () {
                    window.location.href = '/admin/performances/projects/' + saved.projectId + '/edit';
                }, 300);
            }
        } catch (error) {
            AdminUI.toast(error.message, 'error');
            AdminUI.setButtonLoading(saveButton, false);
        }
    });

    posterInput.addEventListener('input', updatePosterPreview);
    [
        ['publishComingSoonButton', 'COMING_SOON'],
        ['publishOnSaleButton', 'ON_SALE'],
        ['publishSoldOutButton', 'SOLD_OUT'],
        ['unpublishProjectButton', 'OFFLINE']
    ].forEach(function (entry) {
        const button = document.getElementById(entry[0]);
        if (button) button.addEventListener('click', function () { updateProjectStatus(entry[1]); });
    });
    const toggleButton = document.getElementById('toggleRecommendButton');
    if (toggleButton) toggleButton.addEventListener('click', toggleRecommend);
    const serviceButton = document.getElementById('saveServiceTagsButton');
    if (serviceButton) serviceButton.addEventListener('click', saveServiceTags);
    const noticeButton = document.getElementById('saveNoticesButton');
    if (noticeButton) noticeButton.addEventListener('click', saveNotices);
    const previewProviderButton = document.getElementById('previewProviderProjectButton');
    if (previewProviderButton) previewProviderButton.addEventListener('click', function () { providerOperation('preview'); });
    const syncProviderButton = document.getElementById('syncProviderProjectButton');
    if (syncProviderButton) syncProviderButton.addEventListener('click', function () { providerOperation('sync'); });

    updatePosterPreview();
    loadProject();
})();
