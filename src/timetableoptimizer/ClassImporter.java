package timetableoptimizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ClassImporter {
    private static final DateTimeFormatter DATE_WITH_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_NO_YEAR = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    public static List<ClassRecord> importClass(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) throw new FileNotFoundException("CSV file not found: " + filePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) throw new IllegalArgumentException("CSV file is empty.");

        String first = stripBom(lines.get(0));
        List<String> headers = parseCsvLine(first);
        boolean hasHeader = headers.size() == 8 && headers.get(0).equalsIgnoreCase("Topic");
        int startLine = hasHeader ? 1 : 0;
        List<ClassRecord> records = new ArrayList<>();
        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) continue;
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 8) throw new IllegalArgumentException("Line " + (i + 1) + " must contain exactly 8 columns, found " + fields.size() + ".");
            records.add(parseRecord(fields, i + 1));
        }
        return records;
    }

    private static ClassRecord parseRecord(List<String> f, int lineNo) {
        try {
            TopicParts topic = parseTopic(f.get(0));
            Availability availability = Availability.parse(f.get(1));
            String className = required(f.get(2), "Class", lineNo);
            int classInstance = Integer.parseInt(required(f.get(3), "Class instance", lineNo));
            DateRange dates = parseDateRange(required(f.get(4), "Date", lineNo));
            DayOfWeek day = parseDay(required(f.get(5), "Day", lineNo));
            TimeRange times = parseTimeRange(required(f.get(6), "Time", lineNo));
            LocationParts location = parseLocation(required(f.get(7), "Location", lineNo));
            String id = UUID.randomUUID().toString().substring(0, 8);
            return new ClassRecord(id, topic.code, topic.name, availability, className, classInstance,
                    dates.start, dates.end, day, times.start, times.end, location.building, location.room);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Line " + lineNo + " is invalid: " + ex.getMessage(), ex);
        }
    }

    private static String required(String s, String field, int lineNo) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(field + " is missing on line " + lineNo);
        return s.trim();
    }

    private static TopicParts parseTopic(String raw) {
        String value = required(raw, "Topic", -1);
        String[] parts = value.trim().split("\\s+", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Topic must contain code and name, e.g. COMP1701 Game Design: " + raw);
        return new TopicParts(parts[0].trim(), parts[1].trim());
    }

    private static DateRange parseDateRange(String raw) {
        String[] parts = raw.split(" - ");
        if (parts.length != 2) throw new IllegalArgumentException("Date must be a range: " + raw);
        LocalDate start = parseDate(parts[0].trim(), Year.now().getValue());
        LocalDate end = parseDate(parts[1].trim(), start.getYear());
        if (end.isBefore(start)) end = end.plusYears(1);
        return new DateRange(start, end);
    }

    private static LocalDate parseDate(String raw, int defaultYear) {
        try { return LocalDate.parse(raw, DATE_WITH_YEAR); }
        catch (DateTimeParseException ignored) {
            MonthDay md = MonthDay.parse(raw, DATE_NO_YEAR);
            return md.atYear(defaultYear);
        }
    }

    private static TimeRange parseTimeRange(String raw) {
        String[] parts = raw.split(" - ");
        if (parts.length != 2) throw new IllegalArgumentException("Time must be a range: " + raw);
        LocalTime start = LocalTime.parse(parts[0].trim(), TIME_FORMAT);
        LocalTime end = LocalTime.parse(parts[1].trim(), TIME_FORMAT);
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time: " + raw);
        return new TimeRange(start, end);
    }

    private static DayOfWeek parseDay(String raw) {
        return DayOfWeek.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static LocationParts parseLocation(String raw) {
        int comma = raw.indexOf(',');
        if (comma < 0) return new LocationParts(raw.trim(), "");
        return new LocationParts(raw.substring(0, comma).trim(), raw.substring(comma + 1).trim());
    }

    public static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                result.add(current.toString().trim()); current.setLength(0);
            } else current.append(c);
        }
        if (quoted) throw new IllegalArgumentException("CSV line has an unclosed quote.");
        result.add(current.toString().trim());
        return result;
    }

    private static String stripBom(String s) { return s != null && s.startsWith("\uFEFF") ? s.substring(1) : s; }

    private record TopicParts(String code, String name) {}
    private record DateRange(LocalDate start, LocalDate end) {}
    private record TimeRange(LocalTime start, LocalTime end) {}
    private record LocationParts(String building, String room) {}
}
