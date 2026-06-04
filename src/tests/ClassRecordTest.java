package tests;

import org.junit.jupiter.api.*;
import timetableoptimizer.*;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClassRecordTest {


    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DataStore dataStore = new DataStore();
    private static ClassManager classManager = new ClassManager(dataStore);
    public static List<ClassRecord> TheRecords;
    public static ClassRecord FirstRecord;

    public static LocalDate NowDate = LocalDate.now();
    public static LocalDate NowDatePlusMonth = NowDate.plusMonths(1);
    public static LocalTime NowTime = LocalTime.now();
    public static LocalTime NowTimePlusHour = NowTime.plusHours(1);

    public static String TopicCode;
    public static List<String> RandomTopicNames = List.of("game design","game creation","game destruction");
    public static String TopicName;
    public static int RandomClassTypesIntResult;
    public static List<String> RandomClassTypes = List.of("lecture","workshop","practical");
    public static String ClassName;
    public static DayOfWeek TheDay;

    public static List<String> AttendanceModes = List.of("online","in person","something else");
    public static String AttendanceMode;

    public static List<String> RandomBuildings = List.of("BuildingOne","Festival Tower","Torture chamber");
    public static String Building;
    public static List<String> RandomLocations = List.of("Computer Lab","Classroom","Sensory Deprivation room");
    public static String Location;

    public static int ClassInstance;

    @BeforeAll
    public static void Setup() throws IOException {

        TheRecords =  ClassImporter.importClass("sample-data/sample-classes.csv");
        FirstRecord = TheRecords.get(0);


        Random rand = new Random();
        TopicCode = "comp" + rand.nextInt(10) + rand.nextInt(10) + rand.nextInt(10) +rand.nextInt(10);
        TopicName = RandomTopicNames.get(rand.nextInt(3));
        AttendanceMode = AttendanceModes.get(rand.nextInt(3));
        RandomClassTypesIntResult = rand.nextInt(3);
        ClassName = RandomClassTypes.get(RandomClassTypesIntResult) + "-" +  rand.nextInt(6);
        TheDay = DayOfWeek.of(rand.nextInt(7) + 1);

        Building = RandomBuildings.get(rand.nextInt(3));
        Location = rand.nextInt(10) + rand.nextInt(10) + rand.nextInt(10) + " " + RandomLocations.get(rand.nextInt(3));
        ClassInstance = rand.nextInt(-1,5);
    }


    @Test
    @Order(1)
    @Tag("Kat")
    @Tag("Core")
    public void TestGetAndSet()
    {
        String ID = FirstRecord.getID();
        assertEquals(ID,FirstRecord.getID());
        FirstRecord.setID(ID);
        assertEquals(ID,FirstRecord.getID());




        assertEquals("COMP1701",FirstRecord.getTopicCode());
        assertEquals("Game Design",FirstRecord.getTopicName());
        assertEquals("in person|CITY|2|1",FirstRecord.getAvailability().key());
        assertEquals("Workshop-1", FirstRecord.getClassName());
        assertEquals(1,FirstRecord.getClassInstance());
        assertEquals("2026-07-27",FirstRecord.getStartDate().toString());
        assertEquals("2026-09-14", FirstRecord.getEndDate().toString());
        assertEquals(DayOfWeek.MONDAY,FirstRecord.getDay());
        assertEquals("09:00",FirstRecord.getStartTime().toString());
        assertEquals("10:00",FirstRecord.getEndTime().toString());
        assertEquals("Festival Tower", FirstRecord.getBuilding());
        assertEquals("506 Computer Lab",FirstRecord.getLocation());

        FirstRecord.setTopicCode(TopicCode);
        FirstRecord.setTopicName(TopicName);
        Availability TheAV = FirstRecord.getAvailability();
        TheAV.setAttendanceMode(AttendanceMode);
        FirstRecord.setAvailability(TheAV);
        FirstRecord.setClassInstance(ClassInstance);
        FirstRecord.setClassName(ClassName);
        FirstRecord.setStartDate(NowDate);
        FirstRecord.setEndDate(NowDatePlusMonth);
        FirstRecord.setDay(TheDay);
        FirstRecord.setStartTime(NowTime);
        FirstRecord.setEndTime(NowTimePlusHour);
        FirstRecord.setBuilding(Building);
        FirstRecord.setLocation(Location);


        assertEquals(TopicCode,FirstRecord.getTopicCode());
        assertEquals(TopicName,FirstRecord.getTopicName());
        assertEquals( AttendanceMode + "|CITY|2|1",FirstRecord.getAvailability().key());
        assertEquals(ClassName, FirstRecord.getClassName());
        assertEquals(ClassInstance,FirstRecord.getClassInstance());
        assertEquals(NowDate,FirstRecord.getStartDate());
        assertEquals(NowDatePlusMonth, FirstRecord.getEndDate());
        assertEquals(TheDay,FirstRecord.getDay());
        assertEquals(NowTime,FirstRecord.getStartTime());
        assertEquals(NowTimePlusHour,FirstRecord.getEndTime());
        assertEquals(Building, FirstRecord.getBuilding());
        assertEquals(Location,FirstRecord.getLocation());
    }
    @Test
    @DisplayName("Test duplicateKey()")
    @Order(2)
    @Tag("Kat")
    @Tag("Core")
    public void TestDuplicateKey()
    {
        String Expected = String.format("%s|%s|%s|city|2|1|%s|%s|%s|%s|%s",
                TopicCode,TopicName,AttendanceMode,ClassName,ClassInstance,NowDate,NowDatePlusMonth,TheDay.toString().toLowerCase());
        assertEquals(Expected,FirstRecord.duplicateKey());
    }
    @Test
    @DisplayName("Test classGroupKey() & selectableGroupKey & classFormatKey")
    @Order(3)
    @Tag("Kat")
    @Tag("Core")
    public void TestMultiple()
    {
        String Expected = String.format("%s|%s|%s|city|2|1|%s|%s",
                TopicCode,TopicName,AttendanceMode,ClassName,FirstRecord.getClassInstance());
        //"comp1702|game creation|online|city|2|1|lecture-1|1";
        assertEquals(Expected,FirstRecord.classGroupKey());
        Expected = String.format("%s|%s|%s|%s|city|2|1",
            TopicCode,ClassName,FirstRecord.getClassInstance(),AttendanceMode);
        //"comp1702|lecture-1|1|online|city|2|1";
        assertEquals(Expected,FirstRecord.selectableGroupKey());
        Expected = TopicCode + "|" + ClassName;
        assertEquals(Expected,FirstRecord.classFormatKey());
    }
    @Test
    @DisplayName("Test isLecture()")
    @Order(4)
    @Tag("Kat")
    @Tag("Core")
    public void TestIsLecture()
    {
        if (RandomClassTypesIntResult == 0) { assertTrue(FirstRecord.isLecture()); }
        else { assertFalse(FirstRecord.isLecture()); }
    }
    @Test
    @DisplayName("Test Summary")
    @Order(5)
    @Tag("Kat")
    @Tag("Core")
    public void TestSummary()
    {
        String Expected = String.format("[%s] %s %s | %s | %s #%d | %s - %s | %s | %s - %s | %s, %s",
                FirstRecord.getID(), TopicCode, TopicName, FirstRecord.getAvailability().display(), ClassName, FirstRecord.getClassInstance(),
                DATE_FORMAT.format(NowDate), DATE_FORMAT.format(NowDatePlusMonth), PrettyDay(TheDay),
                TIME_FORMAT.format(NowTime), TIME_FORMAT.format(NowTimePlusHour), Building, Location);
        assertEquals(Expected,FirstRecord.fullSummary());
        Expected = String.format("[%s] %s %s | %s | %s #%d",
                FirstRecord.getID(), TopicCode, TopicName, FirstRecord.getAvailability().display(), ClassName, FirstRecord.getClassInstance());
        assertEquals(Expected,FirstRecord.shortSummary());

    }
    @Test
    @DisplayName("Constuctor test")
    @Order(6)
    @Tag("Kat")
    @Tag("Core")
    public void TestConstructor()
    {
        ClassRecord AnotherRecord = new ClassRecord(FirstRecord);
        assertTrue(AnotherRecord.fullSummary() != FirstRecord.fullSummary());
        AnotherRecord = FirstRecord.copy();
        assertTrue(AnotherRecord.fullSummary() != FirstRecord.fullSummary());

        String TheCVS = FirstRecord.toCsvRow();

        assertEquals(toCsvRow(), TheCVS);


    }


    public static String PrettyDay(DayOfWeek day) {
        String lower = day.toString().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
    public String toCsvRow() {
        return String.join(",",
                csv(TopicCode),
                csv(TopicName),
                csv(FirstRecord.getAvailability().getAttendanceMode()),
                csv(FirstRecord.getAvailability().getCampus().getDisplayName()),
                String.valueOf(FirstRecord.getAvailability().getSemester()),
                String.valueOf(FirstRecord.getAvailability().getAvailabilityNum()),
                csv(ClassName),
                String.valueOf(FirstRecord.getClassInstance()),
                csv(DATE_FORMAT.format(NowDate)),
                csv(DATE_FORMAT.format(NowDatePlusMonth)),
                csv(PrettyDay(TheDay)),
                csv(TIME_FORMAT.format(NowTime)),
                csv(TIME_FORMAT.format(NowTimePlusHour)),
                csv(Building),
                csv(Location));
    }
    private static String csv(String value) {
        String v = value == null ? "" : value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
