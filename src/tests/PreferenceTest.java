package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class PreferenceTest {

    enum FakePreference {
        INVALID
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Default Constructor")
    @Test
    void defaultConstructor() {
        Preference preference = new Preference();

        assertAll(
                () -> assertTrue(preference.getCampus().isEmpty()),
                () -> assertTrue(preference.getDayOfWeek().isEmpty()),
                () -> assertTrue(preference.getOrderedPreferences().isEmpty()),
                () -> assertFalse(preference.isAllAtSameCampus()),
                () -> assertNull(preference.getTimeOfDay()),
                () -> assertNull(preference.getClassSpread()),
                () -> assertEquals("No preferences selected", preference.display())
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Constructor With Ordered Preferences")
    @Test
    void constructorWithOrderedPreferences() {
        Preference preference = new Preference(List.of(
                LocationPreferences.BEDFORD,
                TimeOfDayPreferences.MORNING,
                DayPreferences.MONDAY,
                ClassSpreadPreferences.COMPACT_SPREAD
        ));

        assertAll(
                () -> assertEquals(List.of(Campus.BEDFORD), preference.getCampus()),
                () -> assertEquals(TimeOfDayPreferences.MORNING, preference.getTimeOfDay()),
                () -> assertEquals(List.of(DayOfWeek.MONDAY), preference.getDayOfWeek()),
                () -> assertEquals(ClassSpreadPreferences.COMPACT_SPREAD, preference.getClassSpread())
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Set Ordered Preferences Removes Duplicates")
    @Test
    void setOrderedPreferencesRemovesDuplicates() {
        Preference preference = new Preference();

        preference.setOrderedPreferences(List.of(
                LocationPreferences.BEDFORD,
                LocationPreferences.BEDFORD,
                DayPreferences.FRIDAY,
                DayPreferences.FRIDAY
        ));

        assertAll(
                () -> assertEquals(2, preference.getOrderedPreferences().size()),
                () -> assertEquals(List.of(Campus.BEDFORD), preference.getCampus()),
                () -> assertEquals(List.of(DayOfWeek.FRIDAY), preference.getDayOfWeek())
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Set Ordered Preferences Ignores Null")
    @Test
    void setOrderedPreferencesIgnoresNull() {
        Preference preference = new Preference();

        preference.setOrderedPreferences(List.of(
                LocationPreferences.CITY,
                DayPreferences.TUESDAY
        ));

        assertAll(
                () -> assertEquals(List.of(Campus.CITY), preference.getCampus()),
                () -> assertEquals(List.of(DayOfWeek.TUESDAY), preference.getDayOfWeek())
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("All At Same Campus Preference")
    @Test
    void allAtSameCampusPreference() {
        Preference preference = new Preference(List.of(
                LocationPreferences.ALL_AT_SAME_CAMPUS
        ));

        assertTrue(preference.isAllAtSameCampus());
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Display Ordered Preferences")
    @Test
    void displayOrderedPreferences() {
        Preference preference = new Preference(List.of(
                LocationPreferences.TONSLEY,
                TimeOfDayPreferences.AFTERNOON,
                ClassSpreadPreferences.EVEN_SPREAD
        ));

        assertEquals("TONSLEY > AFTERNOON > EVEN_SPREAD", preference.display());
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Ordered Preferences Cannot Be Modified")
    @Test
    void orderedPreferencesCannotBeModified() {
        Preference preference = new Preference(List.of(LocationPreferences.BEDFORD));

        assertThrows(UnsupportedOperationException.class,
                () -> preference.getOrderedPreferences().add(LocationPreferences.CITY));
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Unsupported Preference Type")
    @Test
    void unsupportedPreferenceType() {
        Preference preference = new Preference();

        assertThrows(IllegalArgumentException.class,
                () -> preference.setOrderedPreferences(List.of(FakePreference.INVALID)));
    }
}
