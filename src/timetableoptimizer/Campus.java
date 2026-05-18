package timetableoptimizer;

public enum Campus {
    BEDFORD("Bedford Park"),
    TONSLEY("Tonsley"),
    CITY("Flinders City Campus");

    private final String displayName;

    Campus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public static Campus fromString(String raw) {
        if (raw == null) throw new IllegalArgumentException("Campus is missing.");
        String value = raw.trim().toLowerCase();
        if (value.contains("bedford")) return BEDFORD;
        if (value.contains("tonsley")) return TONSLEY;
        if (value.contains("city") || value.contains("festival")) return CITY;
        throw new IllegalArgumentException("Unknown campus: " + raw);
    }
}
