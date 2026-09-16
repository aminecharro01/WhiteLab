package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.controllers.modules.patient.impl.PatientsControllerImpl;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatientsPanel extends JPanel {

    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PatientsControllerImpl controller;
    private final PatientService patientService;
    private final DossierMedicalService dossierMedicalService;
    private final UserPrincipal principal;

    private List<PatientDTO> patients;
    private JTable table;
    private static DefaultTableModel tableModel;
    private JTextField searchField;

    public PatientsPanel(PatientsControllerImpl controller,
                         PatientService patientService,
                         DossierMedicalService dossierMedicalService,
                         List<PatientDTO> patients,
                         UserPrincipal principal) {
        this.controller = controller;
        this.patientService = patientService;
        this.dossierMedicalService = dossierMedicalService;
        this.patients = patients;
        this.principal = principal;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Gestion des Patients");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY);

        JButton addBtn = iconBtn("/static/icons/add.png", PRIMARY, "Ajouter un patient", "➕", 30);
        addBtn.addActionListener(e -> controller.openCreatePatient(principal));

        searchField = new JTextField(26);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.putClientProperty("JTextField.placeholderText", "Nom, prénom, téléphone, email...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { search(); }
            @Override public void removeUpdate(DocumentEvent e) { search(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void search() {
                SwingUtilities.invokeLater(() -> performSearch(searchField.getText().trim()));
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);
        right.add(searchField);
        right.add(addBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {
                "ID", "Nom Prénom", "Date naissance",
                "Téléphone", "Email", "Sexe", "Assurance", "Actions"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Largeurs des colonnes
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(60);
        cm.getColumn(1).setPreferredWidth(240);
        cm.getColumn(2).setPreferredWidth(110);
        cm.getColumn(3).setPreferredWidth(110);
        cm.getColumn(4).setPreferredWidth(180);
        cm.getColumn(5).setPreferredWidth(70);
        cm.getColumn(6).setPreferredWidth(110);
        cm.getColumn(7).setPreferredWidth(180);

        // Centre les colonnes
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        cm.getColumn(0).setCellRenderer(center);
        cm.getColumn(2).setCellRenderer(center);
        cm.getColumn(5).setCellRenderer(center);
        cm.getColumn(6).setCellRenderer(center);

        // Renderer spécial pour la colonne Actions
        cm.getColumn(7).setCellRenderer(new ActionsColumnRenderer());

        // Gestion des clics sur la colonne Actions
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());

                if (row < 0 || column < 0) return;

                if (column == 7) { // Colonne Actions
                    int modelRow = table.convertRowIndexToModel(row);
                    Long patientId = (Long) tableModel.getValueAt(modelRow, 0);

                    Point p = e.getPoint();
                    int cellX = table.getCellRect(row, column, true).x;
                    int relX = p.x - cellX;

                    // 1. Voir le dossier
                    if (relX >= 10 && relX <= 50) {
                        controller.openPatientDossier(patientId, principal);
                    }
                    // 2. Modifier le patient
                    else if (relX >= 60 && relX <= 100) {
                        controller.openEditPatient(patientId, principal);
                    }
                    // 3. Supprimer patient + dossier
                    else if (relX >= 110 && relX <= 150) {
                        int choice = JOptionPane.showConfirmDialog(
                                PatientsPanel.this,
                                "Cette action va supprimer définitivement :\n" +
                                        "• Le patient\n" +
                                        "• Son dossier médical complet (consultations, ordonnances, certificats...)\n\n" +
                                        "Cette opération est irréversible.\n\nVoulez-vous vraiment continuer ?",
                                "Suppression définitive",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            try {
                                // Étape 1 : Suppression du dossier médical s'il existe
                                dossierMedicalService.getDossierByPatientId(patientId)
                                        .ifPresent(dm -> dossierMedicalService.deleteDossier(dm.getId()));

                                // Étape 2 : Suppression du patient
                                patientService.deletePatient(patientId, principal);

                                // Étape 3 : Rafraîchissement de la liste
                                refreshData(patientService.getAllPatients());

                                JOptionPane.showMessageDialog(PatientsPanel.this,
                                        "Patient et dossier médical supprimés avec succès.",
                                        "Suppression terminée",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(PatientsPanel.this,
                                        "Erreur lors de la suppression :\n" + ex.getMessage(),
                                        "Erreur",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        populateTable(patients);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void populateTable(List<PatientDTO> list) {
        tableModel.setRowCount(0);

        if (list == null || list.isEmpty()) {
            tableModel.addRow(new Object[]{
                    "-", "Aucun patient trouvé", "-", "-", "-", "-", "-", null
            });
            return;
        }

        for (PatientDTO dto : list) {
            String fullName = (Optional.ofNullable(dto.getPrenom()).orElse("") +
                    " " + Optional.ofNullable(dto.getNom()).orElse("").toUpperCase()).trim();
            if (fullName.isBlank()) fullName = "Patient inconnu";

            tableModel.addRow(new Object[]{
                    dto.getId(),
                    fullName,
                    Optional.ofNullable(dto.getDateNaissance()).map(d -> d.format(DATE_FORMAT)).orElse("-"),
                    Optional.ofNullable(dto.getTelephone()).orElse("-"),
                    Optional.ofNullable(dto.getEmail()).orElse("-"),
                    Optional.ofNullable(dto.getSexe()).orElse("-"),
                    Optional.ofNullable(dto.getAssurance()).orElse("-"),
                    dto
            });
        }
    }

    private void performSearch(String query) {
        if (query.isBlank()) {
            populateTable(patients);
            return;
        }

        String q = query.toLowerCase();
        populateTable(
                patients.stream()
                        .filter(dto ->
                                Optional.ofNullable(dto.getNom()).orElse("").toLowerCase().contains(q) ||
                                        Optional.ofNullable(dto.getPrenom()).orElse("").toLowerCase().contains(q) ||
                                        Optional.ofNullable(dto.getTelephone()).orElse("").toLowerCase().contains(q) ||
                                        Optional.ofNullable(dto.getEmail()).orElse("").toLowerCase().contains(q)
                        )
                        .collect(Collectors.toList())
        );
    }

    public void refreshData(List<PatientDTO> newPatients) {
        this.patients = newPatients;
        searchField.setText("");
        populateTable(newPatients);
    }

    // Bouton avec icône + hover effect
    private static JButton iconBtn(String iconPath, Color hoverBg, String tooltip, String fallbackText, int size) {
        JButton btn;
        URL url = PatientsPanel.class.getResource(iconPath);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallbackText);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        }

        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
        });

        return btn;
    }

    // Renderer personnalisé pour la colonne Actions
    public static class ActionsColumnRenderer extends JPanel implements TableCellRenderer {

        private final JButton btnView;
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 8, 0));
            setOpaque(true);

            btnView   = iconBtn("/static/icons/open.png",   PRIMARY,    "Voir le dossier",   "📂", 28);
            btnEdit   = iconBtn("/static/icons/edit.png",   SUCCESS,    "Modifier",          "✏️", 28);
            btnDelete = iconBtn("/static/icons/delete.png", DANGER,     "Supprimer",         "🗑️", 28);

            add(btnView);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            if (value == null || tableModel.getValueAt(row, 0).equals("-")) {
                setBackground(Color.WHITE);
                btnView.setVisible(false);
                btnEdit.setVisible(false);
                btnDelete.setVisible(false);
                return this;
            }

            setBackground(isSelected ? LIGHT_GRAY : Color.WHITE);

            btnView.setVisible(true);
            btnEdit.setVisible(true);
            btnDelete.setVisible(true);

            return this;
        }
    }
}