package com.petarmc.durationparser;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationParserTest {
    @Test
    void parsesSimpleMixed() {
        DurationParser d = DurationParser.parse("2h 30m");
        assertEquals(Duration.ofHours(2).plusMinutes(30), d.duration());
    }

    @Test
    void parsesWithoutSpaces() {
        DurationParser d = DurationParser.parse("1d5h15m");
        assertEquals(Duration.ofDays(1).plusHours(5).plusMinutes(15), d.duration());
    }

    @Test
    void parsesMinutesLongForm() {
        DurationParser d = DurationParser.parse("45 minutes");
        assertEquals(Duration.ofMinutes(45), d.duration());
    }

    @Test
    void parsesSecondsNumericOnly() {
        DurationParser d = DurationParser.parse("3600s");
        assertEquals(Duration.ofHours(1), d.duration());
    }

    @Test
    void parsesWordy() {
        DurationParser d = DurationParser.parse("2 hours 30 minutes");
        assertEquals(Duration.ofHours(2).plusMinutes(30), d.duration());
    }

    @Test
    void orderDoesNotMatter() {
        DurationParser d = DurationParser.parse("15m 1d 3h");
        assertEquals(Duration.ofDays(1).plusHours(3).plusMinutes(15), d.duration());
    }

    @Test
    void rejectsNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("-5m"));
        assertTrue(ex.getMessage().toLowerCase().contains("negative"));
    }

    @Test
    void rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("abc"));
    }

    @Test
    void formatsShort() {
        DurationParser d = new DurationParser(Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4));
        assertEquals("1d2h3m4s", d.toShortString());
    }

    @Test
    void formatsCompact() {
        DurationParser d = new DurationParser(Duration.ofHours(2).plusMinutes(30));
        assertEquals("2h 30m", d.toCompactString());
    }

    @Test
    void formatsLong() {
        DurationParser d = new DurationParser(Duration.ofHours(1).plusMinutes(1).plusSeconds(1));
        assertEquals("1 hour, 1 minute and 1 second", d.toLongString());
    }

    @Test
    void zeroDuration() {
        DurationParser d = new DurationParser(Duration.ZERO);
        assertEquals("0s", d.toShortString());
        assertEquals("0s", d.toCompactString());
        assertEquals("0 seconds", d.toLongString());
    }

    @Test
    void parsesUppercase() {
        DurationParser d = DurationParser.parse("1D 2H");
        assertEquals(Duration.ofDays(1).plusHours(2), d.duration());
    }

    @Test
    void aggregatesDuplicateUnits() {
        DurationParser d = DurationParser.parse("1h 30m 30m");
        assertEquals(Duration.ofHours(2), d.duration());
    }

    @Test
    void rejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("   "));
    }

    @Test
    void rejectsTrailingGarbage() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("1h abc"));
    }
}
