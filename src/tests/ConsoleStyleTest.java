package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.ConsoleStyle;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class ConsoleStyleTest {

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Constructor")
    @Test
    void construct(){
        ConsoleStyle style = new ConsoleStyle();
        assertNotNull(style);
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Title")
    @Test
    void title(){
        assertDoesNotThrow(() -> ConsoleStyle.title());
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Heading")
    @Test
    void heading(){
        assertDoesNotThrow(() -> ConsoleStyle.heading("Classes"));
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Success")
    @Test
    void success(){
        assertDoesNotThrow(() -> ConsoleStyle.success("Saved"));
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Warn")
    @Test
    void warn(){
        assertDoesNotThrow(() -> ConsoleStyle.warn("Careful"));
    }

    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("Error")
    @Test
    void error(){
        assertDoesNotThrow(() -> ConsoleStyle.error("Broken"));
    }
}
