package com.spring.befwlc.v2.util;

import com.spring.befwlc.v2.exception.TestExecutionException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    public static final String FULL_DATE_AND_OFFSET_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";
    public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private DateUtils() {}

    public static String shortDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }
    public static String fullOffsetDate() {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }
    public static String fullOffsetDatePlusOneHour() {
        return OffsetDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }
    public static String fullDatePlusMinutes(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }
    public static String shortDatePlusDays(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }
    public static String shortDateMinusDays(int days) {
        return LocalDate.now().minusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }
    public static String fullDateMinusMinutes(int minutes) {
        return LocalDateTime.now().minusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }
    public static String ofFormat(String format) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            throw new TestExecutionException("Invalid date format pattern: %s", format);
        }
    }
    public static String fullDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }
    public static String isoDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }
    public static String reformatDate(String input, String fromPattern, String toPattern) {
        return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(fromPattern))
                .format(DateTimeFormatter.ofPattern(toPattern));
    }
    public static String valueDateSkippingWeekends(int spotDays) {
        LocalDate target = LocalDate.now().plusDays(spotDays);
        if (target.getDayOfWeek() == DayOfWeek.SATURDAY) target = target.plusDays(2);
        else if (target.getDayOfWeek() == DayOfWeek.SUNDAY) target = target.plusDays(1);
        return target.format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }
}
