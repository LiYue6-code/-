package com.employment.common;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * 轻量 CSV 导出工具：输出 UTF-8 BOM + CRLF，Excel 可直接双击打开且中文不乱码。
 */
public final class CsvExporter {

    private static final String CRLF = "\r\n";

    private final StringBuilder sb = new StringBuilder();

    public CsvExporter() {
        // UTF-8 BOM，保证 Excel 正确识别编码
        sb.append('\uFEFF');
    }

    /** 追加一行。 */
    public CsvExporter row(Object... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cells[i] == null ? "" : String.valueOf(cells[i])));
        }
        sb.append(CRLF);
        return this;
    }

    /** 追加多行（二维数据）。 */
    public CsvExporter rows(Collection<? extends Collection<?>> data) {
        for (Collection<?> line : data) {
            row(line.toArray());
        }
        return this;
    }

    public byte[] toBytes() {
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** 包装为 CSV 下载响应。 */
    public org.springframework.http.ResponseEntity<byte[]> asResponse(String filename) {
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, attachment(filename))
                .contentType(new org.springframework.http.MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(toBytes());
    }

    private static String escape(String value) {
        boolean needQuote = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        String v = value.replace("\"", "\"\"");
        return needQuote ? '"' + v + '"' : v;
    }

    /**
     * 生成 Content-Disposition 头，兼容中文文件名（RFC 5987）。
     *
     * <p>HTTP 头只允许 ASCII，因此 {@code filename} 使用安全回退名，
     * 真实中文名放在 {@code filename*} 中，主流浏览器优先使用后者。
     */
    public static String attachment(String filename) {
        String safeName = filename == null || filename.isBlank() ? "download" : filename;
        String encoded = java.net.URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + asciiFallback(safeName) + "\"; filename*=UTF-8''" + encoded;
    }

    private static String asciiFallback(String name) {
        boolean ascii = name.chars().allMatch(c -> c >= 32 && c < 127 && c != '"' && c != '\\');
        if (ascii) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            String ext = name.substring(dot + 1);
            if (ext.chars().allMatch(c -> Character.isLetterOrDigit(c) && c < 127)) {
                return "download." + ext;
            }
        }
        return "download";
    }
}
