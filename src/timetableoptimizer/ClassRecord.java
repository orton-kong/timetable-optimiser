package timetableoptimizer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClassRecord {
    private String ID;
    private String topicCode;
    private String topicName;
    private Availability availability;
    private String className;
    private int classInstance;
    private LocalDate startDate;
    private LocalDate endDate;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String building;
    private String location;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public ClassRecord(String ID, String topicCode, String topicName, Availability availability, String className, int classInstance,
                       LocalDate startDate, LocalDate endDate, DayOfWeek day, LocalTime startTime, LocalTime endTime,
                       String building, String location) {
        this.ID = ID;
        this.topicCode = topicCode;
        this.topicName = topicName;
        this.availability = availability;
        this.className = className;
        this.classInstance = classInstance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.building = building;
        this.location = location;
    }

    public ClassRecord(ClassRecord other) {
        this(other.ID, other.topicCode, other.topicName, new Availability(other.availability), other.className,
                other.classInstance, other.startDate, other.endDate, other.day, other.startTime, other.endTime,
                other.building, other.location);
    }

    public String getID() { return ID; }
    public String getTopicCode() { return topicCode; }
    public String getTopicName() { return topicName; }
    public Availability getAvailability() { return availability; }
    public String getClassName() { return className; }
    public int getClassInstance() { return classInstance; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getBuilding() { return building; }
    public String getLocation() { return location; }

    public void setID(String ID) { this.ID = ID; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public void setAvailability(Availability availability) { this.availability = availability; }
    public void setClassName(String className) { this.className = className; }
    public void setClassInstance(int classInstance) { this.classInstance = classInstance; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setDay(DayOfWeek day) { this.day = day; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setBuilding(String building) { this.building = building; }
    public void setLocation(String location) { this.location = location; }

    public String duplicateKey() {
        return String.join("|", topicCode, topicName, availability.key(), className, String.valueOf(classInstance),
                startDate.toString(), endDate.toString(), day.toString()).toLowerCase(Locale.ROOT);
    }

    public String classGroupKey() {
        return String.join("|", topicCode, topicName, availability.key(), className, String.valueOf(classInstance)).toLowerCase(Locale.ROOT);
    }

    public String selectableGroupKey() {
        return String.join("|", topicCode, className, String.valueOf(classInstance), availability.key()).toLowerCase(Locale.ROOT);
    }

    public String classFormatKey() {
        return (topicCode + "|" + className).toLowerCase(Locale.ROOT);
    }

    public boolean isLecture() { return className.toLowerCase(Locale.ROOT).contains("lecture"); }

    public String shortSummary() {
        return String.format("[%s] %s %s | %s | %s #%d", ID, topicCode, topicName, availability.display(), className, classInstance);
    }

    public String fullSummary() {
        return String.format("[%s] %s %s | %s | %s #%d | %s - %s | %s | %s - %s | %s, %s",
                ID, topicCode, topicName, availability.display(), className, classInstance,
                DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate), prettyDay(day),
                TIME_FORMAT.format(startTime), TIME_FORMAT.format(endTime), building, location);
    }

    public String toCsvRow() {
        return String.join(",",
                csv(topicCode),
                csv(topicName),
                csv(availability.getAttendanceMode()),
                csv(availability.getCampus().getDisplayName()),
                String.valueOf(availability.getSemester()),
                String.valueOf(availability.getAvailabilityNum()),
                csv(className),
                String.valueOf(classInstance),
                csv(DATE_FORMAT.format(startDate)),
                csv(DATE_FORMAT.format(endDate)),
                csv(prettyDay(day)),
                csv(TIME_FORMAT.format(startTime)),
                csv(TIME_FORMAT.format(endTime)),
                csv(building),
                csv(location));
    }

    public ClassRecord copy() { return new ClassRecord(this); }

    private static String csv(String value) {
        String v = value == null ? "" : value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }

    public static String prettyDay(DayOfWeek day) {
        String lower = day.toString().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
