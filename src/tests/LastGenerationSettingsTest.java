package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LastGenerationSettingsTest {

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Constructor")
    @Test
    void construct(){
        LastGenerationSettings settings = new LastGenerationSettings();

        assertAll(
                () -> assertNotNull(settings),
                () -> assertEquals("", settings.getName()),
                () -> assertEquals(0, settings.getSemester()),
                () -> assertEquals(0, settings.getTopics().size()),
                () -> assertEquals(3, settings.getCampuses().size()),
                () -> assertFalse(settings.isLectureOverlap()),
                () -> assertEquals(0, settings.getPreferences().size())
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Getters and Setters")
    @Test
    void gettersAndSetters(){
        LastGenerationSettings settings = new LastGenerationSettings();
        settings.setName("My Timetable");
        settings.setSemester(2);
        settings.setTopics(List.of("COMP1001", "COMP1701"));
        settings.setCampuses(List.of(Campus.BEDFORD, Campus.TONSLEY));
        settings.setLectureOverlap(true);
        settings.setPreferences(List.of(DayOfWeek.MONDAY, Campus.CITY));

        assertAll(
                () -> assertEquals("My Timetable", settings.getName()),
                () -> assertEquals(2, settings.getSemester()),
                () -> assertEquals(List.of("COMP1001", "COMP1701"), settings.getTopics()),
                () -> assertEquals(List.of(Campus.BEDFORD, Campus.TONSLEY), settings.getCampuses()),
                () -> assertTrue(settings.isLectureOverlap()),
                () -> assertEquals(List.of(DayOfWeek.MONDAY, Campus.CITY), settings.getPreferences())
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Setters With Null")
    @Test
    void settersWithNull(){
        LastGenerationSettings settings = new LastGenerationSettings();
        settings.setName(null);
        settings.setTopics(null);
        settings.setCampuses(null);
        settings.setPreferences(null);

        assertAll(
                () -> assertEquals("", settings.getName()),
                () -> assertEquals(0, settings.getTopics().size()),
                () -> assertEquals(0, settings.getCampuses().size()),
                () -> assertEquals(0, settings.getPreferences().size())
        );
    }
}
