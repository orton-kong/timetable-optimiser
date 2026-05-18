package timetableoptimizer;

import java.time.*;
import java.util.*;

public class ClassManager implements Manager {
    private DataStore dataStore;

    public ClassManager(DataStore dataStore) { this.dataStore = dataStore; }

    public ImportResult importFromCsv(String filePath) throws Exception {
        List<ClassRecord> imported = ClassImporter.importClass(filePath);
        int inserted = 0, updated = 0;
        for (ClassRecord incoming : imported) {
            Optional<ClassRecord> duplicate = dataStore.getClasses().values().stream()
                    .filter(existing -> existing.duplicateKey().equals(incoming.duplicateKey()))
                    .findFirst();
            if (duplicate.isPresent()) {
                ClassRecord target = duplicate.get();
                target.setStartTime(incoming.getStartTime());
                target.setEndTime(incoming.getEndTime());
                target.setBuilding(incoming.getBuilding());
                target.setLocation(incoming.getLocation());
                updated++;
            } else {
                while (dataStore.getClasses().containsKey(incoming.getID())) incoming.setID(UUID.randomUUID().toString().substring(0, 8));
                dataStore.getClasses().put(incoming.getID(), incoming);
                inserted++;
            }
        }
        return new ImportResult(inserted, updated);
    }

    @Override public void listAll() { printBrowse(dataStore.getClasses().values()); }

    public void viewAll() { printView(dataStore.getClasses().values()); }

    public List<ClassRecord> search(Map<String, String> searchCriteria) {
        return dataStore.getClasses().values().stream().filter(r -> matchesAll(r, searchCriteria)).toList();
    }

    public void printSearch(Map<String, String> searchCriteria) { printView(search(searchCriteria)); }

    @Override public void edit(String ID, String field, String value) {
        ClassRecord record = dataStore.getClasses().get(ID);
        if (record == null) throw new IllegalArgumentException("No class exists with ID: " + ID);
        setField(record, field, value);
    }

    @Override public void delete(String ID) {
        if (dataStore.getClasses().remove(ID) == null) throw new IllegalArgumentException("No class exists with ID: " + ID);
        for (Timetable t : dataStore.getTimetables().values()) t.getClasses().removeIf(c -> c.getID().equals(ID));
    }

    public ClassRecord findById(String id) { return dataStore.getClasses().get(id); }

    private void printBrowse(Collection<ClassRecord> records) {
        Map<String, List<ClassRecord>> grouped = groupByCombinedClass(records);
        if (grouped.isEmpty()) { ConsoleStyle.warn("No class records available."); return; }
        for (List<ClassRecord> group : grouped.values()) {
            ClassRecord r = group.get(0);
            System.out.printf("%s%s%s [%s] %s | %s | Instance %d | %d date record(s)%n",
                    ConsoleStyle.BOLD, r.getTopicCode(), ConsoleStyle.RESET, r.getTopicName(), r.getAvailability().display(), r.getClassName(), r.getClassInstance(), group.size());
        }
    }

    public static void printView(Collection<ClassRecord> records) {
        Map<String, List<ClassRecord>> grouped = groupByCombinedClass(records);
        if (grouped.isEmpty()) { ConsoleStyle.warn("No matching class records."); return; }
        for (List<ClassRecord> group : grouped.values()) {
            ClassRecord head = group.get(0);
            ConsoleStyle.heading(head.getTopicCode() + " " + head.getTopicName() + " | " + head.getAvailability().display() + " | " + head.getClassName() + " #" + head.getClassInstance());
            group.stream().sorted(Comparator.comparing(ClassRecord::getStartDate).thenComparing(ClassRecord::getDay).thenComparing(ClassRecord::getStartTime))
                    .forEach(r -> System.out.println("  " + r.fullSummary()));
        }
    }

    public static Map<String, List<ClassRecord>> groupByCombinedClass(Collection<ClassRecord> records) {
        Map<String, List<ClassRecord>> grouped = new LinkedHashMap<>();
        for (ClassRecord r : records) grouped.computeIfAbsent(r.classGroupKey(), k -> new ArrayList<>()).add(r);
        return grouped;
    }

    private boolean matchesAll(ClassRecord r, Map<String, String> criteria) {
        for (Map.Entry<String, String> e : criteria.entrySet()) {
            String field = normaliseField(e.getKey());
            String expected = e.getValue().trim().toLowerCase(Locale.ROOT);
            if (expected.isEmpty()) continue;
            String actual = getFieldValue(r, field).toLowerCase(Locale.ROOT);
            if (!actual.contains(expected)) return false;
        }
        return true;
    }

    private static String getFieldValue(ClassRecord r, String field) {
        return switch (field) {
            case "topiccode" -> r.getTopicCode();
            case "topicname" -> r.getTopicName();
            case "attendancemode" -> r.getAvailability().getAttendanceMode();
            case "campus" -> r.getAvailability().getCampus().getDisplayName();
            case "semester" -> String.valueOf(r.getAvailability().getSemester());
            case "availabilitynumber", "availabilitynum" -> String.valueOf(r.getAvailability().getAvailabilityNum());
            case "class", "classname" -> r.getClassName();
            case "classinstance" -> String.valueOf(r.getClassInstance());
            case "dateoffirstclass", "startdate" -> r.getStartDate().toString();
            case "dateoflastclass", "enddate" -> r.getEndDate().toString();
            case "day" -> r.getDay().toString();
            case "starttime" -> r.getStartTime().toString();
            case "endtime" -> r.getEndTime().toString();
            case "building" -> r.getBuilding();
            case "room", "location" -> r.getLocation();
            default -> throw new IllegalArgumentException("Unsupported search/edit field: " + field);
        };
    }

    private void setField(ClassRecord r, String field, String value) {
        String f = normaliseField(field);
        switch (f) {
            case "topiccode" -> r.setTopicCode(value.trim());
            case "topicname" -> r.setTopicName(value.trim());
            case "attendancemode" -> r.getAvailability().setAttendanceMode(value.trim());
            case "campus" -> r.getAvailability().setCampus(Campus.fromString(value));
            case "semester" -> r.getAvailability().setSemester(Integer.parseInt(value.trim()));
            case "availabilitynumber", "availabilitynum" -> r.getAvailability().setAvailabilityNum(Integer.parseInt(value.trim()));
            case "class", "classname" -> r.setClassName(value.trim());
            case "classinstance" -> r.setClassInstance(Integer.parseInt(value.trim()));
            case "dateoffirstclass", "startdate" -> r.setStartDate(LocalDate.parse(value.trim()));
            case "dateoflastclass", "enddate" -> r.setEndDate(LocalDate.parse(value.trim()));
            case "day" -> r.setDay(DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ROOT)));
            case "starttime" -> r.setStartTime(LocalTime.parse(value.trim()));
            case "endtime" -> r.setEndTime(LocalTime.parse(value.trim()));
            case "building" -> r.setBuilding(value.trim());
            case "room", "location" -> r.setLocation(value.trim());
            default -> throw new IllegalArgumentException("Unsupported field: " + field);
        }
    }

    public static String normaliseField(String field) {
        return field == null ? "" : field.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
    }
}
