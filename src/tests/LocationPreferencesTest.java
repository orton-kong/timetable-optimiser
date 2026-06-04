package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.LocationPreferences;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class LocationPreferencesTest {

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Location Preferences Initialise")
    @Test
    void locationPreferencesInitialise() {
        assertArrayEquals(
                new LocationPreferences[]{
                        LocationPreferences.BEDFORD,
                        LocationPreferences.TONSLEY,
                        LocationPreferences.CITY,
                        LocationPreferences.ALL_AT_SAME_CAMPUS
                },
                LocationPreferences.values()
        );
    }
}
