package ma.WhiteLab.mvc.ui.pages.otherPages.common;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

import javax.swing.*;
import java.awt.*;

public class BasePlaceholderPanel extends JPanel {

    public BasePlaceholderPanel(String title, UserPrincipal principal) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Optima", Font.BOLD, 28));

        JPanel head = new JPanel(new GridLayout(0, 1, 0, 6));
        head.setOpaque(false);
        head.add(lblTitle);

        if (principal != null) {
            // Correction ici : fullName() au lieu de nom()
            JLabel lblUser = new JLabel(
                    "Session: " + safe(principal.fullName()) +
                            "  |  Login: " + safe(principal.login())
            );
            lblUser.setFont(new Font("Optima", Font.PLAIN, 14));
            head.add(lblUser);

            JLabel lblRole = new JLabel(
                    "Rôle principal: " +
                            (principal.rolePrincipal() != null ? principal.rolePrincipal().name() : "—")
            );
            lblRole.setFont(new Font("Optima", Font.PLAIN, 14));
            head.add(lblRole);
        }

        JTextArea info = new JTextArea(
                "✅ Placeholder prêt.\n" +
                        "Ici tu mettras la vraie UI du module (table, forms, filtres...).\n" +
                        "Tu peux remplacer ce panel par ton vrai panel plus tard sans changer le routing."
        );
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font("Optima", Font.PLAIN, 15));
        info.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        info.setBackground(Color.WHITE);
        info.setOpaque(false);

        add(head, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
    }

    private String safe(String s) {
        return s == null ? "—" : s;
    }
}