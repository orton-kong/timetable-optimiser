package timetableoptimizer;

import java.time.DayOfWeek;
import java.util.*;

public class Preference {
    private List<Campus> campus;
    private boolean allAtSameCampus;
    private TimeOfDayPreferences timeOfDay;
    private List<DayOfWeek> dayOfWeek;
    private ClassSpreadPreferences classSpread;
    private List<Preferences> orderedPreferences;

    public Preference() {
        this.campus = new ArrayList<>();
        this.dayOfWeek = new ArrayList<>();
        this.orderedPreferences = new ArrayList<>();
    }

    public Preference(List<Preferences> orderedPreferences) {
        this();
        setOrderedPreferences(orderedPreferences);
    }

    public List<Campus> getCampus() { return campus; }
    public boolean isAllAtSameCampus() { return allAtSameCampus; }
    public TimeOfDayPreferences getTimeOfDay() { return timeOfDay; }
    public List<DayOfWeek> getDayOfWeek() { return dayOfWeek; }
    public ClassSpreadPreferences getClassSpread() { return classSpread; }
    public List<Preferences> getOrderedPreferences() { return orderedPreferences; }

    public void setOrderedPreferences(List<Preferences> orderedPreferences) {
        this.orderedPreferences = new ArrayList<>(orderedPreferences == null ? List.of() : orderedPreferences);
        this.campus.clear();
        this.dayOfWeek.clear();
        this.allAtSameCampus = false;
        this.timeOfDay = null;
        this.classSpread = null;
        for (Preferences p : this.orderedPreferences) {
            switch (p) {
                case BEDFORD -> campus.add(Campus.BEDFORD);
                case TONSLEY -> campus.add(Campus.TONSLEY);
                case CITY -> campus.add(Campus.CITY);
                case ALL_AT_SAME_CAMPUS -> allAtSameCampus = true;
                case MORNING -> timeOfDay = TimeOfDayPreferences.MORNING;
                case AFTERNOON -> timeOfDay = TimeOfDayPreferences.AFTERNOON;
                case MONDAY -> dayOfWeek.add(DayOfWeek.MONDAY);
                case TUESDAY -> dayOfWeek.add(DayOfWeek.TUESDAY);
                case WEDNESDAY -> dayOfWeek.add(DayOfWeek.WEDNESDAY);
                case THURSDAY -> dayOfWeek.add(DayOfWeek.THURSDAY);
                case FRIDAY -> dayOfWeek.add(DayOfWeek.FRIDAY);
                case EVEN_SPREAD -> classSpread = ClassSpreadPreferences.EVEN_SPREAD;
                case COMPACT_SPREAD -> classSpread = ClassSpreadPreferences.COMPACT_SPREAD;
            }
        }
    }

    public String display() {
        if (orderedPreferences.isEmpty()) return "No preferences selected";
        return String.join(" > ", orderedPreferences.stream().map(Enum::name).toList());
    }
}
