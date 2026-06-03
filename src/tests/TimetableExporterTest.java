package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TimetableExporterTest {

    @AfterEach
    void end() throws IOException {
        Files.deleteIfExists(Paths.get("Export_Test.csv"));
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Constructor")
    @Test
    void construct(){
        TimetableExporter exporter = new TimetableExporter();
        assertNotNull(exporter);
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Export Timetable")
    @Test
    void exportTimetable() throws IOException {
        Timetable timetable = new Timetable("Export Test", List.of(makeClass("1", "Tutorial", "09:00", "10:00")), false, new Preference());

        TimetableExporter.exportTimetable(timetable);

        String text = Files.readString(Paths.get("Export_Test.csv"));
        assertAll(
                () -> assertTrue(text.contains("Topic code")),
                () -> assertTrue(text.contains("COMP1001")),
                () -> assertTrue(text.contains("Tutorial"))
        );
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Export Timetable With Clash")
    @Test
    void exportTimetableWithClash() throws IOException {
        String filePath = "out/test-exports/clash.csv";
        ClassRecord a = makeClass("1", "Tutorial", "09:00", "10:00");
        ClassRecord b = makeClass("2", "Workshop", "09:30", "10:30");
        Timetable timetable = new Timetable("Clash Test", List.of(a, b), false, new Preference());

        TimetableExporter.exportTimetable(timetable, filePath);

        String text = Files.readString(Paths.get(filePath));
        assertAll(
                () -> assertTrue(text.contains("Warnings")),
                () -> assertTrue(text.contains("Time clash"))
        );
    }

    private ClassRecord makeClass(String id, String className, String start, String end){
        return new ClassRecord(
                id,
                "COMP1001",
                "Programming",
                new Availability("In person", Campus.BEDFORD, 2, 1),
                className,
                1,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                DayOfWeek.MONDAY,
                LocalTime.parse(start),
                LocalTime.parse(end),
                "Building",
                "Room"
        );
    }
}
