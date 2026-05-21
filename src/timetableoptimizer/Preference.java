package timetableoptimizer;

import java.time.DayOfWeek;
import java.util.*;

public class Preference {
    private List<Campus> campus;
    private boolean allAtSameCampus;
    private TimeOfDayPreferences timeOfDay;
    private List<DayOfWeek> dayOfWeek;
    private ClassSpreadPreferences classSpread;
    private List<Enum<?>> orderedPreferences;

    public Preference() {
        this.campus = new ArrayList<>();
        this.dayOfWeek = new ArrayList<>();
        this.orderedPreferences = new ArrayList<>();
    }

    public Preference(List<? extends Enum<?>> orderedPreferences) {
        this();
        setOrderedPreferences(orderedPreferences);
    }

    public List<Campus> getCampus() { return campus; }
    public boolean isAllAtSameCampus() { return allAtSameCampus; }
    public TimeOfDayPreferences getTimeOfDay() { return timeOfDay; }
    public List<DayOfWeek> getDayOfWeek() { return dayOfWeek; }
    public ClassSpreadPreferences getClassSpread() { return classSpread; }
    public List<Enum<?>> getOrderedPreferences() { return Collections.unmodifiableList(orderedPreferences); }

    public void setOrderedPreferences(List<? extends Enum<?>> orderedPreferences) {
        this.orderedPreferences = new ArrayList<>();
        LinkedHashSet<Enum<?>> unique = new LinkedHashSet<>();
        if (orderedPreferences != null) {
            for (Enum<?> preference : orderedPreferences) {
                if (preference == null) continue;
                validatePreferenceType(preference);
                unique.add(preference);
            }
        }
        this.orderedPreferences.addAll(unique);
        rebuildStructuredFields();
    }

    private void rebuildStructuredFields() {
        this.campus.clear();
        this.dayOfWeek.clear();
        this.allAtSameCampus = false;
        this.timeOfDay = null;
        this.classSpread = null;

        for (Enum<?> preference : this.orderedPreferences) {
            if (preference instanceof LocationPreferences locationPreference) {
                switch (locationPreference) {
                    case BEDFORD -> campus.add(Campus.BEDFORD);
                    case TONSLEY -> campus.add(Campus.TONSLEY);
                    case CITY -> campus.add(Campus.CITY);
                    case ALL_AT_SAME_CAMPUS -> allAtSameCampus = true;
                }
            } else if (preference instanceof TimeOfDayPreferences timePreference) {
                timeOfDay = timePreference;
            } else if (preference instanceof DayPreferences dayPreference) {
                dayOfWeek.add(toDayOfWeek(dayPreference));
            } else if (preference instanceof ClassSpreadPreferences spreadPreference) {
                classSpread = spreadPreference;
            }
        }
    }

    private void validatePreferenceType(Enum<?> preference) {
        if (!(preference instanceof LocationPreferences)
                && !(preference instanceof TimeOfDayPreferences)
                && !(preference instanceof DayPreferences)
                && !(preference instanceof ClassSpreadPreferences)) {
            throw new IllegalArgumentException("Unsupported preference type: " + preference.getClass().getSimpleName());
        }
    }

    private DayOfWeek toDayOfWeek(DayPreferences preference) {
        return switch (preference) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
        };
    }

    public String display() {
        if (orderedPreferences.isEmpty()) return "No preferences selected";
        return String.join(" > ", orderedPreferences.stream().map(Enum::name).toList());
    }
}
