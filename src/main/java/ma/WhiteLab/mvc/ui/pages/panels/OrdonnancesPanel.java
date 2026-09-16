package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.impl.DossiersControllerImpl;
import ma.WhiteLab.mvc.ui.pages.otherPages.PrescriptionDetailPanel;
import ma.WhiteLab.mvc.ui.pages.panels.Ordonnance.OrdonnanceEditDialog;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdonnancesPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private final DossierMedical dossier;
    private final DossierMedicalService dossierService;
    private final PrescriptionService prescriptionService;
    private final MedicamentService medicamentService;
    private final DossiersController controller;

    private DefaultTableModel model;
    private JTable table;

    public OrdonnancesPanel(
            DossierMedical dossier,
            DossierMedicalService dossierService,
            PrescriptionService prescriptionService,
            MedicamentService medicamentService,
            DossiersController controller) {

        this.dossier = dossier;
        this.dossierService = dossierService;
        this.prescriptionService = prescriptionService;
        this.medicamentService = medicamentService;
        this.controller = controller;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Ordonnances");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JButton addBtn = createIconButton("/static/icons/add.png", SUCCESS, "Nouvelle ordonnance", "➕", 30);
        addBtn.addActionListener(e -> createNewOrdonnance());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Date", "Nombre de lignes", "Actions"};

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Ordonnance.class : String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(140);
        cm.getColumn(1).setPreferredWidth(300);
        cm.getColumn(2).setPreferredWidth(180);

        cm.getColumn(2).setCellRenderer(new ActionsColumnRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 2) return;

                int modelRow = table.convertRowIndexToModel(row);
                Object hidden = table.getValueAt(modelRow, 2);
                if (!(hidden instanceof Ordonnance ordonnance)) return;

                Rectangle cellRect = table.getCellRect(row, col, true);
                int relX = e.getPoint().x - cellRect.x;

                // Zone "Voir détails" (œil) → gauche
                if (relX >= 10 && relX <= 60) {
                    showOrdonnanceDetails(ordonnance);
                }
                // Zone "Supprimer" (poubelle) → droite
                else if (relX >= 80 && relX <= 140) {
                    deleteOrdonnance(ordonnance, modelRow);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void loadData() {
        model.setRowCount(0);

        try {
            List<Ordonnance> ordonnances = dossierService.getOrdonnances(dossier.getId());

            if (ordonnances == null || ordonnances.isEmpty()) {
                model.addRow(new Object[]{"—", "Aucune ordonnance enregistrée", null});
                return;
            }

            for (Ordonnance o : ordonnances) {
                int count = o.getPrescriptions() != null ? o.getPrescriptions().size() : 0;
                model.addRow(new Object[]{
                        formatDate(o.getDateOrdonnance()),
                        count + " prescription(s)",
                        o
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"—", "Erreur de chargement", null});
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les ordonnances :\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "—";
    }

    private void createNewOrdonnance() {
        if (medicamentService == null) {
            JOptionPane.showMessageDialog(this,
                    "Le service des médicaments n'est pas disponible.\nImpossible de créer une ordonnance.",
                    "Erreur critique", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Ordonnance newOrdonnance = new Ordonnance();
        newOrdonnance.setDossierMedical(dossier);

        OrdonnanceEditDialog editDialog = new OrdonnanceEditDialog(
                SwingUtilities.getWindowAncestor(this),
                newOrdonnance,
                true,  // isNew
                prescriptionService
        );

        editDialog.setVisible(true);

        if (editDialog.isConfirmed()) {
            try {
                ((DossiersControllerImpl) controller).createOrdonnance(newOrdonnance);
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Ordonnance créée avec succès (" + newOrdonnance.getPrescriptions().size() + " lignes)",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la création :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showOrdonnanceDetails(Ordonnance ordonnance) {
        JDialog detailDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Détails de l'ordonnance du " + formatDate(ordonnance.getDateOrdonnance()),
                true
        );

        PrescriptionDetailPanel detailPanel = new PrescriptionDetailPanel(ordonnance, prescriptionService);
        detailDialog.setContentPane(detailPanel);
        detailDialog.setSize(1000, 650);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }

    private void deleteOrdonnance(Ordonnance ordonnance, int modelRow) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Supprimer définitivement cette ordonnance ?\n\n" +
                        "Date : " + formatDate(ordonnance.getDateOrdonnance()) + "\n" +
                        "Nombre de prescriptions : " + (ordonnance.getPrescriptions() != null ? ordonnance.getPrescriptions().size() : 0) + "\n\n" +
                        "Cette action est irréversible.",
                "Confirmation suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                ((DossiersControllerImpl) controller).deleteOrdonnance(ordonnance.getId());
                model.removeRow(modelRow);
                JOptionPane.showMessageDialog(this,
                        "Ordonnance supprimée avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Erreur suppression :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createIconButton(String iconPath, Color hoverBg, String tooltip, String fallback, int size) {
        JButton btn;
        URL url = getClass().getResource(iconPath);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallback);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        }

        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(hoverBg);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
        });

        return btn;
    }

    private class ActionsColumnRenderer extends JPanel implements TableCellRenderer {

        private final JButton btnView;
        private final JButton btnDelete;

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 30, 0));  // ← ESPACEMENT AUGMENTÉ ICI (30 pixels)
            setOpaque(true);

            btnView   = createIconButton("/static/icons/open.png",   PRIMARY,  "Voir détails",   "👁", 28);
            btnDelete = createIconButton("/static/icons/delete.png", DANGER,   "Supprimer",      "🗑", 28);

            add(btnView);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Object hiddenValue = table.getValueAt(row, 2);
            String dateCell = (String) table.getValueAt(row, 0);

            boolean isEmptyRow = hiddenValue == null ||
                    !(hiddenValue instanceof Ordonnance) ||
                    "—".equals(dateCell) ||
                    "Aucune ordonnance enregistrée".equals(dateCell);

            setBackground(isSelected ? LIGHT_GRAY : Color.WHITE);

            btnView.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}