package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.Campus;
import static org.junit.jupiter.api.Assertions.*;

class CampusTest {

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("Display Names")
    @Test
    void displayNames(){
        assertAll(
                () -> assertEquals("Bedford Park", Campus.BEDFORD.getDisplayName()),
                () -> assertEquals("Tonsley", Campus.TONSLEY.getDisplayName()),
                () -> assertEquals("Flinders City Campus", Campus.CITY.getDisplayName())
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("From String")
    @Test
    void fromString(){
        assertAll(
                () -> assertEquals(Campus.BEDFORD, Campus.fromString("Bedford Park")),
                () -> assertEquals(Campus.TONSLEY, Campus.fromString("Tonsley")),
                () -> assertEquals(Campus.CITY, Campus.fromString("Flinders City Campus")),
                () -> assertEquals(Campus.CITY, Campus.fromString("Festival Plaza"))
        );
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("From String Ignores Spaces And Capitals")
    @Test
    void fromStringIgnoresSpacesAndCapitals(){
        assertEquals(Campus.BEDFORD, Campus.fromString("  BEDFORD  "));
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("From String Null")
    @Test
    void fromStringNull(){
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Campus.fromString(null));
        assertEquals("Campus is missing.", exception.getMessage());
    }

    @Tag("Akarsh")
    @Tag("Additional")
    @DisplayName("From String Unknown")
    @Test
    void fromStringUnknown(){
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Campus.fromString("Not a campus"));
        assertEquals("Unknown campus: Not a campus", exception.getMessage());
    }
}
