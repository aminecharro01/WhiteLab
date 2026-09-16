package ma.WhiteLab.common.consoleLog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ConsoleLogger : affiche des messages colorés dans la console via ANSI escape codes.
 *
 * Remarque :
 * - Cette classe marche bien sur macOS / Linux et la plupart des terminaux IntelliJ.
 * - Sur Windows, ça dépend du terminal (Windows Terminal OK). Sur l'ancien cmd, parfois non.
 */
public final class ConsoleLogger {

    private ConsoleLogger() {}

    // --- ANSI codes ---
    private static final String RESET = "\u001B[0m";

    private static final String RED    = "\u001B[31m"; // ERROR
    private static final String YELLOW = "\u001B[33m"; // WARN
    private static final String GREEN  = "\u001B[32m"; // INFO
    private static final String CYAN   = "\u001B[36m"; // DEBUG/LOG
    private static final String GRAY   = "\u001B[90m"; // time

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Active/désactive couleurs (utile si terminal ne supporte pas ANSI)
    private static boolean colorsEnabled = true;

    public static void setColorsEnabled(boolean enabled) {
        colorsEnabled = enabled;
    }

    // ---------------- Public API ----------------

    public static void info(String message) {
        print("INFO ", GREEN, message, false);
    }

    public static void warn(String message) {
        print("WARN ", YELLOW, message, false);
    }

    public static void error(String message) {
        print("ERROR", RED, message, true);
    }

    public static void error(String message, String ExceptionMsg) {
        print("ERROR", RED, message + " => " + ExceptionMsg, true);
    }

    public static void log(String message) {
        print("LOG  ", CYAN, message, false);
    }

    public static void debug(String message) {
        print("DEBUG", CYAN, message, false);
    }

    // Overloads avec exception
    public static void error(String message, Throwable t) {

        if (t != null) {
            error(message, t.getMessage());
        }
        else error(message);
    }


    // ---------------- Internal ----------------

    private static void print(String level, String color, String message, boolean useErrStream) {
        String ts = LocalDateTime.now().format(TS);

        String prefix = format(GRAY, "[" + ts + "] ") +
                        format(color, "[" + level + "] ") +
                        RESET;

        String line = prefix + (message == null ? "null" : message);

        if (useErrStream) System.err.println(line);
        else System.out.println(line);
    }

    private static String format(String color, String text) {
        if (!colorsEnabled) return text;
        return color + text + RESET;
    }

    private static String stackTraceLine(Throwable t) {
        // Affiche une ligne simple + le premier élément de stack trace
        StackTraceElement first = (t.getStackTrace() != null && t.getStackTrace().length > 0)
                ? t.getStackTrace()[0]
                : null;

        String where = (first == null) ? "" : (" at " + first);
        return format(RED, "↳ " + t.getClass().getSimpleName() + ": " + t.getMessage() + where) + RESET;
    }
}
