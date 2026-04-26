/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.impl.JavaTimeConverters;
import com.ibm.icu.message2.Directionality;
import com.ibm.icu.message2.FormattedPlaceholder;
import com.ibm.icu.message2.Formatter;
import com.ibm.icu.message2.FormatterFactory;
import com.ibm.icu.message2.OptUtils;
import com.ibm.icu.message2.PlainStringFormattedValue;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.SimpleTimeZone;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DateTimeFormatterFactory
implements FormatterFactory {
    private final String kind;
    private static final Pattern ISO_PATTERN = Pattern.compile("^(([0-9]{4})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])){1}(T([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]{1,3})?(Z|[+-]((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?)?$");

    DateTimeFormatterFactory(String kind) {
        switch (kind) {
            case "date": {
                break;
            }
            case "time": {
                break;
            }
            case "datetime": {
                break;
            }
            default: {
                kind = "datetime";
            }
        }
        this.kind = kind;
    }

    private static int stringToStyle(String option) {
        switch (option) {
            case "full": {
                return 0;
            }
            case "long": {
                return 1;
            }
            case "medium": {
                return 2;
            }
            case "short": {
                return 3;
            }
        }
        throw new IllegalArgumentException("Invalid datetime style: " + option);
    }

    @Override
    public Formatter createFormatter(Locale locale, Map<String, Object> fixedOptions) {
        locale = OptUtils.getBestLocale(fixedOptions, locale);
        Directionality dir = OptUtils.getBestDirectionality(fixedOptions, locale);
        boolean reportErrors = OptUtils.reportErrors(fixedOptions);
        int dateStyle = -1;
        int timeStyle = -1;
        switch (this.kind) {
            case "date": {
                dateStyle = DateTimeFormatterFactory.getDateTimeStyle(fixedOptions, "style");
                break;
            }
            case "time": {
                timeStyle = DateTimeFormatterFactory.getDateTimeStyle(fixedOptions, "style");
                break;
            }
            default: {
                dateStyle = DateTimeFormatterFactory.getDateTimeStyle(fixedOptions, "dateStyle");
                timeStyle = DateTimeFormatterFactory.getDateTimeStyle(fixedOptions, "timeStyle");
            }
        }
        if (dateStyle == -1 && timeStyle == -1) {
            String skeleton = "";
            switch (this.kind) {
                case "date": {
                    skeleton = DateTimeFormatterFactory.getDateFieldOptions(fixedOptions);
                    break;
                }
                case "time": {
                    skeleton = DateTimeFormatterFactory.getTimeFieldOptions(fixedOptions);
                    break;
                }
                default: {
                    skeleton = DateTimeFormatterFactory.getDateFieldOptions(fixedOptions);
                    skeleton = skeleton + DateTimeFormatterFactory.getTimeFieldOptions(fixedOptions);
                }
            }
            if (skeleton.isEmpty()) {
                skeleton = OptUtils.getString(fixedOptions, "icu:skeleton", "");
            }
            if (!skeleton.isEmpty()) {
                DateFormat df = DateFormat.getInstanceForSkeleton(skeleton, locale);
                return new DateTimeFormatter(locale, df, reportErrors);
            }
            switch (this.kind) {
                case "date": {
                    dateStyle = 3;
                    timeStyle = -1;
                    break;
                }
                case "time": {
                    dateStyle = -1;
                    timeStyle = 3;
                    break;
                }
                default: {
                    dateStyle = 3;
                    timeStyle = 3;
                }
            }
        }
        DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        return new DateTimeFormatter(locale, df, reportErrors);
    }

    private static int getDateTimeStyle(Map<String, Object> options, String key) {
        String opt = OptUtils.getString(options, key);
        if (opt != null) {
            return DateTimeFormatterFactory.stringToStyle(opt);
        }
        return -1;
    }

    private static String getDateFieldOptions(Map<String, Object> options) {
        String opt;
        StringBuilder skeleton = new StringBuilder();
        switch (opt = OptUtils.getString(options, "weekday", "")) {
            case "long": {
                skeleton.append("EEEE");
                break;
            }
            case "short": {
                skeleton.append("E");
                break;
            }
            case "narrow": {
                skeleton.append("EEEEEE");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "era", "")) {
            case "long": {
                skeleton.append("GGGG");
                break;
            }
            case "short": {
                skeleton.append("G");
                break;
            }
            case "narrow": {
                skeleton.append("GGGGG");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "year", "")) {
            case "numeric": {
                skeleton.append("y");
                break;
            }
            case "2-digit": {
                skeleton.append("yy");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "month", "")) {
            case "numeric": {
                skeleton.append("M");
                break;
            }
            case "2-digit": {
                skeleton.append("MM");
                break;
            }
            case "long": {
                skeleton.append("MMMM");
                break;
            }
            case "short": {
                skeleton.append("MMM");
                break;
            }
            case "narrow": {
                skeleton.append("MMMMM");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "day", "")) {
            case "numeric": {
                skeleton.append("d");
                break;
            }
            case "2-digit": {
                skeleton.append("dd");
                break;
            }
        }
        return skeleton.toString();
    }

    private static String getTimeFieldOptions(Map<String, Object> options) {
        String opt;
        StringBuilder skeleton = new StringBuilder();
        int showHour = 0;
        switch (opt = OptUtils.getString(options, "hour", "")) {
            case "numeric": {
                showHour = 1;
                break;
            }
            case "2-digit": {
                showHour = 2;
                break;
            }
        }
        if (showHour > 0) {
            String hourCycle = "";
            switch (opt = OptUtils.getString(options, "hourCycle", "")) {
                case "h11": {
                    hourCycle = "K";
                    break;
                }
                case "h12": {
                    hourCycle = "h";
                    break;
                }
                case "h23": {
                    hourCycle = "H";
                    break;
                }
                case "h24": {
                    hourCycle = "k";
                    break;
                }
                default: {
                    hourCycle = "j";
                }
            }
            skeleton.append(hourCycle);
            if (showHour == 2) {
                skeleton.append(hourCycle);
            }
        }
        switch (opt = OptUtils.getString(options, "minute", "")) {
            case "numeric": {
                skeleton.append("m");
                break;
            }
            case "2-digit": {
                skeleton.append("mm");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "second", "")) {
            case "numeric": {
                skeleton.append("s");
                break;
            }
            case "2-digit": {
                skeleton.append("ss");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "fractionalSecondDigits", "")) {
            case "1": {
                skeleton.append("S");
                break;
            }
            case "2": {
                skeleton.append("SS");
                break;
            }
            case "3": {
                skeleton.append("SSS");
                break;
            }
        }
        switch (opt = OptUtils.getString(options, "timeZoneName", "")) {
            case "long": {
                skeleton.append("z");
                break;
            }
            case "short": {
                skeleton.append("zzzz");
                break;
            }
            case "shortOffset": {
                skeleton.append("O");
                break;
            }
            case "longOffset": {
                skeleton.append("OOOO");
                break;
            }
            case "shortGeneric": {
                skeleton.append("v");
                break;
            }
            case "longGeneric": {
                skeleton.append("vvvv");
                break;
            }
        }
        return skeleton.toString();
    }

    private static Integer safeParse(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return Integer.parseInt(str);
    }

    private static Object parseIso8601(String text) {
        Matcher m = ISO_PATTERN.matcher(text);
        if (m.find() && m.groupCount() == 12 && !m.group().isEmpty()) {
            Integer year = DateTimeFormatterFactory.safeParse(m.group(2));
            Integer month = DateTimeFormatterFactory.safeParse(m.group(3));
            Integer day = DateTimeFormatterFactory.safeParse(m.group(4));
            Integer hour = DateTimeFormatterFactory.safeParse(m.group(6));
            Integer minute = DateTimeFormatterFactory.safeParse(m.group(7));
            Integer second = DateTimeFormatterFactory.safeParse(m.group(8));
            Integer millisecond = 0;
            if (m.group(9) != null) {
                String z = (m.group(9) + "000").substring(1, 4);
                millisecond = DateTimeFormatterFactory.safeParse(z);
            } else {
                millisecond = 0;
            }
            String tzPart = m.group(10);
            if (hour == null) {
                hour = 0;
                minute = 0;
                second = 0;
            }
            GregorianCalendar gc = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            gc.set(14, millisecond);
            if (tzPart != null) {
                if (tzPart.equals("Z")) {
                    gc.setTimeZone(com.ibm.icu.util.TimeZone.GMT_ZONE);
                } else {
                    int sign = tzPart.startsWith("-") ? -1 : 1;
                    String[] tzParts = tzPart.substring(1).split(":");
                    if (tzParts.length == 2) {
                        Integer tzHour = DateTimeFormatterFactory.safeParse(tzParts[0]);
                        Integer tzMin = DateTimeFormatterFactory.safeParse(tzParts[1]);
                        if (tzHour != null && tzMin != null) {
                            int offset = sign * (tzHour * 60 + tzMin) * 60 * 1000;
                            gc.setTimeZone(new SimpleTimeZone(offset, "offset"));
                        }
                    }
                }
            }
            return gc;
        }
        return text;
    }

    private static class DateTimeFormatter
    implements Formatter {
        private final DateFormat icuFormatter;
        private final Locale locale;
        private final boolean reportErrors;

        private DateTimeFormatter(Locale locale, DateFormat df, boolean reportErrors) {
            this.locale = locale;
            this.icuFormatter = df;
            this.reportErrors = reportErrors;
        }

        @Override
        public FormattedPlaceholder format(Object toFormat, Map<String, Object> variableOptions) {
            if (toFormat == null) {
                return null;
            }
            if (toFormat instanceof CharSequence) {
                if ((toFormat = DateTimeFormatterFactory.parseIso8601(toFormat.toString())) instanceof CharSequence) {
                    if (this.reportErrors) {
                        throw new IllegalArgumentException("bad-operand: argument must be ISO 8601");
                    }
                    return new FormattedPlaceholder(toFormat, new PlainStringFormattedValue("{|" + toFormat + "|}"));
                }
            } else if (toFormat instanceof Temporal) {
                toFormat = JavaTimeConverters.temporalToCalendar((Temporal)toFormat);
            }
            if (toFormat instanceof Calendar) {
                TimeZone tz = ((Calendar)toFormat).getTimeZone();
                long milis = ((Calendar)toFormat).getTimeInMillis();
                com.ibm.icu.util.TimeZone icuTz = com.ibm.icu.util.TimeZone.getTimeZone(tz.getID());
                com.ibm.icu.util.Calendar calendar = com.ibm.icu.util.Calendar.getInstance(icuTz, this.locale);
                calendar.setTimeInMillis(milis);
                toFormat = calendar;
            }
            String result = this.icuFormatter.format(toFormat);
            return new FormattedPlaceholder(toFormat, new PlainStringFormattedValue(result));
        }

        @Override
        public String formatToString(Object toFormat, Map<String, Object> variableOptions) {
            FormattedPlaceholder result = this.format(toFormat, variableOptions);
            return result != null ? result.toString() : null;
        }
    }
}

