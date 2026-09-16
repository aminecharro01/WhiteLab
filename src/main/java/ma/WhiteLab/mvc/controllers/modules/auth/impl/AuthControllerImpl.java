package ma.WhiteLab.mvc.controllers.modules.auth.impl;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.dashboardModule.api.DashboardController;
import ma.WhiteLab.mvc.controllers.modules.auth.api.AuthController;
import ma.WhiteLab.mvc.dto.auth.AuthRequest;
import ma.WhiteLab.mvc.dto.auth.AuthResult;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.frames.AuthView;
import ma.WhiteLab.service.modules.auth.api.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    private AuthView view;

    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void showLoginView() {
        SwingUtilities.invokeLater(() -> {
            if (view == null || view.isDisposed()) {
                view = new AuthView(this);
            }
            view.clearErrors();
            view.setVisible(true);
            view.requestFocusInLoginField(); // Optionnel : focus sur le champ login
        });
    }

    @Override
    public void onLoginRequested(String login, String password) {
        String trimmedLogin = (login != null) ? login.trim() : "";
        String trimmedPassword = (password != null) ? password : "";

        if (trimmedLogin.isEmpty() || trimmedPassword.isEmpty()) {
            view.showFieldErrors(Map.of("login", "Veuillez remplir tous les champs"));
            return;
        }

        AuthResult result = authService.authenticate(new AuthRequest(trimmedLogin, trimmedPassword));

        // 1. Erreurs de validation du formulaire
        if (result.hasFieldErrors() && !result.getFieldErrors().isEmpty()) {
            view.showFieldErrors(result.getFieldErrors());
            return;
        }

        // 2. Échec d'authentification
        if (!result.isSuccess() || result.getUserPrincipal() == null) {
            JOptionPane.showMessageDialog(
                    view,
                    result.getMessage() != null ? result.getMessage() : "Identifiants incorrects",
                    "Échec de l'authentification",
                    JOptionPane.ERROR_MESSAGE
            );
            view.clearPasswordField(); // Sécurité : efface le mot de passe
            return;
        }
        
        // 2.1. Changement de mot de passe obligatoire
        if (result.isMustChangePassword()) {
            view.setVisible(false); // Hide login view
            showChangePasswordDialog(result.getUserPrincipal());
            return; // Stop the normal login flow
        }

        // 3. Succès
        UserPrincipal principal = result.getUserPrincipal();

        JOptionPane.showMessageDialog(
                view,
                "Bienvenue, " + principal.fullName() + " !",
                "Connexion réussie",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Fermer proprement la fenêtre de login
        if (view != null) {
            view.dispose();
            view = null; // Permet de recréer une nouvelle vue au prochain login
        }

        // Ouvrir le dashboard
        onLoginSuccess(principal);
    }
    
    @Override
    public void onForgotPasswordRequested() {
        String email = JOptionPane.showInputDialog(
                view,
                "Veuillez saisir votre adresse e-mail pour réinitialiser votre mot de passe :",
                "Mot de passe oublié",
                JOptionPane.QUESTION_MESSAGE
        );

        if (email == null || email.trim().isEmpty()) {
            return; // User cancelled or entered empty email
        }

        try {
            String tempPassword = authService.forgotPassword(email.trim());
            JOptionPane.showMessageDialog(
                    view,
                    "Un mot de passe temporaire vous a été généré :\n\n" + tempPassword +
                    "\nVeuillez l'utiliser pour vous connecter. Vous devrez le changer à la connexion.",
                    "Mot de passe temporaire",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    view,
                    "Erreur : " + e.getMessage(),
                    "Échec de la réinitialisation",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void onCancelRequested() {
        if (view == null) {
            System.exit(0);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment quitter l'application WhiteLab ?",
                "Confirmation de fermeture",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            view.dispose();
            System.exit(0);
        }
    }

    @Override
    public void onLoginSuccess(UserPrincipal principal) {
        SwingUtilities.invokeLater(() -> {
            DashboardController dashboardController = ApplicationContext.getInstance().getBean(DashboardController.class);

            if (dashboardController != null) {
                dashboardController.showDashboard(principal);
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Connexion réussie, " + principal.fullName() + " !\n" +
                                "Cependant, le tableau de bord n'est pas disponible.",
                        "Connexion réussie",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    private void showChangePasswordDialog(UserPrincipal principal) {
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Veuillez définir un nouveau mot de passe :"));
        panel.add(new JLabel("Nouveau mot de passe :"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirmer le mot de passe :"));
        panel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Changement de mot de passe obligatoire",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                showChangePasswordDialog(principal); // Retry
                return;
            }

            try {
                authService.forceChangePassword(principal.id(), newPassword);
                JOptionPane.showMessageDialog(null, "Mot de passe changé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);

                // Now proceed to dashboard
                 if (view != null) {
                    view.dispose();
                    view = null;
                }
                onLoginSuccess(principal);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erreur lors du changement de mot de passe : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                showChangePasswordDialog(principal); // Retry
            }
        } else {
            // User cancelled, exit or show login again
            showLoginView();
        }
    }
}
