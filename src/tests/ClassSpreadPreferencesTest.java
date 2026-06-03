package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.ClassSpreadPreferences;

import static org.junit.jupiter.api.Assertions.*;

class ClassSpreadPreferencesTest {

    @Tag("Akarsh")
    @Tag("Core")
    @DisplayName("Class Spread Preferences Initialise")
    @Test
    void classSpreadPreferencesInitialise() {
        assertArrayEquals(
                new ClassSpreadPreferences[]{
                        ClassSpreadPreferences.EVEN_SPREAD,
                        ClassSpreadPreferences.COMPACT_SPREAD
                },
                ClassSpreadPreferences.values()
        );
    }
}
