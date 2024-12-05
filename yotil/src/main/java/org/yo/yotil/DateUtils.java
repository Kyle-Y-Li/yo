package org.yo.yotil;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 *  时间操作的帮助类
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 12/05/2024 16:03:58 16:03
 */
public class DateUtils {
    public static Date tryParseDate(final String value, final String... patterns) {
        return tryParseDate(value, TimeZone.getDefault().toZoneId(), patterns);
    }
    
    public static Date tryParseDate(final String value, final ZoneId zoneId, final String... patterns) {
        if (StringUtils.isBlank(value) || patterns == null) {
            return null;
        }
        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setTimeZone(TimeZone.getTimeZone(zoneId));
                return format.parse(value);
            } catch (RuntimeException | ParseException ignore) {
            }
        }
        return null;
    }
    
    public static String tryFormatDateDate(final Date value, final String... patterns) {
        return tryFormatDateDate(value, TimeZone.getDefault().toZoneId(), patterns);
    }
    
    public static String tryFormatDateDate(final Date value, final ZoneId zoneId, final String... patterns) {
        if (value == null || patterns == null) {
            return null;
        }
        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setTimeZone(TimeZone.getTimeZone(zoneId));
                return format.format(value);
            } catch (RuntimeException ignore) {
            }
        }
        return null;
    }
    
    public static Date parseDate(final Long value) {
        if (value == null) {
            return null;
        }
        long millis = value < 10000000000L ? value * 1000L : value;
        return new Date(millis);
    }
    
    public static Date parseDate(final LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    public static Timestamp parseTimestamp(final String value, final String... patterns) {
        if (StringUtils.isBlank(value) || patterns == null) {
            return null;
        }
        Date date = tryParseDate(value, patterns);
        if (date == null) {
            return null;
        }
        return parseTimestamp(date);
    }
    
    public static Timestamp parseTimestamp(final Date value) {
        if (value == null) {
            return null;
        }
        return new Timestamp(value.getTime());
    }
    
    public static LocalDateTime getDateTimeNow(final ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }
    
    public static LocalDateTime parseLocalDateTime(final String value, final String pattern, final ZoneId zoneId) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
        return LocalDateTime.parse(value, formatter);
    }
    
    public static LocalDateTime parseLocalDateTime(final Date value, final ZoneId zoneId) {
        if (value == null) {
            return null;
        }
        return value.toInstant().atZone(zoneId).toLocalDateTime();
    }
    
    public static LocalDateTime parseLocalDateTime(final Long value, final ZoneId zoneId) {
        if (value == null) {
            return null;
        }
        long millis = value < 10000000000L ? value * 1000L : value;
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime();
    }
    
    public static String formatLocalDateTime(final LocalDateTime value, final String pattern) {
        if (value == null) {
            return null;
        }
        return value.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * 将带有时区的 sourceDate 的转换为 targetZoneId 时区的字符串时间
     *
     * @param sourceDate    原头时间
     * @param sourcePattern 原头时间格式
     * @param targetPattern 目标时间格式
     * @param targetZoneId  目标时间时区
     * @return 字符串时间
     */
    public static String formatDateWithZone(final String sourceDate, final String sourcePattern, final String targetPattern, final ZoneId targetZoneId) {
        if (sourceDate == null) {
            return null;
        }
        //source
        DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern(sourcePattern);
        //target
        ZonedDateTime targetZonedDateTime = ZonedDateTime.parse(sourceDate, sourceFormatter);
        if (targetZoneId != null) {
            targetZonedDateTime = targetZonedDateTime.withZoneSameInstant(targetZoneId);
        }
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern(targetPattern);
        return targetFormatter.format(targetZonedDateTime);
    }
    
    /**
     * 将 sourceDate 的转换为 targetZoneId 时区的字符串时间
     *
     * @param sourceDate    原头时间
     * @param sourcePattern 原头时间格式
     * @param targetPattern 目标时间格式
     * @param targetZoneId  目标时间时区
     * @return 字符串时间
     */
    public static String formatDate(final String sourceDate, final String sourcePattern, final String targetPattern, final ZoneId targetZoneId) {
        if (sourceDate == null) {
            return null;
        }
        //source
        Date sourcedDate = parseDate(sourceDate, sourcePattern);
        if (sourcedDate == null) {
            return null;
        }
        //target
        return formatDate(sourcedDate, targetPattern, TimeZone.getTimeZone(targetZoneId));
    }
}
