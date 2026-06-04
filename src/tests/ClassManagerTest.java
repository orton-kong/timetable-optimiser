package tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import timetableoptimizer.DataStore;
import timetableoptimizer.ClassManager;
import timetableoptimizer.ClassRecord;
import timetableoptimizer.ImportResult;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class ClassManagerTest {
    private final InputStream originalInputStream = System.in;
    private final PrintStream originalOutputStream = System.out;
    private final ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        // Reassigns the "standard" output stream to "captureOutputStream".
        System.setOut(new PrintStream(captureOutputStream));
    }

    @AfterEach
    public void tearDown() {
        // Reassigns the "standard" input stream back to what was originally stored in "originalInputStream".
        System.setIn(originalInputStream);
        // Reassigns the "standard" output stream back to what was originally stored in "originalOutputStream".
        System.setOut(originalOutputStream);
    }

    //--- importFromCsv() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Valid CSV")
    void testImportCSV() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        assertEquals(new ImportResult(1, 0), classManager.importFromCsv("test-data/one-class.csv"));

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        assertAll(
                () -> assertEquals("COMP1701", dataStore.getClasses().get(classRecordKey).getTopicCode()),
                () -> assertEquals("Game Design", dataStore.getClasses().get(classRecordKey).getTopicName()),
                () -> assertEquals("In person", dataStore.getClasses().get(classRecordKey).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", dataStore.getClasses().get(classRecordKey).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, dataStore.getClasses().get(classRecordKey).getAvailability().getSemester()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", dataStore.getClasses().get(classRecordKey).getClassName()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey).getClassInstance()),
                () -> assertEquals("2026-07-27", dataStore.getClasses().get(classRecordKey).getStartDate().toString()),
                () -> assertEquals("2026-09-14", dataStore.getClasses().get(classRecordKey).getEndDate().toString()),
                () -> assertEquals("09:00", dataStore.getClasses().get(classRecordKey).getStartTime().toString()),
                () -> assertEquals("10:00", dataStore.getClasses().get(classRecordKey).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, dataStore.getClasses().get(classRecordKey).getDay()),
                () -> assertEquals("Festival Tower", dataStore.getClasses().get(classRecordKey).getBuilding()),
                () -> assertEquals("506 Computer Lab", dataStore.getClasses().get(classRecordKey).getLocation())
        );
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Valid CSV (with multiple classes)")
    // This only tests if the inserted value is incremented correctly
    void testImportCSV2() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        assertEquals(new ImportResult(9, 0), classManager.importFromCsv("test-data/sample-classes.csv"));
    }

    //--- listAll() and printBrowse()---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("List All Classes")
    void testListAll() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        classManager.importFromCsv("test-data/sample-classes-small-2.csv");

        classManager.listAll();
        assertEquals("\u001B[1mCOMP1701\u001B[0m [Game Design] In person - Flinders City Campus - S2 - 1 | Workshop-1 | Instance 1 | 2 date record(s)" + System.lineSeparator() +
                              "\u001B[1mCOMP1801\u001B[0m [Something Else] In person - Flinders City Campus - S2 - 1 | Workshop-1 | Instance 1 | 1 date record(s)" + System.lineSeparator(),
                              captureOutputStream.toString()
        );
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("List All Classes (no classes)")
    void testListAllNoClasses(){
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        classManager.listAll();
        assertEquals("\u001B[33mNo class records available.\u001B[0m" + System.lineSeparator(),
                              captureOutputStream.toString()
        );
    }

    //--- viewAll(), printView() and groupByCombinedClass() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("View All Classes")
    void testViewAll() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        classManager.importFromCsv("test-data/sample-classes-small-2.csv");
        classManager.viewAll();

        String output = captureOutputStream.toString();
        output = output.replaceAll("\\[(([0-9]|[a-z]){8})]", "[RANDOM ID]");

        assertEquals("\n" + "\u001B[34m\u001B[1m\u001B[4mCOMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1\u001B[0m" + "\r\n" +
                        "  [RANDOM ID] COMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 27 Jul 2026 - 14 Sep 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab" + "\r\n" +
                        "  [RANDOM ID] COMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 05 Oct 2026 - 26 Oct 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab" + "\r\n" + "\n" +
                        "\u001B[34m\u001B[1m\u001B[4mCOMP1801 Something Else | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1\u001B[0m" + "\r\n" +
                        "  [RANDOM ID] COMP1801 Something Else | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 05 Oct 2026 - 26 Oct 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab" + "\r\n",
                output
        );
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("View All Classes (no matching class records)")
    void testViewAllNoClasses() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);

        classManager.viewAll();
        assertEquals("\u001B[33mNo matching class records.\u001B[0m" + System.lineSeparator(),
                captureOutputStream.toString()
        );
    }

    //--- normaliseField() ---//
    @ParameterizedTest
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Normalise Field")
    @CsvSource({
                "a b,ab",
                "z_b,zb",
                "a-b,ab",
                " a - b,ab",
                "a _-b,ab",
                "     a------b_____,ab",
                "  ZB--- - _AabaAB-- _baZb  ba -- _,zbaabaabbazbba"
                })
    void testNormaliseField(String input, String output){
        assertEquals(output, ClassManager.normaliseField(input));
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Normalise Field (null)")
    void testNormaliseFieldNull(){
        assertEquals("", ClassManager.normaliseField(null));
    }

    //--- printSearch(), search(), matchesAll(), dateSearchValue() and getSearchableFieldValue() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Search (no classes)")
    void testSearchNoClasses() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
//        classManager.importFromCsv("test-data/sample-classes.csv");

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("topic code", "COMP1701");
        criteria.put("topic name", "Game Design");
        criteria.put("attendance mode", "In person");
        criteria.put("campus ", "Flinders City Campus");
        criteria.put("semester", "2");
        criteria.put("availability number", "1");
        criteria.put("class", "Workshop-1");
        criteria.put("class instance", "1");
        criteria.put("date of first class", "2026-07-27");
        criteria.put("date of last class", "2026-09-14");
        criteria.put("day", "Monday");
        criteria.put("start time", "09:00");
        criteria.put("end time", "10:00");
        criteria.put("building", "Festival Tower");
        criteria.put("room", "506 Computer Lab");

        classManager.printSearch(criteria);

        assertEquals("\u001B[33mNo matching class records.\u001B[0m" + System.lineSeparator(),
                captureOutputStream.toString()
        );

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Search (no MATCHING classes)")
    void testSearchNoMatchingClasses() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes.csv");

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("topic code", "COMP1801"); // This is the search criteria that causes 0 matches
        criteria.put("topic name", "Game Design");
        criteria.put("attendance mode", "In person");
        criteria.put("campus ", "Flinders City Campus");
        criteria.put("semester", "2");
        criteria.put("availability number", "1");
        criteria.put("class", "Workshop-1");
        criteria.put("class instance", "1");
        criteria.put("date of first class", "2026-07-27");
        criteria.put("date of last class", "2026-09-14");
        criteria.put("day", "Monday");
        criteria.put("start time", "09:00");
        criteria.put("end time", "10:00");
        criteria.put("building", "Festival Tower");
        criteria.put("room", "506 Computer Lab");

        classManager.printSearch(criteria);

        assertEquals("\u001B[33mNo matching class records.\u001B[0m" + System.lineSeparator(),
                captureOutputStream.toString()
        );

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Search (invalid search field)")
    void testSearchInvalidField() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes.csv");

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("topic code", "COMP1701");
        criteria.put("topic name", "Game Design");
        criteria.put("attendance mode", "In person");
        criteria.put("campus ", "Flinders City Campus");
        criteria.put("semester", "2");
        criteria.put("availability number", "1");
        criteria.put("class", "Workshop-1");
        criteria.put("class instance", "1");
        criteria.put("date of first class", "2026-07-27");
        criteria.put("date of last class", "2026-09-14");
        criteria.put("day", "Monday");
        criteria.put("start time", "09:00");
        criteria.put("end time", "10:00");
        criteria.put("building", "Festival Tower");
        criteria.put("INVALID SEARCH FIELD", "506 Computer Lab"); // This is the invalid search field

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.printSearch(criteria)
        );
        assertEquals("Unsupported search/edit field: invalidsearchfield", exception.getMessage());

        // This determines that instead of an error, the output is blank
