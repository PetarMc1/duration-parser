# duration-parser

A lightweight, zero-dependency Java 17+ library for parsing and formatting human-readable durations (e.g., `"2h 30m"`, `"1d5h15m"`, `"45min"`) to and from `java.time.Duration`.

![Build Status](https://img.shields.io/badge/build-passing-lightgrey)
![Maven Central](https://img.shields.io/badge/maven--central-pending-lightgrey)

## Maven
```xml
<dependency>
    <groupId>com.petarmc</groupId>
    <artifactId>duration-parser</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
```java
import com.petarmc.durationparser.DurationParser;

DurationParser d = DurationParser.parse("1d 2h 30m");
System.out.println(d.duration());        // PT26H30M
System.out.println(d.toShortString());   // 1d2h30m
System.out.println(d.toCompactString()); // 1d 2h 30m
System.out.println(d.toLongString());    // 1 day, 2 hours and 30 minutes
```

## Supported inputs
- Units: days (`d`), hours (`h`), minutes (`m`, `min`), seconds (`s`)
- Mixed order, with or without spaces, case-insensitive, plural optional (e.g., `"1 minute"`, `"2 minutes"`)
- Examples: `"2h 30m"`, `"1d5h15m"`, `"45min"`, `"3600s"`, `"2 hours 30 minutes"`, `"1day 3hours"`

## Formatting styles
- Short: `2h30m15s`
- Long/natural: `2 hours, 30 minutes and 15 seconds`
- Compact: `2h 30m`

## License
Apache License 2.0. See `LICENSE` and `NOTICE`.
