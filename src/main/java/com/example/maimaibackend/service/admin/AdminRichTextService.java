package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.media.MediaBusinessType;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 后台演出详情富文本清洗服务。
 *
 * <p>富文本只允许当前后台编辑器产生的少量展示标签；图片和视频必须来自
 * 对应业务的独立媒体目录。这样可以避免 script、事件属性、外链媒体、
 * data URL 以及任意 HTML 被保存后在后台或 HarmonyOS Web 组件中执行。</p>
 */
@Service
public class AdminRichTextService {

    private static final int MAX_HTML_BYTES = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_TAGS = Set.of(
            "p", "br", "h2", "h3", "h4", "strong", "b", "em", "i", "u", "s",
            "ul", "ol", "li", "blockquote", "a", "img", "video", "source",
            "strike",
            "figure", "figcaption", "hr", "div", "span"
    );

    private static final Set<String> VOID_TAGS = Set.of("br", "img", "source", "hr");

    private static final Map<String, Set<String>> ALLOWED_ATTRIBUTES = Map.of(
            "a", Set.of("href", "target", "rel"),
            "img", Set.of("src", "alt", "title"),
            "video", Set.of("src", "controls", "preload"),
            "source", Set.of("src", "type")
    );

    private static final Pattern OPEN_TAG_PATTERN = Pattern.compile("^([A-Za-z][A-Za-z0-9]*)(.*?)(/?)$");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "([A-Za-z][A-Za-z0-9:-]*)\\s*=\\s*(\\\"[^\\\"]*\\\"|'[^']*')"
    );
    private static final Pattern TEXT_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern NUMERIC_ENTITY_PATTERN = Pattern.compile("&#([xX]?[0-9A-Fa-f]+);");

    private final AdminMediaService adminMediaService;

    public AdminRichTextService(AdminMediaService adminMediaService) {
        this.adminMediaService = adminMediaService;
    }

    public String sanitizeProjectDetail(String html) {
        return sanitize(
                html,
                MediaBusinessType.PROJECT_DETAIL_IMAGE,
                MediaBusinessType.PROJECT_DETAIL_VIDEO,
                "演出详情"
        );
    }

    public String sanitizeSessionDetail(String html) {
        return sanitize(
                html,
                MediaBusinessType.SESSION_DETAIL_IMAGE,
                MediaBusinessType.SESSION_DETAIL_VIDEO,
                "城市站详情"
        );
    }

    private String sanitize(
            String html,
            MediaBusinessType imageBusinessType,
            MediaBusinessType videoBusinessType,
            String fieldName
    ) {
        String value = html == null ? "" : html.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_HTML_BYTES) {
            throw new BusinessException(fieldName + "内容不能超过 2 MB");
        }
        if (!value.contains("<")) {
            return plainTextToHtml(value);
        }

        StringBuilder output = new StringBuilder(value.length());
        Deque<String> tagStack = new ArrayDeque<>();
        int cursor = 0;
        while (cursor < value.length()) {
            int tagStart = value.indexOf('<', cursor);
            if (tagStart < 0) {
                output.append(value.substring(cursor));
                break;
            }
            output.append(value, cursor, tagStart);
            int tagEnd = value.indexOf('>', tagStart + 1);
            if (tagEnd < 0) {
                throw new BusinessException(fieldName + "包含不完整的 HTML 标签");
            }

            String token = value.substring(tagStart + 1, tagEnd).trim();
            if (token.isEmpty() || token.startsWith("!") || token.startsWith("?")) {
                throw new BusinessException(fieldName + "包含不支持的 HTML 标签");
            }

            if (token.startsWith("/")) {
                String tagName = token.substring(1).trim().toLowerCase(Locale.ROOT);
                if (!ALLOWED_TAGS.contains(tagName) || VOID_TAGS.contains(tagName)) {
                    throw new BusinessException(fieldName + "包含不支持的结束标签：" + tagName);
                }
                if (tagStack.isEmpty() || !tagStack.peek().equals(tagName)) {
                    throw new BusinessException(fieldName + "HTML 标签嵌套不正确：" + tagName);
                }
                tagStack.pop();
                output.append("</").append(tagName).append('>');
                cursor = tagEnd + 1;
                continue;
            }

            Matcher openMatcher = OPEN_TAG_PATTERN.matcher(token);
            if (!openMatcher.matches()) {
                throw new BusinessException(fieldName + "包含无法识别的 HTML 标签");
            }
            String tagName = openMatcher.group(1).toLowerCase(Locale.ROOT);
            if (!ALLOWED_TAGS.contains(tagName)) {
                throw new BusinessException(fieldName + "不支持标签：" + tagName);
            }

            String rawAttributes = openMatcher.group(2) == null ? "" : openMatcher.group(2).trim();
            boolean selfClosing = VOID_TAGS.contains(tagName) || !openMatcher.group(3).isEmpty();
            Map<String, String> attributes = parseAttributes(rawAttributes, fieldName, tagName);
            normalizeAttributes(tagName, attributes, imageBusinessType, videoBusinessType, fieldName);

            output.append('<').append(tagName);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                output.append(' ')
                        .append(entry.getKey())
                        .append("=\"")
                        .append(escapeAttribute(entry.getValue()))
                        .append('"');
            }
            output.append('>');
            if (!selfClosing) {
                tagStack.push(tagName);
            }
            cursor = tagEnd + 1;
        }

        if (!tagStack.isEmpty()) {
            throw new BusinessException(fieldName + "HTML 标签未正确闭合：" + tagStack.peek());
        }

        String sanitized = output.toString().trim();
        if (isVisualEmpty(sanitized)) {
            return null;
        }
        return sanitized;
    }

    private Map<String, String> parseAttributes(String raw, String fieldName, String tagName) {
        Map<String, String> attributes = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(raw);
        int cursor = 0;
        while (matcher.find()) {
            String between = raw.substring(cursor, matcher.start()).trim();
            if (!between.isEmpty()) {
                throw new BusinessException(fieldName + "的 " + tagName + " 标签属性格式不正确");
            }
            String name = matcher.group(1).toLowerCase(Locale.ROOT);
            String quoted = matcher.group(2);
            String value = decodeAttributeEntities(quoted.substring(1, quoted.length() - 1));
            if (name.startsWith("on") || "style".equals(name) || "srcdoc".equals(name)) {
                throw new BusinessException(fieldName + "包含不安全属性：" + name);
            }
            Set<String> allowed = ALLOWED_ATTRIBUTES.getOrDefault(tagName, Set.of());
            if (!allowed.contains(name)) {
                throw new BusinessException(fieldName + "的 " + tagName + " 标签不支持属性：" + name);
            }
            attributes.put(name, value);
            cursor = matcher.end();
        }
        if (!raw.substring(cursor).trim().isEmpty()) {
            throw new BusinessException(fieldName + "的 " + tagName + " 标签属性格式不正确");
        }
        return attributes;
    }

    private void normalizeAttributes(
            String tagName,
            Map<String, String> attributes,
            MediaBusinessType imageBusinessType,
            MediaBusinessType videoBusinessType,
            String fieldName
    ) {
        if ("img".equals(tagName)) {
            String src = attributes.get("src");
            attributes.put("src", adminMediaService.requireStoredMediaReference(
                    src, imageBusinessType, fieldName + "图片"
            ));
            attributes.putIfAbsent("alt", "");
            return;
        }
        if ("video".equals(tagName) || "source".equals(tagName)) {
            String src = attributes.get("src");
            attributes.put("src", adminMediaService.requireStoredMediaReference(
                    src, videoBusinessType, fieldName + "视频"
            ));
            if ("video".equals(tagName)) {
                attributes.put("controls", "controls");
                String preload = attributes.getOrDefault("preload", "metadata").toLowerCase(Locale.ROOT);
                if (!Set.of("none", "metadata", "auto").contains(preload)) {
                    preload = "metadata";
                }
                attributes.put("preload", preload);
            } else {
                String type = attributes.get("type");
                if (type != null && !Set.of("video/mp4", "video/webm").contains(type.toLowerCase(Locale.ROOT))) {
                    throw new BusinessException(fieldName + "视频 source 类型仅支持 video/mp4 或 video/webm");
                }
            }
            return;
        }
        if ("a".equals(tagName)) {
            String href = attributes.get("href");
            String normalizedHref = href == null ? "" : href.toLowerCase(Locale.ROOT);
            if (!(normalizedHref.startsWith("https://") || normalizedHref.startsWith("http://"))) {
                throw new BusinessException(fieldName + "链接仅支持 http 或 https 地址");
            }
            attributes.put("target", "_blank");
            attributes.put("rel", "noopener noreferrer");
        }
    }

    private boolean isVisualEmpty(String html) {
        if (html.contains("<img") || html.contains("<video") || html.contains("<source")) {
            return false;
        }
        String text = TEXT_TAG_PATTERN.matcher(html).replaceAll("")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .trim();
        return text.isEmpty();
    }

    private String plainTextToHtml(String text) {
        String escaped = escapeText(text);
        String[] paragraphs = escaped.split("(?:\\r?\\n){2,}");
        List<String> htmlParagraphs = new ArrayList<>();
        for (String paragraph : paragraphs) {
            String content = paragraph.replace("\r\n", "\n")
                    .replace('\r', '\n')
                    .replace("\n", "<br>");
            if (!content.isBlank()) {
                htmlParagraphs.add("<p>" + content + "</p>");
            }
        }
        return htmlParagraphs.isEmpty() ? null : String.join("", htmlParagraphs);
    }

    private String decodeAttributeEntities(String value) {
        String decoded = value
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        Matcher matcher = NUMERIC_ENTITY_PATTERN.matcher(decoded);
        StringBuffer buffer = new StringBuffer(decoded.length());
        while (matcher.find()) {
            String token = matcher.group(1);
            int radix = token.startsWith("x") || token.startsWith("X") ? 16 : 10;
            String digits = radix == 16 ? token.substring(1) : token;
            String replacement;
            try {
                int codePoint = Integer.parseInt(digits, radix);
                replacement = Character.isValidCodePoint(codePoint)
                        ? new String(Character.toChars(codePoint))
                        : "\uFFFD";
            } catch (NumberFormatException exception) {
                replacement = "\uFFFD";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String escapeText(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeAttribute(String value) {
        return escapeText(value == null ? "" : value);
    }
}
