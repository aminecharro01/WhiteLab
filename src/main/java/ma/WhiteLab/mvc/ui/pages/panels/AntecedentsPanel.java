package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.ui.pages.panels.Antecedent.AntecedentFormPanel;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

public class AntecedentsPanel extends JPanel {

    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private final DossierMedical dossier;
    private final AntecedentService antecedentService;
    private final PatientService patientService;

    private DefaultTableModel model;
    private JTable table;

    public AntecedentsPanel(
            DossierMedical dossier,
            AntecedentService antecedentService,
            PatientService patientService) {

        this.dossier = dossier;
        this.antecedentService = antecedentService;
        this.patientService = patientService;

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

        JLabel title = new JLabel("Antécédents médicaux");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JButton addBtn = createIconButton("/static/icons/add.png", SUCCESS, "Associer un antécédent existant", "➕", 30);
        addBtn.addActionListener(e -> associateNewAntecedent());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Nom", "Catégorie", "Niveau de risque", "Description", "Actions"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? AntecedentDTO.class : String.class;
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
        cm.getColumn(0).setPreferredWidth(180);
        cm.getColumn(1).setPreferredWidth(140);
        cm.getColumn(2).setPreferredWidth(140);
        cm.getColumn(3).setPreferredWidth(250);
        cm.getColumn(4).setPreferredWidth(180);

        cm.getColumn(4).setCellRenderer(new ActionsColumnRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 4) return;

                int modelRow = table.convertRowIndexToModel(row);
                Object hidden = table.getValueAt(modelRow, 4);
                if (!(hidden instanceof AntecedentDTO dto)) return;

                Rectangle cellRect = table.getCellRect(row, col, true);
                int relX = e.getPoint().x - cellRect.x;

                if (relX >= 10 && relX <= 50) {
                    showDetails(dto);
                } else if (relX >= 110 && relX <= 150) {  // Seulement voir + supprimer
                    deleteAntecedent(dto, modelRow);
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
            List<AntecedentDTO> dtos = antecedentService.getAntecedentsByPatientId(dossier.getPat().getId());

            if (dtos.isEmpty()) {
                model.addRow(new Object[]{"—", "—", "—", "Aucun antécédent associé", null});
                return;
            }

            for (AntecedentDTO dto : dtos) {
                model.addRow(new Object[]{
                        dto.getNom(),
                        dto.getCategorie() != null ? dto.getCategorie() : "—",
                        dto.getNiveauRisque() != null ? dto.getNiveauRisque() : "—",
                        dto.getDescription() != null ? truncate(dto.getDescription(), 80) : "—",
                        dto
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"—", "—", "—", "Erreur de chargement", null});
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les antécédents :\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //                           CRUD OPERATIONS (adaptées)
    // ════════════════════════════════════════════════════════════════════════

    private void associateNewAntecedent() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Associer un antécédent existant",
                true
        );

        dialog.setContentPane(
                new AntecedentFormPanel(
                        dossier,
                        antecedentService,
                        patientService,
                        this::loadData
                )
        );

        dialog.setSize(600, 380);  // Plus petit, car moins de champs
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteAntecedent(AntecedentDTO dto, int modelRow) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Dissocier définitivement cet antécédent du patient ?\n\n" +
                        "Nom : " + dto.getNom() + "\n" +
                        "Catégorie : " + (dto.getCategorie() != null ? dto.getCategorie() : "—") + "\n" +
                        "Niveau de risque : " + (dto.getNiveauRisque() != null ? dto.getNiveauRisque() : "—") + "\n\n" +
                        "Cette action ne supprime pas l'antécédent, elle le dissocie seulement du patient.",
                "Confirmation dissociation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                Long patientId = dossier.getPat().getId();
                patientService.removeAntecedentFromPatient(patientId, dto.getId());
                model.removeRow(modelRow);
                JOptionPane.showMessageDialog(this,
                        "Antécédent dissocié avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la dissociation :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDetails(AntecedentDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Antécédent**\n\n");
        sb.append("Nom : ").append(dto.getNom()).append("\n");
        sb.append("Catégorie : ").append(dto.getCategorie() != null ? dto.getCategorie() : "—").append("\n");
        sb.append("Niveau de risque : ").append(dto.getNiveauRisque() != null ? dto.getNiveauRisque() : "—").append("\n\n");
        sb.append("Description :\n").append(dto.getDescription() != null ? dto.getDescription() : "—");

        JOptionPane.showMessageDialog(this, sb.toString(), "Détails de l'antécédent", JOptionPane.INFORMATION_MESSAGE);
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
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
        private final JButton btnDelete;  // Plus de bouton Éditer

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 20, 0));
            setOpaque(true);

            btnView   = createIconButton("/static/icons/open.png",   PRIMARY,  "Voir détails",   "👁", 28);
            btnDelete = createIconButton("/static/icons/delete.png", DANGER,   "Dissocier",      "🗑", 28);

            add(btnView);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Object hiddenValue = table.getValueAt(row, 4);
            String nomCell = (String) table.getValueAt(row, 0);

            boolean isEmptyRow = hiddenValue == null ||
                    !(hiddenValue instanceof AntecedentDTO) ||
                    "—".equals(nomCell) ||
                    "Aucun antécédent associé".equals(nomCell) ||
                    "Erreur de chargement".equals(nomCell);

            setBackground(isSelected ? LIGHT_GRAY : Color.WHITE);

            btnView.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}