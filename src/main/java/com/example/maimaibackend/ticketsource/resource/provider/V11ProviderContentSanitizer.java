package com.example.maimaibackend.ticketsource.resource.provider;

import com.example.maimaibackend.service.admin.AdminRichTextService;
import com.example.maimaibackend.ticketsource.provider.model.ProviderProjectDetail;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 第三方演出详情进入本地核心表前的安全边界。
 *
 * <p>优先复用后台富文本白名单；第三方包含外链媒体、脚本或不受支持标签时，
 * 不让原始 HTML 直接落库，而是降级为转义后的纯文本并返回同步警告。</p>
 */
@Component
public class V11ProviderContentSanitizer {
    private static final Pattern SCRIPT_STYLE = Pattern.compile(
            "(?is)<(script|style|iframe|object|embed)[^>]*>.*?</\\1>");
    private static final Pattern TAG = Pattern.compile("(?is)<[^>]+>");
    private final AdminRichTextService richTextService;

    public V11ProviderContentSanitizer(AdminRichTextService richTextService) {
        this.richTextService = richTextService;
    }

    public String sanitize(ProviderProjectDetail detail, List<String> warnings) {
        String combined = V11ToLegacyResourceMapper.buildDetail(detail);
        if (combined == null || combined.isBlank()) return null;
        try {
            return richTextService.sanitizeProjectDetail(combined);
        } catch (RuntimeException unsafeHtml) {
            if (warnings != null) {
                warnings.add("第三方演出详情包含本地不允许的HTML或外链媒体，已安全降级为纯文本");
            }
            String withoutDangerousBlocks = SCRIPT_STYLE.matcher(combined).replaceAll(" ");
            String plain = TAG.matcher(withoutDangerousBlocks).replaceAll(" ")
                    .replaceAll("\\s+", " ").trim();
            return plain.isEmpty() ? null : "<p>" + escape(plain) + "</p>";
        }
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
