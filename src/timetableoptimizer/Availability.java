package timetableoptimizer;

import java.util.Objects;

public class Availability {
    private String attendanceMode;
    private Campus campus;
    private int semester;
    private int availabilityNum;

    public Availability(String attendanceMode, Campus campus, int semester, int availabilityNum) {
        this.attendanceMode = attendanceMode;
        this.campus = campus;
        this.semester = semester;
        this.availabilityNum = availabilityNum;
    }

    public String getAttendanceMode() { return attendanceMode; }
    public Campus getCampus() { return campus; }
    public int getSemester() { return semester; }
    public int getAvailabilityNum() { return availabilityNum; }

    public void setAttendanceMode(String attendanceMode) { this.attendanceMode = attendanceMode; }
    public void setCampus(Campus campus) { this.campus = campus; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setAvailabilityNum(int availabilityNum) { this.availabilityNum = availabilityNum; }

    public String display() {
        return attendanceMode + " - " + campus.getDisplayName() + " - S" + semester + " - " + availabilityNum;
    }

    public String key() {
        return attendanceMode.toLowerCase() + "|" + campus + "|" + semester + "|" + availabilityNum;
    }

    public static Availability parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Availability is missing.");
        String[] parts = raw.split(" - ");
        if (parts.length < 4) throw new IllegalArgumentException("Availability must be attendance mode - campus - S1/S2 - number: " + raw);
        String attendanceMode = parts[0].trim();
        String campusText = parts[1].trim();
        String semesterText = parts[2].trim().toUpperCase().replace("SEMESTER", "S");
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
