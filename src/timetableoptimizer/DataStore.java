package timetableoptimizer;

import java.util.*;

public class DataStore {
    private HashMap<String, ClassRecord> classes;
    private HashMap<String, Timetable> timetables;

    public DataStore() {
        classes = new LinkedHashMap<>();
        timetables = new LinkedHashMap<>();
    }

    public HashMap<String, ClassRecord> getClasses() { return classes; }
    public HashMap<String, Timetable> getTimetables() { return timetables; }
}
