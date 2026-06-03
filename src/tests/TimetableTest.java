package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TimetableTest {

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Constructor")
    @Test
    void construct(){
        ClassRecord record = makeClass("1");
        Preference preference = new Preference();
        Timetable timetable = new Timetable("My Timetable", List.of(record), true, preference);

        assertAll(
                () -> assertNotNull(timetable),
                () -> assertEquals("My Timetable", timetable.getName()),
                () -> assertEquals(1, timetable.getClasses().size()),
                () -> assertTrue(timetable.isAllowLectureOverlap()),
                () -> assertEquals(preference, timetable.getPrefrences())
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Getters and Setters")
    @Test
    void gettersAndSetters(){
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

    private ClassRecord makeClass(String id){
        return new ClassRecord(
                id,
                "COMP1001",
                "Programming",
                new Availability("In person", Campus.BEDFORD, 2, 1),
                "Tutorial",
                1,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                DayOfWeek.MONDAY,
                LocalTime.parse("09:00"),
                LocalTime.parse("10:00"),
                "Building",
                "Room"
        );
    }
}
