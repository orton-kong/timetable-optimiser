package timetableoptimizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class TimetableExporter {
    public static void exportTimetable(Timetable timetable) throws IOException {
        exportTimetable(timetable, timetable.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".csv");
    }

    public static void exportTimetable(Timetable timetable, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("Topic code,Topic name,Attendance mode,Campus,Semester,Availability number,Class,Class instance,Date of first class,Date of last class,Day,Start time,End time,Building,Room");
            writer.newLine();
            for (ClassRecord r : timetable.getClasses()) {
                writer.write(r.toCsvRow());
                writer.newLine();
            }
            var warnings = ClashDetector.clashDetection(timetable.getClasses(), timetable.isAllowLectureOverlap());
            if (!warnings.isEmpty()) {
                writer.newLine();
                writer.write("Warnings"); writer.newLine();
                for (String warning : warnings) { writer.write('"' + warning.replace("\"", "\"\"") + '"'); writer.newLine(); }
            }
        }
    }
}
