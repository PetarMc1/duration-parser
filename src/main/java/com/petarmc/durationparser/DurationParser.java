package com.petarmc.durationparser;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility record that wraps a {@link Duration} and provides parsing and
 * formatting helpers for human reeadable duration strings.
 *
 * Example supported inputs: "1d 2h 3m 4s", "2 hours 30 minutes", "45s".
 * Supported units: days (d), hours (h, hr, hrs), minutes (m, min, mins),
 * seconds (s, sec, secs). Parsing is case-insensitive.
 */
public record DurationParser(Duration duration) {
    /**
     * Pattern for parsing a numeric value followed by a time unit.
     * Captures a signed integer and a unit token (days/hours/minutes/seconds
     * and common short forms). The pattern is case-insensitive.
     */
    private static final Pattern TOKEN = Pattern.compile("([+-]?\\d+)\\s*(days?|hours?|minutes?|seconds?|hrs?|mins?|secs?|hr|min|sec|d|h|m|s)", Pattern.CASE_INSENSITIVE);

    /**
     * Compact canonical constructor used by the record.
     * Validates that the supplied {@code duration} is non-null and non-negative.
     *
     * @param duration the wrapped {@link Duration}
     * @throws IllegalArgumentException if {@code duration} is null or negative
     */
    public DurationParser {
        if (duration == null) {
            throw new IllegalArgumentException("duration must not be 0");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration can not be negative!");
        }
    }

    /**
     * Parse a human-friendly duration string into a {@link DurationParser}.
     *
     * <p>The input may contain multiple token pairs of "<number><unit>" where
     * unit can be one of: d, day, days, h, hr, hrs, hour, hours, m, min, mins,
     * minute, minutes, s, sec, secs, second, seconds. Tokens may be separated
     * by whitespace. Examples: "1d 2h", "90 minutes", "45s".
     *
     * @param input the string to parse; must not be null or blank
     * @return a new {@link DurationParser} representing the parsed duration
     * @throws IllegalArgumentException if the input is null/blank, contains
     *                                  invalid tokens, negative values, or
     *                                  if the resulting duration is too large
     */
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
            if (containsNonWhitespace(normalized, position, matcher.start())) {
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
        
        if (matches == 0 || containsNonWhitespace(normalized, position, normalized.length())) {
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

    /**
     * Format the wrapped duration in a short compact representation.
     * Example: "1d2h3m4s" (no spaces).
     *
     * @return short formatted representation
     */
    public String toShortString() {
        return format(false, "");
    }

    /**
     * Format the wrapped duration in a compact but readable representation.
     * Example: "1d 2h 3m 4s" (spaces between parts).
     *
     * @return compact formatted representation
     */
    public String toCompactString() {
        return format(true, " ");
    }

    /**
     * Format the wrapped duration into a long human-readable string.
     * Example: "1 day, 2 hours and 3 minutes". Uses proper pluralization
     * and joins the last two parts with "and".
     *
     * @return long human-readable representation
     */
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

    /**
     * Append a single long-format part (e.g. "2 hours") to the target
     * {@link StringBuilder} if {@code value} &gt; 0.
     *
     * @param sb   the target builder
     * @param value the numeric value for the unit
     * @param unit  singular unit name (e.g. "hour")
     */
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

    /**
     * Internal formatter used by the short/compact formatters. When {@code spaced}
     * is true the provided {@code separator} is inserted between parts.
     *
     * @param spaced    whether to insert separators between non-empty parts
     * @param separator the separator to use when spaced==true (e.g. " ")
     * @return formatted string such as "1d2h3m4s" or "1d 2h 3m 4s"
     */
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
            if (!sb.isEmpty() && spaced) sb.append(separator);
            sb.append(hours).append('h');
        }
        if (minutes != 0) {
            if (!sb.isEmpty() && spaced) sb.append(separator);
            sb.append(minutes).append('m');
        }
        if (seconds != 0 || sb.isEmpty()) {
            if (!sb.isEmpty() && spaced) sb.append(separator);
            sb.append(seconds).append('s');
        }
        return sb.toString();
    }

    /**
     * Check whether the substring value[start:end] contains any non-whitespace
     * character. Used by the parser to ensure there are no unexpected characters
     * between matched tokens.
     *
     * @param value the full string
     * @param start inclusive start index
     * @param end   exclusive end index
     * @return true if the range contains at least one non-whitespace character
     */
    private static boolean containsNonWhitespace(String value, int start, int end) {
        if (start >= end) {
            return false;
        }
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
