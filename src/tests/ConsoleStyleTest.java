package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.ConsoleStyle;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleStyleTest {

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Constructor")
    @Test
    void construct(){
        ConsoleStyle style = new ConsoleStyle();
        assertNotNull(style);
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Title")
    @Test
    void title(){
        assertDoesNotThrow(() -> ConsoleStyle.title());
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Heading")
    @Test
    void heading(){
        assertDoesNotThrow(() -> ConsoleStyle.heading("Classes"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Success")
    @Test
    void success(){
        assertDoesNotThrow(() -> ConsoleStyle.success("Saved"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Warn")
    @Test
    void warn(){
        assertDoesNotThrow(() -> ConsoleStyle.warn("Careful"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Error")
    @Test
    void error(){
        assertDoesNotThrow(() -> ConsoleStyle.error("Broken"));
    }
}
