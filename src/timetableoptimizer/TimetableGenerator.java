package timetableoptimizer;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableGenerator {
    public static Timetable generateTimetable(String name, int semester, List<String> topics, List<Campus> campus,
                                              boolean lectureOverlap, Preference preferences, DataStore dataStore) {
        if (topics == null || topics.isEmpty()) throw new IllegalArgumentException("At least one topic must be selected.");
        if (campus == null || campus.isEmpty()) throw new IllegalArgumentException("At least one campus must be selected.");
        if (name == null || name.isBlank()) name = generateName(dataStore);
        if (dataStore.getTimetables().containsKey(name)) throw new IllegalArgumentException("Timetable name must be unique: " + name);

        List<ClassRecord> pool = dataStore.getClasses().values().stream()
                .filter(r -> (semester == 0 || r.getAvailability().getSemester() == semester))
                .filter(r -> topics.contains(r.getTopicCode()))
                .filter(r -> campus.contains(r.getAvailability().getCampus()))
                .toList();
        if (pool.isEmpty()) throw new IllegalArgumentException("No imported classes match the selected semester, topics, and campus choices.");

        List<List<List<ClassRecord>>> perTopicPlans = new ArrayList<>();
        for (String topic : topics) {
            List<ClassRecord> topicRecords = pool.stream().filter(r -> r.getTopicCode().equals(topic)).toList();
            List<List<ClassRecord>> plans = buildTopicPlans(topicRecords, lectureOverlap);
            if (plans.isEmpty()) throw new IllegalArgumentException("No valid class combination could be found for topic " + topic + ".");
            perTopicPlans.add(plans);
        }

        SearchState best = new SearchState(null, Integer.MIN_VALUE);
        combineTopics(perTopicPlans, 0, new ArrayList<>(), lectureOverlap, preferences, best);
        if (best.records == null) {
            throw new IllegalArgumentException("No timetable could be generated without time clashes or invalid commute gaps.");
        }
        return new Timetable(name, best.records, lectureOverlap, preferences);
    }

    public static Timetable generateTimetable(String name, int semester, List<String> topics, List<Campus> campus,
                                              boolean lectureOverlap, Preference preferences) {
        throw new UnsupportedOperationException("Use overload with DataStore so generated timetables can be checked for unique names.");
    }

    private static String generateName(DataStore dataStore) {
        int i = dataStore.getTimetables().size() + 1;
        while (dataStore.getTimetables().containsKey("Timetable " + i)) i++;
        return "Timetable " + i;
    }

    private static List<List<ClassRecord>> buildTopicPlans(List<ClassRecord> topicRecords, boolean lectureOverlap) {
        if (topicRecords.isEmpty()) return List.of();
        List<ClassRecord> city = topicRecords.stream().filter(r -> r.getAvailability().getCampus() == Campus.CITY).toList();
        List<ClassRecord> nonCity = topicRecords.stream().filter(r -> r.getAvailability().getCampus() != Campus.CITY).toList();
        List<List<ClassRecord>> plans = new ArrayList<>();
        if (!city.isEmpty()) plans.addAll(buildPlansWithinCampusRule(city, lectureOverlap));
        if (!nonCity.isEmpty()) plans.addAll(buildPlansWithinCampusRule(nonCity, lectureOverlap));
        return plans;
    }

    private static List<List<ClassRecord>> buildPlansWithinCampusRule(List<ClassRecord> records, boolean lectureOverlap) {
        Map<String, Map<String, List<ClassRecord>>> classFormatToInstance = new LinkedHashMap<>();
        for (ClassRecord r : records) {
            classFormatToInstance.computeIfAbsent(r.classFormatKey(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(r.selectableGroupKey(), k -> new ArrayList<>()).add(r);
        }
        List<List<List<ClassRecord>>> options = new ArrayList<>();
        for (Map<String, List<ClassRecord>> instanceMap : classFormatToInstance.values()) options.add(new ArrayList<>(instanceMap.values()));
        List<List<ClassRecord>> plans = new ArrayList<>();
        backtrackOptions(options, 0, new ArrayList<>(), plans, lectureOverlap);
        return plans;
    }

    private static void backtrackOptions(List<List<List<ClassRecord>>> options, int index, List<ClassRecord> current,
                                         List<List<ClassRecord>> plans, boolean lectureOverlap) {
        if (index == options.size()) {
            if (!ClashDetector.hasHardClash(current, lectureOverlap)) plans.add(new ArrayList<>(current));
            return;
        }
        for (List<ClassRecord> choice : options.get(index)) {
            current.addAll(choice);
            if (!ClashDetector.hasHardClash(current, lectureOverlap)) backtrackOptions(options, index + 1, current, plans, lectureOverlap);
            current.subList(current.size() - choice.size(), current.size()).clear();
        }
    }

    private static void combineTopics(List<List<List<ClassRecord>>> perTopicPlans, int index, List<ClassRecord> current,
                                      boolean lectureOverlap, Preference preference, SearchState best) {
        if (index == perTopicPlans.size()) {
            if (!ClashDetector.hasHardClash(current, lectureOverlap)) {
                int score = score(current, preference);
                if (score > best.score) { best.records = new ArrayList<>(current); best.score = score; }
            }
            return;
        }
        for (List<ClassRecord> plan : perTopicPlans.get(index)) {
            current.addAll(plan);
            if (!ClashDetector.hasHardClash(current, lectureOverlap)) combineTopics(perTopicPlans, index + 1, current, lectureOverlap, preference, best);
            current.subList(current.size() - plan.size(), current.size()).clear();
        }
    }

    private static int score(List<ClassRecord> records, Preference preference) {
        if (preference == null) return 0;
        int score = 0;
        int weight = preference.getOrderedPreferences().size() + 1;
        for (Preferences p : preference.getOrderedPreferences()) {
            int local = switch (p) {
                case BEDFORD -> campusCount(records, Campus.BEDFORD);
                case TONSLEY -> campusCount(records, Campus.TONSLEY);
                case CITY -> campusCount(records, Campus.CITY);
                case ALL_AT_SAME_CAMPUS -> allAtSameCampus(records) ? 10 : -10;
                case MORNING -> (int) records.stream().filter(r -> r.getStartTime().isBefore(LocalTime.NOON)).count();
                case AFTERNOON -> (int) records.stream().filter(r -> !r.getStartTime().isBefore(LocalTime.NOON)).count();
                case MONDAY -> dayCount(records, DayOfWeek.MONDAY);
                case TUESDAY -> dayCount(records, DayOfWeek.TUESDAY);
                case WEDNESDAY -> dayCount(records, DayOfWeek.WEDNESDAY);
                case THURSDAY -> dayCount(records, DayOfWeek.THURSDAY);
                case FRIDAY -> dayCount(records, DayOfWeek.FRIDAY);
                case EVEN_SPREAD -> evenSpreadScore(records);
                case COMPACT_SPREAD -> compactScore(records);
            };
            score += local * weight;
            weight--;
        }
        return score;
    }

    private static int campusCount(List<ClassRecord> records, Campus campus) {
        return (int) records.stream().filter(r -> r.getAvailability().getCampus() == campus).count();
    }
    private static int dayCount(List<ClassRecord> records, DayOfWeek day) {
        return (int) records.stream().filter(r -> r.getDay() == day).count();
    }
    private static boolean allAtSameCampus(List<ClassRecord> records) {
        return records.stream().map(r -> r.getAvailability().getCampus()).collect(Collectors.toSet()).size() <= 1;
    }
    private static int compactScore(List<ClassRecord> records) {
        return 20 - (int) records.stream().map(ClassRecord::getDay).distinct().count();
    }
    private static int evenSpreadScore(List<ClassRecord> records) {
        Map<DayOfWeek, Long> counts = records.stream().collect(Collectors.groupingBy(ClassRecord::getDay, Collectors.counting()));
        if (counts.isEmpty()) return 0;
        long max = Collections.max(counts.values());
        long min = Collections.min(counts.values());
        return (int) (20 - (max - min));
    }

    private static class SearchState {
        List<ClassRecord> records;
        int score;
        SearchState(List<ClassRecord> records, int score) { this.records = records; this.score = score; }
    }
}
