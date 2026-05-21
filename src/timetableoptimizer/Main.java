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
                if (running) pauseBeforeMainMenu();
            } catch (Exception ex) {
                ConsoleStyle.error("Error: " + ex.getMessage());
                if (running) pauseBeforeMainMenu();
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
        System.out.println("Allowed criteria: topic code, topic name, attendance mode, campus, semester, availability number, class, class instance, date of first class, date of last class, day, start time, end time, building, room");
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
        if (dataStore.getClasses().isEmpty()) {
            ConsoleStyle.warn("No class records available to edit.");
            return;
        }

        classManager.viewAll();
        String id = prompt("Class ID to edit");
        ClassRecord selected = dataStore.getClasses().get(id);
        if (selected == null) throw new IllegalArgumentException("No class exists with ID: " + id);

        ConsoleStyle.heading("Editing Class ID: " + id);
        System.out.println("Type a field number/name to edit it, or type 0 / exit / back when finished.");

        boolean editing = true;
        while (editing) {
            System.out.println();
            System.out.println(ConsoleStyle.BOLD + "Current record:" + ConsoleStyle.RESET);
            System.out.println("  " + selected.fullSummary());
            printEditableFields();

            String fieldChoice = prompt("Field to edit");
            if (isExitEditCommand(fieldChoice)) {
                editing = false;
                continue;
            }

            try {
                String field = resolveEditableField(fieldChoice);
                String value = prompt("New value for " + field);
                if (confirm("WARNING: This will permanently edit this class record. Continue?")) {
                    classManager.edit(id, field, value);
                    selected = dataStore.getClasses().get(id);
                    ConsoleStyle.success("Class updated. You are still editing the same class record.");
                } else {
                    ConsoleStyle.warn("Edit cancelled. You are still editing the same class record.");
                }
            } catch (Exception ex) {
                ConsoleStyle.error("Edit not applied: " + ex.getMessage());
                ConsoleStyle.warn("You are still editing the same class record.");
            }
        }

        ConsoleStyle.success("Exited editing mode for class ID: " + id);
    }

    private void deleteClass() {
        ConsoleStyle.heading("Delete Class");
        if (dataStore.getClasses().isEmpty()) {
            ConsoleStyle.warn("No class records available to delete.");
            return;
        }
        classManager.viewAll();
        String id = prompt("Class ID to delete");
        if (confirm("WARNING: This will permanently delete the selected class record from imported class data. Existing timetables remain as generated snapshots. Continue?")) {
            classManager.delete(id);
            ConsoleStyle.success("Class deleted.");
        } else ConsoleStyle.warn("Delete cancelled.");
    }

    private void generateTimetable() {
        ConsoleStyle.heading("Generate Timetable");
        if (dataStore.getClasses().isEmpty()) {
            ConsoleStyle.warn("Import class data before generating a timetable.");
            return;
        }

        if (!lastSettings.getName().isBlank()) {
            System.out.println("Last timetable name: " + lastSettings.getName() + " (blank still auto-generates a new unique name)");
        }
        String name = prompt("Timetable name (unique; leave blank to auto-generate)");
        int semester = readSemester();
        List<String> topics = readTopics();
        List<Campus> campuses = readCampuses();
        boolean lectureOverlap = readBoolean("Allow lecture overlap?", lastSettings.isLectureOverlap());
        List<Enum<?>> preferences = readPreferences();

        lastSettings.setSemester(semester);
        lastSettings.setTopics(new ArrayList<>(topics));
        lastSettings.setCampuses(new ArrayList<>(campuses));
        lastSettings.setLectureOverlap(lectureOverlap);
        lastSettings.setPreferences(new ArrayList<>(preferences));

        Timetable timetable = timetableManager.generate(name, semester, topics, campuses, lectureOverlap, new Preference(preferences));
        lastSettings.setName(timetable.getName());
        ConsoleStyle.success("Generated timetable: " + timetable.getName());
        timetableManager.view(timetable.getName());
    }

    private int readSemester() {
        String current = lastSettings.getSemester() == 0 ? "both" : String.valueOf(lastSettings.getSemester());
        while (true) {
            String raw = promptWithDefault("Semester (1, 2, or both)", current).trim().toLowerCase(Locale.ROOT);
            if (raw.equals("both") || raw.equals("0") || raw.isBlank()) return 0;
            if (raw.equals("1") || raw.equals("2")) return Integer.parseInt(raw);
            ConsoleStyle.warn("Semester must be 1, 2, or both.");
        }
    }

    private List<String> readTopics() {
        List<String> importedTopics = dataStore.getClasses().values().stream()
                .map(r -> r.getTopicCode().toUpperCase(Locale.ROOT))
                .distinct().sorted().toList();
        System.out.println("Imported topics: " + String.join(", ", importedTopics));
        String def = lastSettings.getTopics().isEmpty() ? "" : String.join(",", lastSettings.getTopics());
        while (true) {
            String raw = promptWithDefault("Topics to include (comma-separated topic codes; no blank selection allowed)", def);
            List<String> selected = splitCsvInput(raw).stream().map(s -> s.toUpperCase(Locale.ROOT)).distinct().toList();
            if (selected.isEmpty()) {
                ConsoleStyle.warn("Selecting no topics is invalid.");
                continue;
            }
            Optional<String> invalid = selected.stream().filter(topic -> !importedTopics.contains(topic)).findFirst();
            if (invalid.isPresent()) {
                ConsoleStyle.warn("Topic is not currently imported: " + invalid.get());
                continue;
            }
            return selected;
        }
    }

    private List<Campus> readCampuses() {
        String def = lastSettings.getCampuses().stream().map(Enum::name).collect(Collectors.joining(","));
        while (true) {
            try {
                String raw = promptWithDefault("Campuses (Bedford, Tonsley, City; comma-separated)", def);
                List<Campus> campuses = new ArrayList<>();
                for (String item : splitCsvInput(raw)) campuses.add(Campus.fromString(item));
                campuses = campuses.stream().distinct().toList();
                if (campuses.isEmpty()) {
                    ConsoleStyle.warn("At least one campus must be selected.");
                    continue;
                }
                return campuses;
            } catch (Exception ex) {
                ConsoleStyle.warn(ex.getMessage());
            }
        }
    }

    private List<Enum<?>> readPreferences() {
        System.out.println("Preference numbers, highest to lowest. Blank is valid.");
        System.out.println("1 Bedford Park, 2 Tonsley, 3 Flinders City Campus, 4 all at same campus, 5 mornings, 6 afternoons,");
        System.out.println("7 Mondays, 8 Tuesdays, 9 Wednesdays, 10 Thursdays, 11 Fridays, 12 evenly spread, 13 compact classes");
        String def = lastSettings.getPreferences().stream().map(p -> String.valueOf(preferenceMenuNumber(p))).collect(Collectors.joining(","));
        while (true) {
            try {
                String raw = promptWithDefault("Ordered preferences (comma-separated numbers)", def);
                if (raw.isBlank()) return List.of();
                LinkedHashSet<Enum<?>> preferences = new LinkedHashSet<>();
                for (String item : splitCsvInput(raw)) preferences.add(preferenceFromMenuNumber(Integer.parseInt(item)));
                return new ArrayList<>(preferences);
            } catch (Exception ex) {
                ConsoleStyle.warn("Preferences must be blank or comma-separated numbers from 1 to 13.");
            }
        }
    }

    private Enum<?> preferenceFromMenuNumber(int number) {
        return switch (number) {
            case 1 -> LocationPreferences.BEDFORD;
            case 2 -> LocationPreferences.TONSLEY;
            case 3 -> LocationPreferences.CITY;
            case 4 -> LocationPreferences.ALL_AT_SAME_CAMPUS;
            case 5 -> TimeOfDayPreferences.MORNING;
            case 6 -> TimeOfDayPreferences.AFTERNOON;
            case 7 -> DayPreferences.MONDAY;
            case 8 -> DayPreferences.TUESDAY;
            case 9 -> DayPreferences.WEDNESDAY;
            case 10 -> DayPreferences.THURSDAY;
            case 11 -> DayPreferences.FRIDAY;
            case 12 -> ClassSpreadPreferences.EVEN_SPREAD;
            case 13 -> ClassSpreadPreferences.COMPACT_SPREAD;
            default -> throw new IllegalArgumentException("Invalid preference number.");
        };
    }

    private int preferenceMenuNumber(Enum<?> preference) {
        if (preference == LocationPreferences.BEDFORD) return 1;
        if (preference == LocationPreferences.TONSLEY) return 2;
        if (preference == LocationPreferences.CITY) return 3;
        if (preference == LocationPreferences.ALL_AT_SAME_CAMPUS) return 4;
        if (preference == TimeOfDayPreferences.MORNING) return 5;
        if (preference == TimeOfDayPreferences.AFTERNOON) return 6;
        if (preference == DayPreferences.MONDAY) return 7;
        if (preference == DayPreferences.TUESDAY) return 8;
        if (preference == DayPreferences.WEDNESDAY) return 9;
        if (preference == DayPreferences.THURSDAY) return 10;
        if (preference == DayPreferences.FRIDAY) return 11;
        if (preference == ClassSpreadPreferences.EVEN_SPREAD) return 12;
        if (preference == ClassSpreadPreferences.COMPACT_SPREAD) return 13;
        throw new IllegalArgumentException("Unsupported preference type: " + preference);
    }

    private void viewTimetable() {
        ConsoleStyle.heading("View Timetable");
        if (!hasTimetablesOrAbort("view")) return;

        timetableManager.listAll();
        String name = prompt("Timetable name");
        timetableManager.view(name);
    }

    private void editTimetable() {
        ConsoleStyle.heading("Edit Timetable");
        if (!hasTimetablesOrAbort("edit")) return;

        timetableManager.listAll();
        String name = prompt("Timetable name");
        timetableManager.view(name);
        String oldId = prompt("Class record ID in timetable to swap");
        List<ClassRecord> options = timetableManager.findSwapOptions(name, oldId);
        if (options.isEmpty()) throw new IllegalArgumentException("No alternative instances exist for the same topic and class.");
        ConsoleStyle.warn("Selecting any record ID from an alternative instance will swap the whole matching class instance group, including all date records for that instance.");
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
        if (!hasTimetablesOrAbort("delete")) return;

        timetableManager.listAll();
        String name = prompt("Timetable name to delete");
        if (confirm("WARNING: This will permanently delete the selected timetable. Continue?")) {
            timetableManager.delete(name);
            ConsoleStyle.success("Timetable deleted.");
        } else ConsoleStyle.warn("Delete cancelled.");
    }

    private void exportTimetable() throws Exception {
        ConsoleStyle.heading("Export Timetable");
        if (!hasTimetablesOrAbort("export")) return;

        timetableManager.listAll();
        String name = prompt("Timetable name to export");
        String path = prompt("Output CSV path");
        timetableManager.export(name, path);
        ConsoleStyle.success("Timetable exported to " + path);
    }

    private boolean hasTimetablesOrAbort(String action) {
        if (!dataStore.getTimetables().isEmpty()) return true;
        ConsoleStyle.warn("No timetables available to " + action + ". Generate a timetable first.");
        return false;
    }

    private void printEditableFields() {
        ConsoleStyle.heading("Editable Fields");
        List<String> fields = allowedFields();
        for (int i = 0; i < fields.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, fields.get(i));
        }
        System.out.println(" 0. Exit editing mode");
    }

    private boolean isExitEditCommand(String input) {
        String normalised = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        return normalised.equals("0") || normalised.equals("exit") || normalised.equals("back") || normalised.equals("done") || normalised.equals("q");
    }

    private String resolveEditableField(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.matches("\\d+")) {
            int index = Integer.parseInt(trimmed);
            List<String> fields = allowedFields();
            if (index >= 1 && index <= fields.size()) return fields.get(index - 1);
            throw new IllegalArgumentException("Field number must be between 1 and " + fields.size() + ", or 0 to exit editing mode.");
        }
        validateField(trimmed);
        return allowedFields().stream()
                .filter(field -> ClassManager.normaliseField(field).equals(ClassManager.normaliseField(trimmed)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported field: " + input));
    }

    private void pauseBeforeMainMenu() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean readBoolean(String label, boolean defaultValue) {
        while (true) {
            String raw = promptWithDefault(label + " (yes/no)", defaultValue ? "yes" : "no").trim().toLowerCase(Locale.ROOT);
            if (raw.equals("yes") || raw.equals("y")) return true;
            if (raw.equals("no") || raw.equals("n")) return false;
            ConsoleStyle.warn("Please enter yes or no.");
        }
    }

    private boolean confirm(String warning) {
        ConsoleStyle.warn(warning);
        String answer = prompt("Type YES to confirm");
        return answer.equalsIgnoreCase("YES");
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
        return List.of("topic code", "topic name", "attendance mode", "campus", "semester", "availability number", "class", "class instance", "date of first class", "date of last class", "day", "start time", "end time", "building", "room");
    }

    private void validateField(String field) {
        String normalised = ClassManager.normaliseField(field);
        boolean ok = allowedFields().stream().map(ClassManager::normaliseField).anyMatch(f -> f.equals(normalised));
        if (!ok) throw new IllegalArgumentException("Additional search/edit criteria are not permitted: " + field);
    }
}
