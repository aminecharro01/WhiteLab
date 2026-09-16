package ma.WhiteLab.mvc.ui.pages.panels.RDV;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class RendezVousFormPanel extends JPanel {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color PRIMARY = new Color(45, 118, 255);
    private static final Color GRAY = new Color(108, 117, 125);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private final Long dossierMedicalId;
    private final RendezVousService rendezVousService;
    private final ConsultationService consultationService;
    private final Runnable onSuccess;
    private final RendezVousDTO originalDto;

    private JTextField dateField;
    private JTextArea motifArea;
    private JComboBox statusCombo;
    private JTextArea noteArea;
    private JComboBox consultationCombo;

    public RendezVousFormPanel(
            DossierMedical dossierMedical,
            RendezVousDTO dto,
            RendezVousService rendezVousService,
            ConsultationService consultationService,
            Runnable onSuccess
    ) {
        if (dossierMedical == null)
            throw new IllegalArgumentException("Le dossier médical est obligatoire");

        this.dossierMedicalId = dossierMedical.getId();
        this.originalDto = dto;
        this.rendezVousService = rendezVousService;
        this.consultationService = consultationService;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadConsultations();

        if (dto != null) {
            fillForm(dto);
        } else {
            dateField.setText(LocalDateTime.now().plusDays(1).format(FORMAT));
            dateField.setForeground(TEXT_PRIMARY);
            statusCombo.setSelectedItem(Status.AUCUNE);
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel(
                originalDto == null ? "Nouveau rendez-vous" : "Modifier le rendez-vous"
        );
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Dossier médical ID : " + dossierMedicalId);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(GRAY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        dateField = createTextField("yyyy-MM-dd HH:mm");
        addLabeledField(form, gbc, 0, "Date et heure *", dateField);

        motifArea = createTextArea(3);
        addLabeledField(form, gbc, 1, "Motif *", new JScrollPane(motifArea));

        statusCombo = new JComboBox(Status.values());
        addLabeledField(form, gbc, 2, "Statut", statusCombo);

        noteArea = createTextArea(5);
        addLabeledField(form, gbc, 3, "Note médecin", new JScrollPane(noteArea));

        consultationCombo = new JComboBox();
        addLabeledField(form, gbc, 4, "Consultation (optionnel)", consultationCombo);

        return form;
    }

    private void addLabeledField(
            JPanel parent,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent field
    ) {
        gbc.gridy = row * 2;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 0, 2, 0);
        gbc.weighty = 0;

        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_PRIMARY);
        parent.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 0, 10, 0);

        if (field instanceof JScrollPane) {
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 0.2;
        } else {
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
        }
        parent.add(field, gbc);
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FIELD_FONT);
        tf.setText(placeholder);
        tf.setForeground(TEXT_SECONDARY);
        tf.setBorder(border());

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isBlank()) {
                    tf.setText(placeholder);
                    tf.setForeground(TEXT_SECONDARY);
                }
            }
        });
        return tf;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 40);
        ta.setFont(FIELD_FONT);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(border());
        return ta;
    }

    private Border border() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton saveBtn = new JButton("Enregistrer");
        saveBtn.setFont(BUTTON_FONT);
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> save());

        footer.add(saveBtn);
        return footer;
    }

    private void loadConsultations() {
        try {
            List consultations = consultationService.getConsultationsByDossierMedicalId(dossierMedicalId);

            consultationCombo.removeAllItems();
            consultationCombo.addItem("Aucune");

            for (Object obj : consultations) {
                ConsultationDTO c = (ConsultationDTO) obj;
                String label = c.getId() + " - " + c.getDate() + " - " + c.getStatus();
                consultationCombo.addItem(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            String dateStr = dateField.getText().trim();
            String motif = motifArea.getText().trim();

            if (dateStr.isBlank() || dateStr.equals("yyyy-MM-dd HH:mm"))
                throw new IllegalArgumentException("Date obligatoire");

            if (motif.isBlank())
                throw new IllegalArgumentException("Motif obligatoire");

            LocalDateTime date = LocalDateTime.parse(dateStr, FORMAT);

            RendezVousDTO dto = originalDto != null ? originalDto : new RendezVousDTO();
            dto.setDossierMedId(dossierMedicalId);
            dto.setDate(date);
            dto.setMotif(motif);
            dto.setStatus(((Status) statusCombo.getSelectedItem()).name());
            dto.setNoteMedecin(noteArea.getText().trim());

            Object selected = consultationCombo.getSelectedItem();
            if (selected != null && !selected.equals("Aucune")) {
                String selectedText = selected.toString();
                String idStr = selectedText.split(" - ")[0];
                dto.setConsultationId(Long.parseLong(idStr));
            } else {
                dto.setConsultationId(null);
            }

            if (originalDto == null)
                rendezVousService.createRendezVous(dto);
            else
                rendezVousService.updateRendezVous(originalDto.getId(), dto);

            JOptionPane.showMessageDialog(this,
                    "Rendez-vous enregistré avec succès",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            if (onSuccess != null) onSuccess.run();
            closeWindow();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillForm(RendezVousDTO dto) {
        if (dto.getDate() != null) {
            dateField.setText(dto.getDate().format(FORMAT));
            dateField.setForeground(TEXT_PRIMARY);
        }

        motifArea.setText(Objects.toString(dto.getMotif(), ""));
        noteArea.setText(Objects.toString(dto.getNoteMedecin(), ""));

        if (dto.getStatus() != null) {
            statusCombo.setSelectedItem(Status.valueOf(dto.getStatus()));
        } else {
            statusCombo.setSelectedItem(Status.AUCUNE);
        }

        if (dto.getConsultationId() != null) {
            for (int i = 0; i < consultationCombo.getItemCount(); i++) {
                Object item = consultationCombo.getItemAt(i);
                if (item != null && !item.equals("Aucune")) {
                    String text = item.toString();
                    if (text.startsWith(dto.getConsultationId().toString() + " - ")) {
                        consultationCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void closeWindow() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
    }
}
