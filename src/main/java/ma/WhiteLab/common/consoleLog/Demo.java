package ma.WhiteLab.common.consoleLog;

public class Demo {
    public static void main(String[] args) {
        ConsoleLogger.info("Application démarrée");
        ConsoleLogger.warn("Configuration manquante: default value utilisé");
        ConsoleLogger.log("Connexion DB établie");
        ConsoleLogger.debug("User count = 12");

        try {
            int x = 1 / 0;
        } catch (Exception e) {
            ConsoleLogger.error("Erreur pendant le calcul", e);
        }
    }
}
