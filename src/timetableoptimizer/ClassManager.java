package timetableoptimizer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClassManager implements Manager {
    private final DataStore dataStore;
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    public ClassManager(DataStore dataStore) { this.dataStore = dataStore; }

    public ImportResult importFromCsv(String filePath) throws Exception {
        List<ClassRecord> imported = ClassImporter.importClass(filePath);
        int inserted = 0, updated = 0;
        for (ClassRecord incoming : imported) {
            validateRecord(incoming);
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
        ClassRecord before = record.copy();
        try {
            setField(record, field, value);
            validateRecord(record);
        } catch (RuntimeException ex) {
            restore(record, before);
            throw ex;
        }
    }

    @Override public void delete(String ID) {
        if (dataStore.getClasses().remove(ID) == null) throw new IllegalArgumentException("No class exists with ID: " + ID);
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
            String actual = getSearchableFieldValue(r, field).toLowerCase(Locale.ROOT);
            if (!actual.contains(expected)) return false;
        }
        return true;
    }

    private static String getSearchableFieldValue(ClassRecord r, String field) {
        return switch (field) {
            case "topiccode" -> r.getTopicCode();
            case "topicname" -> r.getTopicName();
            case "attendancemode" -> r.getAvailability().getAttendanceMode();
            case "campus" -> r.getAvailability().getCampus().getDisplayName();
            case "semester" -> String.valueOf(r.getAvailability().getSemester());
            case "availabilitynumber", "availabilitynum" -> String.valueOf(r.getAvailability().getAvailabilityNum());
            case "class", "classname" -> r.getClassName();
            case "classinstance" -> String.valueOf(r.getClassInstance());
            case "dateoffirstclass", "startdate" -> dateSearchValue(r.getStartDate());
            case "dateoflastclass", "enddate" -> dateSearchValue(r.getEndDate());
            case "day" -> r.getDay() + " " + ClassRecord.prettyDay(r.getDay());
            case "starttime" -> r.getStartTime().toString();
            case "endtime" -> r.getEndTime().toString();
            case "building" -> r.getBuilding();
            case "room", "location" -> r.getLocation();
            default -> throw new IllegalArgumentException("Unsupported search/edit field: " + field);
        };
    }

    private static String dateSearchValue(LocalDate date) {
        return date + " " + DISPLAY_DATE.format(date) + " " + DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH).format(date);
    }

    private void setField(ClassRecord r, String field, String value) {
        String f = normaliseField(field);
        String trimmed = requireNonBlank(value, field);
        switch (f) {
            case "topiccode" -> r.setTopicCode(trimmed.toUpperCase(Locale.ROOT));
            case "topicname" -> r.setTopicName(trimmed);
            case "attendancemode" -> r.getAvailability().setAttendanceMode(trimmed);
            case "campus" -> r.getAvailability().setCampus(Campus.fromString(trimmed));
            case "semester" -> r.getAvailability().setSemester(Integer.parseInt(trimmed));
            case "availabilitynumber", "availabilitynum" -> r.getAvailability().setAvailabilityNum(Integer.parseInt(trimmed));
            case "class", "classname" -> r.setClassName(trimmed);
            case "classinstance" -> r.setClassInstance(Integer.parseInt(trimmed));
            case "dateoffirstclass", "startdate" -> r.setStartDate(parseFlexibleDate(trimmed, r.getStartDate().getYear()));
            case "dateoflastclass", "enddate" -> r.setEndDate(parseFlexibleDate(trimmed, r.getEndDate().getYear()));
            case "day" -> r.setDay(ClassImporter.parseDay(trimmed));
            case "starttime" -> r.setStartTime(ClassImporter.parseTime(trimmed));
            case "endtime" -> r.setEndTime(ClassImporter.parseTime(trimmed));
            case "building" -> r.setBuilding(trimmed);
            case "room", "location" -> r.setLocation(trimmed);
            default -> throw new IllegalArgumentException("Unsupported field: " + field);
        }
    }

    public static LocalDate parseFlexibleDate(String value, int defaultYear) {
        try { return LocalDate.parse(value.trim()); }
        catch (RuntimeException ignored) { return ClassImporter.parseDate(value.trim(), defaultYear); }
    }

    public static void validateRecord(ClassRecord r) {
        requireNonBlank(r.getTopicCode(), "topic code");
        requireNonBlank(r.getTopicName(), "topic name");
        requireNonBlank(r.getClassName(), "class");
        requireNonBlank(r.getBuilding(), "building");
        requireNonBlank(r.getLocation(), "room");
        if (r.getAvailability() == null) throw new IllegalArgumentException("Availability cannot be blank.");
        r.getAvailability().setAttendanceMode(r.getAvailability().getAttendanceMode());
        r.getAvailability().setCampus(r.getAvailability().getCampus());
        r.getAvailability().setSemester(r.getAvailability().getSemester());
        r.getAvailability().setAvailabilityNum(r.getAvailability().getAvailabilityNum());
        if (r.getClassInstance() <= 0) throw new IllegalArgumentException("Class instance must be greater than 0.");
        if (r.getStartDate() == null || r.getEndDate() == null) throw new IllegalArgumentException("Class dates cannot be blank.");
        if (r.getEndDate().isBefore(r.getStartDate())) throw new IllegalArgumentException("Date of last class cannot be before date of first class.");
        if (r.getDay() == null) throw new IllegalArgumentException("Day cannot be blank.");
        if (r.getStartTime() == null || r.getEndTime() == null) throw new IllegalArgumentException("Class times cannot be blank.");
        if (!r.getEndTime().isAfter(r.getStartTime())) throw new IllegalArgumentException("End time must be after start time.");
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " cannot be blank.");
        return value.trim();
    }

    private void restore(ClassRecord target, ClassRecord source) {
        target.setID(source.getID());
        target.setTopicCode(source.getTopicCode());
        target.setTopicName(source.getTopicName());
        target.setAvailability(new Availability(source.getAvailability()));
        target.setClassName(source.getClassName());
        target.setClassInstance(source.getClassInstance());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setDay(source.getDay());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setBuilding(source.getBuilding());
        target.setLocation(source.getLocation());
    }

    public static String normaliseField(String field) {
        return field == null ? "" : field.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
    }
}
