(function () {
    'use strict';

    let redirectingToLogin = false;

    function redirectToLogin() {
        if (redirectingToLogin) return;
        redirectingToLogin = true;
        const currentPath = window.location.pathname + window.location.search + window.location.hash;
        const loginUrl = '/admin/login?redirect=' + encodeURIComponent(currentPath);
        window.location.replace(loginUrl);
    }

    async function request(url, options) {
        const config = Object.assign({ credentials: 'same-origin', cache: 'no-store' }, options || {});
        config.headers = Object.assign({ Accept: 'application/json' }, config.headers || {});
        if (config.body && !(config.body instanceof FormData) && typeof config.body !== 'string') {
            config.headers['Content-Type'] = 'application/json;charset=UTF-8';
            config.body = JSON.stringify(config.body);
        }

        let response;
        try {
            response = await fetch(url, config);
        } catch (error) {
            throw new Error('网络请求失败，请检查后端服务是否启动');
        }

        if (response.status === 401) {
            redirectToLogin();
            throw new Error('管理员登录已失效');
        }

        let payload = null;
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            try {
                payload = await response.json();
            } catch (error) {
                throw new Error('服务器返回了无法解析的 JSON 数据');
            }
        }

        if (!response.ok) {
            throw new Error(payload && payload.message ? payload.message : '请求失败（HTTP ' + response.status + '）');
        }
        if (payload && typeof payload.code !== 'undefined') {
            if (Number(payload.code) !== 200) {
                throw new Error(payload.message || '业务处理失败');
            }
            return payload.data;
        }
        return payload;
    }

    window.AdminRequest = {
        get: function (url) { return request(url, { method: 'GET' }); },
        post: function (url, body) { return request(url, { method: 'POST', body: body }); },
        put: function (url, body) { return request(url, { method: 'PUT', body: body }); },
        delete: function (url, body) { return request(url, { method: 'DELETE', body: body }); },
        request: request
    };
})();
