package ma.WhiteLab.mvc.ui.pages.panels.Consultation;

import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class ConsultationFormPanel extends JPanel {

    private JTextField dateField;
    private JComboBox<String> statusCombo;
    private JTextArea notesArea;

    public ConsultationFormPanel(ConsultationDTO consultation) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(20);
        add(dateField, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Statut:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"EN_COURS", "TERMINEE", "ANNULEE"});
        add(statusCombo, gbc);

        // Notes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        notesArea = new JTextArea(5, 20);
        add(new JScrollPane(notesArea), gbc);

        if (consultation != null) {
            dateField.setText(consultation.getDate() != null ? consultation.getDate().toString() : "");
            statusCombo.setSelectedItem(consultation.getStatus());
            notesArea.setText(consultation.getNotes());
        } else {
            dateField.setText(LocalDateTime.now().toString());
        }
    }

    public ConsultationDTO getConsultationDTO(Long dossierId, String createdBy) {
        ConsultationDTO dto = new ConsultationDTO();
        dto.setDossierMedicalId(dossierId);
        try {
            dto.setDate(LocalDateTime.parse(dateField.getText()));
        } catch (Exception e) {
            // Handle parsing exception
            dto.setDate(LocalDateTime.now());
        }
        dto.setStatus((String) statusCombo.getSelectedItem());
        dto.setNotes(notesArea.getText());
        dto.setCreePar(createdBy);
        return dto;
    }
}
