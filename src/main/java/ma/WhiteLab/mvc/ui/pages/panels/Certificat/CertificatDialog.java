package ma.WhiteLab.mvc.ui.pages.panels.Certificat;

import lombok.Getter;
import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CertificatDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DATE_HINT = "jj/mm/aaaa";

    private JTextField txtDateDebut;
    private JTextField txtDateFin;
    private JSpinner spinnerDureeRepos;
    private JTextArea txtContenu;

    @Getter
    private boolean confirmed = false;
    private final Certificat certificat;
    private final boolean isCreation;

    public CertificatDialog(Window parent, Certificat certificat, boolean isCreation) {
        super(parent, isCreation ? "Nouveau Certificat" : "Modifier Certificat", ModalityType.APPLICATION_MODAL);
        this.certificat = certificat;
        this.isCreation = isCreation;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 520);
        setMinimumSize(new Dimension(520, 460));
        setLocationRelativeTo(parent);

        initComponents();
        loadData();

        txtDateDebut.requestFocusInWindow();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---------------- Form ----------------
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date début
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Date début * :"), gbc);

        txtDateDebut = createDateField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtDateDebut, gbc);

        // Date fin
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Date fin :"), gbc);

        txtDateFin = createDateField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(txtDateFin, gbc);

        // Durée repos
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Durée de repos (jours) * :"), gbc);

        spinnerDureeRepos = new JSpinner(new SpinnerNumberModel(3, 0, 365, 1));
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(spinnerDureeRepos, gbc);

        // Contenu
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formPanel.add(new JLabel("Motif / Contenu du certificat :"), gbc);

        gbc.gridy = 4; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        txtContenu = new JTextArea(10, 50);
        txtContenu.setLineWrap(true);
        txtContenu.setWrapStyleWord(true);
        txtContenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtContenu);
        formPanel.add(scroll, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ---------------- Boutons ----------------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = new JButton("Annuler");
        JButton btnSave = new JButton("Valider");

        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnSave.setPreferredSize(new Dimension(110, 38));
        btnSave.setBackground(new Color(64, 120, 255));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            if (validateAndSave()) {
                confirmed = true;
                dispose();
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JTextField createDateField() {
        JTextField field = new JTextField(12);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setText(DATE_HINT);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(DATE_HINT)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(DATE_HINT);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private void loadData() {
        if (!isCreation && certificat != null) {
            if (certificat.getDateDebut() != null) {
                txtDateDebut.setText(certificat.getDateDebut().format(DATE_FORMAT));
                txtDateDebut.setForeground(Color.BLACK);
            }
            if (certificat.getDateFin() != null) {
                txtDateFin.setText(certificat.getDateFin().format(DATE_FORMAT));
                txtDateFin.setForeground(Color.BLACK);
            }
            spinnerDureeRepos.setValue(certificat.getDureeRepos() != 0 ? certificat.getDureeRepos() : 0);
            txtContenu.setText(certificat.getContenu() != null ? certificat.getContenu() : "");
        } else {
            spinnerDureeRepos.setValue(3); // valeur par défaut raisonnable
        }
    }

    private boolean validateAndSave() {
        String debutStr = txtDateDebut.getText().trim();
        String finStr = txtDateFin.getText().trim();

        if (debutStr.equals(DATE_HINT)) debutStr = "";
        if (finStr.equals(DATE_HINT)) finStr = "";

        // Date début obligatoire
        if (debutStr.isEmpty()) {
            showError("La date de début est obligatoire.");
            txtDateDebut.requestFocus();
            return false;
        }

        LocalDate dateDebut;
        try {
            dateDebut = LocalDate.parse(debutStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            showError("Format de date début invalide (utilisez jj/mm/aaaa)");
            txtDateDebut.requestFocus();
            return false;
        }

        LocalDate dateFin = null;
        if (!finStr.isEmpty()) {
            try {
                dateFin = LocalDate.parse(finStr, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                showError("Format de date fin invalide (utilisez jj/mm/aaaa)");
                txtDateFin.requestFocus();
                return false;
            }
        }

        int duree = (Integer) spinnerDureeRepos.getValue();

        // Logique métier simple
        if (dateFin != null) {
            if (dateFin.isBefore(dateDebut)) {
                showError("La date de fin doit être postérieure ou égale à la date de début.");
                txtDateFin.requestFocus();
                return false;
            }
            if (duree != 0 && java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1 != duree) {
                int calculated = (int) java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
                int choice = JOptionPane.showConfirmDialog(this,
                        "La durée saisie (" + duree + ") ne correspond pas à la période (" + calculated + " jours).\nVoulez-vous corriger automatiquement ?",
                        "Incohérence durée", JOptionPane.YES_NO_CANCEL_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    spinnerDureeRepos.setValue(calculated);
                    duree = calculated;
                } else if (choice == JOptionPane.CANCEL_OPTION) {
                    return false;
                }
            }
        }

        // Sauvegarde dans l'entité
        certificat.setDateDebut(dateDebut);
        certificat.setDateFin(dateFin);
        certificat.setDureeRepos(duree);
        certificat.setContenu(txtContenu.getText().trim());

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
    }


    public CertificatDTO getResultAsDTO() {
        if (!confirmed) {
            return null;
        }

        CertificatDTO dto = new CertificatDTO();

        dto.setId(certificat.getId()); // null en création, présent en modification
        dto.setDateDebut(certificat.getDateDebut());
        dto.setDateFin(certificat.getDateFin());
        dto.setDureeRepos(certificat.getDureeRepos());
        dto.setContenu(certificat.getContenu());

        // Champs relationnels - à remplir correctement selon le contexte
        if (certificat.getDossierMedical() != null) {
            dto.setDossierMedicalId(certificat.getDossierMedical().getId());
        }

        // Si vous associez parfois à une consultation :
        // if (certificat.getConsultation() != null) {
        //     dto.setConsultationId(certificat.getConsultation().getId());
        // }

        // Champs d'audit (optionnels ici, souvent gérés côté service)
        if (isCreation) {
            dto.setDateCreation(LocalDateTime.now());
            // dto.setCreePar(...) → à passer via le controller si besoin
        } else {
            dto.setDateMiseAJour(LocalDateTime.now());
            // dto.setModifierPar(...)
        }

        return dto;
    }
}