package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.controllers.modules.patient.impl.PatientsControllerImpl;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Objects;

public class PatientFormPanel extends JPanel {

    private final PatientService patientService;
    private final DossierMedicalService dossierMedicalService;
    private final PatientsControllerImpl controller;
    private final UserPrincipal principal;
    private final PatientDTO dto; // null = create

    private JTextField nomField;
    private JTextField prenomField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> sexeBox;
    private JTextField dateNaissanceField;
    private JComboBox<Assurance> assuranceBox;

    public PatientFormPanel(
            PatientDTO dto,
            UserPrincipal principal,
            PatientService patientService,
            DossierMedicalService dossierMedicalService,
            PatientsControllerImpl controller) {

        this.dto = dto;
        this.principal = principal;
        this.patientService = patientService;
        this.dossierMedicalService = dossierMedicalService;
        this.controller = controller;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        if (dto != null) {
            fillForm(dto);
        }
    }

    // ================= HEADER =================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel(dto == null
                ? "Ajouter un patient"
                : "Modifier le patient");

        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(64, 120, 255));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    // ================= FORM =================

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(30, 0, 30, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nomField = createField();
        prenomField = createField();
        phoneField = createField();
        emailField = createField();
        dateNaissanceField = createField();
        sexeBox = new JComboBox<>(new String[]{"HOMME", "FEMME"});

        // Assurance field with null option
        assuranceBox = new JComboBox<>();
        assuranceBox.addItem(null); // no assurance
        for (Assurance a : Assurance.values()) {
            assuranceBox.addItem(a);
        }

        int y = 0;
        addRow(form, gbc, y++, "Nom *", nomField);
        addRow(form, gbc, y++, "Prénom *", prenomField);
        addRow(form, gbc, y++, "Téléphone", phoneField);
        addRow(form, gbc, y++, "Email", emailField);
        addRow(form, gbc, y++, "Sexe", sexeBox);
        addRow(form, gbc, y++, "Date de naissance (yyyy-mm-dd)", dateNaissanceField);
        addRow(form, gbc, y++, "Assurance", assuranceBox);

        return form;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc,
                        int y, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        gbc.weightx = 0;
    }

    private JTextField createField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setPreferredSize(new Dimension(250, 36));
        return tf;
    }

    // ================= FOOTER =================

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);

        JButton cancelBtn = createButton("Annuler", new Color(120, 120, 120));
        JButton saveBtn = createButton("Enregistrer", new Color(64, 120, 255));

        cancelBtn.addActionListener(e -> controller.refresh());
        saveBtn.addActionListener(e -> save());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        return footer;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        return btn;
    }

    // ================= SAVE =================

    private void save() {
        try {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();

            if (nom.isBlank() || prenom.isBlank()) {
                throw new IllegalArgumentException("Nom et prénom sont obligatoires.");
            }

            PatientDTO saveDto = dto != null ? dto : new PatientDTO();

            saveDto.setNom(nom);
            saveDto.setPrenom(prenom);
            saveDto.setTelephone(phoneField.getText().trim());
            saveDto.setEmail(emailField.getText().trim());

            String sexeStr = (String) sexeBox.getSelectedItem();
            saveDto.setSexe(sexeStr);

            Assurance ass = (Assurance) assuranceBox.getSelectedItem();
            saveDto.setAssurance(ass != null ? ass.name() : null);

            String dateStr = dateNaissanceField.getText().trim();
            if (!dateStr.isBlank()) {
                saveDto.setDateNaissance(LocalDate.parse(dateStr));
            }

            if (dto == null) {
                // === Création du patient ===
                patientService.createPatient(saveDto, principal);
                Long patientId = saveDto.getId();

                // === Création automatique du dossier SEULEMENT si c'est un médecin ===
                if (principal != null && principal.roles().contains(RoleR.MEDECIN)) {
                    try {
                        String creePar = Objects.toString(principal.fullName(), "Médecin inconnu").trim();
                        if (creePar.isEmpty()) {
                            creePar = "Médecin (ID: " + principal.id() + ")";
                        }

                        dossierMedicalService.createDossierForPatient(
                                patientId,
                                principal.id(),     // ID du médecin connecté
                                creePar
                        );

                        JOptionPane.showMessageDialog(this,
                                "Patient créé avec succès.\nDossier médical créé automatiquement.",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception dossierEx) {
                        JOptionPane.showMessageDialog(this,
                                "Patient créé, mais échec création dossier :\n" + dossierEx.getMessage(),
                                "Attention",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    // Cas non-médecin (admin, secrétaire, etc.)
                    JOptionPane.showMessageDialog(this,
                            "Patient créé avec succès.\n(Aucun dossier automatique créé – rôle non médecin)",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // === Mise à jour ===
                patientService.updatePatient(dto.getId(), saveDto);
                JOptionPane.showMessageDialog(this,
                        "Patient modifié avec succès.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            controller.refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement :\n" + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= FILL =================

    private void fillForm(PatientDTO d) {
        nomField.setText(d.getNom());
        prenomField.setText(d.getPrenom());
        phoneField.setText(Objects.toString(d.getTelephone(), ""));
        emailField.setText(Objects.toString(d.getEmail(), ""));
        sexeBox.setSelectedItem(Objects.toString(d.getSexe(), "HOMME"));
        assuranceBox.setSelectedItem(d.getAssurance() != null ? Assurance.valueOf(d.getAssurance()) : null);

        if (d.getDateNaissance() != null) {
            dateNaissanceField.setText(d.getDateNaissance().toString());
        }
    }
}