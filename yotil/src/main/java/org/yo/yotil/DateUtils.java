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
import java.util.Set;

/**
 * 时间操作的帮助类
 *
 * @author Kyle.Y.Li
 * @since 1.0.0 12/05/2024 16:03:58 16:03
 */
public class DateUtils {
    public static final ZoneId PST_ZONE_ID = ZoneId.of("America/Los_Angeles");
    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId CHN_ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    public static Date tryParseDate(final String value, final String... patterns) {
        return tryParseDate(value, java.util.TimeZone.getDefault().toZoneId(), patterns);
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
                format.setTimeZone(java.util.TimeZone.getTimeZone(zoneId));
                return format.parse(value);
            } catch (RuntimeException | ParseException ignore) {
            }
        }
        return null;
    }
    
    public static String tryFormatDate(final Date value, final String... patterns) {
        return tryFormatDate(value, java.util.TimeZone.getDefault().toZoneId(), patterns);
    }
    
    public static String tryFormatDate(final Date value, final ZoneId zoneId, final String... patterns) {
        if (value == null || patterns == null) {
            return null;
        }
        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setTimeZone(java.util.TimeZone.getTimeZone(zoneId));
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
    
    public static Timestamp tryParseTimestamp(final String value, final String... patterns) {
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
    
    public static LocalDateTime getDateTimeNow() {
        return LocalDateTime.now();
    }
    
    public static LocalDateTime getDateTimeNow(final ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }
    
    public static LocalDateTime tryParseLocalDateTime(final String value, final String... patterns) {
        return tryParseLocalDateTime(value, ZoneId.systemDefault(), patterns);
    }
    
    public static LocalDateTime tryParseLocalDateTime(final String value, final ZoneId zoneId, final String... patterns) {
        if (StringUtils.isBlank(value) || patterns == null) {
            return null;
        }
        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
                return LocalDateTime.parse(value, formatter);
            } catch (RuntimeException ignore) {
            }
        }
        return null;
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
    
    public static String tryFormatLocalDateTime(final LocalDateTime value, final String... patterns) {
        return tryFormatLocalDateTime(value, ZoneId.systemDefault(), patterns);
    }
    
    public static String tryFormatLocalDateTime(final LocalDateTime value, final ZoneId zoneId, final String... patterns) {
        if (value == null || patterns == null) {
            return null;
        }
        for (String pattern : patterns) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
                return formatter.format(value);
            } catch (RuntimeException ignore) {
            }
        }
        return null;
    }
    
    /**
     * 将带有时区的 sourceDate 的转换为 targetZoneId 时区的字符串时间
     *
     * @param sourceDate    原头时间
     * @param sourcePattern 原头时间格式
     * @param sourceZoneId  原头时间时区
     * @param targetPattern 目标时间格式
     * @param targetZoneId  目标时间时区
     * @return 字符串时间
     */
    public static String formatDateWithZone(final String sourceDate, final String sourcePattern, final ZoneId sourceZoneId, final String targetPattern, final ZoneId targetZoneId) {
        if (sourceDate == null) {
            return null;
        }
        //source
        DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern(sourcePattern).withZone(sourceZoneId);
        ZonedDateTime sourceZonedDateTime = ZonedDateTime.parse(sourceDate, sourceFormatter);
        //target
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern(targetPattern).withZone(targetZoneId);
        //convert
        return targetFormatter.format(sourceZonedDateTime);
    }
    
    public static void main(String[] args) {
        Set<String> availableZones = ZoneId.getAvailableZoneIds();
        availableZones.forEach(System.out::println);
        
        var x1 = DateUtils.tryParseDate("2024/12/06 17:26:20.333", "yyyy/MM/dd HH:mm:ss");
        var x2 = DateUtils.tryFormatDate(x1, "MM/dd/yyyy HH:mm:ss");
        
        var x3 = DateUtils.tryParseDate("2024/12/06 17:26:20.333", PST_ZONE_ID, "yyyy/MM/dd HH:mm:ss");
        var x4 = DateUtils.tryFormatDate(x3, java.util.TimeZone.getDefault().toZoneId(), "MM/dd/yyyy HH:mm:ss");
        
        var x5 = DateUtils.parseDate(System.currentTimeMillis());
        
        var x6 = DateUtils.parseDate(DateUtils.getDateTimeNow());
        
        var x7 = DateUtils.tryParseTimestamp("2024/12/06 17:26:20.333", "yyyy/MM/dd HH:mm:ss");
        
        var x8 = DateUtils.tryParseLocalDateTime("2024/12/06 17:26:20.333", "yyyy/MM/dd HH:mm:ss");
        
        var x9 = DateUtils.parseLocalDateTime(System.currentTimeMillis(), java.util.TimeZone.getDefault().toZoneId());
        
        var x10 = DateUtils.tryFormatLocalDateTime(x9, "yyyy/MM/dd HH:mm:ss");
        
        var x11 = DateUtils.formatDateWithZone("2024/12/07 09:44:20", "yyyy/MM/dd HH:mm:ss", java.util.TimeZone.getDefault().toZoneId(), "MM/dd/yyyy HH:mm:ss", PST_ZONE_ID);
    }
}
