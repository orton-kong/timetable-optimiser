package timetableoptimizer;

import java.util.*;

public class LastGenerationSettings {
    private String name = "";
    private int semester = 0;
    private List<String> topics = new ArrayList<>();
    private List<Campus> campuses = new ArrayList<>(List.of(Campus.BEDFORD, Campus.TONSLEY, Campus.CITY));
    private boolean lectureOverlap = false;
    private List<Enum<?>> preferences = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = new ArrayList<>(topics == null ? List.of() : topics); }

    public List<Campus> getCampuses() { return campuses; }
    public void setCampuses(List<Campus> campuses) { this.campuses = new ArrayList<>(campuses == null ? List.of() : campuses); }

    public boolean isLectureOverlap() { return lectureOverlap; }
    public void setLectureOverlap(boolean lectureOverlap) { this.lectureOverlap = lectureOverlap; }

    public List<Enum<?>> getPreferences() { return preferences; }
    public void setPreferences(List<? extends Enum<?>> preferences) { this.preferences = new ArrayList<>(preferences == null ? List.of() : preferences); }
}
