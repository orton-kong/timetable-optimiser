package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.DayPreferences;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class DayPreferencesTest {

    @Tag("Akarsh")
    @Tag("Core")
    @DisplayName("Day Preferences Initialise")
    @Test
    void dayPreferencesInitialise() {
        assertArrayEquals(
                new DayPreferences[]{
                        DayPreferences.MONDAY,
                        DayPreferences.TUESDAY,
                        DayPreferences.WEDNESDAY,
                        DayPreferences.THURSDAY,
                        DayPreferences.FRIDAY
                },
                DayPreferences.values()
        );
    }
}
