package tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.time.DayOfWeek;
import timetableoptimizer.DataStore;
import timetableoptimizer.ClassManager;

import static org.junit.jupiter.api.Assertions.*;

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

}