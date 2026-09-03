(function () {
    'use strict';

    const modal = document.getElementById('mediaSelectorModal');
    if (!modal) return;

    const state = {
        activePicker: null,
        confirmCallback: null,
        businessType: '',
        mediaType: 'IMAGE',
        title: '选择图片',
        pageNo: 1,
        pageSize: 20,
        total: 0,
        items: [],
        selectedItem: null,
        currentValue: '',
        requestSerial: 0
    };

    const businessLabels = {
        PROJECT_POSTER: '演出项目 / 海报',
        BANNER_IMAGE: '运营配置 / Banner',
        CATEGORY_ICON: '运营配置 / 分类图标',
        NOTICE_ICON: '运营配置 / 观演须知图标',
        PROJECT_DETAIL_IMAGE: '演出项目 / 详情图片',
        PROJECT_DETAIL_VIDEO: '演出项目 / 详情视频',
        SESSION_DETAIL_IMAGE: '演出场次 / 城市站详情图片',
        SESSION_DETAIL_VIDEO: '演出场次 / 城市站详情视频'
    };

    const title = document.getElementById('mediaSelectorTitle');
    const searchForm = document.getElementById('mediaSelectorSearchForm');
    const keyword = document.getElementById('mediaSelectorKeyword');
    const grid = document.getElementById('mediaSelectorGrid');
    const totalText = document.getElementById('mediaSelectorTotalText');
    const pageText = document.getElementById('mediaSelectorPageText');
    const prevButton = document.getElementById('mediaSelectorPrevButton');
    const nextButton = document.getElementById('mediaSelectorNextButton');
    const selectedText = document.getElementById('mediaSelectorSelectedText');
    const confirmButton = document.getElementById('mediaSelectorConfirmButton');
    const uploadButton = document.getElementById('mediaSelectorUploadButton');
    const uploadInput = document.getElementById('mediaSelectorUploadInput');
    const uploadHelp = document.getElementById('mediaSelectorUploadHelp');
    const currentBusiness = document.getElementById('mediaSelectorCurrentBusiness');

    function normalizeUrl(value) {
        return String(value || '').trim();
    }

    function sameUrl(left, right) {
        return normalizeUrl(left) === normalizeUrl(right);
    }

    function inferMediaType(businessType, explicitType) {
        const normalized = String(explicitType || '').trim().toUpperCase();
        if (normalized === 'IMAGE' || normalized === 'VIDEO') return normalized;
        return String(businessType || '').toUpperCase().endsWith('_VIDEO') ? 'VIDEO' : 'IMAGE';
    }

    function mediaLabel() {
        return state.mediaType === 'VIDEO' ? '视频' : '图片';
    }

    function formatFileSize(size) {
        const value = Number(size || 0);
        if (value < 1024) return value + ' B';
        if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB';
        return (value / 1024 / 1024).toFixed(1) + ' MB';
    }

    function getTargetInput(picker) {
        if (!picker) return null;
        const targetId = picker.dataset.targetInput;
        return targetId ? document.getElementById(targetId) : null;
    }

    function getCurrentValue(picker) {
        const input = getTargetInput(picker);
        return input ? normalizeUrl(input.value) : '';
    }

    function updatePickerSummary(picker) {
        if (!picker) return;
        const value = getCurrentValue(picker);
        const summary = picker.querySelector('[data-media-picker-summary]');
        const clearButton = picker.querySelector('[data-media-picker-clear]');
        if (summary) {
            summary.textContent = value || '尚未选择图片';
            summary.title = value;
        }
        if (clearButton) clearButton.disabled = !value;
    }

    function dispatchValueChanged(input) {
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function setPickerValue(picker, value) {
        const input = getTargetInput(picker);
        if (!input) return;
        input.value = normalizeUrl(value);
        updatePickerSummary(picker);
        dispatchValueChanged(input);
    }

    function updateSelectionSummary() {
        const label = mediaLabel();
        if (state.selectedItem) {
            selectedText.innerHTML = '已选择' + label + '：<strong>' +
                AdminUI.escapeHtml(state.selectedItem.originalName || state.selectedItem.fileName || '-') +
                '</strong><span>' + AdminUI.escapeHtml(state.selectedItem.url || '') + '</span>';
            confirmButton.disabled = false;
            return;
        }
        if (state.currentValue) {
            selectedText.textContent = '当前已保存：' + state.currentValue;
        } else {
            selectedText.textContent = '尚未选择' + label;
        }
        confirmButton.disabled = true;
    }

    function mediaPreview(item) {
        const url = AdminUI.escapeHtml(item.url || '');
        if (state.mediaType === 'VIDEO') {
            return '<span class="media-selector-thumb is-video"><video src="' + url +
                '" preload="metadata" muted></video><i>VIDEO</i></span>';
        }
        return '<span class="media-selector-thumb"><img src="' + url +
            '" alt="" loading="lazy" onerror="this.closest(\'.media-selector-thumb\').classList.add(\'is-error\');this.remove()"></span>';
    }

    function renderGrid() {
        const label = mediaLabel();
        if (!state.items.length) {
            grid.innerHTML = '<div class="table-empty">当前业务目录还没有' + label + '，请先上传本地' + label + '。</div>';
            return;
        }
        grid.innerHTML = state.items.map(function (item) {
            const selected = state.selectedItem && sameUrl(state.selectedItem.url, item.url);
            const current = !selected && state.currentValue && sameUrl(state.currentValue, item.url);
            const marker = selected ? '<span class="media-selector-marker">已选择</span>' :
                (current ? '<span class="media-selector-marker is-current">当前使用</span>' : '');
            return '<button class="media-selector-item ' + (selected ? 'is-selected' : '') +
                (current ? ' is-current' : '') + '" type="button" data-media-url="' +
                AdminUI.escapeHtml(item.url || '') + '" title="双击可直接确认选择">' +
                marker + mediaPreview(item) +
                '<span class="media-selector-name" title="' + AdminUI.escapeHtml(item.originalName || item.fileName || '') + '">' +
                AdminUI.escapeHtml(item.originalName || item.fileName || '-') + '</span>' +
                '<span class="media-selector-meta">' + AdminUI.escapeHtml(formatFileSize(item.fileSize)) + '</span>' +
                '</button>';
        }).join('');
    }

    function renderPager() {
        const totalPages = Math.max(1, Math.ceil(state.total / state.pageSize));
        if (state.pageNo > totalPages) state.pageNo = totalPages;
        pageText.textContent = '第 ' + state.pageNo + ' / ' + totalPages + ' 页';
        prevButton.disabled = state.pageNo <= 1;
        nextButton.disabled = state.pageNo >= totalPages;
        totalText.textContent = '共 ' + AdminUI.formatNumber(state.total) + ' 个' + mediaLabel();
    }

    function restoreCurrentSelectionFromItems() {
        if (state.selectedItem || !state.currentValue) return;
        const currentItem = state.items.find(function (candidate) {
            return sameUrl(candidate.url, state.currentValue);
        });
        if (currentItem) state.selectedItem = currentItem;
    }

    async function loadMedia() {
        const requestId = ++state.requestSerial;
        grid.innerHTML = '<div class="table-empty">正在加载' + mediaLabel() + '...</div>';
        const params = new URLSearchParams({
            businessType: state.businessType,
            mediaType: state.mediaType,
            pageNo: String(state.pageNo),
            pageSize: String(state.pageSize)
        });
        if (keyword.value.trim()) params.set('keyword', keyword.value.trim());
        try {
            const data = await AdminRequest.get('/api/admin/media?' + params.toString());
            if (requestId !== state.requestSerial) return;
            state.total = Number(data.total || 0);
            state.pageNo = Number(data.pageNo || state.pageNo);
            state.pageSize = Number(data.pageSize || state.pageSize);
            state.items = data.items || [];
            restoreCurrentSelectionFromItems();
            renderGrid();
            renderPager();
            updateSelectionSummary();
        } catch (error) {
            if (requestId !== state.requestSerial) return;
            state.items = [];
            state.total = 0;
            grid.innerHTML = '<div class="table-empty">' + AdminUI.escapeHtml(error.message) + '</div>';
            renderPager();
            AdminUI.toast(error.message, 'error');
        }
    }

    function selectItemByUrl(url) {
        const item = state.items.find(function (candidate) {
            return sameUrl(candidate.url, url);
        });
        if (!item) return;
        state.selectedItem = item;
        renderGrid();
        updateSelectionSummary();
    }

    function validateUploadFile(file) {
        const extension = String(file.name || '').split('.').pop().toLowerCase();
        if (state.mediaType === 'VIDEO') {
            const allowedTypes = ['video/mp4', 'video/webm'];
            const allowedExtensions = ['mp4', 'webm'];
            if (!allowedTypes.includes(file.type) || !allowedExtensions.includes(extension)) {
                throw new Error('请选择 MP4 或 WEBM 视频');
            }
            if (file.size > 200 * 1024 * 1024) {
                throw new Error('单个视频不能超过 200 MB');
            }
            return;
        }
        const noticeIcon = state.businessType === 'NOTICE_ICON';
        const allowedTypes = noticeIcon
            ? ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml', 'text/xml', 'application/xml']
            : ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        const allowedExtensions = noticeIcon ? ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'] : ['jpg', 'jpeg', 'png', 'gif', 'webp'];
        if (!allowedTypes.includes(file.type) || !allowedExtensions.includes(extension)) {
            throw new Error(noticeIcon ? '请选择 JPG、PNG、GIF、WEBP 或 SVG 图片' : '请选择 JPG、PNG、GIF 或 WEBP 图片');
        }
        if (file.size > 10 * 1024 * 1024) {
            throw new Error('单张图片不能超过 10 MB');
        }
    }

    async function uploadSelectedFile() {
        const file = uploadInput.files && uploadInput.files.length ? uploadInput.files[0] : null;
        if (!file) return;
        try {
            validateUploadFile(file);
        } catch (error) {
            uploadInput.value = '';
            AdminUI.toast(error.message, 'warning');
            return;
        }
        const formData = new FormData();
        formData.append('file', file);
        formData.append('businessType', state.businessType);
        AdminUI.setButtonLoading(uploadButton, true, '上传中...');
        try {
            const item = await AdminRequest.request('/api/admin/media/upload', {
                method: 'POST',
                body: formData
            });
            state.selectedItem = item;
            state.pageNo = 1;
            keyword.value = '';
            AdminUI.toast(mediaLabel() + '上传成功，确认后才会插入当前内容', 'success');
            await loadMedia();
            state.selectedItem = item;
            renderGrid();
            updateSelectionSummary();
        } catch (error) {
            AdminUI.toast(error.message, 'error');
        } finally {
            uploadInput.value = '';
            AdminUI.setButtonLoading(uploadButton, false);
        }
    }

    function configureMediaUi() {
        const isVideo = state.mediaType === 'VIDEO';
        const noticeIcon = state.businessType === 'NOTICE_ICON';
        uploadInput.accept = isVideo
            ? 'video/mp4,video/webm'
            : noticeIcon
                ? 'image/jpeg,image/png,image/gif,image/webp,image/svg+xml,.svg'
                : 'image/jpeg,image/png,image/gif,image/webp';
        uploadButton.textContent = isVideo ? '上传本地视频' : noticeIcon ? '上传图标' : '上传本地图片';
        uploadHelp.textContent = isVideo
            ? '视频最大 200 MB，支持 MP4、WEBM。'
            : noticeIcon
                ? '图标最大 10 MB，支持 JPG、PNG、GIF、WEBP、SVG。'
                : '图片最大 10 MB，支持 JPG、PNG、GIF、WEBP。';
    }

    function openWithOptions(options) {
        const businessType = String(options.businessType || '').trim();
        if (!businessType) {
            AdminUI.toast('媒体选择器缺少业务类型配置', 'error');
            return;
        }
        state.activePicker = options.picker || null;
        state.confirmCallback = typeof options.onSelect === 'function' ? options.onSelect : null;
        state.businessType = businessType;
        state.mediaType = inferMediaType(businessType, options.mediaType);
        state.title = options.title || ('选择' + (state.mediaType === 'VIDEO' ? '视频' : '图片'));
        state.pageNo = 1;
        state.total = 0;
        state.items = [];
        state.selectedItem = null;
        state.currentValue = normalizeUrl(options.currentValue || '');
        title.textContent = state.title;
        keyword.value = '';
        currentBusiness.textContent = '当前业务目录：' +
            (businessLabels[state.businessType] || state.businessType);
        configureMediaUi();
        updateSelectionSummary();
        AdminUI.openModal(modal);
        loadMedia();
    }

    function open(picker) {
        openWithOptions({
            picker: picker,
            businessType: picker.dataset.businessType || '',
            mediaType: picker.dataset.mediaType || '',
            title: picker.dataset.title || '选择图片',
            currentValue: getCurrentValue(picker)
        });
    }

    function pick(options) {
        openWithOptions(options || {});
    }

    function close() {
        state.requestSerial += 1;
        AdminUI.closeModal(modal);
        state.activePicker = null;
        state.confirmCallback = null;
        state.selectedItem = null;
        state.currentValue = '';
    }

    document.querySelectorAll('[data-media-picker]').forEach(function (picker) {
        updatePickerSummary(picker);
        const openButton = picker.querySelector('[data-media-picker-open]');
        const clearButton = picker.querySelector('[data-media-picker-clear]');
        const input = getTargetInput(picker);
        if (openButton) openButton.addEventListener('click', function () { open(picker); });
        if (clearButton) clearButton.addEventListener('click', function () { setPickerValue(picker, ''); });
        if (input) input.addEventListener('input', function () { updatePickerSummary(picker); });
    });

    searchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        state.pageNo = 1;
        state.selectedItem = null;
        loadMedia();
    });
    document.getElementById('mediaSelectorResetButton').addEventListener('click', function () {
        keyword.value = '';
        state.pageNo = 1;
        state.selectedItem = null;
        loadMedia();
    });
    grid.addEventListener('click', function (event) {
        const item = event.target.closest('[data-media-url]');
        if (item) selectItemByUrl(item.dataset.mediaUrl);
    });
    grid.addEventListener('dblclick', function (event) {
        const item = event.target.closest('[data-media-url]');
        if (!item) return;
        selectItemByUrl(item.dataset.mediaUrl);
        confirmButton.click();
    });
    prevButton.addEventListener('click', function () {
        if (state.pageNo <= 1) return;
        state.pageNo -= 1;
        state.selectedItem = null;
        loadMedia();
    });
    nextButton.addEventListener('click', function () {
        const totalPages = Math.max(1, Math.ceil(state.total / state.pageSize));
        if (state.pageNo >= totalPages) return;
        state.pageNo += 1;
        state.selectedItem = null;
        loadMedia();
    });
    uploadButton.addEventListener('click', function () { uploadInput.click(); });
    uploadInput.addEventListener('change', uploadSelectedFile);
    confirmButton.addEventListener('click', function () {
        if (!state.selectedItem) {
            AdminUI.toast('请先选择' + mediaLabel(), 'warning');
            return;
        }
        const item = state.selectedItem;
        if (state.confirmCallback) {
            state.confirmCallback(item);
        } else if (state.activePicker) {
            setPickerValue(state.activePicker, item.url);
        }
        close();
    });
    document.getElementById('mediaSelectorCloseButton').addEventListener('click', close);
    document.getElementById('mediaSelectorCancelButton').addEventListener('click', close);
    modal.addEventListener('click', function (event) {
        if (event.target === modal) close();
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) close();
    });

    window.AdminMediaSelector = {
        open: open,
        pick: pick,
        close: close,
        update: updatePickerSummary
    };
})();
