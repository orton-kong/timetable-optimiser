package timetableoptimizer;

import java.util.*;

public class Availability {
    private String attendanceMode;
    private Campus campus;
    private int semester;
    private int availabilityNum;

    public Availability(String attendanceMode, Campus campus, int semester, int availabilityNum) {
        setAttendanceMode(attendanceMode);
        setCampus(campus);
        setSemester(semester);
        setAvailabilityNum(availabilityNum);
    }

    public Availability(Availability other) {
        this(other.attendanceMode, other.campus, other.semester, other.availabilityNum);
    }

    public String getAttendanceMode() { return attendanceMode; }
    public Campus getCampus() { return campus; }
    public int getSemester() { return semester; }
    public int getAvailabilityNum() { return availabilityNum; }

    public void setAttendanceMode(String attendanceMode) {
        if (attendanceMode == null || attendanceMode.trim().isEmpty()) throw new IllegalArgumentException("Attendance mode cannot be blank.");
        this.attendanceMode = attendanceMode.trim();
    }
    public void setCampus(Campus campus) {
        if (campus == null) throw new IllegalArgumentException("Campus cannot be blank.");
        this.campus = campus;
    }
    public void setSemester(int semester) {
        if (semester != 1 && semester != 2) throw new IllegalArgumentException("Semester must be 1 or 2.");
        this.semester = semester;
    }
    public void setAvailabilityNum(int availabilityNum) {
        if (availabilityNum <= 0) throw new IllegalArgumentException("Availability number must be greater than 0.");
        this.availabilityNum = availabilityNum;
    }

    public String display() {
        return attendanceMode + " - " + campus.getDisplayName() + " - S" + semester + " - " + availabilityNum;
    }

    public String key() {
        return attendanceMode.toLowerCase(Locale.ROOT) + "|" + campus + "|" + semester + "|" + availabilityNum;
    }

    public static Availability parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Availability is missing.");
        String[] parts = raw.split(" - ");
        if (parts.length != 4) throw new IllegalArgumentException("Availability must be attendance mode - campus - S1/S2 - number: " + raw);
        String attendanceMode = parts[0].trim();
        String campusText = parts[1].trim();
        String semesterText = parts[2].trim().toUpperCase(Locale.ROOT).replace("SEMESTER", "S");
        if (!semesterText.startsWith("S")) throw new IllegalArgumentException("Semester must be S1 or S2 in availability: " + raw);
        int semester = Integer.parseInt(semesterText.substring(1).trim());
        int number = Integer.parseInt(parts[3].trim());
        return new Availability(attendanceMode, Campus.fromString(campusText), semester, number);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Availability that)) return false;
        return semester == that.semester && availabilityNum == that.availabilityNum && Objects.equals(attendanceMode, that.attendanceMode) && campus == that.campus;
    }

    @Override public int hashCode() { return Objects.hash(attendanceMode, campus, semester, availabilityNum); }
}
