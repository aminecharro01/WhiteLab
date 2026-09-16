package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.mvc.ui.pages.otherPages.DossierDetailPanel;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;

public class SituationFinancierePanel extends JPanel implements DossierDetailPanel.RefreshablePanel {

    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color CREDIT_COLOR = new Color(200, 0, 0);

    private final DossierMedical dossier;
    private final SituationFinanciereService sfService;
    private final boolean isReadOnly;

    private SituationFinanciere currentSituation;

    private JTextField totalDesActesField;
    private JTextField totalPayeField;
    private JLabel creditField;
    private JComboBox<Status> statusComboBox;
    private JComboBox<PromoStatus> promoComboBox;
    private JButton saveButton;

    public SituationFinancierePanel(DossierMedical dossier, SituationFinanciereService sfService, boolean isReadOnly) {
        this.dossier = dossier;
        this.sfService = sfService;
        this.isReadOnly = isReadOnly;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildForm(), BorderLayout.CENTER);
        if (!isReadOnly) {
            add(buildButtonPanel(), BorderLayout.SOUTH);
        }

        refresh();
    }

    private JPanel buildForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.2;

        formPanel.add(createLabel("Total des actes (DH):"), gbc(gbc, 0, 0));
        formPanel.add(createLabel("Total payé (DH):"), gbc(gbc, 0, 1));
        formPanel.add(createLabel("Crédit (DH):"), gbc(gbc, 0, 2));
        formPanel.add(createLabel("Statut:"), gbc(gbc, 0, 3));
        formPanel.add(createLabel("Promotion:"), gbc(gbc, 0, 4));

        // Fields
        gbc.gridx = 1;
        gbc.weightx = 0.8;

        totalDesActesField = createTextField();
        totalPayeField = createTextField();
        creditField = new JLabel("0.00");
        creditField.setFont(FIELD_FONT.deriveFont(Font.BOLD));
        creditField.setForeground(CREDIT_COLOR);
        statusComboBox = new JComboBox<>(Status.values());
        promoComboBox = new JComboBox<>(PromoStatus.values());

        // Add listeners to update credit automatically
        DocumentListener creditUpdater = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCreditField(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCreditField(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCreditField(); }
        };
        totalDesActesField.getDocument().addDocumentListener(creditUpdater);
        totalPayeField.getDocument().addDocumentListener(creditUpdater);

        formPanel.add(totalDesActesField, gbc(gbc, 1, 0));
        formPanel.add(totalPayeField, gbc(gbc, 1, 1));
        formPanel.add(creditField, gbc(gbc, 1, 2));
        formPanel.add(statusComboBox, gbc(gbc, 1, 3));
        formPanel.add(promoComboBox, gbc(gbc, 1, 4));

        // Spacer to push everything to the top
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JPanel() {{ setBackground(Color.WHITE); }}, gbc);

        return formPanel;
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = new JButton("Enregistrer");
        saveButton.setFont(LABEL_FONT);
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveSituation());

        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void populateForm(SituationFinanciere sf) {
        this.currentSituation = sf;
        if (sf != null) {
            totalDesActesField.setText(String.format("%.2f", sf.getTotalDesActes()));
            totalPayeField.setText(String.format("%.2f", sf.getTotalPaye()));
            creditField.setText(String.format("%.2f", sf.getCredit()));
            statusComboBox.setSelectedItem(sf.getStatus());
            promoComboBox.setSelectedItem(sf.getEnPromo());
        } else {
            totalDesActesField.setText("0.00");
            totalPayeField.setText("0.00");
            creditField.setText("0.00");
            statusComboBox.setSelectedItem(Status.ACTIVE);
            promoComboBox.setSelectedItem(PromoStatus.AUCUNE);
        }
        updateCreditField();
        setFieldsEditable(!isReadOnly);
    }
    
    private void setFieldsEditable(boolean editable) {
        totalDesActesField.setEditable(false); // This field is now calculated by the service
        totalPayeField.setEditable(editable);
        statusComboBox.setEnabled(editable);
        promoComboBox.setEnabled(editable);
    }

    private void saveSituation() {
        try {
            boolean isNew = (currentSituation == null);
            SituationFinanciere sfToSave = isNew ? new SituationFinanciere() : currentSituation;

            if (isNew) {
                sfToSave.setDossierMedical(dossier);
            }

            sfToSave.setTotalPaye(parseFloat(totalPayeField.getText()));
            sfToSave.setStatus((Status) statusComboBox.getSelectedItem());
            sfToSave.setEnPromo((PromoStatus) promoComboBox.getSelectedItem());

            // Always recalculate credit before saving
            sfService.calculateCredit(sfToSave);

            if (isNew) {
                sfService.create(sfToSave);
                JOptionPane.showMessageDialog(this, "Situation financière créée avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                sfService.update(sfToSave);
                JOptionPane.showMessageDialog(this, "Situation financière mise à jour.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
            refresh(); // Refresh to get the created/updated entity with ID
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des montants valides.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCreditField() {
        try {
            float totalActes = parseFloat(totalDesActesField.getText());
            float totalPaye = parseFloat(totalPayeField.getText());
            float credit = totalActes - totalPaye;
            creditField.setText(String.format("%.2f", credit));
        } catch (NumberFormatException e) {
            creditField.setText("Invalide");
        }
    }

    @Override
    public void refresh() {
        try {
            SituationFinanciere sf = sfService.findByDossierMedicalId(dossier.getId());
            populateForm(sf);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de chargement de la situation financière.", "Erreur", JOptionPane.ERROR_MESSAGE);
            populateForm(null); // Show empty form on error
        }
    }

    // --- UTILS ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(15);
        field.setFont(FIELD_FONT);
        return field;
    }

    private GridBagConstraints gbc(GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }

    private float parseFloat(String text) {
        return Optional.ofNullable(text)
                .filter(s -> !s.isBlank())
                .map(s -> s.replace(',', '.'))
                .map(Float::parseFloat)
                .orElse(0.0f);
    }
}
