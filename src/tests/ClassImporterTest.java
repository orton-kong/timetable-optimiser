package tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import timetableoptimizer.ClassImporter;
import timetableoptimizer.ClassRecord;

class ClassImporterTest {

    //--- importClass() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Class Empty CSV")
    void testImportClassEmptyCSV(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/empty.csv")
        );
        assertEquals("CSV file is empty.", exception.getMessage());
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Class File Not Found")
    void testImportClassInvalidFile(){
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> ClassImporter.importClass("this-does-not-exist")
        );
        assertEquals("CSV file not found: this-does-not-exist", exception.getMessage());
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Class No Class Records")
    void testImportClassNoLines(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/no-header.csv")
        );
        assertEquals("CSV file does not contain any class records.", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/header-no-data.csv")
        );
        assertEquals("CSV file does not contain any class records.", exception.getMessage());
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Class Invalid Number of Lines")
    void testImportClassMalformed(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-num-of-lines.txt")
        );
        assertEquals("Line 2 must contain exactly 8 columns, found 1.", exception.getMessage());
    }

    //--- importClass() and all other private methods used within importClass() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Import Class Valid CSV")
    void testImportClass() throws IOException {
        // Test constructor :D
        ClassImporter classImporter = new ClassImporter();

        List<ClassRecord> classRecords = classImporter.importClass("test-data/sample-classes-small.csv");

        assertAll(
                () -> assertEquals("COMP1701", classRecords.get(0).getTopicCode()),
                () -> assertEquals("Game Design", classRecords.get(0).getTopicName()),
                () -> assertEquals("In person", classRecords.get(0).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", classRecords.get(0).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, classRecords.get(0).getAvailability().getSemester()),
                () -> assertEquals(1, classRecords.get(0).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", classRecords.get(0).getClassName()),
                () -> assertEquals(1, classRecords.get(0).getClassInstance()),
                () -> assertEquals("2026-07-27", classRecords.get(0).getStartDate().toString()),
                () -> assertEquals("2026-09-14", classRecords.get(0).getEndDate().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                () -> assertEquals("09:00", classRecords.get(0).getStartTime().toString()),
                () -> assertEquals("10:00", classRecords.get(0).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                () -> assertEquals("Festival Tower", classRecords.get(0).getBuilding()),
                () -> assertEquals("506 Computer Lab", classRecords.get(0).getLocation())
        );
    }


    //--- looksLikeHeader() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("No Header")
    void testNoHeader() throws IOException {
        List<ClassRecord> classRecords = ClassImporter.importClass("test-data/sample-classes-no-header.csv");

        assertAll(
                () -> assertEquals("COMP1701", classRecords.get(0).getTopicCode()),
                () -> assertEquals("Game Design", classRecords.get(0).getTopicName()),
                () -> assertEquals("In person", classRecords.get(0).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", classRecords.get(0).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, classRecords.get(0).getAvailability().getSemester()),
                () -> assertEquals(1, classRecords.get(0).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", classRecords.get(0).getClassName()),
                () -> assertEquals(1, classRecords.get(0).getClassInstance()),
                () -> assertEquals("2026-07-27", classRecords.get(0).getStartDate().toString()),
                () -> assertEquals("2026-09-14", classRecords.get(0).getEndDate().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                // Note that 9:00 (in the CSV file) does not follow HH:MM but is still valid (as referenced in Invalid Time)
                () -> assertEquals("09:00", classRecords.get(0).getStartTime().toString()),
                () -> assertEquals("10:00", classRecords.get(0).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                () -> assertEquals("Festival Tower", classRecords.get(0).getBuilding()),
                () -> assertEquals("506 Computer Lab", classRecords.get(0).getLocation())
        );
    }

    //--- validateHeader() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Header (missing column)")
    void testInvalidHeaderMissingColumn() throws IOException {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/missing-header-column.txt")
        );
        assertEquals("CSV header must contain exactly these 8 columns: Topic, Availability, Class, Class instance, Date, Day, Time, Location", exception.getMessage());
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Header (misspelling)")
    void testInvalidHeaderMisspelling() throws IOException {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/misspelled-header.txt")
        );
        assertEquals("CSV header column 3 must be 'Class', found 'BRUH'.", exception.getMessage());
    }


    //--- required() ---//
    @Test
    @Tag("Jay")
    @Tag("Additional")
    @DisplayName("Missing Column")
    void testMissingColumn() throws IOException {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/missing-column.txt")
        );
        assertEquals("Line 2 is invalid: Class is missing on line 2.", exception.getMessage());
    }

    //--- parsePositiveInt() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Negative Integer")
    void testNegativeInteger() throws IOException {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/negative-int.txt")
        );
        assertEquals("Line 2 is invalid: Class instance must be greater than 0.", exception.getMessage());
    }

    //--- parseTopic() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @Tag("Fail")
    @DisplayName("Invalid Topic")
    void testInvalidTopic() throws IOException {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-topic.txt")
        );
        assertEquals("Line 2 is invalid: Topic must contain code and name, e.g. COMP1701 Game Design: COMP1701", exception.getMessage());

        //*** FAILS (no error thrown) given that "Game Design" is separated into Topic code: Game, Topic name: Design. ***//
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-topic-2.txt")
        );
        assertEquals("Line 2 is invalid: Topic must contain code and name, e.g. COMP1701 Game Design: Game Design", exception2.getMessage());
    }

    //--- parseDateRange() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Date Range")
    void testInvalidDateRange() throws IOException {
        // Incorrect format
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-date-range-1.txt")
        );
        assertEquals("Line 2 is invalid: Date must be a range: 27 Jul | 14 Sep", exception.getMessage());

        // Start date later than end date
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-date-range-2.txt")
        );
        assertEquals("Line 2 is invalid: Date of last class must not be before date of first class: 14 Sep - 27 Jul", exception2.getMessage());
    }

    //--- parseDate() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Date")
    void testInvalidDate() throws IOException {
        // Incorrect format
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-date.txt")
        );
        assertEquals("Line 2 is invalid: Date must use yyyy-mm-dd, d MMM, or d MMM yyyy format: 27 July", exception.getMessage());
    }

    //--- parseTimeRange() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Time Range")
    void testInvalidTimeRange() throws IOException {
        // Incorrect format
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-time-range-1.txt")
        );
        assertEquals("Line 2 is invalid: Time must be a range: 09:00 | 10:00", exception.getMessage());

        // Start date later than end date
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-time-range-2.txt")
        );
        assertEquals("Line 2 is invalid: End time must be after start time: 10:00 - 09:00", exception2.getMessage());

        // Start date later than end date
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-time-range-3.txt")
        );
        assertEquals("Line 2 is invalid: End time must be after start time: 10:00 - 10:00", exception3.getMessage());
    }

    //--- parseTime() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Time")
    void testInvalidTime() throws IOException {
        // Incorrect format
        // Note: 9:00 is not the cause of the error (as noted in No Header)
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-time.txt")
        );
        assertEquals("Line 2 is invalid: Time must use HH:mm format: 9:00:00", exception.getMessage());
    }

    //--- parseDay() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Day")
    void testInvalidDay() throws IOException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-day.txt")
        );
        assertEquals("Line 2 is invalid: Day must be Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, or Sunday: Mon", exception.getMessage());
    }

    //--- parseLocation() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Invalid Location")
    void testInvalidLocation() throws IOException {
        // No comma
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-location-1.txt")
        );
        assertEquals("Line 3 is invalid: Location must contain building and room separated by a comma: Festival Tower 506 Computer Lab", exception.getMessage());

        // Missing building
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-location-2.txt")
        );
        assertEquals("Line 3 is invalid: Location must contain both building and room: ,Festival Tower 506 Computer Lab", exception2.getMessage());

        // Missing room
        IllegalArgumentException exception3 = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/invalid-location-3.txt")
        );
        assertEquals("Line 3 is invalid: Location must contain both building and room: Festival Tower 506 Computer Lab,", exception3.getMessage());
    }

    //--- parseCsvLine() ---//
    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("Unclosed Quote")
    void testUnclosedQuote() throws IOException {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/unclosed-quote.txt")
        );
        assertEquals("CSV line has an unclosed quote.", exception.getMessage());
    }

    @Test
    @Tag("Jay")
    @Tag("Core")
    @DisplayName("No Quotes")
    void testNoQuotes() throws IOException {
        // This should throw an error that 9 columns are found given the use of a comma in the location field.
        // If 9 columns are found, CSV parsing works without quotes except when considering the location field.
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ClassImporter.importClass("test-data/no-quotes.txt")
        );
        assertEquals("Line 2 must contain exactly 8 columns, found 9.", exception.getMessage());
    }

    //--- stripBom() ---//
    @Test
    @Tag("Jay")
    @Tag("Additional")
    @DisplayName("CSV with BOM")
    void testWithBom() throws IOException {
        List<ClassRecord> classRecords = ClassImporter.importClass("test-data/with-BOM.txt");

        assertAll(
                () -> assertEquals("COMP1701", classRecords.get(0).getTopicCode()),
                () -> assertEquals("Game Design", classRecords.get(0).getTopicName()),
                () -> assertEquals("In person", classRecords.get(0).getAvailability().getAttendanceMode()),
                () -> assertEquals("Flinders City Campus", classRecords.get(0).getAvailability().getCampus().getDisplayName()),
                () -> assertEquals(2, classRecords.get(0).getAvailability().getSemester()),
                () -> assertEquals(1, classRecords.get(0).getAvailability().getAvailabilityNum()),
                () -> assertEquals("Workshop-1", classRecords.get(0).getClassName()),
                () -> assertEquals(1, classRecords.get(0).getClassInstance()),
                () -> assertEquals("2026-07-27", classRecords.get(0).getStartDate().toString()),
                () -> assertEquals("2026-09-14", classRecords.get(0).getEndDate().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                () -> assertEquals("09:00", classRecords.get(0).getStartTime().toString()),
                () -> assertEquals("10:00", classRecords.get(0).getEndTime().toString()),
                () -> assertEquals(DayOfWeek.MONDAY, classRecords.get(0).getDay()),
                () -> assertEquals("Festival Tower", classRecords.get(0).getBuilding()),
                () -> assertEquals("506 Computer Lab", classRecords.get(0).getLocation())
        );
    }
}