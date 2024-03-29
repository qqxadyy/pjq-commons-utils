/*
 * Copyright © 2024 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-commons-utils
 * 项目描述：个人整理的工具类
 * 项目地址：https://github.com/qqxadyy/pjq-commons-utils
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2024 pengjianqiang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pjq.commons.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.constant.DateTimePattern;
import pjq.commons.utils.reflect.MethodUtils;

/**
 * 使用java.time实现的日期时间工具类
 *
 * @author pengjianqiang
 * @date 2018年11月15日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {
    private static final String startTime = " 00:00:00";
    private static final String endTime = " 23:59:59";
    private static ZoneId defaultZone = ZoneId.systemDefault();

    /**
     * 把{@link LocalDate}对象转成{@link Date}对象
     *
     * @param date
     * @return
     */
    public static Date localDateToDate(LocalDate date) {
        return temporalAccessorToDate(date);
    }

    /**
     * 把{@link LocalDateTime}对象转成{@link Date}对象
     *
     * @param dateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        return temporalAccessorToDate(dateTime);
    }

    /**
     * 把{@link LocalDateTime}对象转成{@link LocalDate}对象
     *
     * @param dateTime
     * @return
     */
    public static LocalDate localDateTimeToLocalDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate();
    }

    private static Date temporalAccessorToDate(TemporalAccessor temporal) {
        Class<?> clazz = temporal.getClass();
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return Date.from(((LocalDate) temporal).atStartOfDay(defaultZone).toInstant());
        } else if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return Date.from(((LocalDateTime) temporal).atZone(defaultZone).toInstant());
        } else {
            throw new RuntimeException("暂不支持".concat(clazz.getName()).concat("转换为").concat(Date.class.getName()));
        }
    }

    /**
     * 把{@link Date}对象转成{@link LocalDateTime}对象
     *
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return (LocalDateTime) dateToTemporalAccessor(date, LocalDateTime.class);
    }

    /**
     * 把{@link Date}对象转成{@link LocalDate}对象
     *
     * @param date
     * @return
     */
    public static LocalDate dateToLocalDate(Date date) {
        return (LocalDate) dateToTemporalAccessor(date, LocalDate.class);
    }

    private static TemporalAccessor dateToTemporalAccessor(Date date, Class<? extends TemporalAccessor> clazz) {
        CheckUtils.checkNotNull(date, "需要转换的日期对象不能为空");
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return LocalDateTime.ofInstant(date.toInstant(), defaultZone).toLocalDate();
        } else if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return LocalDateTime.ofInstant(date.toInstant(), defaultZone).withNano(0);
        } else {
            throw new RuntimeException("暂不支持".concat(Date.class.getName()).concat("转换为").concat(clazz.getName()));
        }
    }

    /**
     * 把yyyy-MM-dd格式的日期字符串转成{@link Date}对象
     *
     * @param dateStr
     * @return
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 把指定格式的日期字符串转成{@link Date}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date parseDate(String dateStr, DateTimePattern pattern) {
        return parseDate(dateStr, pattern.value());
    }

    /**
     * 把指定格式的日期字符串转成{@link Date}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date parseDate(String dateStr, String pattern) {
        DateTimeFormatter formatter = ofPattern(pattern);
        return temporalAccessorToDate(DateTimePattern.isDatePattern(pattern) ? LocalDate.parse(dateStr, formatter)
                : LocalDateTime.parse(dateStr, formatter));
    }

    /**
     * 把yyyy-MM-dd格式的日期字符串转成{@link LocalDate}对象
     *
     * @param dateStr
     * @return
     */
    public static LocalDate parseLocalDate(String dateStr) {
        return parseLocalDate(dateStr, DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 把指定格式的日期字符串转成{@link LocalDate}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDate parseLocalDate(String dateStr, DateTimePattern pattern) {
        return parseLocalDate(dateStr, pattern.value());
    }

    /**
     * 把指定格式的日期字符串转成{@link LocalDate}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDate parseLocalDate(String dateStr, String pattern) {
        DateTimeFormatter formatter = ofPattern(pattern);
        return DateTimePattern.isDatePattern(pattern) ? LocalDate.parse(dateStr, formatter)
                : LocalDateTime.parse(dateStr, formatter).toLocalDate();
    }

    /**
     * 把yyyy-MM-dd HH:mm:ss格式的日期字符串转成{@link LocalDateTime}对象
     *
     * @param dateStr
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String dateStr) {
        return parseLocalDateTime(dateStr, DateTimePattern.PATTERN_DATETIME);
    }

    /**
     * 把指定格式的日期字符串转成{@link LocalDateTime}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String dateStr, DateTimePattern pattern) {
        return parseLocalDateTime(dateStr, pattern.value());
    }

    /**
     * 把指定格式的日期字符串转成{@link LocalDateTime}对象
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String dateStr, String pattern) {
        DateTimeFormatter formatter = ofPattern(pattern);
        LocalDateTime isoDateTime = parseISOLocalDateTime(dateStr);
        return Optional.ofNullable(isoDateTime).orElseGet(() -> LocalDateTime.parse(dateStr, formatter));
    }

    /**
     * 通用日期时间格式化，用于直接指定pattern<br>
     * 其它parse方法都限定成使用较多的pattern才能使用，而该方法可直接指定不常用的pattern，但是必须根据date的类型指定可用的pattern
     *
     * @param dateStr
     * @param pattern
     * @param dateClass
     *         {@link Date}、{@link LocalDate}、{@link LocalDate}、{@link LocalTime}的Class对象
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <R> R commonParse(String dateStr, String pattern, Class<R> dateClass) {
        DateTimeFormatter formatter = ofPattern(pattern);
        if (Date.class.isAssignableFrom(dateClass)) {
            try {
                return (R) new SimpleDateFormat(pattern).parse(dateStr);
            } catch (Exception e) {
                throw new RuntimeException(dateStr.concat("转换为").concat(Date.class.getName()).concat("失败"));
            }
        } else if (LocalDate.class.isAssignableFrom(dateClass)) {
            return (R) LocalDate.parse(dateStr, formatter);
        } else if (LocalDateTime.class.isAssignableFrom(dateClass)) {
            return (R) parseLocalDateTime(dateStr, pattern);
        } else if (LocalTime.class.isAssignableFrom(dateClass)) {
            return (R) LocalTime.parse(dateStr, formatter);
        } else {
            throw new RuntimeException("不支持类".concat(dateClass.getClass().getName()).concat("的日期时间转换"));
        }
    }

    /**
     * 通用日期时间格式化，用于直接指定pattern<br>
     * 其它parse方法都限定成使用较多的pattern才能使用，而该方法可直接指定不常用的pattern，但是必须根据date的类型指定可用的pattern
     *
     * @param <R>
     * @param dateStr
     * @param formatter
     * @param dateClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <R> R commonParse(String dateStr, DateTimeFormatter formatter, Class<R> dateClass) {
        if (Date.class.isAssignableFrom(dateClass)) {
            try {
                return (R) localDateTimeToDate(LocalDateTime.parse(dateStr, formatter));
            } catch (Exception e) {
                throw new RuntimeException(dateStr.concat("转换为").concat(Date.class.getName()).concat("失败"));
            }
        } else if (LocalDate.class.isAssignableFrom(dateClass)) {
            return (R) LocalDate.parse(dateStr, formatter);
        } else if (LocalDateTime.class.isAssignableFrom(dateClass)) {
            return (R) LocalDateTime.parse(dateStr, formatter);
        } else if (LocalTime.class.isAssignableFrom(dateClass)) {
            return (R) LocalTime.parse(dateStr, formatter);
        } else {
            throw new RuntimeException("不支持类".concat(dateClass.getClass().getName()).concat("的日期时间转换"));
        }
    }

    /**
     * JavaScript生成JSON字符串时，Date对象为(UTC/ISO instant,'yyyy-MM-ddTHH:mm:ss.SSSZ')格式，需要特殊处理
     *
     * @param dateStr
     * @return
     */
    private static LocalDateTime parseISOLocalDateTime(String dateStr) {
        LocalDateTime dateTime = null;
        try {
            if (CheckUtils.isEmpty(dateStr)) {
                return dateTime;
            }
            if (dateStr.length() > 10 && (dateStr.charAt(10) == 'T' || dateStr.charAt(10) == 't')) {
                if (dateStr.endsWith("Z") || dateStr.endsWith("z")) {
                    return LocalDateTime.ofInstant(Instant.parse(dateStr), ZoneOffset.UTC);
                } else {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }
        } catch (Exception e) {
        }
        return dateTime;
    }

    /**
     * 把{@link Date}对象转成yyyy-MM-dd格式的日期字符串
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        return format(date, DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 把{@link Date}对象转成指定格式的日期字符串
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static String format(Date date, DateTimePattern pattern) {
        return format(date, pattern.value());
    }

    /**
     * 把{@link Date}对象转成指定格式的日期字符串
     *
     * @param dateStr
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        return java8TimeCommonFormat(dateToTemporalAccessor(date,
                DateTimePattern.isDatePattern(pattern) ? LocalDate.class : LocalDateTime.class), pattern);
    }

    /**
     * 把{@link java.time.LocalDate}对象转成yyyy-MM-dd格式的日期字符串
     *
     * @param date
     * @return
     */
    public static String format(LocalDate date) {
        return format(date, DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 把{@link java.time.LocalDateTime}对象转成yyyy-MM-dd格式的日期字符串
     *
     * @param date
     * @return
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 把{@link java.time.LocalDate}对象转成指定格式的日期字符串
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(LocalDate date, DateTimePattern pattern) {
        return format(date, pattern.value());
    }

    /**
     * 把{@link java.time.LocalDate}对象转成指定格式的日期字符串
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(LocalDate date, String pattern) {
        return format(localDateToDateTime(date), pattern);
    }

    /**
     * 把{@link java.time.LocalDateTime}对象转成指定格式的日期字符串
     *
     * @param dateTime
     * @param pattern
     * @return
     */
    public static String format(LocalDateTime dateTime, DateTimePattern pattern) {
        return format(dateTime, pattern.value());
    }

    /**
     * 把{@link java.time.LocalDateTime}对象转成指定格式的日期字符串
     *
     * @param dateTime
     * @param pattern
     * @return
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return java8TimeCommonFormat(DateTimePattern.isDatePattern(pattern) ? dateTime.toLocalDate() : dateTime,
                pattern);
    }

    /**
     * 通用日期时间格式化，用于直接指定pattern<br>
     * 其它format方法都限定成使用较多的pattern才能使用，而该方法可直接指定不常用的pattern，但是必须根据date的类型指定可用的pattern
     *
     * @param date
     *         {@link Date}、{@link LocalDate}、{@link LocalDate}、{@link LocalTime}对象
     * @param pattern
     * @return
     */
    public static <T> String commonFormat(T date, String pattern) {
        if (date instanceof Date) {
            return java8TimeCommonFormat(dateToLocalDateTime(Date.class.cast(date)), pattern);
        } else if (date instanceof TemporalAccessor) {
            return java8TimeCommonFormat(TemporalAccessor.class.cast(date), pattern);
        } else {
            throw new RuntimeException("不支持类".concat(date.getClass().getName()).concat("的日期时间格式化"));
        }
    }

    private static String java8TimeCommonFormat(TemporalAccessor temporal, String pattern) {
        return ofPattern(pattern).format(temporal);
    }

    private static DateTimeFormatter ofPattern(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    private static LocalDateTime localDateToDateTime(LocalDate date) {
        return date.atTime(0, 0, 0, 0);
    }

    /**
     * 获取当前日期时间对象
     *
     * @return
     */
    public static Date currentDateObj() {
        return localDateTimeToDate(LocalDateTime.now());
    }

    /**
     * 获取当前日期对象
     *
     * @return
     */
    public static LocalDate currentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前日期时间对象
     *
     * @return
     */
    public static LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期,格式为yyyy-MM-dd的字符串
     *
     * @return
     */
    public static String currentDateStr() {
        return format(LocalDate.now(), DateTimePattern.PATTERN_DEFAULT);
    }

    /**
     * 获取当前日期,格式为yyyyMMdd的字符串
     *
     * @return
     */
    public static String currentDateCompactStr() {
        return format(LocalDate.now(), DateTimePattern.PATTERN_DATE_COMPACT);
    }

    /**
     * 获取当前时间,格式为yyyy-MM-dd HH:mm:ss的字符串
     *
     * @return
     */
    public static String currentDateTimeStr() {
        return format(LocalDateTime.now(), DateTimePattern.PATTERN_DATETIME);
    }

    /**
     * 获取当前时间,格式为yyyyMMddHHmmss的字符串
     *
     * @return
     */
    public static String currentDateTimeCompactStr() {
        return format(LocalDateTime.now(), DateTimePattern.PATTERN_DATETIME_COMPACT);
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public static int currentYear() {
        return Year.now().getValue();
    }

    /**
     * 获取当前月份
     *
     * @return
     */
    public static int currentMonth() {
        return MonthDay.now().getMonthValue();
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public static int currentDay() {
        return MonthDay.now().getDayOfMonth();
    }

    /**
     * 获取当前年月,格式为yyyyMM的字符串
     *
     * @return
     */
    public static String currentYearMonth() {
        return format(LocalDate.now(), DateTimePattern.PATTERN_YEARMONTH);
    }

    /**
     * 获取两个时间相差的天数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int durationDays(Date d1, Date d2) {
        return durationDays(dateToLocalDate(d1), dateToLocalDate(d2));
    }

    /**
     * 获取两个时间相差的天数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int durationDays(LocalDateTime d1, LocalDateTime d2) {
        return durationDays(d1.toLocalDate(), d2.toLocalDate());
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int durationDays(LocalDate d1, LocalDate d2) {
        return duration(d1, d2, ChronoUnit.DAYS);
    }

    /**
     * 获取两个日期对应单位的差值<br>
     * 例如unit传入{@link ChronoUnit#YEARS}，表示计算相差的年数
     *
     * @param d1
     * @param d2
     * @param unit
     *         只支持年(YEARS)、月(MONTHS)、日(DAYS)
     * @return
     */
    public static int duration(LocalDate d1, LocalDate d2, ChronoUnit unit) {
        CheckUtils.checkNotNull(unit, "单位不能为空");

        boolean unitValid = true;
        switch (unit) {
            case DAYS:
            case MONTHS:
            case YEARS:
                break;
            default:
                unitValid = false;
        }
        CheckUtils.checkNotFalse(unitValid, "只支持年(YEARS)、月(MONTHS)、日(DAYS)这几个单位的差值计算");

        // 问题：Period.getDays只能获取相差的时间中的天数部分，但是相差的年份、月份获取不了，即getDays不是获取相差总天数，实际应使用以下方式：
        // 方法1：Duration.between(LocaleDateTime d1,LocaleDateTime d2).getSeconds/toDays等
        // 方法2：ChronoUnit.SECONDS/DAYS等.between(LocaleDateTime d1,LocaleDateTime d2)
        return Math.abs(new Long(unit.between(d1, d2)).intValue());
    }

    /**
     * 获取两个日期相差的天数，并返回"Y年M个月D天"的字符串
     *
     * @param d1
     * @param d2
     * @return
     * @author pengjianqiang@2022年7月3日
     */
    public static String durationYearMonthDay(LocalDate d1, LocalDate d2) {
        int daysOfOneYear = 365;
        int daysOfOneMonth = 30; // 大约取1个月30天
        int totalDurationDays = durationDays(d1, d2);

        int durationYears = 0;
        int durationMonths = 0;
        int durationDays = 0;
        if (totalDurationDays >= daysOfOneYear) {
            // 大于等于1年
            int leftDaysOfYear = totalDurationDays % daysOfOneYear; // 取年份余数

            durationYears = (totalDurationDays - leftDaysOfYear) / daysOfOneYear; // 年份余数/365=年数
            if (leftDaysOfYear >= daysOfOneMonth) {
                // 大于等于1月
                int leftDaysOfMonth = leftDaysOfYear % daysOfOneMonth; // 取月份余数
                durationMonths = (leftDaysOfYear - leftDaysOfMonth) / daysOfOneMonth; // 月份余数/30=月数

                durationDays = leftDaysOfMonth;
            } else {
                // 小于1月
                durationDays = leftDaysOfYear;
            }
        } else {
            // 小于1年
            if (totalDurationDays >= daysOfOneMonth) {
                // 大于等于1月
                int leftDaysOfMonth = totalDurationDays % daysOfOneMonth; // 取月份余数
                durationMonths = (totalDurationDays - leftDaysOfMonth) / daysOfOneMonth; // 月份余数/30=月数

                durationDays = leftDaysOfMonth;
            } else {
                // 小于1月
                durationDays = totalDurationDays;
            }
        }

        if (durationYears == 0 && durationMonths == 0 && durationDays == 0) {
            return "0天";
        } else {
            return (durationYears > 0 ? (durationYears + "年") : "")
                    + (durationMonths > 0 ? (durationMonths + "个月") : "") + (durationDays > 0 ? (durationDays + "天") : "");
        }
    }

    /**
     * 获取两个时间相差的秒数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long durationSeconds(Date d1, Date d2) {
        return durationSeconds(dateToLocalDateTime(d1), dateToLocalDateTime(d2));
    }

    /**
     * 获取两个日期相差的秒数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long durationSeconds(LocalDate d1, LocalDate d2) {
        return durationSeconds(localDateToDateTime(d1), localDateToDateTime(d2));
    }

    /**
     * 获取两个时间相差的秒数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long durationSeconds(LocalDateTime d1, LocalDateTime d2) {
        return Math.abs(Duration.between(d1, d2).getSeconds());
    }

    /**
     * 根据日期得到当天开始时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDayStart(Date date) {
        return format(date).concat(startTime);
    }

    /**
     * 根据日期得到当天开始时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDayStart(LocalDate date) {
        return format(date).concat(startTime);
    }

    /**
     * 根据日期得到当天开始时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime
     * @return
     */
    public static String getDayStart(LocalDateTime dateTime) {
        return format(dateTime).concat(startTime);
    }

    /**
     * 根据日期得到当天开始时间的字符串,格式为yyyy-MM-dd HH:mm:ss，并转为对应的日期时间对象
     *
     * @param date
     * @param dateClass
     * @return
     */
    public static <T, R> R getDayStart(T date, Class<R> dateClass) {
        return getDayStartOrEnd(date, dateClass, "getDayStart");
    }

    /**
     * 根据日期得到当天结束时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDayEnd(Date date) {
        return format(date).concat(endTime);
    }

    /**
     * 根据日期得到当天结束时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDayEnd(LocalDate date) {
        return format(date).concat(endTime);
    }

    /**
     * 根据日期得到当天结束时间的字符串,格式为yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime
     * @return
     */
    public static String getDayEnd(LocalDateTime dateTime) {
        return format(dateTime).concat(endTime);
    }

    /**
     * 根据日期得到当天结束时间的字符串,格式为yyyy-MM-dd HH:mm:ss，并转为对应的日期时间对象
     *
     * @param date
     * @param dateClass
     * @return
     */
    public static <T, R> R getDayEnd(T date, Class<R> dateClass) {
        return getDayStartOrEnd(date, dateClass, "getDayEnd");
    }

    private static <T, R> R getDayStartOrEnd(T date, Class<R> dateClass, String methodName) {
        try {
            String dateStr = (String) MethodUtils.invokeStaticOrDefault(DateTimeUtils.class,
                    DateTimeUtils.class.getMethod(methodName, date.getClass()), date);
            return commonParse(dateStr, DateTimePattern.PATTERN_DATETIME.value(), dateClass);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期相加/减<br>
     *
     * @param dateTime
     * @param amountToAdd
     *         传入负值可实现相减
     * @param unit
     *         只支持年(YEARS)、月(MONTHS)、日(DAYS)、时(HOURS)、分(MINUTES)、秒(SECONDS)
     * @return
     */
    public static Date plus(Date date, long amountToAdd, ChronoUnit unit) {
        return localDateTimeToDate(plus(dateToLocalDateTime(date), amountToAdd, unit));
    }

    /**
     * 日期相加/减<br>
     * (实际相当于00:00:00的日期做时间相加减，然后返回缺少时分秒信息的日期对象)
     *
     * @param dateTime
     * @param amountToAdd
     *         传入负值可实现相减
     * @param unit
     *         只支持年(YEARS)、月(MONTHS)、日(DAYS)、时(HOURS)、分(MINUTES)、秒(SECONDS)
     * @return
     */
    public static LocalDate plus(LocalDate date, long amountToAdd, ChronoUnit unit) {
        return plus(localDateToDateTime(date), amountToAdd, unit).toLocalDate();
    }

    /**
     * 时间相加/减
     *
     * @param dateTime
     * @param amountToAdd
     *         传入负值可实现相减
     * @param unit
     *         只支持年(YEARS)、月(MONTHS)、日(DAYS)、时(HOURS)、分(MINUTES)、秒(SECONDS)
     * @return
     */
    public static LocalDateTime plus(LocalDateTime dateTime, long amountToAdd, ChronoUnit unit) {
        CheckUtils.checkNotNull(unit, "单位不能为空");

        dateTime = dateTime.withNano(0);
        switch (unit) {
            case SECONDS:
                return dateTime.plusSeconds(amountToAdd);
            case MINUTES:
                return dateTime.plusMinutes(amountToAdd);
            case HOURS:
                return dateTime.plusHours(amountToAdd);
            case DAYS:
                return dateTime.plusDays(amountToAdd);
            case MONTHS:
                return dateTime.plusMonths(amountToAdd);
            case YEARS:
                return dateTime.plusYears(amountToAdd);
            default:
                throw new RuntimeException("只支持年(YEARS)、月(MONTHS)、日(DAYS)、时(HOURS)、分(MINUTES)、秒(SECONDS)这几个单位的日期计算");
        }
    }

    /**
     * 获取日期月份的第一天
     *
     * @param date
     * @return
     */
    public static Date firstDayOfMonth(Date date) {
        return localDateTimeToDate(firstDayOfMonth(dateToLocalDateTime(date)));
    }

    /**
     * 获取日期月份的第一天
     *
     * @param date
     * @return
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        return firstDayOfMonth(localDateToDateTime(date)).toLocalDate();
    }

    /**
     * 获取日期月份的第一天
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime firstDayOfMonth(LocalDateTime dateTime) {
        // 当前日期-(当前日期的天)+(1天)，再带上日期开始的时分秒信息
        return plus(dateTime, -1 * dateTime.getDayOfMonth() + 1, ChronoUnit.DAYS)
                .with(LocalTime.parse(startTime, ofPattern(" HH:mm:ss")));
    }

    /**
     * 获取日期月份的最后一天
     *
     * @param date
     * @return
     */
    public static Date lastDayOfMonth(Date date) {
        return localDateTimeToDate(lastDayOfMonth(dateToLocalDateTime(date)));
    }

    /**
     * 获取日期月份的最后一天
     *
     * @param date
     * @return
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        return lastDayOfMonth(localDateToDateTime(date)).toLocalDate();
    }

    /**
     * 获取日期月份的最后一天
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime lastDayOfMonth(LocalDateTime dateTime) {
        // 当前日期+(1月)-(当前日期的天)，再带上日期结束的时分秒信息
        return plus(plus(dateTime, 1, ChronoUnit.MONTHS), -1 * dateTime.getDayOfMonth(), ChronoUnit.DAYS)
                .with(LocalTime.parse(endTime, ofPattern(" HH:mm:ss")));
    }

    /**
     * 获取UTC时间戳，单位秒
     *
     * @param dateTime
     * @return
     */
    public static long getUTCSeconds(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * 获取UTC时间戳，单位毫秒
     *
     * @param dateTime
     * @return
     */
    public static long getUTCMillSeconds(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * 时间戳转换为LocalDateTime(时区为UTC+8)
     *
     * @param timeMillis
     * @return
     */
    public static LocalDateTime timestampToLocalDateTime(long timeMillis) {
        return Instant.ofEpochMilli(timeMillis).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }

    /**
     * 时间戳转换为Date(时区为UTC+8)
     *
     * @param timeMillis
     * @return
     */
    public static Date timestampToDate(long timeMillis) {
        return localDateTimeToDate(timestampToLocalDateTime(timeMillis));
    }
}