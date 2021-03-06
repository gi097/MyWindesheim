/**
 * Copyright (c) 2019 Giovanni Terlingen
 * <p/>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.giovanniterlingen.windesheim.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class TimeUtils {

    private static final SimpleDateFormat yearMonthDayDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE);
    private static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("dd", Locale.FRANCE);
    private static final SimpleDateFormat hourMinuteFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

    public static SimpleDateFormat getYearMonthDayDateFormat() {
        return yearMonthDayDateFormat;
    }

    public static SimpleDateFormat getDayDateFormat() {
        return dayDateFormat;
    }

    public static SimpleDateFormat getHourMinuteFormat() {
        return hourMinuteFormat;
    }

    public static Calendar getCalendar() {
        return GregorianCalendar.getInstance(Locale.FRANCE);
    }

    public static String[] getWeekDates(Date date) {
        SimpleDateFormat yearMonthDateFormat = getYearMonthDayDateFormat();
        Calendar calendar = GregorianCalendar.getInstance(Locale.FRANCE);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
        String lowestDate = yearMonthDateFormat.format(calendar.getTime());
        calendar.add(GregorianCalendar.DATE, 6);
        String highestDate = yearMonthDateFormat.format(calendar.getTime());
        return new String[]{lowestDate, highestDate};
    }

    /**
     * Windesheim API serves epoch timestamps, but not in UTC
     */
    public static long removeTimeOffset(long time) {
        time -= TimeZone.getTimeZone("Europe/Amsterdam").getOffset(time);
        return time;
    }
}
