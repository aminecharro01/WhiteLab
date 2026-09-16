package ma.WhiteLab.mvc.ui.pages.panels.Consultation;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class NouvelleConsultationPanel extends JPanel {

    private final ConsultationService consultationService;
    private final Long dossierMedicalId;
    private final Long consultationId;   // ← pour édition
    private final UserPrincipal principal;
    private final Runnable onSuccessCallback;

    // Champs du formulaire
    private JTextField motifField;
    private JTextArea examenArea;
    private JTextArea diagnosticArea;
    private JTextArea conduiteArea;
    private JTextArea observationsArea;

    public NouvelleConsultationPanel(
            ConsultationService consultationService,
            Long dossierMedicalId,
            UserPrincipal principal,
            Runnable onSuccessCallback) {
        this(consultationService, dossierMedicalId, null, principal, onSuccessCallback);
    }

    public NouvelleConsultationPanel(
            ConsultationService consultationService,
            Long dossierMedicalId,
            Long consultationId,
            UserPrincipal principal,
            Runnable onSuccessCallback) {

        this.consultationService = consultationService;
        this.dossierMedicalId = dossierMedicalId;
        this.consultationId = consultationId;
        this.principal = principal;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        loadConsultationIfEditing();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(
                consultationId != null ? "Éditer Consultation" : "Nouvelle Consultation",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 120, 255));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Motif
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Motif de consultation * :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        motifField = new JTextField(40);
        motifField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(motifField, gbc);

        // Examen clinique
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0;
        formPanel.add(new JLabel("Examen clinique :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        examenArea = new JTextArea(5, 40); examenArea.setLineWrap(true); examenArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(examenArea), gbc);

        // Diagnostic
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JLabel("Diagnostic :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        diagnosticArea = new JTextArea(5, 40); diagnosticArea.setLineWrap(true); diagnosticArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(diagnosticArea), gbc);

        // Conduite à tenir
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JLabel("Conduite à tenir :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        conduiteArea = new JTextArea(5, 40); conduiteArea.setLineWrap(true); conduiteArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(conduiteArea), gbc);

        // Observations complémentaires
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JLabel("Observations complémentaires :"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        observationsArea = new JTextArea(4, 40); observationsArea.setLineWrap(true); observationsArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(observationsArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // Boutons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnEnregistrer = new JButton(
                consultationId != null ? "Mettre à jour Consultation" : "Enregistrer Consultation"
        );

        btnAnnuler.setPreferredSize(new Dimension(140, 40));
        btnEnregistrer.setPreferredSize(new Dimension(220, 40));
        btnEnregistrer.setBackground(new Color(40, 167, 69));
        btnEnregistrer.setForeground(Color.WHITE);
        btnEnregistrer.setFocusPainted(false);

        btnAnnuler.addActionListener(e -> onCancel());
        btnEnregistrer.addActionListener(e -> saveConsultation());

        buttonsPanel.add(btnAnnuler);
        buttonsPanel.add(btnEnregistrer);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadConsultationIfEditing() {
        if (consultationId == null) return;

        try {
            ConsultationDTO dto = consultationService.getConsultationById(consultationId);
            if (dto != null) {
                motifField.setText(dto.getNotes());

                String obs = dto.getObservationsMedecin();
                if (obs != null && !obs.isEmpty()) {
                    String[] parts = obs.split("\n\n");
                    for (String part : parts) {
                        if (part.startsWith("Examen clinique :")) {
                            examenArea.setText(part.replace("Examen clinique :", "").trim());
                        } else if (part.startsWith("Diagnostic :")) {
                            diagnosticArea.setText(part.replace("Diagnostic :", "").trim());
                        } else if (part.startsWith("Conduite à tenir :")) {
                            conduiteArea.setText(part.replace("Conduite à tenir :", "").trim());
                        } else if (part.startsWith("Observations complémentaires :")) {
                            observationsArea.setText(part.replace("Observations complémentaires :", "").trim());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger la consultation :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConsultation() {
        String motif = motifField.getText().trim();
        if (motif.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le motif de consultation est obligatoire !",
                    "Champ requis", JOptionPane.WARNING_MESSAGE);
            motifField.requestFocus();
            return;
        }

        try {
            ConsultationDTO dto = new ConsultationDTO();
            dto.setDossierMedicalId(dossierMedicalId);
            dto.setNotes(motif);
            dto.setObservationsMedecin(
                    "Examen clinique :\n" + examenArea.getText().trim() + "\n\n" +
                            "Diagnostic :\n" + diagnosticArea.getText().trim() + "\n\n" +
                            "Conduite à tenir :\n" + conduiteArea.getText().trim() + "\n\n" +
                            "Observations complémentaires :\n" + observationsArea.getText().trim()
            );
            dto.setCreePar(principal.fullName());
            dto.setDate(LocalDateTime.now());
            dto.setDateCreation(LocalDateTime.now());
            dto.setStatus("EN_COURS");

            if (consultationId == null) {
                consultationService.createConsultation(dto);
            } else {
                consultationService.updateConsultation(consultationId, dto);
            }

            JOptionPane.showMessageDialog(this, "Consultation enregistrée avec succès !",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccessCallback != null) onSuccessCallback.run();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de validation :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment annuler ?", "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            Container topParent = SwingUtilities.getWindowAncestor(this);
            if (topParent instanceof JDialog) ((JDialog) topParent).dispose();
        }
    }
}
