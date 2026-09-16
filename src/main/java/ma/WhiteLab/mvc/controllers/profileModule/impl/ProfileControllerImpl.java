package ma.WhiteLab.mvc.controllers.profileModule.impl;

import ma.WhiteLab.mvc.controllers.profileModule.api.ProfileController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;
import ma.WhiteLab.mvc.ui.pages.profilePages.ProfilePanel;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ProfileControllerImpl implements ProfileController {

    private final ProfileService service;

    public ProfileControllerImpl(ProfileService service) {
        this.service = service;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        System.out.println("Loading profile for user ID: " + principal.id());
        return getView(principal, null);
    }

    @Override
    public JPanel getView(UserPrincipal principal, Consumer<ProfileData> onProfileSaved) {
        System.out.println("Loading profile for user ID: " + principal.id());
        if (principal == null || principal.id() == null) {
            return errorPanel("Session invalide : utilisateur non identifié.", principal, onProfileSaved);
        }

        ProfileData data;
        try {
            data = service.loadByUserId(principal.id());
        } catch (Exception e) {
            e.printStackTrace();
            return errorPanel("Impossible de charger le profil. Veuillez réessayer.", principal, onProfileSaved);
        }

        if (data == null) {
            return errorPanel("Profil introuvable pour l'utilisateur connecté.", principal, onProfileSaved);
        }

        return new ProfilePanel( service, data, onProfileSaved);
    }

    // ======================== Helper ========================

    private JPanel errorPanel(String message, UserPrincipal principal, Consumer<ProfileData> onProfileSaved) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel errorLabel = new JLabel("<html><div style='text-align:center; color:red; font-size:18px;'>" +
                message + "</div></html>");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(errorLabel);

        panel.add(Box.createVerticalStrut(30));

        JButton retryButton = new JButton("Réessayer");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        retryButton.setPreferredSize(new Dimension(150, 45));

        // Capture finale des variables principal et onProfileSaved
        final UserPrincipal finalPrincipal = principal;
        final Consumer<ProfileData> finalOnProfileSaved = onProfileSaved;

        retryButton.addActionListener(e -> {
            JPanel parent = (JPanel) panel.getParent();
            if (parent != null) {
                parent.remove(panel);
                parent.add(getView(finalPrincipal, finalOnProfileSaved));
                parent.revalidate();
                parent.repaint();
            }
        });

        panel.add(retryButton);

        return panel;
    }
}