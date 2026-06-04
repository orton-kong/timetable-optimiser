package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
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
        Files.deleteIfExists(Paths.get("out/test-exports/test.csv"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Constructor")
    @Test
    void construct(){
        Main main = new Main();
        assertNotNull(main);
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Main")
    @Test
    void main(){
        String fakeTyping = "0\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main.main(new String[0]);

        String text = output.toString();
        assertTrue(text.contains("Goodbye"));
    }

    @Tag("Orton")
    @Tag("Critical")
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
    @Tag("Critical")
    @DisplayName("Print Main Menu")
    @Test
    void printMainMenu(){
        Main main = new Main();
        main.printMainMenu();

        String text = output.toString();
        assertAll(
                () -> assertTrue(text.contains("Main Menu")),
                () -> assertTrue(text.contains("Generate timetable")),
                () -> assertTrue(text.contains("0. Exit"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Import Classes")
    @Test
    void importClasses() throws Exception {
        String fakeTyping = "sample-data/sample-classes.csv\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        main.importClasses();

        String text = output.toString();
        assertAll(
                () -> assertTrue(text.contains("Import complete")),
                () -> assertEquals(9, main.dataStore.getClasses().size())
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Search Classes")
    @Test
    void searchClasses() throws Exception {
        String fakeTyping = "";
        fakeTyping += "COMP1001\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        fakeTyping += "\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ArrayList<ClassRecord> records = new ArrayList<>(ClassImporter.importClass("sample-data/sample-classes.csv"));
        for (ClassRecord record : records) {
            main.dataStore.getClasses().put(record.getID(), record);
        }

        main.searchClasses();

        String text = output.toString();
        assertAll(
                () -> assertTrue(text.contains("Search Classes")),
                () -> assertTrue(text.contains("COMP1001"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Edit Class")
    @Test
    void editClass(){
        String fakeTyping = "";
        fakeTyping += "C1\n";
        fakeTyping += "topic name\n";
        fakeTyping += "New Topic Name\n";
        fakeTyping += "YES\n";
        fakeTyping += "0\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);

        main.editClass();

        String text = output.toString();
        assertAll(
                () -> assertEquals("New Topic Name", record.getTopicName()),
                () -> assertTrue(text.contains("Class updated")),
                () -> assertTrue(text.contains("Exited editing mode"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Delete Class")
    @Test
    void deleteClass(){
        String fakeTyping = "";
        fakeTyping += "C1\n";
        fakeTyping += "YES\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);

        main.deleteClass();

        String text = output.toString();
        assertAll(
                () -> assertEquals(0, main.dataStore.getClasses().size()),
                () -> assertTrue(text.contains("Class deleted"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
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
    @Tag("Critical")
    @DisplayName("Read Semester")
    @Test
    void readSemester(){
        String fakeTyping = "";
        fakeTyping += "bad\n";
        fakeTyping += "2\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals(2, main.readSemester());
        assertTrue(output.toString().contains("Semester must be 1, 2, or both"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Read Topics")
    @Test
    void readTopics(){
        String fakeTyping = "";
        fakeTyping += "\n";
        fakeTyping += "BADTOPIC\n";
        fakeTyping += "COMP1001\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);

        assertEquals(List.of("COMP1001"), main.readTopics());
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Read Campuses")
    @Test
    void readCampuses(){
        String fakeTyping = "";
        fakeTyping += "bad campus\n";
        fakeTyping += "Bedford,Tonsley\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals(List.of(Campus.BEDFORD, Campus.TONSLEY), main.readCampuses());
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Read Preferences")
    @Test
    void readPreferences(){
        String fakeTyping = "";
        fakeTyping += "99\n";
        fakeTyping += "1,5,7\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals(List.of(LocationPreferences.BEDFORD, TimeOfDayPreferences.MORNING, DayPreferences.MONDAY), main.readPreferences());
    }

    @Tag("Orton")
    @Tag("Critical")
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
    @Tag("Critical")
    @DisplayName("View Timetable")
    @Test
    void viewTimetable(){
        String fakeTyping = "View Test\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        Timetable timetable = new Timetable("View Test", List.of(makeClass("C1")), false, new Preference());
        main.dataStore.getTimetables().put("View Test", timetable);

        main.viewTimetable();

        String text = output.toString();
        assertTrue(text.contains("Timetable: View Test"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Edit Timetable")
    @Test
    void editTimetable(){
        String fakeTyping = "";
        fakeTyping += "Edit Test\n";
        fakeTyping += "T1\n";
        fakeTyping += "T2\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        ClassRecord oldRecord = makeClass("T1", "Tutorial", 1, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord newRecord = makeClass("T2", "Tutorial", 2, DayOfWeek.TUESDAY, "09:00", "10:00");
        main.dataStore.getClasses().put(oldRecord.getID(), oldRecord);
        main.dataStore.getClasses().put(newRecord.getID(), newRecord);
        main.dataStore.getTimetables().put("Edit Test", new Timetable("Edit Test", List.of(oldRecord), false, new Preference()));

        main.editTimetable();

        String text = output.toString();
        assertTrue(text.contains("Timetable updated"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Delete Timetable")
    @Test
    void deleteTimetable(){
        String fakeTyping = "";
        fakeTyping += "Delete Test\n";
        fakeTyping += "YES\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();
        Timetable timetable = new Timetable("Delete Test", List.of(makeClass("C1")), false, new Preference());
        main.dataStore.getTimetables().put("Delete Test", timetable);

        main.deleteTimetable();

        assertEquals(0, main.dataStore.getTimetables().size());
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Has Timetables Or Abort")
    @Test
    void hasTimetablesOrAbort(){
        Main main = new Main();

        assertFalse(main.hasTimetablesOrAbort("view"));

        Timetable timetable = new Timetable("Test", List.of(makeClass("C1")), false, new Preference());
        main.dataStore.getTimetables().put("Test", timetable);

        assertTrue(main.hasTimetablesOrAbort("view"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Print Editable Fields")
    @Test
    void printEditableFields(){
        Main main = new Main();

        main.printEditableFields();

        String text = output.toString();
        assertAll(
                () -> assertTrue(text.contains("Editable Fields")),
                () -> assertTrue(text.contains("topic code")),
                () -> assertTrue(text.contains("0. Exit editing mode"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Is Exit Edit Command")
    @Test
    void isExitEditCommand(){
        Main main = new Main();

        assertAll(
                () -> assertTrue(main.isExitEditCommand("0")),
                () -> assertTrue(main.isExitEditCommand("exit")),
                () -> assertTrue(main.isExitEditCommand("back")),
                () -> assertTrue(main.isExitEditCommand("done")),
                () -> assertTrue(main.isExitEditCommand("q")),
                () -> assertFalse(main.isExitEditCommand("topic name"))
        );
    }

    @Tag("Orton")
    @Tag("Critical")
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

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Pause Before Main Menu")
    @Test
    void pauseBeforeMainMenu(){
        Main main = new Main();
        assertDoesNotThrow(() -> main.pauseBeforeMainMenu());
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Read Boolean")
    @Test
    void readBoolean(){
        String fakeTyping = "";
        fakeTyping += "maybe\n";
        fakeTyping += "yes\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertTrue(main.readBoolean("Question", false));
        assertTrue(output.toString().contains("Please enter yes or no"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Confirm")
    @Test
    void confirm(){
        String fakeTyping = "YES\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertTrue(main.confirm("Are you sure?"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Prompt")
    @Test
    void prompt(){
        String fakeTyping = "hello\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals("hello", main.prompt("Say something"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Prompt Optional")
    @Test
    void promptOptional(){
        String fakeTyping = "optional answer\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals("optional answer", main.promptOptional("Optional thing"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Prompt With Default")
    @Test
    void promptWithDefault(){
        String fakeTyping = "\n";
        System.setIn(new ByteArrayInputStream(fakeTyping.getBytes()));

        Main main = new Main();

        assertEquals("default answer", main.promptWithDefault("Question", "default answer"));
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Split Csv Input")
    @Test
    void splitCsvInput(){
        Main main = new Main();

        assertAll(
                () -> assertEquals(List.of("one", "two", "three"), main.splitCsvInput("one, two,three")),
                () -> assertEquals(0, main.splitCsvInput("").size()),
                () -> assertEquals(0, main.splitCsvInput(null).size())
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Allowed Fields")
    @Test
    void allowedFields(){
        Main main = new Main();

        assertAll(
                () -> assertTrue(main.allowedFields().contains("topic code")),
                () -> assertTrue(main.allowedFields().contains("room")),
                () -> assertEquals(15, main.allowedFields().size())
        );
    }

    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("Validate Field")
    @Test
    void validateField(){
        Main main = new Main();

        assertAll(
                () -> assertDoesNotThrow(() -> main.validateField("topic_code")),
                () -> assertThrows(IllegalArgumentException.class, () -> main.validateField("not a field"))
        );
    }

    //make a quick mock class
    private ClassRecord makeClass(String id){
        return new ClassRecord(
                id,
                "COMP1001",
                "Programming",
                new Availability("In person", Campus.BEDFORD, 2, 1),
                "Tutorial",
                1,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                DayOfWeek.MONDAY,
                LocalTime.parse("09:00"),
                LocalTime.parse("10:00"),
                "Building",
                "Room"
        );
    }

    //make a sorta quick mock class
    private ClassRecord makeClass(String id, String className, int instance, DayOfWeek day, String start, String end){
        return new ClassRecord(
                id,
                "COMP1001",
                "Programming",
                new Availability("In person", Campus.BEDFORD, 2, 1),
                className,
                instance,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 9, 14),
                day,
                LocalTime.parse(start),
                LocalTime.parse(end),
                "Building",
                "Room"
        );
    }
}
