package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.ui.pages.panels.RDV.RendezVousFormPanel;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RendezVousPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private final DossierMedical dossier;
    private final RendezVousService rendezVousService;
    private final ConsultationService consultationService;

    private DefaultTableModel model;
    private JTable table;

    public RendezVousPanel(DossierMedical dossier, RendezVousService rendezVousService, ConsultationService consultationService) {
        this.dossier = dossier;
        this.rendezVousService = rendezVousService;
        this.consultationService = consultationService;

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

        JLabel title = new JLabel("Rendez-vous");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JButton addBtn = createIconButton("/static/icons/add.png", SUCCESS, "Nouveau rendez-vous", "➕", 30);
        addBtn.addActionListener(e -> createNewRendezVous());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Date & Heure", "Motif", "Statut", "Note Médecin", "Actions"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? RendezVousDTO.class : String.class;
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
        cm.getColumn(0).setPreferredWidth(160);
        cm.getColumn(1).setPreferredWidth(220);
        cm.getColumn(2).setPreferredWidth(120);
        cm.getColumn(3).setPreferredWidth(200);
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
                if (!(hidden instanceof RendezVousDTO dto)) return;

                Rectangle cellRect = table.getCellRect(row, col, true);
                int relX = e.getPoint().x - cellRect.x;

                if (relX >= 10 && relX <= 50) {
                    showDetails(dto);
                } else if (relX >= 60 && relX <= 100) {
                    editRendezVous(dto);
                } else if (relX >= 110 && relX <= 150) {
                    deleteRendezVous(dto, modelRow);
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
            List<RendezVousDTO> dtos = rendezVousService.getRendezVousByDossierMedId(dossier.getId());

            if (dtos.isEmpty()) {
                model.addRow(new Object[]{"—", "—", "—", "—", "Aucun rendez-vous"});
                return;
            }

            for (RendezVousDTO dto : dtos) {
                model.addRow(new Object[]{
                        formatDateTime(dto.getDate()),
                        dto.getMotif() != null ? truncate(dto.getMotif(), 50) : "—",
                        dto.getStatus() != null ? getStatusDisplay(dto.getStatus()) : "—",
                        dto.getNoteMedecin() != null ? truncate(dto.getNoteMedecin(), 60) : "—",
                        dto
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"—", "—", "—", "—", "Erreur de chargement"});
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les rendez-vous :\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createNewRendezVous() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Nouveau rendez-vous",
                true
        );

        dialog.setContentPane(
                new RendezVousFormPanel(
                        dossier,
                        null,
                        rendezVousService,
                        consultationService,
                        this::loadData
                )
        );

        dialog.setSize(580, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editRendezVous(RendezVousDTO dto) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Modifier rendez-vous",
                true
        );

        dialog.setContentPane(
                new RendezVousFormPanel(
                        dossier,
                        dto,
                        rendezVousService,
                        consultationService,
                        this::loadData
                )
        );

        dialog.setSize(580, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteRendezVous(RendezVousDTO dto, int modelRow) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Supprimer définitivement ce rendez-vous ?\n\n" +
                        "Date : " + formatDateTime(dto.getDate()) + "\n" +
                        "Motif : " + (dto.getMotif() != null ? truncate(dto.getMotif(), 60) : "—") + "\n\n" +
                        "Cette action est irréversible.",
                "Confirmation suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                rendezVousService.deleteRendezVous(dto.getId());
                model.removeRow(modelRow);
                JOptionPane.showMessageDialog(this,
                        "Rendez-vous supprimé avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur suppression :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDetails(RendezVousDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Rendez-vous**\n\n");
        sb.append("Date & Heure : ").append(formatDateTime(dto.getDate())).append("\n");
        sb.append("Motif : ").append(dto.getMotif() != null ? dto.getMotif() : "—").append("\n");
        sb.append("Statut : ").append(dto.getStatus() != null ? dto.getStatus() : "—").append("\n");
        sb.append("Note médecin :\n").append(dto.getNoteMedecin() != null ? dto.getNoteMedecin() : "—");

        JOptionPane.showMessageDialog(this, sb.toString(), "Détails du rendez-vous", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FORMAT) : "—";
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "—";
        return switch (status) {
            case "PLANIFIE" -> "<html><span style='color:#007bff'>Planifié</span></html>";
            case "EN_COURS" -> "<html><span style='color:#ffc107'>En cours</span></html>";
            case "REALISE" -> "<html><span style='color:#28a745'>Réalisé</span></html>";
            case "ANNULE" -> "<html><span style='color:#dc3545'>Annulé</span></html>";
            default -> status;
        };
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
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 12, 0));
            setOpaque(true);

            btnView   = createIconButton("/static/icons/open.png",   PRIMARY,  "Voir détails",   "👁", 28);
            btnEdit   = createIconButton("/static/icons/edit.png",   SUCCESS,  "Modifier",       "✏", 28);
            btnDelete = createIconButton("/static/icons/delete.png", DANGER,   "Supprimer",      "🗑", 28);

            add(btnView);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Object hiddenValue = table.getValueAt(row, 4);
            String motifCell = (String) table.getValueAt(row, 1);

            boolean isEmptyRow = hiddenValue == null ||
                    !(hiddenValue instanceof RendezVousDTO) ||
                    "Aucun rendez-vous".equals(motifCell) ||
                    "Erreur de chargement".equals(motifCell);

            setBackground(isSelected ? LIGHT_GRAY : Color.WHITE);

            btnView.setVisible(!isEmptyRow);
            btnEdit.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}
