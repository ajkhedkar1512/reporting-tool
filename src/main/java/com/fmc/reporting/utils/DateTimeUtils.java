package com.fmc.reporting.utils;

import com.fmc.reporting.constants.Constants;

import java.awt.desktop.SystemSleepEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    public static String minusDays(final String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(input, formatter);
        LocalDate newDate = date.minusDays( 1);
        System.out.println( newDate.format(formatter));
        return newDate.format(formatter);
    }

    public static String plusDays(final String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(input, formatter);
        LocalDate newDate = date.plusDays( 1);
        System.out.println( newDate.format(formatter));
        return newDate.format(formatter);
    }

    public static boolean isWeekend(final String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(input, formatter);
        System.out.println(date.getDayOfWeek().getValue());
        return date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7;
    }

    public static boolean isFriday(final String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(input, formatter);
        System.out.println(date.getDayOfWeek().getValue());
        return date.getDayOfWeek().getValue() == 5;
    }

    public static String getMondayDate(final String friday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(friday, formatter);
        LocalDate newDate = date.plusDays( 3);
        System.out.println( newDate.format(formatter));
        return newDate.format(formatter);
    }

    public static String getFormattedDate(final String inputDate) {
        String formattedDate = "";
        try {
            final Instant dateInstant = Instant.parse(inputDate);
            final LocalDateTime dateTime = LocalDateTime.ofInstant(dateInstant, ZoneId.of("EST",  ZoneId.SHORT_IDS));
            formattedDate = DateTimeFormatter.ofPattern(Constants.CURRENT_DATE_FORMAT).format(dateTime);
        } catch (final DateTimeParseException e) {
            final SimpleDateFormat receivedFormat = new SimpleDateFormat(Constants.INITIAL_DATE_FORMAT);
            final SimpleDateFormat requiredFormat = new SimpleDateFormat(Constants.CURRENT_DATE_FORMAT);
            try {
                formattedDate = requiredFormat.format(receivedFormat.parse(inputDate));
            } catch (final ParseException parseException) {
                formattedDate = inputDate;
            }
        }
        return formattedDate;
    }
}
