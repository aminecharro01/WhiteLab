package ma.WhiteLab;

import com.formdev.flatlaf.FlatLightLaf;
import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.modules.auth.api.AuthController;

import javax.swing.*;

public class WhiteLabApp {

    public static void main(String[] args) {
        // Appliquer le thème moderne FlatLaf (Light par défaut)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement du Look & Feel FlatLaf");
            ex.printStackTrace();
        }

        // Toute l'interface Swing doit être lancée dans l'Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialiser le contexte d'application (charge tous les beans)
                ApplicationContext context = ApplicationContext.getInstance();

                // Récupérer le contrôleur d'authentification préconfiguré
                AuthController authController = context.getBean(AuthController.class);

                if (authController == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Impossible de charger le module d'authentification.\nVérifiez la configuration des beans.",
                            "Erreur fatale",
                            JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(1);
                }

                // Lancer l'écran de connexion
                authController.showLoginView();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Une erreur critique est survenue au démarrage :\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}