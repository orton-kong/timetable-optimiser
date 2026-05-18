package timetableoptimizer;

public enum Preferences {
    BEDFORD,
    TONSLEY,
    CITY,
    ALL_AT_SAME_CAMPUS,
    MORNING,
    AFTERNOON,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    EVEN_SPREAD,
    COMPACT_SPREAD;

    public static Preferences fromMenuNumber(int number) {
        return switch (number) {
            case 1 -> BEDFORD;
            case 2 -> TONSLEY;
            case 3 -> CITY;
            case 4 -> ALL_AT_SAME_CAMPUS;
            case 5 -> MORNING;
            case 6 -> AFTERNOON;
            case 7 -> MONDAY;
            case 8 -> TUESDAY;
            case 9 -> WEDNESDAY;
            case 10 -> THURSDAY;
            case 11 -> FRIDAY;
            case 12 -> EVEN_SPREAD;
            case 13 -> COMPACT_SPREAD;
            default -> throw new IllegalArgumentException("Invalid preference number.");
        };
    }
}
