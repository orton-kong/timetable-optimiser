package tests;

import org.junit.jupiter.api.*;

import java.security.Key;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import timetableoptimizer.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class DataStoreTest {

    //--- getClasses() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Get Classes")
    void testGetClasses() throws Exception {
        DataStore dataStore = new DataStore();
        ClassManager classManager = new ClassManager(dataStore);
        classManager.importFromCsv("test-data/sample-classes-small.csv");

        // Get the keys from getClasses()
        Object[] classRecordKeys = dataStore.getClasses().keySet().toArray();
        String classRecordKey1 = classRecordKeys[0].toString();
        String classRecordKey2 = classRecordKeys[1].toString();

        assertAll(
                () -> assertEquals("COMP1701", dataStore.getClasses().get(classRecordKey1).getTopicCode()),
                () -> assertEquals("Game Design", dataStore.getClasses().get(classRecordKey1).getTopicName()),
                () -> assertEquals("In person", dataStore.getClasses().get(classRecordKey1).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", dataStore.getClasses().get(classRecordKey1).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, dataStore.getClasses().get(classRecordKey1).getAvailability().getSemester()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey1).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", dataStore.getClasses().get(classRecordKey1).getClassName()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey1).getClassInstance()),
                () -> assertEquals("2026-07-27", dataStore.getClasses().get(classRecordKey1).getStartDate().toString()),
                () -> assertEquals("2026-09-14", dataStore.getClasses().get(classRecordKey1).getEndDate().toString()),
                () -> assertEquals("09:00", dataStore.getClasses().get(classRecordKey1).getStartTime().toString()),
                () -> assertEquals("10:00", dataStore.getClasses().get(classRecordKey1).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, dataStore.getClasses().get(classRecordKey1).getDay()),
                () -> assertEquals("Festival Tower", dataStore.getClasses().get(classRecordKey1).getBuilding()),
                () -> assertEquals("506 Computer Lab", dataStore.getClasses().get(classRecordKey1).getLocation()),

                () -> assertEquals("COMP1701", dataStore.getClasses().get(classRecordKey2).getTopicCode()),
                () -> assertEquals("Game Design", dataStore.getClasses().get(classRecordKey2).getTopicName()),
                () -> assertEquals("In person", dataStore.getClasses().get(classRecordKey2).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", dataStore.getClasses().get(classRecordKey2).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, dataStore.getClasses().get(classRecordKey2).getAvailability().getSemester()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey2).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", dataStore.getClasses().get(classRecordKey2).getClassName()),
                () -> assertEquals(1, dataStore.getClasses().get(classRecordKey2).getClassInstance()),
                () -> assertEquals("2026-10-05", dataStore.getClasses().get(classRecordKey2).getStartDate().toString()),
                () -> assertEquals("2026-10-26", dataStore.getClasses().get(classRecordKey2).getEndDate().toString()),
                () -> assertEquals("09:00", dataStore.getClasses().get(classRecordKey2).getStartTime().toString()),
                () -> assertEquals("10:00", dataStore.getClasses().get(classRecordKey2).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, dataStore.getClasses().get(classRecordKey2).getDay()),
                () -> assertEquals("Festival Tower", dataStore.getClasses().get(classRecordKey2).getBuilding()),
                () -> assertEquals("506 Computer Lab", dataStore.getClasses().get(classRecordKey2).getLocation())
        );

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test get timetables exact")
    @Test
    void TestGetTimetableExact() {
        DataStore ds = new DataStore();
        TimetableManager manager = new TimetableManager(ds);

        Timetable timetable = new Timetable("Study Plan", new ArrayList<>(), false, new Preference());

        ds.getTimetables().put("Study Plan", timetable);

        Timetable result = manager.getTimetable("Study Plan");

        assertSame(timetable, result);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test get timetables exact")
    @Test
    void TestGetTimetableExactCaseInsensitive() {
        DataStore ds = new DataStore();
        TimetableManager manager = new TimetableManager(ds);

        Timetable timetable = new Timetable("Study Plan", new ArrayList<>(), false, new Preference());

        ds.getTimetables().put("Study Plan", timetable);

        Timetable result = manager.getTimetable("study Plan");

        assertSame(timetable, result);
    }

}