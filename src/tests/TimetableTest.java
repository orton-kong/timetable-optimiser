package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TimetableTest {

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test Timetable Constructor")
    @Test
    void testTimetableConstructor(){
        ClassRecord record = makeClass("1");
        Preference preference = new Preference();
        Timetable timetable = new Timetable("Test Timetable", List.of(record), true, preference);

        assertAll(
                () -> assertNotNull(timetable),
                () -> assertEquals("Test Timetable", timetable.getName()),
                () -> assertEquals(1, timetable.getClasses().size()),
                () -> assertTrue(timetable.isAllowLectureOverlap()),
                () -> assertEquals(preference, timetable.getPrefrences())
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test Timetable Getters and Setters")
    @Test
    void testGettersAndSetters(){
        ClassRecord record = makeClass("1");
        Preference preference = new Preference();
        Timetable timetable = new Timetable("Old Name", null, false, null);

        timetable.setName("New Name");
        timetable.setClasses(List.of(record));
        timetable.setAllowLectureOverlap(true);
        timetable.setPrefrences(preference);

        assertAll(
                () -> assertEquals("New Name", timetable.getName()),
                () -> assertEquals(1, timetable.getClasses().size()),
                () -> assertNotSame(record, timetable.getClasses().get(0)),
                () -> assertTrue(timetable.isAllowLectureOverlap()),
                () -> assertEquals(preference, timetable.getPrefrences())
        );

        timetable.setClasses(null);
        assertEquals(0, timetable.getClasses().size());
    }

    ClassRecord makeClass(String id){
        return new ClassRecord(
                id,
                "COMP3033",
                "Cloud Computing",
                new Availability("In person", Campus.TONSLEY, 1, 1),
                "Lecture",
                1,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                DayOfWeek.MONDAY,
                LocalTime.parse("08:00"),
                LocalTime.parse("10:00"),
                "Tonsley",
                "G.42"
        );
    }
}
