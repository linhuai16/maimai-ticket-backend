(function () {
    'use strict';

    const editors = new Map();
    const ALLOWED_PREVIEW_TAGS = new Set([
        'P', 'BR', 'H2', 'H3', 'H4', 'STRONG', 'B', 'EM', 'I', 'U', 'S',
        'UL', 'OL', 'LI', 'BLOCKQUOTE', 'A', 'IMG', 'VIDEO', 'SOURCE',
        'FIGURE', 'FIGCAPTION', 'HR', 'DIV', 'SPAN', 'STRIKE'
    ]);
    const ALLOWED_PREVIEW_ATTRIBUTES = {
        A: new Set(['href', 'target', 'rel']),
        IMG: new Set(['src', 'alt', 'title']),
        VIDEO: new Set(['src', 'controls', 'preload']),
        SOURCE: new Set(['src', 'type'])
    };


    function escapeText(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function normalizeEditorValue(value) {
        const text = String(value || '');
        if (!text.trim()) return '';
        if (/<\/?[A-Za-z][^>]*>/.test(text)) return text;
        return text
            .replace(/\r\n|\r/g, '\n')
            .split(/\n{2,}/)
            .map(function (paragraph) {
                return '<p>' + escapeText(paragraph).replace(/\n/g, '<br>') + '</p>';
            })
            .join('');
    }

    function cleanEmptyHtml(value) {
        const html = String(value || '').trim();
        if (!html) return '';
        const holder = document.createElement('div');
        holder.innerHTML = html;
        const visualMedia = holder.querySelector('img,video,source');
        const text = (holder.textContent || '').replace(/\u00a0/g, ' ').trim();
        return !visualMedia && !text ? '' : holder.innerHTML.trim();
    }

    function saveSelection(instance) {
        const selection = window.getSelection();
        if (!selection || selection.rangeCount === 0) return;
        const range = selection.getRangeAt(0);
        if (instance.surface.contains(range.commonAncestorContainer)) {
            instance.savedRange = range.cloneRange();
        }
    }

    function restoreSelection(instance) {
        instance.surface.focus();
        const selection = window.getSelection();
        if (!selection) return;
        selection.removeAllRanges();
        if (instance.savedRange) {
            selection.addRange(instance.savedRange);
            return;
        }
        const range = document.createRange();
        range.selectNodeContents(instance.surface);
        range.collapse(false);
        selection.addRange(range);
    }

    function sync(instance) {
        const value = cleanEmptyHtml(instance.surface.innerHTML);
        instance.input.value = value;
        const textLength = (instance.surface.textContent || '').replace(/\s+/g, ' ').trim().length;
        const mediaCount = instance.surface.querySelectorAll('img,video').length;
        instance.counter.textContent = textLength + ' 字 · ' + mediaCount + ' 个媒体';
        instance.input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    function executeCommand(instance, command, value) {
        restoreSelection(instance);
        document.execCommand(command, false, value || null);
        saveSelection(instance);
        sync(instance);
    }

    function insertNode(instance, node) {
        restoreSelection(instance);
        const selection = window.getSelection();
        if (!selection || selection.rangeCount === 0) return;
        const range = selection.getRangeAt(0);
        range.deleteContents();
        range.insertNode(node);
        range.setStartAfter(node);
        range.collapse(true);
        selection.removeAllRanges();
        selection.addRange(range);
        instance.savedRange = range.cloneRange();
        sync(instance);
    }

    function insertImage(instance, url, name) {
        const figure = document.createElement('figure');
        const image = document.createElement('img');
        image.src = url;
        image.alt = name || '';
        figure.appendChild(image);
        insertNode(instance, figure);
    }

    function insertVideo(instance, url) {
        const figure = document.createElement('figure');
        const video = document.createElement('video');
        video.src = url;
        video.controls = true;
        video.preload = 'metadata';
        figure.appendChild(video);
        insertNode(instance, figure);
    }

    function pickMedia(instance, mediaType) {
        if (!window.AdminMediaSelector || typeof window.AdminMediaSelector.pick !== 'function') {
            AdminUI.toast('媒体选择器未加载', 'error');
            return;
        }
        saveSelection(instance);
        const isVideo = mediaType === 'VIDEO';
        const businessType = isVideo ? instance.videoBusinessType : instance.imageBusinessType;
        window.AdminMediaSelector.pick({
            businessType: businessType,
            mediaType: mediaType,
            title: isVideo ? '选择详情视频' : '选择详情图片',
            onSelect: function (item) {
                if (isVideo) insertVideo(instance, item.url);
                else insertImage(instance, item.url, item.originalName || item.fileName || '');
            }
        });
    }

    function insertPlainText(text) {
        document.execCommand('insertText', false, text);
    }

    function initialize(root) {
        const inputId = root.dataset.inputId;
        const input = inputId ? document.getElementById(inputId) : null;
        const surface = root.querySelector('.rich-text-surface');
        const counter = root.querySelector('[data-rich-counter]');
        if (!inputId || !input || !surface || !counter) return;

        const instance = {
            root: root,
            input: input,
            surface: surface,
            counter: counter,
            savedRange: null,
            imageBusinessType: root.dataset.imageBusinessType || '',
            videoBusinessType: root.dataset.videoBusinessType || ''
        };
        editors.set(inputId, instance);

        root.querySelectorAll('[data-rich-command]').forEach(function (button) {
            button.addEventListener('mousedown', function (event) {
                event.preventDefault();
                saveSelection(instance);
            });
            button.addEventListener('click', function () {
                executeCommand(instance, button.dataset.richCommand);
            });
        });

        const format = root.querySelector('[data-rich-format]');
        if (format) {
            format.addEventListener('change', function () {
                executeCommand(instance, 'formatBlock', format.value);
                format.value = 'p';
            });
        }

        root.querySelectorAll('[data-rich-action]').forEach(function (button) {
            button.addEventListener('mousedown', function () { saveSelection(instance); });
            button.addEventListener('click', async function () {
                const action = button.dataset.richAction;
                if (action === 'image') pickMedia(instance, 'IMAGE');
                if (action === 'video') pickMedia(instance, 'VIDEO');
                if (action === 'link') {
                    const url = window.prompt('请输入 http 或 https 链接地址');
                    if (!url) return;
                    const safeUrl = url.trim();
                    if (!/^https?:\/\//i.test(safeUrl)) {
                        AdminUI.toast('链接必须以 http:// 或 https:// 开头', 'warning');
                        return;
                    }
                    executeCommand(instance, 'createLink', safeUrl);
                    instance.surface.querySelectorAll('a').forEach(function (anchor) {
                        if (anchor.getAttribute('href') === safeUrl) {
                            anchor.target = '_blank';
                            anchor.rel = 'noopener noreferrer';
                        }
                    });
                    sync(instance);
                }
                if (action === 'clear') {
                    const confirmed = await AdminUI.confirm('确认清空全部富文本内容吗？', '清空详情');
                    if (!confirmed) return;
                    instance.surface.innerHTML = '';
                    instance.savedRange = null;
                    sync(instance);
                }
            });
        });

        surface.addEventListener('input', function () {
            saveSelection(instance);
            sync(instance);
        });
        surface.addEventListener('keyup', function () { saveSelection(instance); });
        surface.addEventListener('mouseup', function () { saveSelection(instance); });
        surface.addEventListener('paste', function (event) {
            event.preventDefault();
            const text = event.clipboardData ? event.clipboardData.getData('text/plain') : '';
            insertPlainText(text);
            sync(instance);
        });
        surface.addEventListener('drop', function (event) {
            event.preventDefault();
            AdminUI.toast('请使用“插入图片”或“插入视频”上传媒体文件', 'warning');
        });

        surface.innerHTML = normalizeEditorValue(input.value);
        sync(instance);
    }

    function setValue(inputId, value) {
        const instance = editors.get(inputId);
        if (!instance) return;
        instance.surface.innerHTML = normalizeEditorValue(value);
        instance.savedRange = null;
        sync(instance);
    }

    function getValue(inputId) {
        const instance = editors.get(inputId);
        if (!instance) {
            const input = document.getElementById(inputId);
            return input ? cleanEmptyHtml(input.value) : '';
        }
        sync(instance);
        return instance.input.value;
    }

    function sanitizePreviewHtml(value) {
        const template = document.createElement('template');
        template.innerHTML = String(value || '');
        const all = Array.from(template.content.querySelectorAll('*'));
        all.forEach(function (element) {
            if (!ALLOWED_PREVIEW_TAGS.has(element.tagName)) {
                element.replaceWith(document.createTextNode(element.textContent || ''));
                return;
            }
            Array.from(element.attributes).forEach(function (attribute) {
                const allowed = ALLOWED_PREVIEW_ATTRIBUTES[element.tagName] || new Set();
                if (!allowed.has(attribute.name.toLowerCase()) || attribute.name.toLowerCase().startsWith('on')) {
                    element.removeAttribute(attribute.name);
                }
            });
            if (element.tagName === 'A') {
                const href = element.getAttribute('href') || '';
                if (!/^https?:\/\//i.test(href)) element.removeAttribute('href');
                element.setAttribute('target', '_blank');
                element.setAttribute('rel', 'noopener noreferrer');
            }
            if (element.tagName === 'IMG' || element.tagName === 'VIDEO' || element.tagName === 'SOURCE') {
                const src = element.getAttribute('src') || '';
                if (!src.startsWith('/media/')) element.remove();
            }
            if (element.tagName === 'VIDEO') {
                element.setAttribute('controls', 'controls');
                element.setAttribute('preload', 'metadata');
            }
        });
        return template.innerHTML;
    }

    document.querySelectorAll('[data-rich-text-editor]').forEach(initialize);
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            editors.forEach(sync);
        }, true);
    });

    window.AdminRichTextEditor = {
        get: getValue,
        set: setValue,
        sanitizePreviewHtml: sanitizePreviewHtml
    };
})();
