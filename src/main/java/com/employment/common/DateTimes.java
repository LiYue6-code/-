package com.employment.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期时间解析工具：兼容 {@code <input type="datetime-local">} 提交的 ISO 格式。
 */
public final class DateTimes {

    private static final DateTimeFormatter SPACE_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimes() {
    }

    /** 解析 "yyyy-MM-dd'T'HH:mm" / "yyyy-MM-dd HH:mm" / "yyyy-MM-dd"；空串返回 null。 */
    public static LocalDateTime parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.trim();
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其它格式
        }
        try {
            return LocalDateTime.parse(value, SPACE_MINUTE);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其它格式
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new BizException("时间格式不正确，请重新选择");
        }
    }
}
