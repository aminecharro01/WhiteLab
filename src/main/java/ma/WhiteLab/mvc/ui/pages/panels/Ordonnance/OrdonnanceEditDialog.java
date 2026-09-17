package ma.WhiteLab.mvc.ui.pages.panels.Ordonnance;

import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceEditDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY = new Color(0x0E, 0xA5, 0xA5);
    private static final Color ACCENT = new Color(40, 167, 69);
    private static final Color WARNING = new Color(220, 53, 69);

    private final Ordonnance ordonnance;
    private final boolean isNew;
    private final PrescriptionService prescriptionService;

    private JTextField dateField;
    private JTable prescriptionsTable;
    private DefaultTableModel tableModel;

    private boolean confirmed = false;

    public OrdonnanceEditDialog(Window owner, Ordonnance ordonnance, boolean isNew, PrescriptionService prescriptionService) {
        super(owner, isNew ? "Nouvelle Ordonnance" : "Modifier Ordonnance", ModalityType.APPLICATION_MODAL);
        this.ordonnance = ordonnance != null ? ordonnance : new Ordonnance();
        this.isNew = isNew;
        this.prescriptionService = prescriptionService;

        if (this.ordonnance.getPrescriptions() == null) {
            this.ordonnance.setPrescriptions(new ArrayList<>());
        }

        setSize(1100, 680);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        // ====== Date Panel ======
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        datePanel.setBorder(BorderFactory.createTitledBorder("Date de l'ordonnance"));
        datePanel.setBackground(Color.WHITE);

        dateField = new JTextField(14);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateField.setForeground(Color.GRAY);
        dateField.setText("jj/MM/aaaa");
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (dateField.getText().equals("jj/MM/aaaa")) {
                    dateField.setText("");
                    dateField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (dateField.getText().isBlank()) {
                    dateField.setText("jj/MM/aaaa");
                    dateField.setForeground(Color.GRAY);
                }
            }
        });

        datePanel.add(new JLabel("Date :"));
        datePanel.add(dateField);

        // ====== Prescriptions Table ======
        String[] columns = {"Médicament", "Quantité", "Durée (jours)", "Posologie / Fréquence"};
        tableModel = new DefaultTableModel(columns, 0);
        prescriptionsTable = new JTable(tableModel);
        prescriptionsTable.setRowHeight(40);
        prescriptionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        prescriptionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        prescriptionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for medication name
        prescriptionsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Prescription p && p.getMedicament() != null) {
                    setText(p.getMedicament().getNom());
                    setToolTipText("Forme: " + p.getMedicament().getForme() + " | Durée: " + p.getDuree() + " jours");
                } else {
                    setText("—");
                }
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(prescriptionsTable);
        tableScroll.setPreferredSize(new Dimension(800, 350));

        // ====== Buttons for prescriptions ======
        JButton addBtn = new JButton("Ajouter une prescription existante");
        addBtn.setBackground(ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.addActionListener(e -> openPrescriptionSelector());

        JButton removeBtn = new JButton("Supprimer sélection");
        removeBtn.setBackground(WARNING);
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        removeBtn.addActionListener(e -> removeSelectedPrescription());

        JPanel tableControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        tableControls.setBackground(Color.WHITE);
        tableControls.add(addBtn);
        tableControls.add(removeBtn);

        JPanel prescriptionsPanel = new JPanel(new BorderLayout(12, 12));
        prescriptionsPanel.setBorder(BorderFactory.createTitledBorder("Prescriptions de l'ordonnance"));
        prescriptionsPanel.setBackground(Color.WHITE);
        prescriptionsPanel.add(tableScroll, BorderLayout.CENTER);
        prescriptionsPanel.add(tableControls, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(datePanel, BorderLayout.NORTH);
        centerPanel.add(prescriptionsPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ====== Bottom buttons ======
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setPreferredSize(new Dimension(140, 45));
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Valider Ordonnance");
        saveBtn.setPreferredSize(new Dimension(220, 50));
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            if (saveChanges()) {
                confirmed = true;
                dispose();
            }
        });

        bottomPanel.add(cancelBtn);
        bottomPanel.add(saveBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        if (isNew) {
            dateField.setText(LocalDate.now().format(DATE_FORMAT));
            dateField.setForeground(Color.BLACK);
        } else {
            if (ordonnance.getDateOrdonnance() != null) {
                dateField.setText(ordonnance.getDateOrdonnance().format(DATE_FORMAT));
                dateField.setForeground(Color.BLACK);
            }

            for (Prescription p : ordonnance.getPrescriptions()) {
                addPrescriptionLine(p);
            }
        }
    }

    private void addPrescriptionLine(Prescription p) {
        tableModel.addRow(new Object[]{
                p,
                p.getQte(),
                p.getDuree(),
                p.getFrequence() != null ? p.getFrequence() : ""
        });
    }

    private void openPrescriptionSelector() {
        try {
            List<Prescription> allPrescriptions = prescriptionService.getAll();
            Prescription selected = (Prescription) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une prescription existante :",
                    "Choisir prescription",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    allPrescriptions.toArray(),
                    null
            );

            if (selected != null) {
                addPrescriptionLine(selected);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement prescriptions : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedPrescription() {
        int row = prescriptionsTable.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Confirmez la suppression de cette prescription ?", "Supprimer", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(row);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean saveChanges() {
        try {
            String dateStr = dateField.getText().trim();
            if (dateStr.isEmpty() || dateStr.equals("jj/MM/aaaa")) {
                throw new IllegalArgumentException("La date de l'ordonnance est obligatoire");
            }

            LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
            ordonnance.setDateOrdonnance(date);

            List<Prescription> selected = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object rowValue = tableModel.getValueAt(i, 0);
                if (rowValue instanceof Prescription p) {
                    selected.add(p);
                }
            }

            if (selected.isEmpty()) {
                throw new IllegalArgumentException("L'ordonnance doit contenir au moins une prescription");
            }

            ordonnance.setPrescriptions(selected);
            return true;

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide. Utilisez : jj/MM/aaaa", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur inattendue : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Ordonnance getOrdonnance() {
        return ordonnance;
    }
}
