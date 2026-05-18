package timetableoptimizer;

import java.util.*;

public class Timetable {
    private String name;
    private List<ClassRecord> classes;
    private boolean allowLectureOverlap;
    private Preference prefrences;

    public Timetable(String name, List<ClassRecord> classes, boolean allowLectureOverlap, Preference prefrences) {
        this.name = name;
        setClasses(classes);
        this.allowLectureOverlap = allowLectureOverlap;
        this.prefrences = prefrences;
    }

    public String getName() { return name; }
    public List<ClassRecord> getClasses() { return classes; }
    public boolean isAllowLectureOverlap() { return allowLectureOverlap; }
    public Preference getPrefrences() { return prefrences; }

    public void setName(String name) { this.name = name; }
    public void setClasses(List<ClassRecord> classes) {
        this.classes = new ArrayList<>();
        if (classes != null) {
            for (ClassRecord record : classes) this.classes.add(record.copy());
        }
    }
    public void setAllowLectureOverlap(boolean allowLectureOverlap) { this.allowLectureOverlap = allowLectureOverlap; }
    public void setPrefrences(Preference prefrences) { this.prefrences = prefrences; }
}
