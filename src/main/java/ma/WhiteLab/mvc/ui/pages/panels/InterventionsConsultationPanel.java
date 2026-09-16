package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.panels.Intervention.NouvelleInterventionPanel;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InterventionsConsultationPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long consultationId;
    private final InterventionMedecinService interventionService;
    private final UserPrincipal principal;
    private final Runnable onDataChanged;
    private final ConsultationService consultationService;

    private DefaultTableModel tableModel;
    private JTable table;

    public InterventionsConsultationPanel(
            Long consultationId,
            InterventionMedecinService interventionService,
            UserPrincipal principal,
            Runnable onDataChanged,
            ConsultationService consultationService) {

        this.consultationId = consultationId;
        this.interventionService = interventionService;
        this.principal = principal;
        this.onDataChanged = onDataChanged;
        this.consultationService = consultationService;

        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(new JScrollPane(buildTable()), BorderLayout.CENTER);

        loadInterventions();
    }



    /* ================= HEADER ================= */

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Interventions pour cette consultation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 60, 100));

        JButton addBtn = new JButton("Nouvelle intervention");
        addBtn.setBackground(new Color(40, 167, 69));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> openInterventionForm(null));

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    /* ================= TABLE ================= */

    private JTable buildTable() {
        String[] columns = {
                "Acte", "Prix patient", "Dent", "Créé par", "Date", "Actions", "ID"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 5) return Component.class;
                return c == 6 ? Long.class : String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(52);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);

        TableColumn actionsCol = table.getColumnModel().getColumn(5);
        actionsCol.setCellRenderer(new ActionsColumnRenderer());
        actionsCol.setPreferredWidth(120);

        hideIdColumn();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 5) return;

                Long id = (Long) tableModel.getValueAt(row, 6);
                if (id == null) return;

                Rectangle cell = table.getCellRect(row, col, true);
                int x = e.getPoint().x - cell.x;

                // Check which button was clicked based on x position
                // This assumes the two buttons are roughly equal width
                if (x < cell.width / 2) { // Click on first half (Edit)
                    openInterventionForm(id);
                } else { // Click on second half (Delete)
                    deleteIntervention(id, row);
                }
            }
        });

        return table;
    }

    private void hideIdColumn() {
        TableColumn idCol = table.getColumnModel().getColumn(6);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);
    }

    /* ================= DATA ================= */

    private void loadInterventions() {
        tableModel.setRowCount(0);

        try {
            // 1. Fetch all available ActeMedical to avoid N+1 queries
            List<ActeMedical> actes = consultationService.getActesDisponibles();
            Map<Long, String> acteNames = actes.stream()
                    .collect(Collectors.toMap(ActeMedical::getId, ActeMedical::getLibelle));

            // 2. Fetch interventions for the current consultation
            List<InterventionDTO> list =
                    interventionService.getInterventionsByConsultationId(consultationId);

            if (list.isEmpty()) {
                tableModel.addRow(new Object[]{
                        "Aucune intervention", "—", "—", "—", "—", null, null
                });
                return;
            }

            // 3. Populate table with resolved names
            for (InterventionDTO dto : list) {
                String acteName = acteNames.getOrDefault(dto.getActeMedicalId(), "Acte Inconnu (ID: " + dto.getActeMedicalId() + ")");

                tableModel.addRow(new Object[]{
                        acteName,
                        String.format("%.2f DH", dto.getPrixDePatient()),
                        dto.getNumDent() > 0 ? String.valueOf(dto.getNumDent()) : "—",
                        dto.getCreePar() != null ? dto.getCreePar() : "—",
                        dto.getDateCreation() != null
                                ? dto.getDateCreation().format(DATE_FORMAT)
                                : "—",
                        null, // Placeholder for Actions renderer
                        dto.getId()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            tableModel.addRow(new Object[]{
                    "Erreur de chargement", "—", "—", "—", "—", e.getMessage(), null
            });
        }
    }

    /* ================= FORM ================= */

    private void openInterventionForm(Long id) {
        String title = (id == null)
                ? "Nouvelle intervention"
                : "Modifier intervention";

        // The dialog will be created here
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);

        // The onSuccess runnable now closes the dialog and refreshes data
        Runnable onSuccess = () -> {
            dialog.dispose();
            loadInterventions();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        };

        NouvelleInterventionPanel panel = new NouvelleInterventionPanel(
                interventionService,
                consultationId,
                principal,
                onSuccess,
                consultationService.getActesDisponibles()
        );

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    /* ================= DELETE ================= */

    private void deleteIntervention(Long id, int row) {
        if (JOptionPane.showConfirmDialog(
                this,
                "Supprimer cette intervention ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) return;

        try {
            interventionService.deleteIntervention(id);
            tableModel.removeRow(row);
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================= UTILS ================= */

    private JTabbedPane findParentTabbedPane() {
        Container c = getParent();
        while (c != null) {
            if (c instanceof JTabbedPane) return (JTabbedPane) c;
            c = c.getParent();
        }
        return null;
    }

    private JButton createIconButton(String iconPath, Color hoverBg, String tooltip, String fallback, int size) {
        JButton btn;
        java.net.URL url = getClass().getResource(iconPath);

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

    private class ActionsColumnRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 10, 0));
            setOpaque(true);

            btnEdit   = createIconButton("/static/icons/edit.png", new Color(200, 225, 255), "Modifier", "✏️", 24);
            btnDelete = createIconButton("/static/icons/delete.png", new Color(255, 210, 210), "Supprimer", "🗑️", 24);

            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Object idValue = table.getModel().getValueAt(row, 6);
            boolean isEmptyRow = idValue == null;

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            btnEdit.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}
