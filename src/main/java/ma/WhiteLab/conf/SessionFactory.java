package ma.WhiteLab.conf;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton thread-safe gérant la connexion JDBC à la base de données.
 * Chargement des paramètres depuis config/db.properties.
 * La connexion est recréée automatiquement si elle est fermée ou invalide.
 */
public final class SessionFactory {

    private static volatile SessionFactory INSTANCE;

    private Connection connection;

    // Chemins et clés de configuration
    private static final String PROPS_PATH = "config/db.properties";
    private static final String URL_KEY    = "db.url";
    private static final String USER_KEY   = "db.username";
    private static final String PASS_KEY   = "db.password";
    private static final String DRIVER_KEY = "db.driver";

    // Paramètres de connexion
    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // ======================== CONSTRUCTEUR PRIVÉ ========================

    private SessionFactory() {
        try {
            System.out.println("[SessionFactory] Chargement de la configuration depuis : " + PROPS_PATH);

            InputStream input = classLoader.getResourceAsStream(PROPS_PATH);
            if (input == null) {
                throw new IllegalArgumentException(
                        "FICHIER INTROUVABLE !\n" +
                                "Le fichier '" + PROPS_PATH + "' n'existe pas dans le classpath.\n" +
                                "Placez-le dans : src/main/resources/" + PROPS_PATH
                );
            }

            Properties props = new Properties();
            props.load(input);
            input.close();

            this.url      = getRequiredProperty(props, URL_KEY);
            this.username = getRequiredProperty(props, USER_KEY);
            this.password = props.getProperty(PASS_KEY, ""); // mot de passe peut être vide
            this.driver   = getRequiredProperty(props, DRIVER_KEY);

            System.out.println("[SessionFactory] Configuration chargée avec succès.");
            System.out.println("URL      : " + url);
            System.out.println("User     : " + username);
            System.out.println("Driver   : " + driver);

            // Chargement explicite du driver (recommandé pour certains anciens drivers)
            try {
                Class.forName(driver);
                System.out.println("[SessionFactory] Driver JDBC chargé avec succès : " + driver);
            } catch (ClassNotFoundException e) {
                System.err.println("[SessionFactory] ERREUR : Driver JDBC introuvable → " + driver);
                throw new RuntimeException("Driver JDBC manquant. Vérifiez le classpath et la propriété " + DRIVER_KEY, e);
            }

        } catch (Exception e) {
            System.err.println("[SessionFactory] ÉCHEC CRITIQUE DU CHARGEMENT DE LA CONFIGURATION");
            System.err.println("Cause : " + e.getMessage());
            e.printStackTrace();

            String userMessage = """
                    Impossible d'initialiser la connexion à la base de données.
                    
                    Causes probables :
                    • Le fichier config/db.properties est absent ou mal placé
                    • Une propriété obligatoire est manquante (db.url, db.username, db.driver)
                    • Le driver JDBC n'est pas dans le classpath
                    
                    Détails techniques :
                    """ + e.getMessage();

            throw new RuntimeException(userMessage, e);
        }
    }

    // ======================== SINGLETON ========================

    public static SessionFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (SessionFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionFactory();
                }
            }
        }
        return INSTANCE;
    }

    // ======================== MÉTHODES PUBLIQUES ========================

    /**
     * Retourne une connexion JDBC valide.
     * Si la connexion actuelle est nulle, fermée ou invalide, une nouvelle est créée.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(3)) {
            System.out.println("[SessionFactory] Création d'une nouvelle connexion JDBC...");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("[SessionFactory] Connexion établie avec succès.");
        }
        return connection;
    }

    /**
     * Ferme proprement la connexion si elle est ouverte.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[SessionFactory] Connexion JDBC fermée proprement.");
                }
            } catch (SQLException e) {
                System.err.println("[SessionFactory] Erreur lors de la fermeture de la connexion : " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // ======================== MÉTHODE UTILITAIRE ========================

    private String getRequiredProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Propriété obligatoire manquante dans " + PROPS_PATH + " : " + key);
        }
        return value.trim();
    }

    // Optionnel : pour forcer la fermeture à la fin de l'application
    public static void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.closeConnection();
            INSTANCE = null;
            System.out.println("[SessionFactory] Shutdown complet.");
        }
    }
}