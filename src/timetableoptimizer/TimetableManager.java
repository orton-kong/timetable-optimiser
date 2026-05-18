package timetableoptimizer;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableManager implements Manager {
    private final DataStore dataStore;

    public TimetableManager(DataStore dataStore) { this.dataStore = dataStore; }

    public Timetable generate(String name, int semester, List<String> topics, List<Campus> campus, boolean lectureOverlap, Preference preferences) {
        Timetable timetable = TimetableGenerator.generateTimetable(name, semester, topics, campus, lectureOverlap, preferences, dataStore);
        dataStore.getTimetables().put(timetable.getName(), timetable);
        return timetable;
    }

    @Override public void listAll() {
        if (dataStore.getTimetables().isEmpty()) { ConsoleStyle.warn("No timetables available."); return; }
        for (Timetable t : dataStore.getTimetables().values()) {
            String topics = t.getClasses().stream().map(ClassRecord::getTopicCode).distinct().collect(Collectors.joining(", "));
            System.out.printf("%s%s%s | %d class records | Topics: %s | Lecture overlap: %s%n",
                    ConsoleStyle.BOLD, t.getName(), ConsoleStyle.RESET, t.getClasses().size(), topics, t.isAllowLectureOverlap() ? "Yes" : "No");
        }
    }

    public void view(String timetableName) {
        Timetable t = getTimetable(timetableName);
        ConsoleStyle.heading("Timetable: " + t.getName());
        ClassManager.printView(t.getClasses());
        List<String> warnings = ClashDetector.clashDetection(t.getClasses(), t.isAllowLectureOverlap());
        if (!warnings.isEmpty()) {
            ConsoleStyle.warn("Warnings:");
            warnings.forEach(w -> System.out.println("  - " + w));
        } else ConsoleStyle.success("No time clashes or invalid commute gaps detected.");
    }

    @Override public void edit(String timetableName, String field, String value) {
        throw new UnsupportedOperationException("Use swapClassInstance for timetable edits.");
    }

    public List<ClassRecord> findSwapOptions(String timetableName, String recordId) {
        Timetable t = getTimetable(timetableName);
        ClassRecord selected = t.getClasses().stream().filter(r -> r.getID().equals(recordId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("That class record is not in the timetable."));
        return dataStore.getClasses().values().stream()
                .filter(r -> r.getTopicCode().equalsIgnoreCase(selected.getTopicCode()))
                .filter(r -> r.getClassName().equalsIgnoreCase(selected.getClassName()))
                .filter(r -> r.getClassInstance() != selected.getClassInstance())
                .toList();
    }

    public List<String> swapClassInstance(String timetableName, String oldRecordId, String newRecordId, boolean confirmDespiteWarnings) {
        Timetable t = getTimetable(timetableName);
        ClassRecord oldRecord = t.getClasses().stream().filter(r -> r.getID().equals(oldRecordId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Old class record is not in the timetable."));
        ClassRecord newRecord = dataStore.getClasses().get(newRecordId);
        if (newRecord == null) throw new IllegalArgumentException("No class exists with ID: " + newRecordId);
        if (!oldRecord.getTopicCode().equalsIgnoreCase(newRecord.getTopicCode()) || !oldRecord.getClassName().equalsIgnoreCase(newRecord.getClassName())) {
            throw new IllegalArgumentException("Timetable edits may only swap to another class instance with the same class and same topic.");
        }
        if (oldRecord.getClassInstance() == newRecord.getClassInstance()) {
            throw new IllegalArgumentException("Choose a different class instance. The selected replacement is not another class instance.");
        }
        List<ClassRecord> replacementGroup = dataStore.getClasses().values().stream()
                .filter(r -> r.selectableGroupKey().equals(newRecord.selectableGroupKey())).toList();
        List<ClassRecord> proposed = new ArrayList<>(t.getClasses());
        proposed.removeIf(r -> r.selectableGroupKey().equals(oldRecord.selectableGroupKey()));
        proposed.addAll(replacementGroup);
        List<String> warnings = ClashDetector.clashDetection(proposed, t.isAllowLectureOverlap());
        if (!warnings.isEmpty() && !confirmDespiteWarnings) return warnings;
        t.setClasses(proposed);
        return warnings;
    }

    @Override public void delete(String timetableName) {
        String actualName = findActualTimetableName(timetableName);
        if (actualName == null) throw new IllegalArgumentException("No timetable exists with name: " + timetableName);
        dataStore.getTimetables().remove(actualName);
    }

    public void export(String timetableName, String filePath) throws Exception {
        TimetableExporter.exportTimetable(getTimetable(timetableName), filePath);
    }

    private Timetable getTimetable(String timetableName) {
        String actualName = findActualTimetableName(timetableName);
        if (actualName == null) throw new IllegalArgumentException("No timetable exists with name: " + timetableName);
        return dataStore.getTimetables().get(actualName);
    }

    private String findActualTimetableName(String timetableName) {
        if (timetableName == null) return null;
        return dataStore.getTimetables().keySet().stream()
                .filter(name -> name.equalsIgnoreCase(timetableName.trim()))
                .findFirst()
                .orElse(null);
    }
}
