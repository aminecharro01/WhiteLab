package ma.WhiteLab.mvc.controllers.modules.notifications.impl;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.modules.notifications.api.NotificationsController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.NotificationsPanel;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;

import javax.swing.*;
import java.awt.*;

/**
 * Contrôleur pour la gestion des notifications.
 * Il fournit la vue (le panel) et gère les actions comme la suppression.
 */
public class NotificationsControllerImpl implements NotificationsController {

    public NotificationsControllerImpl() {
        // Le panel et les actions utilisent ApplicationContext pour obtenir les services.
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (principal == null) {
            return createErrorPanel("Session expirée ou utilisateur non identifié.");
        }

        try {
            // Le NotificationsPanel est autonome pour l'affichage,
            // mais a besoin du contrôleur pour les actions.
            return new NotificationsPanel(this, principal);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorPanel(
                    "Erreur lors du chargement des notifications :<br>" +
                            e.getMessage().replace("\n", "<br>")
            );
        }
    }

    @Override
    public void deleteNotification(Long notificationId, UserPrincipal principal) {
        if (principal == null || notificationId == null) {
            System.err.println("Impossible de supprimer la notification : utilisateur ou ID de notification manquant.");
            return;
        }

        try {
            NotificationService service = ApplicationContext.getInstance().getBean(NotificationService.class);
            service.deleteNotificationForUser(principal.id(), notificationId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Erreur lors de la suppression de la notification : " + e.getMessage(),
                    "Erreur de suppression",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ======================================
    // Méthodes utilitaires (style identique aux autres controllers)
    // ======================================

    private JPanel createErrorPanel(String message) {
        return createStyledPanel(message, "#e74c3c");
    }

    private JPanel createInfoPanel(String message) {
        return createStyledPanel(message, "#3498db");
    }

    private JPanel createStyledPanel(String htmlMessage, String color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JLabel label = new JLabel(
                "<html><div style='text-align:center; font-size:17px; line-height:1.6; color:" + color + ";'>" +
                        htmlMessage +
                        "</div></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }
}