//        assertEquals("", captureOutputStream.toString());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Successful Search for all Fields")
    void testSearchSuccessful1() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes.csv");

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("topic code", "COMP1701");
        criteria.put("topic name", "Game Design");
        criteria.put("attendance mode", "In person");
        criteria.put("campus ", "Flinders City Campus");
        criteria.put("semester", "2");
        criteria.put("availability number", "1");
        criteria.put("class", "Workshop-1");
        criteria.put("class instance", "1");
        criteria.put("date of first class", "2026-07-27");
        criteria.put("date of last class", "2026-09-14");
        criteria.put("day", "Monday");
        criteria.put("start time", "09:00");
        criteria.put("end time", "10:00");
        criteria.put("building", "Festival Tower");
        criteria.put("room", "506 Computer Lab");

        classManager.printSearch(criteria);

        String output = captureOutputStream.toString();
        output = output.replaceAll("\\[(([0-9]|[a-z]){8})]", "[RANDOM ID]");

        assertEquals("\n" +
                        "\u001B[34m\u001B[1m\u001B[4mCOMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1\u001B[0m\r\n" +
                        "  [RANDOM ID] COMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 27 Jul 2026 - 14 Sep 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab\r\n",
                output
        );

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Successful Search for all Fields (multiple matches)")
    void testSearchSuccessful2() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes.csv");

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("TOPIC CODe", "COMP1701"); // Tests capital letters in field
        criteria.put("topic name", "Game Design");
        criteria.put("attendance mode", "In person");
        criteria.put("campus ", "Flinders City Campus");
        criteria.put("semester", "2");
        criteria.put("availability num", "1"); // Tests "availability num" field (instead of "availability number")
        criteria.put("class name", "Workshop-1"); // Tests "class name" field (instead of "class")
        criteria.put("class instance", "1");
        criteria.put("start date", ""); // Tests empty value (this is the field that causes multiple matches) and tests "start date" field (instead of "date of first class")
        criteria.put("end date", ""); // Tests empty value (this is the field that causes multiple matches) and tests "end date" field (instead of "date of last class")
        criteria.put("day", "Monday");
        criteria.put("start time", "09:00");
        criteria.put("end time", "10:00");
        criteria.put("building", "Festival Tower");
        criteria.put("location", "506 Computer Lab"); // Tests "location" field (instead of "room")

        classManager.printSearch(criteria);

        String output = captureOutputStream.toString();
        output = output.replaceAll("\\[(([0-9]|[a-z]){8})]", "[RANDOM ID]");

        assertEquals("\n" +
                        "\u001B[34m\u001B[1m\u001B[4mCOMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1\u001B[0m\r\n" +
                        "  [RANDOM ID] COMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 27 Jul 2026 - 14 Sep 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab\r\n" +
                        "  [RANDOM ID] COMP1701 Game Design | In person - Flinders City Campus - S2 - 1 | Workshop-1 #1 | 05 Oct 2026 - 26 Oct 2026 | Monday | 09:00 - 10:00 | Festival Tower, 506 Computer Lab\r\n",
                output
        );

    }

    //--- edit(), validateRecord(), requireNonBlank() and restore()---//
    //NOTE: Can't test for some of the IllegalArgumentExceptions because it is impossible to make these blank
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (no class with ID)")
    void testEditNoClassWithID() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes.csv");


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit("fakeid00", "topic code", "COMP1801")
        );
        assertEquals("No class exists with ID: fakeid00", exception.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid field)")
    void testEditInvalidField() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "INVALID FIELD", "COMP1801")
        );
        assertEquals("Unsupported field: INVALID FIELD", exception.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid record - blank values)") // Explicitly tests validateRecord(), requireNonBlank() and restore()
    void testEditInvalidRecord1() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "topic code", "")
        );
        assertEquals("topic code cannot be blank.", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "topic name", "")
        );
        assertEquals("topic name cannot be blank.", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "class", "")
        );
        assertEquals("class cannot be blank.", exception3.getMessage());

        IllegalArgumentException exception4 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "building", "")
        );
        assertEquals("building cannot be blank.", exception4.getMessage());

        IllegalArgumentException exception5 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "room", "")
        );
        assertEquals("room cannot be blank.", exception5.getMessage());

        // This indicates that all fields are checked to be nonBlank (within setField()) which may be unnecessary
        IllegalArgumentException exception6 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "semester", "")
        );
        assertEquals("semester cannot be blank.", exception6.getMessage());

        // Test restore()
        assertAll(
                () -> assertEquals("COMP1701", dataStore.getClasses().get(classRecordKey).getTopicCode()),
                () -> assertEquals("Game Design", dataStore.getClasses().get(classRecordKey).getTopicName()),
                () -> assertEquals("In person", dataStore.getClasses().get(classRecordKey).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", dataStore.getClasses().get(classRecordKey).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, dataStore.getClasses().get(classRecordKey).getAvailability().getSemester()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", dataStore.getClasses().get(classRecordKey).getClassName()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey).getClassInstance()),
                () -> assertEquals("2026-07-27", dataStore.getClasses().get(classRecordKey).getStartDate().toString()),
                () -> assertEquals("2026-09-14", dataStore.getClasses().get(classRecordKey).getEndDate().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, dataStore.getClasses().get(classRecordKey).getDay()),
                () -> assertEquals("09:00", dataStore.getClasses().get(classRecordKey).getStartTime().toString()),
                () -> assertEquals("10:00", dataStore.getClasses().get(classRecordKey).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, dataStore.getClasses().get(classRecordKey).getDay()),
                () -> assertEquals("Festival Tower", dataStore.getClasses().get(classRecordKey).getBuilding()),
                () -> assertEquals("506 Computer Lab", dataStore.getClasses().get(classRecordKey).getLocation())
        );

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid record - availability)")
    void testEditInvalidRecord2() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "semester", "BLAH")
        );
        // This error could be improved to be more specific
        assertEquals("For input string: \"BLAH\"", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "availability num", "BLAH")
        );
        // This error could be improved to be more specific
        assertEquals("For input string: \"BLAH\"", exception2.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid record - class instance)")
    void testEditInvalidRecord3() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "class instance", "BLAH")
        );
        // This error could be improved to be more specific
        assertEquals("For input string: \"BLAH\"", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "class instance", "0")
        );
        assertEquals("Class instance must be greater than 0.", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "class instance", "-1")
        );
        assertEquals("Class instance must be greater than 0.", exception3.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid record - date)")
    void testEditInvalidRecord4() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "start date", "BLAH")
        );
        assertEquals("Date must use yyyy-mm-dd, d MMM, or d MMM yyyy format: BLAH", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "start date", "2027-01-01")
        );
        assertEquals("Date of last class cannot be before date of first class.", exception2.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (invalid record - time)")
    void testEditInvalidRecord5() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "start time", "BLAH")
        );
        assertEquals("Time must use HH:mm format: BLAH", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.edit(classRecordKey, "start time", "12:00")
        );
        assertEquals("End time must be after start time.", exception2.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Edit (valid)")
    void testEditValid() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        classManager.edit(classRecordKey, "TOPIC CODe", "COMP1801");
        classManager.edit(classRecordKey, "topic name", "Advanced Game Design");
        classManager.edit(classRecordKey, "attendance mode", "Online");
        classManager.edit(classRecordKey, "campus ", "Tonsley");
        classManager.edit(classRecordKey, "semester", "1");
        classManager.edit(classRecordKey, "availability num", "2");
        classManager.edit(classRecordKey, "class name", "Lecture-2");
        classManager.edit(classRecordKey, "class instance", "3");
        classManager.edit(classRecordKey, "start date", "2026-08-01");
        classManager.edit(classRecordKey, "end date", "2026-10-01");
        classManager.edit(classRecordKey, "day", "Friday");
        classManager.edit(classRecordKey, "end time", "15:00"); // It was found that end time needs to be placed before start time in this instance
        classManager.edit(classRecordKey, "start time", "13:00");
        classManager.edit(classRecordKey, "building", "Engineering North");
        classManager.edit(classRecordKey, "location", "102 Lecture Theatre");

        assertAll(
                () -> assertEquals("COMP1801", dataStore.getClasses().get(classRecordKey).getTopicCode()),
                () -> assertEquals("Advanced Game Design", dataStore.getClasses().get(classRecordKey).getTopicName()),
                () -> assertEquals("Online", dataStore.getClasses().get(classRecordKey).getAvailability().getAttendanceMode()),
                () -> assertEquals("Tonsley", dataStore.getClasses().get(classRecordKey).getAvailability().getCampus().getDisplayName()),() -> assertEquals(1, dataStore.getClasses().get(classRecordKey).getAvailability().getSemester()),
                () -> assertEquals(2, dataStore.getClasses().get(classRecordKey).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Lecture-2", dataStore.getClasses().get(classRecordKey).getClassName()),
                () -> assertEquals(3, dataStore.getClasses().get(classRecordKey).getClassInstance()),
                () -> assertEquals("2026-08-01", dataStore.getClasses().get(classRecordKey).getStartDate().toString()),
                () -> assertEquals("2026-10-01", dataStore.getClasses().get(classRecordKey).getEndDate().toString()),
                () -> assertEquals(DayOfWeek.FRIDAY, dataStore.getClasses().get(classRecordKey).getDay()),
                () -> assertEquals("13:00", dataStore.getClasses().get(classRecordKey).getStartTime().toString()),
                () -> assertEquals("15:00", dataStore.getClasses().get(classRecordKey).getEndTime().toString()),
                () -> assertEquals("Engineering North", dataStore.getClasses().get(classRecordKey).getBuilding()),
                () -> assertEquals("102 Lecture Theatre", dataStore.getClasses().get(classRecordKey).getLocation())
        );
    }

    //--- delete() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Delete (no class with ID)")
    void testDeleteNoClass() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> classManager.delete("fakeid00")
        );
        assertEquals("No class exists with ID: fakeid00", exception.getMessage());

    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Delete (valid)")
    void testDelete() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        classManager.delete(classRecordKey);

        classManager.listAll();

        assertEquals("\u001B[33mNo class records available.\u001B[0m" + System.lineSeparator(), captureOutputStream.toString());

    }

    //--- findById() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Find by ID")
    void testFindById() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/one-class.csv");

        // Get the key of the first class record in datastore
        String classRecordKey = dataStore.getClasses().keySet().iterator().next();

        assertEquals("COMP1701", classManager.findById(classRecordKey).getTopicCode());
        assertNull(classManager.findById("fakeid00"));
    }
}