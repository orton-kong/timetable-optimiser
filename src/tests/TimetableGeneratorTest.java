package tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import timetableoptimizer.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TimetableGeneratorTest {

    static DataStore repeatedUseDataStore;

    @BeforeAll
    static void setup() {
        repeatedUseDataStore = new DataStore();
    }

    @Tag("Nathan")
    @Tag("Critical")
    @Test
    void testTimetableGeneratorConstructor() {
        TimetableGenerator tg = new TimetableGenerator();

        assertInstanceOf(TimetableGenerator.class, tg);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable")
    @Test
    void testGenerateTimetable() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        ClassRecord classRecord2 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(11, 0));
        ClassRecord classRecord3 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(14, 0));

        dataStore.getClasses().put("1", classRecord1);
        dataStore.getClasses().put("2", classRecord2);
        dataStore.getClasses().put("3", classRecord3);

        List<String> topics = new ArrayList<>();
        topics.add("COMP3033");

        List<Campus> campuses = new ArrayList<>();
        campuses.add(Campus.TONSLEY);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(ClassSpreadPreferences.COMPACT_SPREAD);
        Preference preference = new Preference(prefs);

        Timetable generated = TimetableGenerator.generateTimetable("Test timetable", 1, topics, campuses, false, preference, dataStore);

        assertAll(
                () -> assertEquals("Test timetable", generated.getName()),
                () -> assertFalse(generated.getClasses().isEmpty()),
                () -> assertFalse(generated.isAllowLectureOverlap()),
                () -> assertEquals(classRecord1.getClassName(), generated.getClasses().getFirst().getClassName()),
                () -> assertEquals(ClassSpreadPreferences.COMPACT_SPREAD, generated.getPrefrences().getClassSpread())
        );

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with no classes")
    @Test
    void testGenerateTimetableNoClasses() {
        assertThrows(IllegalArgumentException.class, () -> TimetableGenerator.generateTimetable("Test timetable", 1, new ArrayList<>(), new ArrayList<>(), false, new Preference(), new DataStore()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with no campuses")
    @Test
    void testGenerateTimetableNoCampuses() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        dataStore.getClasses().put("1", classRecord1);

        List<String> topics = new ArrayList<>();
        topics.add("COMP3033");

        assertThrows(IllegalArgumentException.class, () -> TimetableGenerator.generateTimetable("Test timetable", 1, topics, new ArrayList<>(), false, new Preference(), new DataStore()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with wrong topic")
    @Test
    void testGenerateTimetableWrongTopic() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        dataStore.getClasses().put("1", classRecord1);

        List<String> topics = new ArrayList<>();
        topics.add("Wrong");

        List<Campus> campuses = new ArrayList<>();
        campuses.add(Campus.TONSLEY);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(ClassSpreadPreferences.COMPACT_SPREAD);
        Preference preference = new Preference(prefs);

        assertThrows(IllegalArgumentException.class, () -> TimetableGenerator.generateTimetable("Test timetable", 1, topics, campuses, false, preference, dataStore));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with overlapping times")
    @Test
    void testGenerateTimetableOverlap() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP1001", "Cloud Computing", "Tutorial", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        dataStore.getClasses().put("1", classRecord1);
        dataStore.getClasses().put("2", classRecord2);

        List<String> topics = new ArrayList<>();
        topics.add("COMP3033");
        topics.add("COMP1001");

        List<Campus> campuses = new ArrayList<>();
        campuses.add(Campus.TONSLEY);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(ClassSpreadPreferences.COMPACT_SPREAD);
        Preference preference = new Preference(prefs);

        assertThrows(IllegalArgumentException.class, () -> TimetableGenerator.generateTimetable("Test timetable", 1, topics, campuses, false, preference, dataStore));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with null name")
    @Test
    void testGenerateTimetableNullName() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        dataStore.getClasses().put("1", classRecord1);

        List<String> topics = new ArrayList<>();
        topics.add("COMP3033");

        List<Campus> campuses = new ArrayList<>();
        campuses.add(Campus.TONSLEY);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(ClassSpreadPreferences.COMPACT_SPREAD);
        Preference preference = new Preference(prefs);

        Timetable generated = TimetableGenerator.generateTimetable(null, 1, topics, campuses, false, preference, dataStore);

        assertEquals("Timetable 1", generated.getName());

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate timetable with empty name")
    @Test
    void testGenerateTimetableEmptyName() {
        DataStore dataStore = new DataStore();

        ClassRecord classRecord1 = createClassGeneral("1", "Comp3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        dataStore.getClasses().put("1", classRecord1);

        List<String> topics = new ArrayList<>();
        topics.add("COMP3033");

        List<Campus> campuses = new ArrayList<>();
        campuses.add(Campus.TONSLEY);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(ClassSpreadPreferences.COMPACT_SPREAD);
        Preference preference = new Preference(prefs);

        Timetable generated = TimetableGenerator.generateTimetable("", 1, topics, campuses, false, preference, dataStore);

        assertEquals("Timetable 1", generated.getName());

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test has timetable name standard usage")
    @Test
    void testHasTimetableNamed() {
        DataStore ds = new DataStore();
        ds.getTimetables().put("Timetable 1", new Timetable("", new ArrayList<>(), true, new Preference()));

        assertTrue(TimetableGenerator.hasTimetableNamed(ds, "Timetable 1"));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test has timetable name case insensitive")
    @Test
    void testHasTimetableNamedCaseInsensitive() {
        DataStore ds = new DataStore();
        ds.getTimetables().put("Timetable 1", new Timetable("", new ArrayList<>(), true, new Preference()));

        assertTrue(TimetableGenerator.hasTimetableNamed(ds, "timetable 1"));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test has timetable name name not found")
    @Test
    void testHasTimetableNamedNone() {
        DataStore ds = new DataStore();
        ds.getTimetables().put("Timetable 1", new Timetable("", new ArrayList<>(), true, new Preference()));

        assertFalse(TimetableGenerator.hasTimetableNamed(ds, "Timetable 2"));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate name single")
    @Test
    void testGenerateNameSingle() {
        DataStore dataStore = new DataStore();

        String result = TimetableGenerator.generateName(dataStore);

        assertEquals("Timetable 1", result);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test generate name with a repeated method")
    @RepeatedTest(10)
    void testGenerateNameRepeated(RepetitionInfo ri) {
        String generated = TimetableGenerator.generateName(repeatedUseDataStore);
        repeatedUseDataStore.getTimetables().put(generated, new Timetable("", new ArrayList<>(), true, new Preference()));

        assertEquals("Timetable " + ri.getCurrentRepetition(), generated);
    }


    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans for City")
    @Test
    void testBuildTopicPlansCityOnly() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        List<Campus> campusList = new ArrayList<>();

        campusList.add(Campus.CITY);

        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        classRecordList.add(createClassAtCampus(Campus.CITY));

        assertEquals(2, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).getFirst().size());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans for Bedford")
    @Test
    void testBuildTopicPlansBedfordOnly() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        List<Campus> campusList = new ArrayList<>();

        campusList.add(Campus.BEDFORD);

        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        classRecordList.add(createClassAtCampus(Campus.CITY));

        assertEquals(1, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, true).getFirst().size());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans with all non City")
    @Test
    void testBuildTopicPlansBedfordAndTonsley() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        List<Campus> campusList = new ArrayList<>();

        campusList.add(Campus.BEDFORD);
        campusList.add(Campus.TONSLEY);

        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.TONSLEY));
        classRecordList.add(createClassAtCampus(Campus.TONSLEY));

        assertAll(
                () -> assertEquals(2, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).size()),
                () -> assertEquals(1, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).getFirst().size()),
                () -> assertEquals(2, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).get(1).size())
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans all campuses")
    @Test
    void testBuildTopicPlansAll() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        List<Campus> campusList = new ArrayList<>();

        campusList.add(Campus.BEDFORD);
        campusList.add(Campus.TONSLEY);
        campusList.add(Campus.CITY);

        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.TONSLEY));
        classRecordList.add(createClassAtCampus(Campus.TONSLEY));

        assertAll(
                () -> assertEquals(3, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).size()),
                () -> assertEquals(2, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).getFirst().size()),
                () -> assertEquals(1, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).get(1).size()),
                () -> assertEquals(2, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).get(2).size())
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans no campuses")
    @Test
    void testBuildTopicPlansNone() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        List<Campus> campusList = new ArrayList<>();

        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));

        assertEquals(0, TimetableGenerator.buildTopicPlans("", classRecordList, campusList, false).size());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans null input")
    @Test
    void testBuildTopicPlansNull() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.buildTopicPlans("", null, null, false));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build topic plans empty input")
    @Test
    void testBuildTopicPlansEmpty() {
        assertTrue(TimetableGenerator.buildTopicPlans("", new ArrayList<>(), new ArrayList<>(), false).isEmpty());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with same selected as required")
    @Test
    void testSelectedRecordsContainCompleteOfferingExactMatch() {
        List<ClassRecord> required = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        required.add(classRecord1);
        required.add(classRecord2);
        List<ClassRecord> selected = new ArrayList<>(required);

        assertTrue(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with fewer selected than required")
    @Test
    void testSelectedRecordsContainCompleteOfferingMissingRequired() {
        List<ClassRecord> required = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        required.add(classRecord1);
        required.add(classRecord2);

        List<ClassRecord> selected = new ArrayList<>();
        selected.add(classRecord1);

        assertFalse(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with more selected than required")
    @Test
    void testSelectedRecordsContainCompleteOfferingExtraSelected() {
        List<ClassRecord> required = new ArrayList<>(), selected = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        selected.add(classRecord1);
        selected.add(classRecord2);

        required.add(classRecord1);

        assertTrue(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with different instance")
    @Test
    void testSelectedRecordsContainCompleteOfferingDifferentInstance() {
        List<ClassRecord> required = new ArrayList<>(), selected = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord3 = createClassGeneral("3", "COMP3033", "Cloud Computing", "Workshop", 2, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        selected.add(classRecord1);
        selected.add(classRecord2);

        required.add(classRecord3);

        assertTrue(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with empty required")
    @Test
    void testSelectedRecordsContainCompleteOfferingNoneRequired() {
        List<ClassRecord> required = new ArrayList<>(), selected = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        selected.add(classRecord1);
        selected.add(classRecord2);

        assertTrue(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with empty selected")
    @Test
    void testSelectedRecordsContainCompleteOfferingNoneSelected() {
        List<ClassRecord> required = new ArrayList<>(), selected = new ArrayList<>();

        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        required.add(classRecord1);
        required.add(classRecord2);

        assertFalse(TimetableGenerator.selectedRecordsContainCompleteOffering(required, selected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with empty lists")
    @Test
    void testSelectedRecordsContainCompleteOfferingEmptyLists() {
        assertTrue(TimetableGenerator.selectedRecordsContainCompleteOffering(new ArrayList<>(), new ArrayList<>()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test selected records contains complete offering with null lists")
    @Test
    void testSelectedRecordsContainCompleteOfferingNullLists() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.selectedRecordsContainCompleteOffering(null, null));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build plans within campus rule")
    @Test
    void buildPlansWithinCampusRule() {
        ClassRecord classRecord1 = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0));
        ClassRecord classRecord2 = createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.WEDNESDAY, LocalTime.of(11, 0));
        ClassRecord classRecord3 = createClassGeneral("3", "COMP3033", "Cloud Computing", "Workshop", 2, Campus.CITY, DayOfWeek.WEDNESDAY, LocalTime.of(14, 0));
        ClassRecord classRecord4 = createClassGeneral("4", "COMP1002", "Computing Fundamentals", "Lab", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(14, 0));

        List<ClassRecord> classes = new ArrayList<>();
        classes.add(classRecord1);
        classes.add(classRecord2);
        classes.add(classRecord3);
        classes.add(classRecord4);

        List<List<ClassRecord>> results = TimetableGenerator.buildPlansWithinCampusRule(classes, true);

        assertAll(
                () -> assertEquals(2, results.size()),
                () -> assertTrue(results.get(1).contains(classRecord1))
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build plans within campus rule with null input")
    @Test
    void buildPlansWithinCampusRuleNull() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.buildPlansWithinCampusRule(null, true));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test build plans within campus rule with empty input")
    @Test
    void buildPlansWithinCampusRuleEmpty() {
        assertEquals(0, TimetableGenerator.buildPlansWithinCampusRule(new ArrayList<>(), true).getFirst().size());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with single option")
    @Test
    void testBacktrackOptionsSingular() {
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        ClassRecord classRecord = createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));

        subjectSchedule.add(classRecord);
        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<List<ClassRecord>> result = new ArrayList<>();

        TimetableGenerator.backtrackOptions(subjects, 0, new ArrayList<>(), result, false);

        assertTrue(result.contains(subjectSchedule));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with multiple classes and lecture overlap true")
    @Test
    void testBacktrackOptionsMultipleClassesLectureOverlap() {
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<List<ClassRecord>> result = new ArrayList<>();

        TimetableGenerator.backtrackOptions(subjects, 0, new ArrayList<>(), result, true);

        assertTrue(result.contains(subjectSchedule));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with multiple classes and lecture overlap false")
    @Test
    void testBacktrackOptionsMultipleClassesNoLectureOverlap() {
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<List<ClassRecord>> result = new ArrayList<>();

        TimetableGenerator.backtrackOptions(subjects, 0, new ArrayList<>(), result, false);

        assertTrue(result.isEmpty());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with multiple schedules")
    @Test
    void testBacktrackOptionsMultipleSchedules() {
        List<ClassRecord> subjectSchedule1 = new ArrayList<>(), subjectSchedule2 = new ArrayList<>(), subjectSchedule3 = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule1.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedule2.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedule3.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule3.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule3.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedules.add(subjectSchedule1);
        subjectSchedules.add(subjectSchedule2);
        subjectSchedules.add(subjectSchedule3);
        subjects.add(subjectSchedules);

        List<List<ClassRecord>> result = new ArrayList<>();

        TimetableGenerator.backtrackOptions(subjects, 0, new ArrayList<>(), result, true);

        assertTrue(result.contains(subjectSchedule1) && result.contains(subjectSchedule2) && !result.contains(subjectSchedule3));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with multiple schedules")
    @Test
    void testBacktrackOptionsMultipleTopics() {
        List<ClassRecord> subjectSchedule1 = new ArrayList<>(), subjectSchedule2 = new ArrayList<>(), subjectSchedule3 = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules1 = new ArrayList<>(), subjectSchedules2 = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule1.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedule2.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));

        subjectSchedule3.add(createClassGeneral("1", "COMP1002", "Computing Fundamentals", "Lecture", 1, Campus.TONSLEY, DayOfWeek.MONDAY, LocalTime.of(9, 0)));
        subjectSchedule3.add(createClassGeneral("2", "COMP1002", "Computing Fundamentals", "Workshop", 1, Campus.TONSLEY, DayOfWeek.TUESDAY, LocalTime.of(9, 0)));
        subjectSchedule3.add(createClassGeneral("3", "COMP1002", "Computing Fundamentals", "Lab", 1, Campus.TONSLEY, DayOfWeek.MONDAY, LocalTime.of(12, 0)));

        subjectSchedules1.add(subjectSchedule1);
        subjectSchedules1.add(subjectSchedule2);
        subjectSchedules2.add(subjectSchedule3);
        subjects.add(subjectSchedules1);
        subjects.add(subjectSchedules2);

        List<List<ClassRecord>> result = new ArrayList<>();

        TimetableGenerator.backtrackOptions(subjects, 0, new ArrayList<>(), result, true);

        List<ClassRecord> expected = new ArrayList<>();
        expected.addAll(subjectSchedule1);
        expected.addAll(subjectSchedule3);

        assertTrue(result.contains(expected));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with Null input")
    @Test
    void testBacktrackOptionsNullInput() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.backtrackOptions(null, 0, null, null, false));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test backtrack options with Empty input")
    @Test
    void testBacktrackOptionsEmptyInput() {
        List<List<List<ClassRecord>>> lists = new ArrayList<>();
        TimetableGenerator.backtrackOptions(lists, 0, new ArrayList<>(), new ArrayList<>(), false);

        assertTrue(lists.isEmpty());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics single class, schedule and topic")
    @Test
    void testCombineTopics() {
        ClassRecord classRecord = createClassGeneral("1", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0));
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule.add(classRecord);
        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(LocationPreferences.CITY);
        Preference preference = new Preference(prefs);

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), Integer.MIN_VALUE);

        TimetableGenerator.combineTopics(subjects, 0, new ArrayList<>(), true, preference, best);

        assertEquals(subjectSchedule, best.records);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics single schedule and topic but multiple classes with lecture overlap")
    @Test
    void testCombineTopicsMultipleClassesLectureOverlap() {
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(LocationPreferences.CITY);
        Preference preference = new Preference(prefs);

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), Integer.MIN_VALUE);

        TimetableGenerator.combineTopics(subjects, 0, new ArrayList<>(), true, preference, best);

        assertEquals(subjectSchedule, best.records);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics single schedule and topic but multiple classes without lecture overlap")
    @Test
    void testCombineTopicsMultipleClassesNoLectureOverlap() {
        List<ClassRecord> subjectSchedule = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedules.add(subjectSchedule);
        subjects.add(subjectSchedules);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(LocationPreferences.CITY);
        Preference preference = new Preference(prefs);

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), Integer.MIN_VALUE);

        TimetableGenerator.combineTopics(subjects, 0, new ArrayList<>(), false, preference, best);

        assertTrue(best.records.isEmpty());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics single topic but multiple classes and schedules")
    @Test
    void testCombineTopicsMultipleSchedules() {
        List<ClassRecord> subjectSchedule1 = new ArrayList<>(), subjectSchedule2 = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule1.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedule2.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedules.add(subjectSchedule1);
        subjectSchedules.add(subjectSchedule2);
        subjects.add(subjectSchedules);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(LocationPreferences.CITY);
        Preference preference = new Preference(prefs);

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), Integer.MIN_VALUE);

        TimetableGenerator.combineTopics(subjects, 0, new ArrayList<>(), true, preference, best);

        assertTrue(best.records.containsAll(subjectSchedule1));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics multiple classes, schedules and topics")
    @Test
    void testCombineMultipleTopics() {
        List<ClassRecord> subjectSchedule1 = new ArrayList<>(), subjectSchedule2 = new ArrayList<>();
        List<List<ClassRecord>> subjectSchedules1 = new ArrayList<>(), subjectSchedules2 = new ArrayList<>();
        List<List<List<ClassRecord>>> subjects = new ArrayList<>();

        subjectSchedule1.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedule2.add(createClassGeneral("1", "COMP3033", "Cloud Computing", "Lecture", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("2", "COMP3033", "Cloud Computing", "Workshop", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("3", "COMP3033", "Cloud Computing", "Lab", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedules1.add(subjectSchedule1);
        subjectSchedules1.add(subjectSchedule2);

        subjectSchedules1.clear();
        subjectSchedule2.clear();

        subjectSchedule1.add(createClassGeneral("1", "COMP1002", "Computing Fundamentals", "Lecture", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("2", "COMP1002", "Computing Fundamentals", "Workshop", 1, Campus.CITY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule1.add(createClassGeneral("3", "COMP1002", "Computing Fundamentals", "Lab", 1, Campus.CITY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedule2.add(createClassGeneral("1", "COMP1002", "Computing Fundamentals", "Lecture", 1, Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("2", "COMP1002", "Computing Fundamentals", "Workshop", 1, Campus.TONSLEY, DayOfWeek.THURSDAY, LocalTime.of(9, 0)));
        subjectSchedule2.add(createClassGeneral("3", "COMP1002", "Computing Fundamentals", "Lab",1 , Campus.TONSLEY, DayOfWeek.FRIDAY, LocalTime.of(13, 0)));

        subjectSchedules1.add(subjectSchedule1);
        subjectSchedules1.add(subjectSchedule2);

        subjects.add(subjectSchedules1);

        List<Enum<?>> prefs = new ArrayList<>();
        prefs.add(LocationPreferences.CITY);
        Preference preference = new Preference(prefs);

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), Integer.MIN_VALUE);

        TimetableGenerator.combineTopics(subjects, 0, new ArrayList<>(), true, preference, best);

        assertTrue(best.records.containsAll(subjectSchedule2));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics with clash")
    @Test
    void testCombineTopicsWithClash() {

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics with null input")
    @Test
    void testCombineTopicsNullInput() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.combineTopics(null, 0,null,false,null,null));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test combine topics with empty input")
    @Test
    void testCombineTopicsEmptyInput() {
        List<List<List<ClassRecord>>> input = new ArrayList<>();

        TimetableGenerator.SearchState best = new TimetableGenerator.SearchState(new ArrayList<>(), 0);

        TimetableGenerator.combineTopics(input, 0,new ArrayList<>(),false,new Preference(),best);

        assertEquals(0, best.records.size());
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score with a single preference")
    @Test
    void testScoreSinglePreference() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.CITY));

        List<Enum<?>> prefs = new ArrayList<>();

        prefs.add(LocationPreferences.CITY);

        Preference preference = new Preference(prefs);

        assertEquals(4, TimetableGenerator.score(classRecordList, preference));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score with multiple preferences")
    @Test
    void testScoreMultiplePreferences() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));

        List<Enum<?>> prefs1 = new ArrayList<>();

        prefs1.add(LocationPreferences.CITY);
        prefs1.add(LocationPreferences.BEDFORD);

        Preference preference = new Preference(prefs1);

        assertEquals(8, TimetableGenerator.score(classRecordList, preference));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score with multiple preferences in different orders are different")
    @Test
    void testScoreMultiplePreferencesOrder() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));

        List<Enum<?>> prefs1 = new ArrayList<>(), prefs2 = new ArrayList<>();

        prefs1.add(LocationPreferences.CITY);
        prefs1.add(LocationPreferences.BEDFORD);

        prefs2.add(LocationPreferences.BEDFORD);
        prefs2.add(LocationPreferences.CITY);

        Preference preference1 = new Preference(prefs1);
        Preference preference2 = new Preference(prefs2);

        assertNotEquals(TimetableGenerator.score(classRecordList, preference1), TimetableGenerator.score(classRecordList, preference2));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score with duplicate preferences")
    @Test
    void testScoreDuplicatePreferences() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.CITY));
        classRecordList.add(createClassAtCampus(Campus.BEDFORD));

        List<Enum<?>> prefs = new ArrayList<>();

        prefs.add(LocationPreferences.CITY);
        prefs.add(LocationPreferences.CITY);

        Preference preference = new Preference(prefs);

        assertEquals(4, TimetableGenerator.score(classRecordList, preference));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score with null values")
    @Test
    void testScoreNull() {
        assertAll(
                () -> assertEquals(0, TimetableGenerator.score(new ArrayList<>(), null)),
                () -> assertEquals(0, TimetableGenerator.score(null, new Preference())),
                () -> assertEquals(0, TimetableGenerator.score(new ArrayList<>(), new Preference())),
                () -> assertEquals(0, TimetableGenerator.score(null, null))
        );

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for campus preference")
    @ParameterizedTest
    @CsvSource({
            "1, 1, 1",
            "3, 2, 1",
            "10, 0, 0",
            "0, 10, 0",
            "0, 0, 10",
            "0, 0, 270",
            "100, 1000, 10000"
    })
    void testScoreSinglePreferenceCampus(int numberAtBedford, int numberAtCity, int numberAtTonsley) {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (int i = 0; i < numberAtBedford; i++)
            classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        for (int i = 0; i < numberAtCity; i++)
            classRecordList.add(createClassAtCampus(Campus.CITY));
        for (int i = 0; i < numberAtTonsley; i++)
            classRecordList.add(createClassAtCampus(Campus.TONSLEY));

        assertAll(
                () -> assertEquals(numberAtBedford, TimetableGenerator.scoreSinglePreference(classRecordList, LocationPreferences.BEDFORD)),
                () -> assertEquals(numberAtCity, TimetableGenerator.scoreSinglePreference(classRecordList, LocationPreferences.CITY)),
                () -> assertEquals(numberAtTonsley, TimetableGenerator.scoreSinglePreference(classRecordList, LocationPreferences.TONSLEY)),
                () -> {
                    if (numberAtBedford == 0 && numberAtCity == 0)
                        assertEquals(10, TimetableGenerator.scoreSinglePreference(classRecordList, LocationPreferences.ALL_AT_SAME_CAMPUS));
                }
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for time preference")
    @Test
    void testScoreSinglePreferenceStartTime() {
        List<ClassRecord> classRecordList = new ArrayList<>();

        classRecordList.add(createClassAtStartTime(LocalTime.of(9, 0)));
        classRecordList.add(createClassAtStartTime(LocalTime.of(12, 0)));
        classRecordList.add(createClassAtStartTime(LocalTime.of(15, 30)));

        assertAll(
                () -> assertEquals(1, TimetableGenerator.scoreSinglePreference(classRecordList, TimeOfDayPreferences.MORNING)),
                () -> assertEquals(2, TimetableGenerator.scoreSinglePreference(classRecordList, TimeOfDayPreferences.AFTERNOON))
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for day preference")
    @Test
    void testScoreSinglePreferenceDay() {
        List<ClassRecord> classRecordList = new ArrayList<>();

        classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));
        classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));
        classRecordList.add(createClassOnDay(DayOfWeek.TUESDAY));

        assertAll(
                () -> assertEquals(2, TimetableGenerator.scoreSinglePreference(classRecordList, DayPreferences.MONDAY)),
                () -> assertEquals(1, TimetableGenerator.scoreSinglePreference(classRecordList, DayPreferences.TUESDAY)),
                () -> assertEquals(0, TimetableGenerator.scoreSinglePreference(classRecordList, DayPreferences.WEDNESDAY)),
                () -> assertEquals(0, TimetableGenerator.scoreSinglePreference(classRecordList, DayPreferences.THURSDAY)),
                () -> assertEquals(0, TimetableGenerator.scoreSinglePreference(classRecordList, DayPreferences.FRIDAY))
        );

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for even spread preference")
    @Test
    void testScoreSinglePreferenceEvenSpread() {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            classRecordList.add(createClassOnDay(day));
            classRecordList.add(createClassOnDay(day));
        }

        assertEquals(TimetableGenerator.evenSpreadScore(classRecordList), TimetableGenerator.scoreSinglePreference(classRecordList, ClassSpreadPreferences.EVEN_SPREAD)
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for compact spread preference")
    @Test
    void testScoreSinglePreferenceCompactSpread() {
        List<ClassRecord> classRecordList = new ArrayList<>();

        classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));
        classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));

        assertEquals(TimetableGenerator.compactScore(classRecordList), TimetableGenerator.scoreSinglePreference(classRecordList, ClassSpreadPreferences.COMPACT_SPREAD)
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test score single preference for non-existent preference type")
    @Test
    void testScoreSinglePreferenceNonExistent() {
        enum nonExistentPreference {PREFERENCE}

        assertThrows(IllegalArgumentException.class, () -> TimetableGenerator.scoreSinglePreference(new ArrayList<>(), nonExistentPreference.PREFERENCE));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test campus count with Csv source")
    @ParameterizedTest
    @CsvSource({
            "1, 1, 1",
            "3, 2, 1",
            "10, 0, 0",
            "0, 10, 0",
            "100, 1000, 10000"
    })
    void testCampusCount(int numberAtBedford, int numberAtCity, int numberAtTonsley) {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (int i = 0; i < numberAtBedford; i++)
            classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        for (int i = 0; i < numberAtCity; i++)
            classRecordList.add(createClassAtCampus(Campus.CITY));
        for (int i = 0; i < numberAtTonsley; i++)
            classRecordList.add(createClassAtCampus(Campus.TONSLEY));

        assertAll(
                () -> assertEquals(numberAtBedford, TimetableGenerator.campusCount(classRecordList, Campus.BEDFORD)),
                () -> assertEquals(numberAtCity, TimetableGenerator.campusCount(classRecordList, Campus.CITY)),
                () -> assertEquals(numberAtTonsley, TimetableGenerator.campusCount(classRecordList, Campus.TONSLEY))
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test campus count with null list")
    @Test
    void testCampusCountNullList() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.campusCount(null, Campus.CITY));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test campus count with an empty list")
    @Test
    void testCampusCountEmpty() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        assertAll(
                () -> assertEquals(0, TimetableGenerator.campusCount(classRecordList, Campus.CITY)),
                () -> assertEquals(0, TimetableGenerator.campusCount(classRecordList, Campus.BEDFORD)),
                () -> assertEquals(0, TimetableGenerator.campusCount(classRecordList, Campus.TONSLEY))
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test day count with Csv source")
    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 1, 1, 1, 1",
            "0, 0, 0, 0, 0, 0, 0",
            "10, 0, 0, 0, 0, 0, 0",
            "0, 10, 0, 0, 0, 0, 0",
            "0, 0, 10, 0, 0, 0, 0",
            "0, 0, 0, 10, 0, 0, 0",
            "0, 0, 0, 0, 10, 0, 0",
            "0, 0, 0, 0, 0, 10, 0",
            "0, 0, 0, 0, 0, 0, 10",
            "3, 2, 1, 4, 5, 6, 7",
            "100, 200, 300, 400, 500, 600, 700"
    })
    void testDayCount(int sundays, int mondays, int tuesdays, int wednesdays, int thursdays, int fridays, int saturdays) {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (int i = 0; i < sundays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.SUNDAY));
        for (int i = 0; i < mondays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));
        for (int i = 0; i < tuesdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.TUESDAY));
        for (int i = 0; i < wednesdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.WEDNESDAY));
        for (int i = 0; i < thursdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.THURSDAY));
        for (int i = 0; i < fridays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.FRIDAY));
        for (int i = 0; i < saturdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.SATURDAY));

        assertAll(
                () -> assertEquals(sundays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.SUNDAY)),
                () -> assertEquals(mondays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.MONDAY)),
                () -> assertEquals(tuesdays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.TUESDAY)),
                () -> assertEquals(wednesdays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.WEDNESDAY)),
                () -> assertEquals(thursdays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.THURSDAY)),
                () -> assertEquals(fridays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.FRIDAY)),
                () -> assertEquals(saturdays, TimetableGenerator.dayCount(classRecordList, DayOfWeek.SATURDAY))
        );

    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test day count with null list")
    @Test
    void testDayCountNullList() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.dayCount(null, DayOfWeek.FRIDAY));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test day count with an empty list")
    @Test
    void testDayCountEmpty() {
        List<ClassRecord> classRecordList = new ArrayList<>();
        assertAll(
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.SUNDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.MONDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.TUESDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.WEDNESDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.THURSDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.FRIDAY)),
                () -> assertEquals(0, TimetableGenerator.dayCount(classRecordList, DayOfWeek.SATURDAY))
        );
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test all at same campus with Csv source")
    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, false",
            "3, 2, 1, false",
            "10, 0, 0, true",
            "0, 10, 0, true",
            "1000, 0, 0, true",
            "100, 1000, 10000, false"
    })
    void testAllAtSameCampus(int numberAtBedford, int numberAtCity, int numberAtTonsley, boolean expected) {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (int i = 0; i < numberAtBedford; i++)
            classRecordList.add(createClassAtCampus(Campus.BEDFORD));
        for (int i = 0; i < numberAtCity; i++)
            classRecordList.add(createClassAtCampus(Campus.CITY));
        for (int i = 0; i < numberAtTonsley; i++)
            classRecordList.add(createClassAtCampus(Campus.TONSLEY));

        assertEquals(expected, TimetableGenerator.allAtSameCampus(classRecordList));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test all at same campus with Null list")
    @Test
    void testAllAtSameCampusNull() {
        assertThrows(NullPointerException.class, ()-> TimetableGenerator.allAtSameCampus(null));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test all at same campus with Empty list")
    @Test
    void testAllAtSameCampusEmpty() {
        assertTrue(TimetableGenerator.allAtSameCampus(new ArrayList<ClassRecord>()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test getting compact score with Csv source")
    @ParameterizedTest
    @CsvSource({
            "0, 20",
            "1, 19",
            "2, 18",
            "3, 17",
            "7, 13"
    })
    void testCompactScore(int distinctDays, int expectedScore) {
        List<ClassRecord> classRecordList = new ArrayList<>();
        DayOfWeek[] allDays = DayOfWeek.values();

        for (int i = 0; i < distinctDays; i++) {
            classRecordList.add(createClassOnDay(allDays[i]));
        }

        assertEquals(expectedScore, TimetableGenerator.compactScore(classRecordList));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test getting compact score with null list")
    @Test
    void testCompactScoreNull() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.compactScore(null));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test getting compact score with empty list")
    @Test
    void testCompactScoreEmpty() {
        assertEquals(20, TimetableGenerator.compactScore(new ArrayList<ClassRecord>()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test even spread score with Csv source")
    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 1, 1, 1, 1, 20",
            "10, 0, 0, 0, 0, 0, 0, 20",
            "0, 10, 0, 0, 0, 0, 0, 20",
            "3, 2, 1, 4, 5, 6, 7, 14",
            "0, 1, 1, 2, 3, 0, 0, 18",
            "100, 200, 300, 400, 500, 600, 700, -580"
    })
    void testEvenSpreadScore(int sundays, int mondays, int tuesdays, int wednesdays, int thursdays, int fridays, int saturdays, int expected) {
        List<ClassRecord> classRecordList = new ArrayList<>();

        for (int i = 0; i < sundays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.SUNDAY));
        for (int i = 0; i < mondays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.MONDAY));
        for (int i = 0; i < tuesdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.TUESDAY));
        for (int i = 0; i < wednesdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.WEDNESDAY));
        for (int i = 0; i < thursdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.THURSDAY));
        for (int i = 0; i < fridays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.FRIDAY));
        for (int i = 0; i < saturdays; i++)
            classRecordList.add(createClassOnDay(DayOfWeek.SATURDAY));

        assertEquals(expected, TimetableGenerator.evenSpreadScore(classRecordList));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test even spread score with null list")
    @Test
    void testEvenSpreadScoreNull() {
        assertThrows(NullPointerException.class, () -> TimetableGenerator.evenSpreadScore(null));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test even spread score with null list")
    @Test
    void testEvenSpreadScoreEmpty() {
        assertEquals(0, TimetableGenerator.evenSpreadScore(new ArrayList<ClassRecord>()));
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test SearchState constructor")
    @Test
    void testSearchStateConstructor() {
        TimetableGenerator.SearchState searchState = new TimetableGenerator.SearchState(new ArrayList<ClassRecord>(), 0);

        assertInstanceOf(TimetableGenerator.SearchState.class, searchState);
    }

    @Tag("Nathan")
    @Tag("Core")
    @DisplayName("Test SearchState variables")
    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "10, 10",
            "0, 10",
            "27, 72"
    })
    void testSearchStateVariables(int numberOfClasses, int score) {
        List<ClassRecord> classRecordList = new ArrayList<>();
        for (int i = 0; i < numberOfClasses; i++)
            classRecordList.add(createClassAtCampus(Campus.CITY));

        TimetableGenerator.SearchState searchState = new TimetableGenerator.SearchState(classRecordList, score);
        assertAll(
                () -> assertEquals(score, searchState.score),
                () -> assertEquals(numberOfClasses, searchState.records.size())
        );
    }

    ClassRecord createClassAtCampus(Campus campus){
        return new ClassRecord(
                "",
                "",
                "",
                new Availability("a", campus, 1, 1),
                "",
                0,
                LocalDate.of(1, 1, 1),
                LocalDate.of(1, 1, 1),
                DayOfWeek.FRIDAY,
                LocalTime.now(),
                LocalTime.now(),
                "",
                ""
        );
    }

    ClassRecord createClassOnDay(DayOfWeek day){
        return new ClassRecord(
                "",
                "",
                "",
                new Availability("a", Campus.CITY, 1, 1),
                "",
                0,
                LocalDate.of(1, 1, 1),
                LocalDate.of(1, 1, 1),
                day,
                LocalTime.now(),
                LocalTime.now(),
                "",
                ""
        );
    }

    ClassRecord createClassAtStartTime(LocalTime localTime){
        return new ClassRecord(
                "",
                "",
                "",
                new Availability("a", Campus.CITY, 1, 1),
                "",
                0,
                LocalDate.of(1, 1, 1),
                LocalDate.of(1, 1, 1),
                null,
                localTime,
                LocalTime.now(),
                "",
                ""
        );
    }

    private ClassRecord createClassGeneral(String id, String topicCode, String topicName, String className, int classInstance, Campus campus, DayOfWeek day, LocalTime start) {
        return new ClassRecord(
                id,
                topicCode,
                topicName,
                new Availability("a", campus, 1, 10),
                className,
                classInstance,
                LocalDate.now(),
                LocalDate.now().plusDays(100),
                day,
                start,
                start.plusHours(1),
                "",
                ""
        );
    }
}