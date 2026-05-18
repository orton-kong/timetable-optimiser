package timetableoptimizer;

public class ConsoleStyle {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    public static void title() {
        System.out.println(CYAN + BOLD + """
        ===============================================================
          _______ _                _        _     _        _____      
         |__   __(_)              | |      | |   | |      / ____|     
            | |   _ _ __ ___   ___| |_ __ _| |__ | | ___ | |  __     
            | |  | | '_ ` _ | / _ | __/ _` | '_ || |/ _ || | |_ |    
            | |  | | | | | | |  __| || (_| | |_) | |  __/| |__| |    
            |_|  |_|_| |_| |_| ___| __|__,_|_.__/|_| ___| _____ |    

                    TIMETABLE OPTIMISER
        ===============================================================
        """ + RESET);
    }

    public static void heading(String text) { System.out.println("\n" + BLUE + BOLD + UNDERLINE + text + RESET); }
    public static void success(String text) { System.out.println(GREEN + text + RESET); }
    public static void warn(String text) { System.out.println(YELLOW + text + RESET); }
    public static void error(String text) { System.out.println(RED + BOLD + text + RESET); }
}
