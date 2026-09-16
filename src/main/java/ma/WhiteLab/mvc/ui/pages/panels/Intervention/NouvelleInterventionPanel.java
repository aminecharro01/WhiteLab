package ma.WhiteLab.mvc.ui.pages.panels.Intervention;

import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NouvelleInterventionPanel extends JPanel {

    private final InterventionMedecinService interventionService;
    private final Long consultationId;
    private final UserPrincipal principal;
    private final Runnable onSuccess;

    private JComboBox<ActeMedical> acteCombo;
    private JTextField prixField;
    private JTextField numDentField;
    private JLabel prixSuggestionLabel;

    private JButton saveBtn;
    private JButton cancelBtn;

    public NouvelleInterventionPanel(
            InterventionMedecinService interventionService,
            Long consultationId,
            UserPrincipal principal,
            Runnable onSuccess,
            List<ActeMedical> actesDisponibles) {

        this.interventionService = interventionService;
        this.consultationId = consultationId;
        this.principal = principal;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(actesDisponibles), BorderLayout.CENTER);
        add(buildButtonsPanel(), BorderLayout.SOUTH);

        // Pré-sélection du premier acte si disponible
        if (!actesDisponibles.isEmpty()) {
            acteCombo.setSelectedIndex(0);
            updatePrixSuggestion();
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Nouvelle Intervention");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 60, 100));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildForm(List<ActeMedical> actesDisponibles) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Acte médical
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Acte médical * :"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        acteCombo = new JComboBox<>(actesDisponibles.toArray(new ActeMedical[0]));
        acteCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ActeMedical acte) {
                    String text = acte.getLibelle();
                    if (acte.getCategorie() != null && !acte.getCategorie().isBlank()) {
                        text += " (" + acte.getCategorie() + ")";
                    }
                    setText(text);
                }
                return this;
            }
        });
        acteCombo.addActionListener(e -> updatePrixSuggestion());
        panel.add(acteCombo, gbc);

        // Prix suggéré
        gbc.gridx = 1;
        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 8, 10);
        prixSuggestionLabel = new JLabel(" ");
        prixSuggestionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        prixSuggestionLabel.setForeground(new Color(100, 100, 120));
        panel.add(prixSuggestionLabel, gbc);

        // Prix patient
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(new JLabel("Prix patient * :"), gbc);

        gbc.gridx = 1;
        prixField = new JTextField(12);
        prixField.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(prixField, gbc);

        // Numéro dent
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Numéro dent :"), gbc);

        gbc.gridx = 1;
        numDentField = new JTextField(8);
        numDentField.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(numDentField, gbc);

        return panel;
    }

    private JPanel buildButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 230)));

        cancelBtn = new JButton("Annuler");
        cancelBtn.setPreferredSize(new Dimension(110, 38));
        cancelBtn.addActionListener(e -> closeDialog());  // ← Correction ici

        saveBtn = new JButton("Enregistrer");
        saveBtn.setPreferredSize(new Dimension(140, 38));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveIntervention());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        return panel;
    }

    /**
     * Ferme la boîte de dialogue parente.
     */
    private void closeDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private void updatePrixSuggestion() {
        ActeMedical selected = (ActeMedical) acteCombo.getSelectedItem();
        if (selected != null && selected.getPrixDeBase() != 0) {
            prixSuggestionLabel.setText("Prix de base suggéré : " + selected.getPrixDeBase() + " DH");
            // Décommentez si vous voulez pré-remplir automatiquement
            // prixField.setText(String.valueOf(selected.getPrixDeBase()));
        } else {
            prixSuggestionLabel.setText(" ");
        }
    }

    private void saveIntervention() {
        ActeMedical selectedActe = (ActeMedical) acteCombo.getSelectedItem();
        if (selectedActe == null) {
            showError("Veuillez sélectionner un acte médical.");
            acteCombo.requestFocusInWindow();
            return;
        }

        String prixText = prixField.getText().trim().replace(",", ".");
        if (prixText.isEmpty()) {
            showError("Le prix patient est obligatoire.");
            prixField.requestFocusInWindow();
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixText);
            if (prix < 0) {
                showError("Le prix ne peut pas être négatif.");
                prixField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Prix invalide.\nExemple : 250 ou 250.50");
            prixField.requestFocusInWindow();
            prixField.selectAll();
            return;
        }

        Integer numDent = null;
        String numText = numDentField.getText().trim();
        if (!numText.isEmpty()) {
            try {
                numDent = Integer.parseInt(numText);
                if (numDent < 1 || numDent > 99) {
                    showError("Numéro de dent semble invalide (1–99).");
                    numDentField.requestFocusInWindow();
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Numéro de dent invalide.");
                numDentField.requestFocusInWindow();
                numDentField.selectAll();
                return;
            }
        }

        try {
            InterventionDTO dto = new InterventionDTO();
            dto.setActeMedicalId(selectedActe.getId());
            dto.setConsultationId(consultationId);
            dto.setPrixDePatient(prix);
            dto.setNumDent(numDent != null ? numDent : 0);
            dto.setCreePar(principal != null ? principal.fullName() : "Système");

            interventionService.createIntervention(dto);

            JOptionPane.showMessageDialog(this,
                    "Intervention enregistrée avec succès.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            // Exécute le callback (ferme l'onglet + refresh consultations)
            if (onSuccess != null) {
                onSuccess.run();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'enregistrement :\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
    }
}