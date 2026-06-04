package tests;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import timetableoptimizer.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @Order(1)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.1 Constructor")
    @Test
    void construct(){
        Main main = new Main();
        assertNotNull(main);
    }

    @Order(2)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.2 main")
    @Test
    void main(){
        setFakeInput("0\n");
        Main.main(new String[0]);
        assertTrue(output.toString().contains("Goodbye"));
    }

    @Order(3)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.3 run")
    @ParameterizedTest
    @ValueSource(strings = {"bad\n0\n0\n"})
    void run(String fakeTyping){
        setFakeInput(fakeTyping);

        Main main = new Main();
        main.run();

        assertAll(
                () -> assertTrue(output.toString().contains("Main Menu")),
                () -> assertTrue(output.toString().contains("Goodbye"))
        );
    }

    @Order(4)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.4 printMainMenu")
    @Test
    void printMainMenu(){
        Main main = new Main();
        main.printMainMenu();

        assertAll(
                () -> assertTrue(output.toString().contains("Main Menu")),
                () -> assertTrue(output.toString().contains("Generate timetable")),
                () -> assertTrue(output.toString().contains("0. Exit"))
        );
    }

    @Order(5)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.5 importClasses")
    @Test
    void importClasses() throws Exception {
        setFakeInput("sample-data/sample-classes.csv\n");

        Main main = new Main();
        main.importClasses();

        assertAll(
                () -> assertTrue(output.toString().contains("Import complete")),
                () -> assertEquals(9, main.dataStore.getClasses().size())
        );
    }

    @Order(6)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.6 searchClasses")
    @Test
    void searchClasses() throws Exception {
        String fakeTyping = "";
        fakeTyping += "COMP1001\n";
        for (int i = 0; i < 14; i++) fakeTyping += "\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        putSampleClassesInto(main);
        main.searchClasses();

        assertAll(
                () -> assertTrue(output.toString().contains("Search Classes")),
                () -> assertTrue(output.toString().contains("COMP1001"))
        );
    }

    @Order(7)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.7 editClass")
    @Test
    void editClass(){
        String fakeTyping = "";
        fakeTyping += "C1\n";
        fakeTyping += "topic name\n";
        fakeTyping += "New Topic Name\n";
        fakeTyping += "YES\n";
        fakeTyping += "bad field\n";
        fakeTyping += "0\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);
        main.editClass();

        assertAll(
                () -> assertEquals("New Topic Name", record.getTopicName()),
                () -> assertTrue(output.toString().contains("Class updated")),
                () -> assertTrue(output.toString().contains("Edit not applied")),
                () -> assertTrue(output.toString().contains("Exited editing mode"))
        );
    }

    @Order(8)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.8 deleteClass")
    @Test
    void deleteClass(){
        setFakeInput("C1\nYES\n");

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);
        main.deleteClass();

        assertAll(
                () -> assertEquals(0, main.dataStore.getClasses().size()),
                () -> assertTrue(output.toString().contains("Class deleted"))
        );
    }

    @Order(9)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.9 generatetimetable")
    @Test
    void generateTimetable() throws Exception {
        String fakeTyping = "";
        fakeTyping += "Generation Test\n";
        fakeTyping += "2\n";
        fakeTyping += "COMP1001\n";
        fakeTyping += "Bedford\n";
        fakeTyping += "no\n";
        fakeTyping += "\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        putSampleClassesInto(main);
        main.generateTimetable();

        assertTrue(output.toString().contains("Generated timetable: Generation Test"));
    }

    @Order(10)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.10 readSemester")
    @ParameterizedTest
    @CsvSource(value= {
            "bad|2|2",
            "both|none|0",
            "0|none|0",
            "1|none|1"
    }, delimiter = '|')
    void readSemester(String firstInput, String secondInput, int expected){
        String fakeTyping = firstInput + "\n";
        if (!secondInput.equals("none")) fakeTyping += secondInput + "\n";
        setFakeInput(fakeTyping);

        Main main = new Main();

        assertEquals(expected, main.readSemester());
    }

    @Order(11)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.11 readTopics")
    @Test
    void readTopics(){
        String fakeTyping = "";
        fakeTyping += "\n";
        fakeTyping += "BADTOPIC\n";
        fakeTyping += "COMP1001\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        main.dataStore.getClasses().put("C1", makeClass("C1"));

        assertEquals(List.of("COMP1001"), main.readTopics());
    }

    @Order(12)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.12 readCampuses")
    @Test
    void readCampuses(){
        String fakeTyping = "";
        fakeTyping += "bad campus\n";
        fakeTyping += "Bedford,Tonsley\n";
        setFakeInput(fakeTyping);

        Main main = new Main();

        assertEquals(List.of(Campus.BEDFORD, Campus.TONSLEY), main.readCampuses());
    }

    @Order(13)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.13 readPreferences")
    @Test
    void readPreferences(){
        String fakeTyping = "";
        fakeTyping += "99\n";
        fakeTyping += "1,5,7\n";
        setFakeInput(fakeTyping);

        Main main = new Main();

        assertEquals(List.of(LocationPreferences.BEDFORD, TimeOfDayPreferences.MORNING, DayPreferences.MONDAY), main.readPreferences());
    }

    @Order(14)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.14 viewTimetable")
    @Test
    void viewTimetable(){
        setFakeInput("View Test\n");

        Main main = new Main();
        main.dataStore.getTimetables().put("View Test", new Timetable("View Test", List.of(makeClass("C1")), false, new Preference()));
        main.viewTimetable();

        assertTrue(output.toString().contains("Timetable: View Test"));
    }

    @Order(15)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.15 editTimetable")
    @Test
    void editTimetable(){
        String fakeTyping = "";
        fakeTyping += "Edit Test\n";
        fakeTyping += "T1\n";
        fakeTyping += "T2\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        ClassRecord oldRecord = makeClass("T1", "Tutorial", 1, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord newRecord = makeClass("T2", "Tutorial", 2, DayOfWeek.TUESDAY, "09:00", "10:00");
        main.dataStore.getClasses().put(oldRecord.getID(), oldRecord);
        main.dataStore.getClasses().put(newRecord.getID(), newRecord);
        main.dataStore.getTimetables().put("Edit Test", new Timetable("Edit Test", List.of(oldRecord), false, new Preference()));
        main.editTimetable();

        assertTrue(output.toString().contains("Timetable updated"));
    }

    @Order(16)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.16 deleteTimetable")
    @Test
    void deleteTimetable(){
        setFakeInput("Delete Test\nYES\n");

        Main main = new Main();
        main.dataStore.getTimetables().put("Delete Test", new Timetable("Delete Test", List.of(makeClass("C1")), false, new Preference()));
        main.deleteTimetable();

        assertEquals(0, main.dataStore.getTimetables().size());
    }

    @Order(17)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.17 exportTimetable")
    @Test
    void exportTimetable() throws Exception {
        String fakeTyping = "";
        fakeTyping += "Export Test\n";
        fakeTyping += "out/test-exports/test.csv\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        ArrayList<ClassRecord> records = new ArrayList<>(ClassImporter.importClass("sample-data/sample-classes.csv"));
        main.dataStore.getTimetables().put("Export Test", new Timetable("Export Test", records, false, new Preference()));
        main.exportTimetable();

        assertAll(
                () -> assertTrue(output.toString().contains("Timetable exported to out/test-exports/test.csv")),
                () -> assertTrue(Files.exists(Paths.get("out/test-exports/test.csv")))
        );
    }

    @Order(18)
    @Tag("Orton")
    @Tag("Critical")
    @DisplayName("1.18 hasTimetablesOrAbort")
    @Test
    void hasTimetablesOrAbort(){
        Main main = new Main();

        assertFalse(main.hasTimetablesOrAbort("view"));

        main.dataStore.getTimetables().put("Test", new Timetable("Test", List.of(makeClass("C1")), false, new Preference()));

        assertTrue(main.hasTimetablesOrAbort("view"));
    }

    @Order(19)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.19 printEditableFields")
    @Test
    void printEditableFields(){
        Main main = new Main();
        main.printEditableFields();

        assertAll(
                () -> assertTrue(output.toString().contains("Editable Fields")),
                () -> assertTrue(output.toString().contains("topic code")),
                () -> assertTrue(output.toString().contains("0. Exit editing mode"))
        );
    }

    @Order(20)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.20 isExitEditCommand")
    @ParameterizedTest
    @ValueSource(strings = {"0", "exit", "back", "done", "q"})
    void isExitEditCommand(String command){
        Main main = new Main();
        assertTrue(main.isExitEditCommand(command));
    }

    @Order(21)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.21 resolveEditableField")
    @ParameterizedTest
    @CsvSource({
            "1, topic code",
            "topic_name, topic name",
            "room, room"
    })
    void resolveEditableField(String input, String expected){
        Main main = new Main();
        assertEquals(expected, main.resolveEditableField(input));
    }

    @Order(22)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.22 pauseBeforeMainMenu")
    @Test
    void pauseBeforeMainMenu(){
        Main main = new Main();
        assertDoesNotThrow(() -> main.pauseBeforeMainMenu());
    }

    @Order(23)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.23 readBoolean")
    @ParameterizedTest
    @CsvSource(value = {
            "maybe|yes|true",
            "maybe|no|false",
            "y|none|true",
            "n|none|false"
    }, delimiter = '|')
    void readBoolean(String firstInput, String secondInput, boolean expected){
        String fakeTyping = firstInput + "\n";
        if (!secondInput.equals("none")) fakeTyping += secondInput + "\n";
        setFakeInput(fakeTyping);

        Main main = new Main();

        assertEquals(expected, main.readBoolean("Question", false));
    }

    @Order(24)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.24 confirm")
    @ParameterizedTest
    @CsvSource({
            "YES, true",
            "no, false"
    })
    void confirm(String answer, boolean expected){
        setFakeInput(answer + "\n");

        Main main = new Main();

        assertEquals(expected, main.confirm("Are you sure?"));
    }

    @Order(25)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.25 prompt")
    @Test
    void prompt(){
        setFakeInput("hello\n");

        Main main = new Main();

        assertEquals("hello", main.prompt("Say something"));
    }

    @Order(26)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.26 promptOptional")
    @Test
    void promptOptional(){
        setFakeInput("optional answer\n");

        Main main = new Main();

        assertEquals("optional answer", main.promptOptional("Optional thing"));
    }

    @Order(27)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.27 promptWithDefault")
    @ParameterizedTest
    @CsvSource({
            "'', default answer, default answer",
            "typed answer, default answer, typed answer"
    })
    void promptWithDefault(String input, String defaultValue, String expected){
        setFakeInput(input + "\n");

        Main main = new Main();

        assertEquals(expected, main.promptWithDefault("Question", defaultValue));
    }

    @Order(28)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.28 splitCsvInput")
    @ParameterizedTest
    @CsvSource({
            "one, 1",
            "'one, two,three', 3",
            "'', 0"
    })
    void splitCsvInput(String raw, int expectedSize){
        Main main = new Main();
        assertEquals(expectedSize, main.splitCsvInput(raw).size());
    }

    @Order(29)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.29 allowedFields")
    @Test
    void allowedFields(){
        Main main = new Main();

        assertAll(
                () -> assertTrue(main.allowedFields().contains("topic code")),
                () -> assertTrue(main.allowedFields().contains("room")),
                () -> assertEquals(15, main.allowedFields().size())
        );
    }

    @Order(30)
    @Tag("Orton")
    @Tag("Core")
    @DisplayName("1.30 validateField")
    @Test
    void validateField(){
        Main main = new Main();

        assertAll(
                () -> assertDoesNotThrow(() -> main.validateField("topic_code")),
                () -> assertThrows(IllegalArgumentException.class, () -> main.validateField("not a field"))
        );
    }

    /*
    These methods arent part of the standard required test cases they are just to ensure good line cover
     */

    @Order(31)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.31 invalid resolveEditableField")
    @ParameterizedTest
    @ValueSource(strings = {"99", "not a field"})
    void invalidResolveEditableField(String input){
        Main main = new Main();
        assertThrows(IllegalArgumentException.class, () -> main.resolveEditableField(input));
    }

    @Order(32)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.32 no class edit and delete")
    @Test
    void noClassEditAndDelete(){
        Main main = new Main();

        main.editClass();
        main.deleteClass();

        assertAll(
                () -> assertTrue(output.toString().contains("No class records available to edit")),
                () -> assertTrue(output.toString().contains("No class records available to delete"))
        );
    }

    @Order(33)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.33 cancelled edit and delete")
    @Test
    void cancelledEditAndDelete(){
        String fakeTyping = "";
        fakeTyping += "C1\n";
        fakeTyping += "topic name\n";
        fakeTyping += "Cancelled Name\n";
        fakeTyping += "no\n";
        fakeTyping += "0\n";
        fakeTyping += "C1\n";
        fakeTyping += "no\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        ClassRecord record = makeClass("C1");
        main.dataStore.getClasses().put(record.getID(), record);

        main.editClass();
        main.deleteClass();

        assertAll(
                () -> assertEquals("Programming", record.getTopicName()),
                () -> assertEquals(1, main.dataStore.getClasses().size()),
                () -> assertTrue(output.toString().contains("Edit cancelled")),
                () -> assertTrue(output.toString().contains("Delete cancelled"))
        );
    }

    @Order(34)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.34 empty generate and timetable actions")
    @Test
    void emptyGenerateAndTimetableActions() throws Exception {
        Main main = new Main();

        main.generateTimetable();
        main.viewTimetable();
        main.editTimetable();
        main.deleteTimetable();
        main.exportTimetable();

        assertAll(
                () -> assertTrue(output.toString().contains("Import class data before generating a timetable")),
                () -> assertTrue(output.toString().contains("No timetables available to view")),
                () -> assertTrue(output.toString().contains("No timetables available to edit")),
                () -> assertTrue(output.toString().contains("No timetables available to delete")),
                () -> assertTrue(output.toString().contains("No timetables available to export"))
        );
    }

    @Order(35)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.35 read blank preferences")
    @Test
    void readBlankPreferences(){
        setFakeInput("\n");

        Main main = new Main();

        assertEquals(0, main.readPreferences().size());
    }

    @Order(36)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.36 read empty then valid campuses")
    @Test
    void readEmptyThenValidCampuses(){
        String fakeTyping = "";
        fakeTyping += "\n";
        fakeTyping += "City\n";
        setFakeInput(fakeTyping);

        Main main = new Main();

        assertEquals(List.of(Campus.BEDFORD, Campus.TONSLEY, Campus.CITY), main.readCampuses());
    }

    @Order(37)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.37 run catches error")
    @Test
    void runCatchesError(){
        String fakeTyping = "";
        fakeTyping += "1\n";
        fakeTyping += "missing-file.csv\n";
        fakeTyping += "0\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        main.run();

        assertTrue(output.toString().contains("Error: CSV file not found"));
    }

    @Order(38)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.38 prompt with no default")
    @Test
    void promptWithNoDefault(){
        setFakeInput("\n");

        Main main = new Main();

        assertEquals("", main.promptWithDefault("Question", null));
    }

    @Order(39)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.39 split csv ignores blanks")
    @Test
    void splitCsvIgnoresBlanks(){
        Main main = new Main();

        assertEquals(List.of("one", "two"), main.splitCsvInput("one, , two"));
    }

    @Order(40)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.40 run extra menu options")
    @Test
    void runExtraMenuOptions(){
        String fakeTyping = "";
        fakeTyping += "2\n";
        fakeTyping += "3\n";
        fakeTyping += "5\n";
        fakeTyping += "6\n";
        fakeTyping += "8\n";
        fakeTyping += "9\n";
        fakeTyping += "10\n";
        fakeTyping += "11\n";
        fakeTyping += "12\n";
        fakeTyping += "0\n";
        setFakeInput(fakeTyping);

        Main main = new Main();
        main.run();

        assertAll(
                () -> assertTrue(output.toString().contains("No class records available")),
                () -> assertTrue(output.toString().contains("No timetables available")),
                () -> assertTrue(output.toString().contains("Goodbye"))
        );
    }

    @Order(41)
    @Tag("Orton")
    @Tag("Additional")
    @DisplayName("1.41 read all preference numbers")
    @Test
    void readAllPreferenceNumbers(){
        setFakeInput("2,3,4,6,8,9,10,11,12,13\n");

        Main main = new Main();
        List<Enum<?>> preferences = main.readPreferences();

        assertAll(
                () -> assertEquals(10, preferences.size()),
                () -> assertTrue(preferences.contains(LocationPreferences.TONSLEY)),
                () -> assertTrue(preferences.contains(LocationPreferences.CITY)),
                () -> assertTrue(preferences.contains(LocationPreferences.ALL_AT_SAME_CAMPUS)),
                () -> assertTrue(preferences.contains(TimeOfDayPreferences.AFTERNOON)),
                () -> assertTrue(preferences.contains(DayPreferences.TUESDAY)),
                () -> assertTrue(preferences.contains(DayPreferences.WEDNESDAY)),
                () -> assertTrue(preferences.contains(DayPreferences.THURSDAY)),
                () -> assertTrue(preferences.contains(DayPreferences.FRIDAY)),
                () -> assertTrue(preferences.contains(ClassSpreadPreferences.EVEN_SPREAD)),
                () -> assertTrue(preferences.contains(ClassSpreadPreferences.COMPACT_SPREAD))
        );
    }

    private void setFakeInput(String text){
        System.setIn(new ByteArrayInputStream(text.getBytes()));
    }

    private void putSampleClassesInto(Main main) throws Exception {
        ArrayList<ClassRecord> records = new ArrayList<>(ClassImporter.importClass("sample-data/sample-classes.csv"));
        for (ClassRecord record : records) {
            main.dataStore.getClasses().put(record.getID(), record);
        }
    }

    private ClassRecord makeClass(String id){
        return makeClass(id, "Tutorial", 1, DayOfWeek.MONDAY, "09:00", "10:00");
    }

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
