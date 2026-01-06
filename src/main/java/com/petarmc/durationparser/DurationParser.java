package com.petarmc.durationparser;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DurationParser(Duration duration) {
    private static final Pattern TOKEN = Pattern.compile("([+-]?\\d+)\\s*(days?|hours?|minutes?|seconds?|hrs?|mins?|secs?|hr|min|sec|d|h|m|s)", Pattern.CASE_INSENSITIVE);

    public DurationParser {
        if (duration == null) {
            throw new IllegalArgumentException("duration must not be 0");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration can not be negative!");
        }
    }

    public static DurationParser parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null or blank!");
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("input must not be null or blank");
        }

        Matcher matcher = TOKEN.matcher(normalized);
        long days = 0, hours = 0, minutes = 0, seconds = 0;
        int matches = 0;
        int position = 0;

        while (matcher.find()) {
            if (!isBlankRange(normalized, position, matcher.start())) {
                throw new IllegalArgumentException("Invalid duration format near position " + position + ": " + input);
            }
            matches++;
            long value = Long.parseLong(matcher.group(1));
            if (value < 0) {
                throw new IllegalArgumentException("duration must not be negative");
            }
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            switch (unit) {
                case "d", "day", "days" -> days = Math.addExact(days, value);
                case "h", "hr", "hrs", "hour", "hours" -> hours = Math.addExact(hours, value);
                case "m", "min", "mins", "minute", "minutes" -> minutes = Math.addExact(minutes, value);
                case "s", "sec", "secs", "second", "seconds" -> seconds = Math.addExact(seconds, value);
                default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
            position = matcher.end();
        }
        
        if (matches == 0 || !isBlankRange(normalized, position, normalized.length())) {
            throw new IllegalArgumentException("Invalid duration format: " + input);
        }

        try {
            Duration d = Duration.ZERO
                    .plusDays(days)
                    .plusHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);
            return new DurationParser(d);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Duration is too large", ex);
        }
    }

    public String toShortString() {
        return format(false, "");
    }

    public String toCompactString() {
        return format(true, " ");
    }

    public String toLongString() {
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        appendLongPart(sb, days, "day");
        appendLongPart(sb, hours, "hour");
        appendLongPart(sb, minutes, "minute");
        appendLongPart(sb, seconds, "second");

        if (sb.isEmpty()) {
            return "0 seconds";
        }

        int lastComma = sb.lastIndexOf(", ");
        if (lastComma != -1) {
            sb.replace(lastComma, lastComma + 2, " and ");
        }
        return sb.toString();
    }

    private void appendLongPart(StringBuilder sb, long value, String unit) {
        if (value <= 0) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(", ");
        }
        sb.append(value).append(' ').append(unit);
        if (value != 1) {
            sb.append('s');
        }
    }

    private String format(boolean spaced, String separator) {
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days != 0) {
            sb.append(days).append('d');
        }
        if (hours != 0) {
            if (sb.length() > 0 && spaced) sb.append(separator);
            sb.append(hours).append('h');
        }
        if (minutes != 0) {
            if (sb.length() > 0 && spaced) sb.append(separator);
            sb.append(minutes).append('m');
        }
        if (seconds != 0 || sb.length() == 0) {
            if (sb.length() > 0 && spaced) sb.append(separator);
            sb.append(seconds).append('s');
        }
        return sb.toString();
    }

    private static boolean isBlankRange(String value, int start, int end) {
        if (start >= end) {
            return true;
        }
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
