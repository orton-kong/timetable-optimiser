package tests;
import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.nio.file.*;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TimetableManagerTest {

    @AfterEach
    void end() throws Exception {
        Files.deleteIfExists(Paths.get("out/test-exports/manager.csv"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Constructor")
    @Test
    void construct(){
        TimetableManager manager = new TimetableManager(new DataStore());
        assertNotNull(manager);
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Generate")
    @Test
    void generate(){
        DataStore dataStore = makeDataStore();
        TimetableManager manager = new TimetableManager(dataStore);

        Timetable timetable = manager.generate("Test Timetable", 2, List.of("COMP1001"), List.of(Campus.BEDFORD), false, new Preference());

        assertAll(
                () -> assertEquals("Test Timetable", timetable.getName()),
                () -> assertEquals(2, timetable.getClasses().size()),
                () -> assertTrue(dataStore.getTimetables().containsKey("Test Timetable"))
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("List All")
    @Test
    void listAll(){
        TimetableManager manager = makeManagerWithTimetable();
        TimetableManager emptyManager = new TimetableManager(new DataStore());
        assertDoesNotThrow(() -> manager.listAll());
        assertDoesNotThrow(() -> emptyManager.listAll());
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("View")
    @Test
    void view(){
        TimetableManager manager = makeManagerWithTimetable();
        TimetableManager clashManager = makeManagerWithClashingTimetable();
        assertDoesNotThrow(() -> manager.view("test timetable"));
        assertDoesNotThrow(() -> clashManager.view("clash timetable"));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Edit")
    @Test
    void edit(){
        TimetableManager manager = new TimetableManager(new DataStore());
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> manager.edit("Test", "name", "New"));
        assertEquals("Use swapClassInstance for timetable edits.", exception.getMessage());
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Find Swap Options")
    @Test
    void findSwapOptions(){
        TimetableManager manager = makeManagerWithTimetable();
        List<ClassRecord> options = manager.findSwapOptions("Test Timetable", "T1");

        assertAll(
                () -> assertEquals(1, options.size()),
                () -> assertEquals("T2", options.get(0).getID()),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.findSwapOptions("Test Timetable", "NOPE"))
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Swap Class Instance")
    @Test
    void swapClassInstance(){
        TimetableManager manager = makeManagerWithTimetable();

        List<String> warnings = manager.swapClassInstance("Test Timetable", "T1", "T2", false);
        TimetableManager warningManager = makeManagerWithWarningSwap();
        List<String> warningResult = warningManager.swapClassInstance("Warning Timetable", "T1", "T2", false);

        assertAll(
                () -> assertEquals(0, warnings.size()),
                () -> assertEquals("T1", manager.findSwapOptions("Test Timetable", "T2").get(0).getID()),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.swapClassInstance("Test Timetable", "NOPE", "T1", false)),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.swapClassInstance("Test Timetable", "T2", "NOPE", false)),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.swapClassInstance("Test Timetable", "T2", "L1", false)),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.swapClassInstance("Test Timetable", "T2", "T2", false)),
                () -> assertEquals(1, warningResult.size()),
                () -> assertEquals(1, warningManager.swapClassInstance("Warning Timetable", "T1", "T2", true).size())
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Delete")
    @Test
    void delete(){
        DataStore dataStore = makeDataStore();
        TimetableManager manager = new TimetableManager(dataStore);
        manager.generate("Test Timetable", 2, List.of("COMP1001"), List.of(Campus.BEDFORD), false, new Preference());

        manager.delete("test timetable");

        assertAll(
                () -> assertEquals(0, dataStore.getTimetables().size()),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.delete("test timetable"))
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Export")
    @Test
    void export() throws Exception {
        TimetableManager manager = makeManagerWithTimetable();

        manager.export("Test Timetable", "out/test-exports/manager.csv");

        assertTrue(Files.exists(Paths.get("out/test-exports/manager.csv")));
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Get Timetable")
    @Test
    void getTimetable(){
        TimetableManager manager = makeManagerWithTimetable();

        Timetable timetable = manager.getTimetable(" test timetable ");

        assertAll(
                () -> assertEquals("Test Timetable", timetable.getName()),
                () -> assertThrows(IllegalArgumentException.class, () -> manager.getTimetable("missing"))
        );
    }

    @Tag("Orton")
    @Tag("Core")
    @DisplayName("Find Actual Timetable Name")
    @Test
    void findActualTimetableName(){
        TimetableManager manager = makeManagerWithTimetable();

        assertAll(
                () -> assertEquals("Test Timetable", manager.findActualTimetableName(" test timetable ")),
                () -> assertNull(manager.findActualTimetableName("missing")),
                () -> assertNull(manager.findActualTimetableName(null))
        );
    }

    private TimetableManager makeManagerWithTimetable(){
        DataStore dataStore = makeDataStore();
        TimetableManager manager = new TimetableManager(dataStore);
        manager.generate("Test Timetable", 2, List.of("COMP1001"), List.of(Campus.BEDFORD), false, new Preference());
        return manager;
    }

    private TimetableManager makeManagerWithClashingTimetable(){
        DataStore dataStore = new DataStore();
        List<ClassRecord> classes = List.of(
                makeClass("A", "Lecture", 1, DayOfWeek.MONDAY, "09:00", "10:00"),
                makeClass("B", "Tutorial", 1, DayOfWeek.MONDAY, "09:30", "10:30")
        );
        dataStore.getTimetables().put("Clash Timetable", new Timetable("Clash Timetable", classes, false, new Preference()));
        return new TimetableManager(dataStore);
    }

    private TimetableManager makeManagerWithWarningSwap(){
        DataStore dataStore = new DataStore();
        ClassRecord workshop = makeClass("W1", "Workshop", 1, DayOfWeek.MONDAY, "09:00", "10:00");
        ClassRecord oldTutorial = makeClass("T1", "Tutorial", 1, DayOfWeek.TUESDAY, "09:00", "10:00");
        ClassRecord newTutorial = makeClass("T2", "Tutorial", 2, DayOfWeek.MONDAY, "09:30", "10:30");
        add(dataStore, workshop);
        add(dataStore, oldTutorial);
        add(dataStore, newTutorial);
        dataStore.getTimetables().put("Warning Timetable", new Timetable("Warning Timetable", List.of(workshop, oldTutorial), false, new Preference()));
        return new TimetableManager(dataStore);
    }

    private DataStore makeDataStore(){
        DataStore dataStore = new DataStore();
        add(dataStore, makeClass("L1", "Lecture", 1, DayOfWeek.MONDAY, "09:00", "10:00"));
        add(dataStore, makeClass("T1", "Tutorial", 1, DayOfWeek.TUESDAY, "09:00", "10:00"));
        add(dataStore, makeClass("T2", "Tutorial", 2, DayOfWeek.WEDNESDAY, "09:00", "10:00"));
        return dataStore;
    }

    private void add(DataStore dataStore, ClassRecord record){
        dataStore.getClasses().put(record.getID(), record);
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
