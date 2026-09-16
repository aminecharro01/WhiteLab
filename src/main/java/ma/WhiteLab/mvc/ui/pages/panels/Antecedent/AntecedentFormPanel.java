package ma.WhiteLab.mvc.ui.pages.panels.Antecedent;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class AntecedentFormPanel extends JPanel {

    private static final Color PRIMARY = new Color(64, 120, 255);

    private final AntecedentService antecedentService;
    private final PatientService patientService;
    private final Runnable onSuccess;
    private final DossierMedical dossierMedical;

    private JComboBox<AntecedentDTO> antecedentComboBox;

    public AntecedentFormPanel(
            DossierMedical dossierMedical,
            AntecedentService antecedentService,
            PatientService patientService,
            Runnable onSuccess) {

        if (dossierMedical == null || dossierMedical.getPat() == null) {
            throw new IllegalArgumentException("Le patient est obligatoire");
        }

        this.dossierMedical = dossierMedical;
        this.antecedentService = antecedentService;
        this.patientService = patientService;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadAllAntecedents();
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Associer un antécédent existant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(PRIMARY);

        header.add(title, BorderLayout.WEST);
        return header;
    }

    // ================= FORM =================
    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(40, 0, 40, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        antecedentComboBox = new JComboBox<>();
        antecedentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AntecedentDTO dto) {
                    setText(dto.getNom() + " (" + dto.getCategorie() + " - " + dto.getNiveauRisque() + ")");
                }
                return this;
            }
        });

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(new JLabel("Antécédent à associer *"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(antecedentComboBox, gbc);

        return form;
    }

    private void loadAllAntecedents() {
        try {
            List<AntecedentDTO> allAntecedents = antecedentService.getAllAntecedents();
            for (AntecedentDTO dto : allAntecedents) {
                antecedentComboBox.addItem(dto);
            }

            if (allAntecedents.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun antécédent existant dans la base.\nContactez l'administrateur.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur chargement des antécédents :\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= FOOTER =================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);

        JButton cancelBtn = createButton("Annuler", new Color(120, 120, 120));
        JButton saveBtn = createButton("Associer", PRIMARY);

        cancelBtn.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        saveBtn.addActionListener(e -> associate());

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

    // ================= ASSOCIATION =================
    private void associate() {
        try {
            AntecedentDTO selected = (AntecedentDTO) antecedentComboBox.getSelectedItem();

            if (selected == null) {
                throw new IllegalArgumentException("Veuillez sélectionner un antécédent.");
            }

            Long patientId = dossierMedical.getPat().getId();
            Long antecedentId = selected.getId();

            // Vérifier si déjà associé (optionnel mais recommandé)
            List<AntecedentDTO> current = antecedentService.getAntecedentsByPatientId(patientId);
            boolean alreadyExists = current.stream().anyMatch(a -> a.getId().equals(antecedentId));

            if (alreadyExists) {
                JOptionPane.showMessageDialog(this,
                        "Cet antécédent est déjà associé au patient.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            patientService.addAntecedentToPatient(patientId, antecedentId);

            JOptionPane.showMessageDialog(this,
                    "Antécédent associé avec succès !",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccess != null) {
                onSuccess.run();
            }

            SwingUtilities.getWindowAncestor(this).dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'association :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}