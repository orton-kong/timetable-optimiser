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
    private static final List<String> EXPECTED_HEADERS = List.of("Topic", "Availability", "Class", "Class instance", "Date", "Day", "Time", "Location");

    public static List<ClassRecord> importClass(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) throw new FileNotFoundException("CSV file not found: " + filePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) throw new IllegalArgumentException("CSV file is empty.");

        String first = stripBom(lines.get(0));
        List<String> headers = parseCsvLine(first);
        boolean hasHeader = looksLikeHeader(headers);
        if (hasHeader) validateHeader(headers);
        int startLine = hasHeader ? 1 : 0;
        List<ClassRecord> records = new ArrayList<>();
        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) continue;
            List<String> fields = parseCsvLine(line);
            if (fields.size() != 8) throw new IllegalArgumentException("Line " + (i + 1) + " must contain exactly 8 columns, found " + fields.size() + ".");
            records.add(parseRecord(fields, i + 1));
        }
        if (records.isEmpty()) throw new IllegalArgumentException("CSV file does not contain any class records.");
        return records;
    }

    private static boolean looksLikeHeader(List<String> headers) {
        return !headers.isEmpty() && headers.get(0).equalsIgnoreCase("Topic");
    }

    private static void validateHeader(List<String> headers) {
        if (headers.size() != EXPECTED_HEADERS.size()) {
            throw new IllegalArgumentException("CSV header must contain exactly these 8 columns: " + String.join(", ", EXPECTED_HEADERS));
        }
        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
            if (!headers.get(i).trim().equalsIgnoreCase(EXPECTED_HEADERS.get(i))) {
                throw new IllegalArgumentException("CSV header column " + (i + 1) + " must be '" + EXPECTED_HEADERS.get(i) + "', found '" + headers.get(i) + "'.");
            }
        }
    }

    private static ClassRecord parseRecord(List<String> f, int lineNo) {
        try {
            TopicParts topic = parseTopic(f.get(0));
            Availability availability = Availability.parse(f.get(1));
            String className = required(f.get(2), "Class", lineNo);
            int classInstance = parsePositiveInt(required(f.get(3), "Class instance", lineNo), "Class instance");
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
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(field + " is missing" + (lineNo > 0 ? " on line " + lineNo : "") + ".");
        return s.trim();
    }

    private static int parsePositiveInt(String raw, String field) {
        int value = Integer.parseInt(raw.trim());
        if (value <= 0) throw new IllegalArgumentException(field + " must be greater than 0.");
        return value;
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
        if (end.isBefore(start)) throw new IllegalArgumentException("Date of last class must not be before date of first class: " + raw);
        return new DateRange(start, end);
    }

    static LocalDate parseDate(String raw, int defaultYear) {
        try { return LocalDate.parse(raw, DATE_WITH_YEAR); }
        catch (DateTimeParseException ignored) {
            try {
                MonthDay md = MonthDay.parse(raw, DATE_NO_YEAR);
                return md.atYear(defaultYear);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Date must use yyyy-mm-dd, d MMM, or d MMM yyyy format: " + raw);
            }
        }
    }

    private static TimeRange parseTimeRange(String raw) {
        String[] parts = raw.split(" - ");
        if (parts.length != 2) throw new IllegalArgumentException("Time must be a range: " + raw);
        LocalTime start = parseTime(parts[0].trim());
        LocalTime end = parseTime(parts[1].trim());
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time: " + raw);
        return new TimeRange(start, end);
    }

    static LocalTime parseTime(String raw) {
        try { return LocalTime.parse(raw.trim(), TIME_FORMAT); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Time must use HH:mm format: " + raw); }
    }

    static DayOfWeek parseDay(String raw) {
        try { return DayOfWeek.valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Day must be Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, or Sunday: " + raw); }
    }

    private static LocationParts parseLocation(String raw) {
        int comma = raw.indexOf(',');
        if (comma < 0) throw new IllegalArgumentException("Location must contain building and room separated by a comma: " + raw);
        String building = raw.substring(0, comma).trim();
        String room = raw.substring(comma + 1).trim();
        if (building.isBlank() || room.isBlank()) throw new IllegalArgumentException("Location must contain both building and room: " + raw);
        return new LocationParts(building, room);
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
