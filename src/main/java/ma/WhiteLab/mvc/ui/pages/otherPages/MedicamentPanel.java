package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MedicamentPanel extends JPanel {

    private final MedicamentService service;
    private final UserPrincipal principal;
    private final JTable table;
    private final DefaultTableModel model;

    public MedicamentPanel(MedicamentService service, UserPrincipal principal) {
        this.service = service;
        this.principal = principal;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titre
        JLabel title = new JLabel("Gestion des Médicaments", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0x0E, 0xA5, 0xA5));
        add(title, BorderLayout.NORTH);

        // Tableau
        String[] columns = {"ID", "Nom", "Laboratoire", "Forme", "Prix Unitaire", "Remboursable", "Stock"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0x0E, 0xA5, 0xA5));
        table.getTableHeader().setForeground(Color.WHITE);

        // Largeurs colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Boutons en bas
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addBtn = new JButton("Ajouter Médicament");
        addBtn.setBackground(new Color(40, 167, 69));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton refreshBtn = new JButton("Rafraîchir");
        refreshBtn.setBackground(new Color(23, 162, 184));
        refreshBtn.setForeground(Color.WHITE);

        bottomPanel.add(refreshBtn);
        bottomPanel.add(addBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> openMedicamentDialog(null)); // null = création
        refreshBtn.addActionListener(e -> loadData());

        // Double-clic pour modifier
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Long id = (Long) model.getValueAt(row, 0);
                        Medicament m = service.getById(id);
                        if (m != null) {
                            openMedicamentDialog(m); // édition
                        }
                    }
                }
            }
        });

        // Chargement initial
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<Medicament> medicaments = service.getAll();

        for (Medicament m : medicaments) {
            model.addRow(new Object[]{
                    m.getId(),
                    m.getNom(),
                    m.getLabo(),
                    m.getForme() != null ? m.getForme().name() : "-",
                    String.format("%.2f DH", m.getPrixUnitaire()),
                    m.isRemboursable() ? "Oui" : "Non",
                    m.getType()
            });
        }

        if (medicaments.isEmpty()) {
            model.addRow(new Object[]{"-", "Aucun médicament trouvé", "-", "-", "-", "-", "-"});
        }
    }

    private void openMedicamentDialog(Medicament medicamentToEdit) {
        boolean isEdit = medicamentToEdit != null;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Modifier Médicament" : "Nouveau Médicament", true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Champs
        JTextField nomField = new JTextField(isEdit ? medicamentToEdit.getNom() : "", 30);
        JTextField laboField = new JTextField(isEdit ? medicamentToEdit.getLabo() : "", 30);
        JComboBox<Forme> formeCombo = new JComboBox<>(Forme.values());
        if (isEdit && medicamentToEdit.getForme() != null) {
            formeCombo.setSelectedItem(medicamentToEdit.getForme());
        }
        JTextField prixField = new JTextField(isEdit ? String.valueOf(medicamentToEdit.getPrixUnitaire()) : "", 15);
        JCheckBox remboursableCheck = new JCheckBox("Remboursable", isEdit && medicamentToEdit.isRemboursable());

        int row = 0;
        addLabeledField(content, "Nom :", nomField, gbc, row++);
        addLabeledField(content, "Laboratoire :", laboField, gbc, row++);
        addLabeledField(content, "Forme :", formeCombo, gbc, row++);
        addLabeledField(content, "Prix unitaire (DH) :", prixField, gbc, row++);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        content.add(remboursableCheck, gbc);
        row++;

        // Boutons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton(isEdit ? "Mettre à jour" : "Créer");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        gbc.gridy = row;
        content.add(buttons, gbc);

        // Action Sauvegarde
        saveBtn.addActionListener(e -> {
            try {
                double prix = Double.parseDouble(prixField.getText().trim());

                Medicament m = isEdit ? medicamentToEdit : new Medicament();
                m.setNom(nomField.getText().trim());
                m.setLabo(laboField.getText().trim());
                m.setForme((Forme) formeCombo.getSelectedItem());
                m.setPrixUnitaire(prix);
                m.setRemboursable(remboursableCheck.isSelected());

                if (isEdit) {
                    service.update(m);
                    JOptionPane.showMessageDialog(dialog, "Médicament mis à jour !");
                } else {
                    service.create(m);
                    JOptionPane.showMessageDialog(dialog, "Médicament créé !");
                }

                dialog.dispose();
                loadData(); // Rafraîchir tableau

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Prix et stock doivent être des nombres valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void addLabeledField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(field, gbc);
    }
}