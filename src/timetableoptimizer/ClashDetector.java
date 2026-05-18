package timetableoptimizer;

import java.time.*;
import java.util.*;

public class ClashDetector {
    public static void clashDetection() { }

    public static List<String> clashDetection(List<ClassRecord> records, boolean allowLectureOverlap) {
        List<String> warnings = new ArrayList<>();
        List<ClassRecord> sorted = records.stream()
                .sorted(Comparator.comparing(ClassRecord::getDay).thenComparing(ClassRecord::getStartTime))
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                ClassRecord a = sorted.get(i), b = sorted.get(j);
                if (a.getDay() != b.getDay()) continue;
                if (!dateRangesOverlap(a, b)) continue;
                boolean lectureCanBeWatchedLater = allowLectureOverlap && (a.isLecture() || b.isLecture());
                if (timeOverlap(a, b)) {
                    if (!lectureCanBeWatchedLater) warnings.add("Time clash: " + a.shortSummary() + " overlaps with " + b.shortSummary());
                } else if (!lectureCanBeWatchedLater) {
                    long gap = gapMinutes(a, b);
                    if (gap >= 0 && gap < 30 && a.getAvailability().getCampus() != b.getAvailability().getCampus()) {
                        warnings.add("Insufficient commute gap (" + gap + " minutes): " + a.shortSummary() + " -> " + b.shortSummary());
                    }
                }
            }
        }
        return warnings;
    }

    public static boolean hasHardClash(List<ClassRecord> records, boolean allowLectureOverlap) {
        return !clashDetection(records, allowLectureOverlap).isEmpty();
    }

    private static boolean dateRangesOverlap(ClassRecord a, ClassRecord b) {
        return !a.getEndDate().isBefore(b.getStartDate()) && !b.getEndDate().isBefore(a.getStartDate());
    }

    private static boolean timeOverlap(ClassRecord a, ClassRecord b) {
        return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
    }

    private static long gapMinutes(ClassRecord a, ClassRecord b) {
        ClassRecord first = a.getEndTime().isBefore(b.getEndTime()) ? a : b;
        ClassRecord second = first == a ? b : a;
        if (first.getEndTime().isAfter(second.getStartTime())) return -1;
        return Duration.between(first.getEndTime(), second.getStartTime()).toMinutes();
    }
}
