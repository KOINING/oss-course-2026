package com.oss.osscourse.util;

/**
 * HTML 工具类——用于从富文本中提取纯文本。
 * <p>
 * 用于课程目标描述的双字段方案：
 * DB 存储富文本 HTML，应用层通过此工具派生纯文本供列表摘要和计算链使用。
 * </p>
 */
public final class HtmlUtils {

    private HtmlUtils() {
        // 工具类禁止实例化
    }

    /**
     * 去除 HTML 标签，返回纯文本。
     * <ul>
     *   <li>移除所有 HTML 标签</li>
     *   <li>将常见 HTML 实体还原为字符</li>
     *   <li>合并多余空白</li>
     * </ul>
     *
     * @param html 原始 HTML 字符串，可为 null
     * @return 纯文本字符串；输入为 null 或空白时返回空字符串
     */
    public static String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        String text = html
                // 移除 HTML 标签
                .replaceAll("<[^>]+>", " ")
                // 还原常见 HTML 实体
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                // 合并连续空白为单个空格
                .replaceAll("\\s+", " ")
                .trim();

        return text;
    }

    /**
     * 判断字符串是否包含 HTML 标签。
     *
     * @param content 待检测字符串
     * @return true 表示包含 HTML 标签
     */
    public static boolean containsHtml(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        return content.matches(".*<[^>]+>.*");
    }
}
