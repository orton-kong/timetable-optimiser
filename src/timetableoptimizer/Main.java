package timetableoptimizer;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private final DataStore dataStore = new DataStore();
    private final ClassManager classManager = new ClassManager(dataStore);
    private final TimetableManager timetableManager = new TimetableManager(dataStore);
    private final Scanner scanner = new Scanner(System.in);
    private final LastGenerationSettings lastSettings = new LastGenerationSettings();

    public static void main(String[] args) { new Main().run(); }

    private void run() {
        ConsoleStyle.title();
        ConsoleStyle.success("Welcome to Timetable Optimiser. Console input/output only. No GUI required.");
        boolean running = true;
        while (running) {
            try {
                printMainMenu();
                String choice = prompt("Choose an option");
                switch (choice) {
                    case "1" -> importClasses();
                    case "2" -> classManager.listAll();
                    case "3" -> classManager.viewAll();
                    case "4" -> searchClasses();
                    case "5" -> editClass();
                    case "6" -> deleteClass();
                    case "7" -> generateTimetable();
                    case "8" -> timetableManager.listAll();
                    case "9" -> viewTimetable();
                    case "10" -> editTimetable();
                    case "11" -> deleteTimetable();
                    case "12" -> exportTimetable();
                    case "0" -> { ConsoleStyle.success("Goodbye."); running = false; }
                    default -> ConsoleStyle.warn("Please enter a valid menu option.");
                }
            } catch (Exception ex) {
                ConsoleStyle.error("Error: " + ex.getMessage());
            }
        }
    }

    private void printMainMenu() {
        ConsoleStyle.heading("Main Menu");
        System.out.println("1. Import classes from CSV");
        System.out.println("2. Browse classes");
        System.out.println("3. View classes");
        System.out.println("4. Search classes");
        System.out.println("5. Edit class");
        System.out.println("6. Delete class");
        System.out.println("7. Generate timetable");
        System.out.println("8. Browse timetables");
        System.out.println("9. View timetable");
        System.out.println("10. Edit timetable by swapping class instance");
        System.out.println("11. Delete timetable");
        System.out.println("12. Export timetable");
        System.out.println("0. Exit");
    }

    private void importClasses() throws Exception {
        ConsoleStyle.heading("Import Classes");
        String path = prompt("CSV file path");
        ImportResult result = classManager.importFromCsv(path);
        ConsoleStyle.success("Import complete. New records: " + result.inserted() + ". Updated records: " + result.updated() + ".");
    }

    private void searchClasses() {
        ConsoleStyle.heading("Search Classes");
        System.out.println("Allowed criteria: topicCode, topicName, attendanceMode, campus, semester, availabilityNumber, class, classInstance, startDate, endDate, day, startTime, endTime, building, room");
        System.out.println("Leave all values blank to show all records.");
        Map<String, String> criteria = new LinkedHashMap<>();
        for (String field : allowedFields()) {
            String value = promptOptional(field);
            if (!value.isBlank()) criteria.put(field, value);
        }
        classManager.printSearch(criteria);
    }

    private void editClass() {
        ConsoleStyle.heading("Edit Class");
        classManager.viewAll();
        String id = prompt("Class ID to edit");
        String field = prompt("Field to edit (same allowed fields as search)");
        validateField(field);
        String value = prompt("New value");
        if (confirm("WARNING: This will permanently edit the selected class record. Continue?")) {
            classManager.edit(id, field, value);
            ConsoleStyle.success("Class updated.");
        } else ConsoleStyle.warn("Edit cancelled.");
    }

    private void deleteClass() {
        ConsoleStyle.heading("Delete Class");
        classManager.viewAll();
        String id = prompt("Class ID to delete");
        if (confirm("WARNING: This will delete the selected class record and remove it from existing timetables. Continue?")) {
            classManager.delete(id);
            ConsoleStyle.success("Class deleted.");
        } else ConsoleStyle.warn("Delete cancelled.");
    }

    private void generateTimetable() {
        ConsoleStyle.heading("Generate Timetable");
        String name = promptWithDefault("Timetable name (unique; leave blank to auto-generate)", lastSettings.name);
        int semester = readSemester();
        List<String> topics = readTopics();
        if (topics.isEmpty()) throw new IllegalArgumentException("Selecting no topics is invalid.");
        List<Campus> campuses = readCampuses();
        boolean lectureOverlap = readBoolean("Allow lecture overlap?", lastSettings.lectureOverlap);
        List<Preferences> preferences = readPreferences();

        lastSettings.name = name;
        lastSettings.semester = semester;
        lastSettings.topics = new ArrayList<>(topics);
        lastSettings.campuses = new ArrayList<>(campuses);
        lastSettings.lectureOverlap = lectureOverlap;
        lastSettings.preferences = new ArrayList<>(preferences);

        Timetable timetable = timetableManager.generate(name, semester, topics, campuses, lectureOverlap, new Preference(preferences));
        ConsoleStyle.success("Generated timetable: " + timetable.getName());
        timetableManager.view(timetable.getName());
    }

    private int readSemester() {
        String current = lastSettings.semester == 0 ? "both" : String.valueOf(lastSettings.semester);
        while (true) {
            String raw = promptWithDefault("Semester (1, 2, or both)", current).trim().toLowerCase(Locale.ROOT);
            if (raw.equals("both") || raw.equals("0") || raw.isBlank()) return 0;
            if (raw.equals("1") || raw.equals("2")) return Integer.parseInt(raw);
            ConsoleStyle.warn("Semester must be 1, 2, or both.");
        }
    }

    private List<String> readTopics() {
        List<String> importedTopics = dataStore.getClasses().values().stream().map(ClassRecord::getTopicCode).distinct().sorted().toList();
        if (importedTopics.isEmpty()) throw new IllegalArgumentException("Import class data before generating a timetable.");
        System.out.println("Imported topics: " + String.join(", ", importedTopics));
        String def = lastSettings.topics.isEmpty() ? "" : String.join(",", lastSettings.topics);
        String raw = promptWithDefault("Topics to include (comma-separated topic codes; no blank selection allowed)", def);
        List<String> selected = splitCsvInput(raw).stream().map(String::toUpperCase).toList();
        for (String topic : selected) if (!importedTopics.contains(topic)) throw new IllegalArgumentException("Topic is not currently imported: " + topic);
        return selected;
    }

    private List<Campus> readCampuses() {
        String def = lastSettings.campuses.stream().map(Enum::name).collect(Collectors.joining(","));
        String raw = promptWithDefault("Campuses (Bedford, Tonsley, City; comma-separated)", def);
        List<Campus> campuses = new ArrayList<>();
        for (String item : splitCsvInput(raw)) campuses.add(Campus.fromString(item));
        return campuses.stream().distinct().toList();
    }

    private List<Preferences> readPreferences() {
        System.out.println("Preference numbers, highest to lowest. Blank is valid.");
        System.out.println("1 Bedford Park, 2 Tonsley, 3 Flinders City Campus, 4 all at same campus, 5 mornings, 6 afternoons,");
        System.out.println("7 Mondays, 8 Tuesdays, 9 Wednesdays, 10 Thursdays, 11 Fridays, 12 evenly spread, 13 compact classes");
        String def = lastSettings.preferences.stream().map(p -> String.valueOf(p.ordinal() + 1)).collect(Collectors.joining(","));
        String raw = promptWithDefault("Ordered preferences (comma-separated numbers)", def);
        if (raw.isBlank()) return List.of();
        List<Preferences> preferences = new ArrayList<>();
        for (String item : splitCsvInput(raw)) preferences.add(Preferences.fromMenuNumber(Integer.parseInt(item)));
        return preferences;
    }

    private void viewTimetable() {
        ConsoleStyle.heading("View Timetable");
        timetableManager.listAll();
        String name = prompt("Timetable name");
        timetableManager.view(name);
    }

    private void editTimetable() {
        ConsoleStyle.heading("Edit Timetable");
        timetableManager.listAll();
        String name = prompt("Timetable name");
        timetableManager.view(name);
        String oldId = prompt("Class record ID in timetable to swap");
        List<ClassRecord> options = timetableManager.findSwapOptions(name, oldId);
        if (options.isEmpty()) throw new IllegalArgumentException("No alternative instances exist for the same topic and class.");
        ClassManager.printView(options);
        String newId = prompt("Replacement class record ID");
        List<String> warnings = timetableManager.swapClassInstance(name, oldId, newId, false);
        if (!warnings.isEmpty()) {
            ConsoleStyle.warn("The swap creates a clash or invalid commute gap:");
            warnings.forEach(w -> System.out.println("  - " + w));
            if (!confirm("WARNING: Complete this timetable edit anyway?")) { ConsoleStyle.warn("Timetable edit cancelled."); return; }
            timetableManager.swapClassInstance(name, oldId, newId, true);
        }
        ConsoleStyle.success("Timetable updated.");
    }

    private void deleteTimetable() {
        ConsoleStyle.heading("Delete Timetable");
        timetableManager.listAll();
        String name = prompt("Timetable name to delete");
        if (confirm("WARNING: This will permanently delete the selected timetable. Continue?")) {
            timetableManager.delete(name);
            ConsoleStyle.success("Timetable deleted.");
        } else ConsoleStyle.warn("Delete cancelled.");
    }

    private void exportTimetable() throws Exception {
        ConsoleStyle.heading("Export Timetable");
        timetableManager.listAll();
        String name = prompt("Timetable name to export");
        String path = prompt("Output CSV path");
        timetableManager.export(name, path);
        ConsoleStyle.success("Timetable exported to " + path);
    }

    private boolean readBoolean(String label, boolean defaultValue) {
        String raw = promptWithDefault(label + " (yes/no)", defaultValue ? "yes" : "no").trim().toLowerCase(Locale.ROOT);
        return raw.startsWith("y");
    }

    private boolean confirm(String warning) {
        ConsoleStyle.warn(warning);
        String answer = prompt("Type YES to confirm");
        return answer.equals("YES");
    }

    private String prompt(String label) { System.out.print(ConsoleStyle.BOLD + label + ": " + ConsoleStyle.RESET); return scanner.nextLine().trim(); }
    private String promptOptional(String label) { System.out.print(label + " (optional): "); return scanner.nextLine().trim(); }
    private String promptWithDefault(String label, String defaultValue) {
        System.out.print(ConsoleStyle.BOLD + label + (defaultValue == null || defaultValue.isBlank() ? "" : " [" + defaultValue + "]") + ": " + ConsoleStyle.RESET);
        String raw = scanner.nextLine().trim();
        return raw.isBlank() && defaultValue != null ? defaultValue : raw;
    }

    private List<String> splitCsvInput(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private List<String> allowedFields() {
        return List.of("topicCode", "topicName", "attendanceMode", "campus", "semester", "availabilityNumber", "class", "classInstance", "startDate", "endDate", "day", "startTime", "endTime", "building", "room");
    }

    private void validateField(String field) {
        String normalised = ClassManager.normaliseField(field);
        boolean ok = allowedFields().stream().map(ClassManager::normaliseField).anyMatch(f -> f.equals(normalised));
        if (!ok) throw new IllegalArgumentException("Additional search/edit criteria are not permitted: " + field);
    }
}
