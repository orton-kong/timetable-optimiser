package timetableoptimizer;

import java.util.*;

public class LastGenerationSettings {
    String name = "";
    int semester = 0;
    List<String> topics = new ArrayList<>();
    List<Campus> campuses = new ArrayList<>(List.of(Campus.BEDFORD, Campus.TONSLEY, Campus.CITY));
    boolean lectureOverlap = false;
    List<Preferences> preferences = new ArrayList<>();
}
