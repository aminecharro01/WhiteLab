package ma.WhiteLab;

import com.formdev.flatlaf.FlatLightLaf;
import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.modules.auth.api.AuthController;
import ma.WhiteLab.mvc.ui.palette.utils.AppTheme;

import javax.swing.*;

public class WhiteLabApp {

    public static void main(String[] args) {
        // Appliquer le thème moderne FlatLaf (Light par défaut) + palette dentaire
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Accent teal "clinique dentaire" — cascade vers boutons par défaut,
            // focus, progress bars, cases à cocher, etc.
            UIManager.put("Component.accentColor", AppTheme.PRIMARY);
            UIManager.put("Component.focusColor", AppTheme.PRIMARY_LIGHT);
            UIManager.put("Button.default.background", AppTheme.PRIMARY);
            UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.default.focusedBackground", AppTheme.PRIMARY_DARK);
            UIManager.put("Button.default.hoverBackground", AppTheme.PRIMARY_DARK);
            UIManager.put("ProgressBar.foreground", AppTheme.PRIMARY);
            UIManager.put("TabbedPane.underlineColor", AppTheme.PRIMARY);
            UIManager.put("TabbedPane.selectedBackground", AppTheme.SURFACE);
            UIManager.put("TabbedPane.hoverColor", AppTheme.PRIMARY_LIGHT);
            UIManager.put("TabbedPane.focusColor", AppTheme.PRIMARY_LIGHT);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);

            // Coins plus arrondis, look "minimal" plus doux
            UIManager.put("Button.arc", 14);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("CheckBox.arc", 6);

            // Champs de saisie : bordures et focus alignés sur la palette dentaire
            UIManager.put("TextComponent.selectionBackground", AppTheme.PRIMARY_LIGHT);
            UIManager.put("TextField.borderColor", AppTheme.BORDER);
            UIManager.put("TextField.focusedBorderColor", AppTheme.PRIMARY);
            UIManager.put("TextArea.borderColor", AppTheme.BORDER);
            UIManager.put("TextArea.focusedBorderColor", AppTheme.PRIMARY);
            UIManager.put("ComboBox.borderColor", AppTheme.BORDER);
            UIManager.put("ComboBox.focusedBorderColor", AppTheme.PRIMARY);
            UIManager.put("ComboBox.buttonEditableBackground", AppTheme.BACKGROUND);
            UIManager.put("Spinner.borderColor", AppTheme.BORDER);
            UIManager.put("Spinner.focusedBorderColor", AppTheme.PRIMARY);
            UIManager.put("PasswordField.borderColor", AppTheme.BORDER);
            UIManager.put("PasswordField.focusedBorderColor", AppTheme.PRIMARY);

            // Tables : en-têtes et sélection cohérents sur toutes les listes (patients,
            // consultations, rendez-vous, etc.) sans retoucher chaque panneau.
            UIManager.put("TableHeader.background", AppTheme.SURFACE);
            UIManager.put("TableHeader.foreground", AppTheme.TEXT_PRIMARY);
            UIManager.put("TableHeader.separatorColor", AppTheme.BORDER);
            UIManager.put("TableHeader.bottomSeparatorColor", AppTheme.BORDER);
            UIManager.put("Table.background", AppTheme.SURFACE);
            UIManager.put("Table.gridColor", AppTheme.BORDER);
            UIManager.put("Table.selectionBackground", AppTheme.PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", AppTheme.TEXT_PRIMARY);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.alternateRowColor", AppTheme.BACKGROUND);

            UIManager.put("@background", AppTheme.BACKGROUND);
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