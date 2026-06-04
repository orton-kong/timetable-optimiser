package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class ClashDetectorTest {

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Empty Clash Detection")
    @Test
    void emptyClashDetection(){
        ClashDetector.clashDetection();
        assertEquals(0, ClashDetector.clashDetection(new ArrayList<>(), false).size());
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Time Clash")
    @Test
    void timeClash(){
        ClassRecord a = makeClass("1", "Workshop", Campus.BEDFORD, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord b = makeClass("2", "Tutorial", Campus.BEDFORD, DayOfWeek.MONDAY, "09:30", "10:30");

        List<String> warnings = ClashDetector.clashDetection(List.of(a, b), false);

        assertAll(
                () -> assertEquals(1, warnings.size()),
                () -> assertTrue(warnings.get(0).contains("Time clash")),
                () -> assertTrue(ClashDetector.hasHardClash(List.of(a, b), false))
        );
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Lecture Clash Allowed")
    @Test
    void lectureClashAllowed(){
        ClassRecord a = makeClass("1", "Lecture", Campus.BEDFORD, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord b = makeClass("2", "Tutorial", Campus.BEDFORD, DayOfWeek.MONDAY, "09:30", "10:30");

        assertAll(
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(a, b), true).size()),
                () -> assertFalse(ClashDetector.hasHardClash(List.of(a, b), true))
        );
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Insufficient Commute Gap")
    @Test
    void insufficientCommuteGap(){
        ClassRecord a = makeClass("1", "Workshop", Campus.BEDFORD, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord b = makeClass("2", "Tutorial", Campus.TONSLEY, DayOfWeek.MONDAY, "10:15", "11:00");

        List<String> warnings = ClashDetector.clashDetection(List.of(b, a), false);

        assertAll(
                () -> assertEquals(1, warnings.size()),
                () -> assertTrue(warnings.get(0).contains("Insufficient commute gap")),
                () -> assertTrue(warnings.get(0).contains("15 minutes"))
        );
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("No Clash")
    @Test
    void noClash(){
        ClassRecord monday = makeClass("1", "Workshop", Campus.BEDFORD, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord tuesday = makeClass("2", "Tutorial", Campus.TONSLEY, DayOfWeek.TUESDAY, "09:30", "10:30");
        ClassRecord oldDate = makeClass("3", "Practical", Campus.CITY, DayOfWeek.MONDAY, "09:30", "10:30");
        ClassRecord sameCampus = makeClass("4", "Seminar", Campus.BEDFORD, DayOfWeek.MONDAY, "10:15", "11:00");
        ClassRecord enoughGap = makeClass("5", "Lab", Campus.TONSLEY, DayOfWeek.MONDAY, "10:30", "11:30");
        ClassRecord lectureGap = makeClass("6", "Lecture", Campus.TONSLEY, DayOfWeek.MONDAY, "10:15", "11:00");
        oldDate.setStartDate(LocalDate.of(2025, 2, 1));
        oldDate.setEndDate(LocalDate.of(2025, 3, 1));

        assertAll(
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(monday, tuesday), false).size()),
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(monday, oldDate), false).size()),
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(monday, sameCampus), false).size()),
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(monday, enoughGap), false).size()),
                () -> assertEquals(0, ClashDetector.clashDetection(List.of(monday, lectureGap), true).size())
        );
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Gap Overlap")
    @Test
    void gapOverlap(){
        ClassRecord a = makeClass("1", "Workshop", Campus.BEDFORD, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord b = makeClass("2", "Tutorial", Campus.TONSLEY, DayOfWeek.MONDAY, "09:30", "10:30");

        assertEquals(-1L, ClashDetector.gapMinutes(a, b));
    }

    private ClassRecord makeClass(String id, String className, Campus campus, DayOfWeek day, String start, String end){
        return new ClassRecord(
                id,
                "COMP1001",
                "Programming",
                new Availability("In person", campus, 2, 1),
                className,
                1,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                day,
                LocalTime.parse(start),
                LocalTime.parse(end),
                "Building",
                "Room"
        );
    }
}
