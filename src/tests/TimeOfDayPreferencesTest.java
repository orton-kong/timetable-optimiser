package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.TimeOfDayPreferences;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TimeOfDayPreferencesTest {

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Time Of Day Preferences Initialise")
    @Test
    void timeOfDayPreferencesInitialise() {
        assertArrayEquals(
                new TimeOfDayPreferences[]{
                        TimeOfDayPreferences.MORNING,
                        TimeOfDayPreferences.AFTERNOON
                },
                TimeOfDayPreferences.values()
        );
    }
}
