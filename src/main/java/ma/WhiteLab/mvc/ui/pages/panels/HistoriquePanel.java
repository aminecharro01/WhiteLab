package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;

import javax.swing.*;
import java.awt.*;

public class HistoriquePanel extends JPanel {

    private static final Color PRIMARY = new Color(64, 120, 255);

    public HistoriquePanel(DossierMedical dossier, DossierMedicalService service, UserPrincipal principal) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea historiqueArea = new JTextArea(dossier.getHistorique() != null ? dossier.getHistorique() : "");
        historiqueArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        historiqueArea.setLineWrap(true);
        historiqueArea.setWrapStyleWord(true);
        historiqueArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scroll = new JScrollPane(historiqueArea);

        JButton saveBtn = new JButton("Enregistrer les modifications");
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveBtn.addActionListener(e -> {
            try {
                String modifiedBy = (principal != null && principal.fullName() != null)
                        ? principal.fullName()
                        : "unknown";
                service.updateHistorique(dossier.getId(), historiqueArea.getText(), modifiedBy);
                JOptionPane.showMessageDialog(this, "Historique mis à jour avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(saveBtn);

        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}