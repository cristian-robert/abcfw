package com.spring.befwlc.utils;



import com.spring.befwlc.exceptions.TestExecutionException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DateUtils {

    public static final String FULL_DATE_AND_OFFSET_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static String shortDateString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String fullSysOffsetDate() {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }

    public static String fullSysDatePlusOneHour() {
        return OffsetDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }

    public static String fullSysDatePlusMinutes(final int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String shortSysDatePlusDays(final int days) {
        return LocalDateTime.now().plusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String shortSysDateMinusDays(final int days) {
        return LocalDateTime.now().minusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String fullSysDateMinusMinutes(final int minutes) {
        return LocalDateTime.now().minusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String sysDateOfFormat(final String format) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            throw new TestExecutionException("Failed to parse date of format: %s", format);
        }
    }

    public static String fullSysDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String fullIsoDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }

    public static String stringDateToFormattedDate(final String inputDateString, final String inputDateStringFormat, final String outputDateFormat) {
        return LocalDateTime.parse(inputDateString, DateTimeFormatter.ofPattern(inputDateStringFormat))
                .format(DateTimeFormatter.ofPattern(outputDateFormat));
    }

    public static String generateValueDate(String product, String priority) {
        Calendar calendar = Calendar.getInstance();
        int spotDays = Integer.parseInt(TransactionDataUtils.getByProductProperty(product, priority, "Spot_days",
                "path/config/src/test/resources/configs/oro/payment_engine_cutoff_times.json"));
        calendar.add(Calendar.DATE, spotDays);
        String valueDate;

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case(Calendar.SATURDAY):
                valueDate = OffsetDateTime.now().plusDays(spotDays + 2).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
                break;
            case(Calendar.SUNDAY):
                valueDate = OffsetDateTime.now().plusDays(spotDays + 1).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
                break;
            default:
                valueDate = LocalDateTime.now().plusDays(spotDays).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
        }

        return valueDate;
    }
}