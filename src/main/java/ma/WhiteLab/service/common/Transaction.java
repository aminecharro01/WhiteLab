package ma.WhiteLab.service.common;

import java.sql.Connection;
import java.sql.SQLException;
import ma.WhiteLab.common.consoleLog.ConsoleLogger;
import ma.WhiteLab.conf.SessionFactory;

public final class Transaction {

    private Transaction() {}

    @FunctionalInterface
    public interface TransactionBlocExecuter<T> {
        T run(Connection c) throws Throwable;
    }

    /**
     * Exécute un bloc transactionnel avec commit / rollback.
     * Compatible avec un SessionFactory qui garde une connexion globale.
     */
    public static <T> T initTransaction(TransactionBlocExecuter<T> blocTransactionnelAExecuter) {
        Connection connection = null;
        boolean oldAutoCommit = true;

        try {
            // Récupère la connexion (SessionFactory garde la connexion)
            connection = SessionFactory.getInstance().getConnection();
            ConsoleLogger.info("Ouverture de la connexion JDBC " + connection.getMetaData().getURL());

            // Sauvegarde l'état autoCommit
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // Exécute le bloc
            T result = blocTransactionnelAExecuter.run(connection);

            // Commit si tout s'est bien passé
            connection.commit();
            ConsoleLogger.info("Transaction committée avec succès");

            return result;

        } catch (Throwable e) {
            // Rollback si erreur
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.rollback();
                        ConsoleLogger.info("Transaction annulée (rollback)");
                    }
                } catch (SQLException rollbackEx) {
                    ConsoleLogger.error("Erreur lors du rollback", rollbackEx);
                }
            }
            throw new RuntimeException("Erreur dans la transaction", e);

        } finally {
            // Restaure autoCommit
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.setAutoCommit(oldAutoCommit);
                    }
                } catch (SQLException e) {
                    ConsoleLogger.error("Impossible de réactiver autoCommit", e);
                }
            }
        }
    }
}
