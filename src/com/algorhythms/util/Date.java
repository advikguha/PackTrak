package com.algorhythms.util;

import java.util.StringTokenizer;

/**
 * Represents a calendar date.
 * @author kyle
 */
public class Date {
    /**
     * The days in each month.
     */
    private static final int[] DAYS_IN_MONTHS = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private int month, date, year;

    /**
     * Parses a new date from a string.
     * @param str The date, in format MM/DD/YYYY.
     */
    public Date(String str) {
        super();
        if (str == null)
            throw new IllegalArgumentException("Cannot be null.");

        StringTokenizer strtok = new StringTokenizer(str, "/");
        if (strtok.countTokens() != 3)
            throw new IllegalArgumentException("String must be in format MM/DD/YYYY");

        try {
            month = Integer.parseInt(strtok.nextToken());
            date = Integer.parseInt(strtok.nextToken());
            year = Integer.parseInt(strtok.nextToken());
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("String must be in format MM/DD/YYYY");
        }
    }

    public int getDate() {
        return date;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    /**
     * Determines whether or not a given date is valid or not. For instance,
     * February 29 is not valid in the year 1800.
     * @return True if the date is valid; false otherwise. For instance,
     */
    public boolean isValid() {
        return !(month < 1 || month > 12 || date < 1 || year < 0 || date > getDaysInMonth(month, year));
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @param month A month, where January is 1.
     * @param year The year. Used to determine leap year.
     * @return The number of days in the month.
     */
    private static int getDaysInMonth(int month, int year) {
        return month == 2 ? (isLeapYear(year) ? 29 : 28) : DAYS_IN_MONTHS[month - 1];
    }

    /**
     * @param year A year.
     * @return True if the year is a leap year; false otherwise.
     */
    private static boolean isLeapYear(int year) {
        return year % 4 == 0 ? (year % 100 == 0 ? (year % 400 == 0) : true) : false;
    }

    /**
     * @param d1 The first date.
     * @param d2 The second date.
     * @return A positive number if the first is later than the second, a
     * negative number if it is earlier, and zero if they are equal.
     */
    public static int compare(Date d1, Date d2) {
        if (d1.year == d2.year) {
            if (d1.month == d2.month) {
                return d1.date - d2.date;
            }
            else {
                return d1.month - d2.month;
            }
        }
        else {
            return d1.year - d2.year;
        }
    }

    /**
     * @param str The date string, in format MM/DD/YYYY.
     * @return True if it is valid; false otherwise.
     */
    public static boolean isValidDateString(String str) {
        try {
            return new Date(str).isValid();
        }
        catch(IllegalArgumentException e) {
            return false;
        }
    }

}