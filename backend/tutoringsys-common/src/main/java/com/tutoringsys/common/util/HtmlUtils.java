package com.tutoringsys.common.util;

import org.springframework.stereotype.Component;

@Component
public class HtmlUtils {

    /**
     * HTML转义，防止XSS攻击
     * @param html 原始HTML内容
     * @return 转义后的内容
     */
    public String escapeHtml(String html) {
        if (html == null) {
            return null;
        }
        html = html.replaceAll("&", "&amp;");
        html = html.replaceAll("<", "&lt;");
        html = html.replaceAll(">", "&gt;");
        html = html.replaceAll("'", "&#39;");
        html = html.replaceAll("\"", "&quot;");
        return html;
    }

    /**
     * 过滤危险的HTML标签和属性
     * @param html 原始HTML内容
     * @return 过滤后的内容
     */
    public String filterHtml(String html) {
        if (html == null) {
            return null;
        }
        // 移除script标签
        html = html.replaceAll("<script[^>]*>.*?</script>", "");
        // 移除iframe标签
        html = html.replaceAll("<iframe[^>]*>.*?</iframe>", "");
        // 移除on*事件属性
        html = html.replaceAll("on\\w+\\s*=\\s*[\"']?[^\"'>]+[\"']?", "");
        return html;
    }

    /**
     * 安全处理HTML内容，先过滤后转义
     * @param html 原始HTML内容
     * @return 安全处理后的内容
     */
    public String sanitizeHtml(String html) {
        return escapeHtml(filterHtml(html));
    }
}