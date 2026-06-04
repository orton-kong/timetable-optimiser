package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class AvailabilityTest {
    private static final DataStore dataStore = new DataStore();
    public Availability TheTest;
    private static ClassManager classManager = new ClassManager(dataStore);
    List<String> DisplayListToAssertAgainst = List.of("In person - Flinders City Campus - S2 - 1", "In person - Tonsley - S2 - 1","In person - Bedford Park - S2 - 1");
    List<String> KeyListToAssertAgainst = List.of("in person|CITY|2|1", "in person|TONSLEY|2|1","in person|BEDFORD|2|1");

    @BeforeAll
    public static void MakeTheTest() throws Exception
    {
        assertEquals(0,dataStore.getClasses().size(),"It should be empty");
        String Test = "sample-data/sample-classes.csv";
        ImportResult result = classManager.importFromCsv(Test);

        assertEquals(9,dataStore.getClasses().size(),"Size not right");
    }
    @Test
    @DisplayName("Test constructor")
    @Tag("Kat")
    @Tag("additional")
    public void JustGiveMeTheLineCoverige()
    {
        Availability TheTestValue = dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[0]).getAvailability();

        Availability TheOtherTestValue = new Availability(TheTestValue);
        assertEquals(TheTestValue,TheOtherTestValue,"If this ever fails something has seriously fucked up");
    }
    @Test
    @DisplayName("Test display() & key()")
    @Tag("Kat")
    @Tag("additional")
    public void TestDisplayAndKey()
    {
       //System.out.println("Size is = " + dataStore.getClasses().size());
        int MiniCounter = 0; int TinyCounter = 0;
        for (int i = 0; i < dataStore.getClasses().keySet().size(); i++)
        {
            String Value = dataStore.getClasses().keySet().toArray()[i].toString();
            Availability TheTestValue = dataStore.getClasses().get(Value).getAvailability();
            assertEquals(DisplayListToAssertAgainst.get(TinyCounter),TheTestValue.display(),"Output of display() is wrong");
            assertEquals(KeyListToAssertAgainst.get(TinyCounter),TheTestValue.key(),"Output of key() is wrong");
            if (MiniCounter == 2) { MiniCounter = 0; TinyCounter++;} else {MiniCounter++;}
        }

    }
    @Test
    @Tag("Kat")
    @Tag("additional")
    public void TestGetAndSet()
    {
        //System.out.println("Size is = " + dataStore.getClasses().size());
        Availability TheTestValue = dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[0]).getAvailability();

        TheTestValue.setCampus(Campus.CITY);
        assertEquals(Campus.CITY,TheTestValue.getCampus(), "Campus wasn't changed");
        TheTestValue.setAvailabilityNum(99);
        assertEquals(99,TheTestValue.getAvailabilityNum(), "AvailabilityNum wasn't changed");
        TheTestValue.setSemester(2);
        assertEquals(2,TheTestValue.getSemester(), "Semester wasn't changed");
        TheTestValue.setAttendanceMode("Online");
        assertEquals("Online",TheTestValue.getAttendanceMode(), "AttendanceMode wasn't changed");
    }

    @Test
    @DisplayName("Test Equals")
    @Tag("Kat")
    @Tag("additional")
    public void TestEquals()
    {
        Availability TestValue =  dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[0]).getAvailability();
        Availability DifferentTestValue =  dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[3]).getAvailability();
        Availability DuplicateTestValue =  dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[0]).getAvailability();

        assertFalse(TestValue.equals(DifferentTestValue), "They shouldn't be equal");
        assertTrue(TestValue.equals(DuplicateTestValue), "They should be equal");
    }
    @Test
    @DisplayName("Test hash")
    @Tag("Kat")
    @Tag("additional")
    public void TestHash()
    {
        Availability TheTestValue = dataStore.getClasses().get(dataStore.getClasses().keySet().toArray()[0]).getAvailability();
        int DaHash = Objects.hash(TheTestValue.getAttendanceMode(), TheTestValue.getCampus(), TheTestValue.getSemester(), TheTestValue.getAvailabilityNum());
        assertEquals(DaHash, TheTestValue.hashCode());
    }

}
