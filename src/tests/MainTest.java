package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
These tests use fake input because Main asks the user to type answers.
Every \n means the fake user pressed Enter.

System.setIn(fakeInput) makes Java read from our fake typing. (make sure to set fake input
before making the main class as Scanner gets ini in constructor)
System.setOut(new PrintStream(output)) saves what the program prints.

At the end, the real input and output are restored so other tests do not use fake input/outputs from
other unrelated tests.
*/
class MainTest {

    InputStream oldInput;
    PrintStream oldOutput;
    ByteArrayOutputStream output;

    @BeforeEach
    void ini(){
        oldInput = System.in;
        oldOutput = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void finish() throws Exception {
        System.setIn(oldInput);
        System.setOut(oldOutput);
        Files.deleteIfExists(Paths.get("out/test-exports/main.csv"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Run")
    @Test
    void run(){
        //choose exit option straight away otherwise we get stuck in infinite loop.
        String fakeTyping = "0\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        main.run();

        String text = output.toString();
        assertTrue(text.contains("Goodbye"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Generate Timetable")
    @Test
    void generateTimetable() throws Exception {
        String fakeTyping = "";
        fakeTyping += "Generation Test\n";
        fakeTyping += "2\n";
        fakeTyping += "COMP1001\n";
        fakeTyping += "Bedford\n";
        fakeTyping += "no\n";
        fakeTyping += "\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ArrayList<ClassRecord> records = new ArrayList<>(ClassImporter.importClass("sample-data/sample-classes.csv"));
        for (ClassRecord record : records) {
            main.dataStore.getClasses().put(record.getID(), record);
        }

        main.generateTimetable();

        String text = output.toString();
        assertTrue(text.contains("Generated timetable: Generation Test"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Export Timetable")
    @Test
    void exportTimetable() throws Exception {
        String fakeTyping = "";
        fakeTyping += "Export Test\n";
        fakeTyping += "out/test-exports/test.csv\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ArrayList<ClassRecord> records = new ArrayList<>(ClassImporter.importClass("sample-data/sample-classes.csv"));
        Timetable timetable = new Timetable("Export Test", records, false, new Preference());
        main.dataStore.getTimetables().put("Export Test", timetable);

        main.exportTimetable();

        String text = output.toString();
        assertAll(
                () -> assertTrue(text.contains("Timetable exported to out/test-exports/test.csv")),
                () -> assertTrue(Files.exists(Paths.get("out/test-exports/test.csv")))
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Resolve Editable Field")
    @Test
    void resolveEditableField(){
        Main main = new Main();

        assertAll(
                () -> assertEquals("topic code", main.resolveEditableField("1")),
                () -> assertEquals("topic name", main.resolveEditableField("topic_name")),
                () -> assertThrows(IllegalArgumentException.class, () -> main.resolveEditableField("99")),
                () -> assertThrows(IllegalArgumentException.class, () -> main.resolveEditableField("not a field"))
        );
    }
}
