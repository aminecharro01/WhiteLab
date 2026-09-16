package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.ui.pages.panels.Certificat.CertificatDialog;
import ma.WhiteLab.service.modules.certificat.api.CertificatService;
import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CertificatsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private final DossierMedical dossier;
    private final CertificatService certificatService;

    private DefaultTableModel model;
    private JTable table;

    public CertificatsPanel(DossierMedical dossier, CertificatService certificatService) {
        this.dossier = dossier;
        this.certificatService = certificatService;

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

        JLabel title = new JLabel("Certificats médicaux");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JButton addBtn = createIconButton("/static/icons/add.png", SUCCESS, "Nouveau certificat", "➕", 30);
        addBtn.addActionListener(e -> createNewCertificat());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Date début", "Date fin", "Durée repos (jours)", "Actions"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? CertificatDTO.class : String.class;
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
        cm.getColumn(1).setPreferredWidth(140);
        cm.getColumn(2).setPreferredWidth(160);
        cm.getColumn(3).setPreferredWidth(180);

        cm.getColumn(3).setCellRenderer(new ActionsColumnRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 3) return;

                int modelRow = table.convertRowIndexToModel(row);
                Object hidden = table.getValueAt(modelRow, 3);
                if (!(hidden instanceof CertificatDTO dto)) return;

                Rectangle cellRect = table.getCellRect(row, col, true);
                int relX = e.getPoint().x - cellRect.x;

                if (relX >= 10 && relX <= 50) {
                    showDetails(dto);
                } else if (relX >= 60 && relX <= 100) {
                    editCertificat(dto);
                } else if (relX >= 110 && relX <= 150) {
                    deleteCertificat(dto, modelRow);
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
            List<CertificatDTO> dtos = certificatService.getCertificatsByDossierMedicalId(dossier.getId());

            if (dtos.isEmpty()) {
                model.addRow(new Object[]{"—", "—", "—", "Aucun certificat"});
                return;
            }

            for (CertificatDTO dto : dtos) {
                model.addRow(new Object[]{
                        formatDate(dto.getDateDebut()),
                        formatDate(dto.getDateFin()),
                        dto.getDureeRepos() + " jours",
                        dto  // DTO stocké ici (colonne 3)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"—", "—", "—", "Erreur de chargement"});
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger les certificats :\n" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────

    private void createNewCertificat() {
        CertificatDialog dialog = new CertificatDialog(
                SwingUtilities.getWindowAncestor(this),
                new Certificat(),  // entité vide pour compatibilité dialog
                true
        );

        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                CertificatDTO dto = dialog.getResultAsDTO();
                if (dto != null) {
                    dto.setDossierMedicalId(dossier.getId());
                    certificatService.createCertificat(dto);
                    loadData();
                    JOptionPane.showMessageDialog(this, "Certificat créé", "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (ValidationException vex) {
                JOptionPane.showMessageDialog(this, vex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Échec création :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCertificat(CertificatDTO dto) {
        CertificatDialog dialog = new CertificatDialog(
                SwingUtilities.getWindowAncestor(this),
                dto.toEntity(),  // conversion DTO → Entity (tu as la méthode)
                false
        );

        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                CertificatDTO updated = dialog.getResultAsDTO();
                if (updated != null) {
                    updated.setId(dto.getId());
                    certificatService.updateCertificat(dto.getId(), updated);
                    loadData();
                    JOptionPane.showMessageDialog(this, "Certificat modifié", "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (ValidationException vex) {
                JOptionPane.showMessageDialog(this, vex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Échec modification :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCertificat(CertificatDTO dto, int modelRow) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Supprimer définitivement ce certificat ?\n\n" +
                        "Période : " + formatDate(dto.getDateDebut()) + " → " + formatDate(dto.getDateFin()) + "\n" +
                        "Durée : " + dto.getDureeRepos() + " jours\n\n" +
                        "Cette action est irréversible.",
                "Confirmation suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                certificatService.deleteCertificat(dto.getId());
                model.removeRow(modelRow);
                JOptionPane.showMessageDialog(this, "Certificat supprimé", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur suppression :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDetails(CertificatDTO dto) {
        String contenu = (dto.getContenu() != null && !dto.getContenu().trim().isEmpty())
                ? dto.getContenu().trim()
                : "(Aucun détail particulier)";

        JOptionPane.showMessageDialog(this,
                String.format("**Certificat médical**\n\n" +
                                "Période : %s → %s\n" +
                                "Durée de repos : %d jours\n\n" +
                                "Contenu :\n%s",
                        formatDate(dto.getDateDebut()),
                        formatDate(dto.getDateFin()),
                        dto.getDureeRepos(),
                        contenu),
                "Détails du certificat",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "—";
    }

    // Méthode identique à PatientsPanel pour les boutons avec icônes
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

    // Renderer aligné sur PatientsPanel (icônes réelles + hover)
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
            Object hiddenValue = table.getValueAt(row, 3);
            String dateDebutCell = (String) table.getValueAt(row, 0);

            boolean isEmptyRow = hiddenValue == null ||
                    !(hiddenValue instanceof CertificatDTO) ||
                    "—".equals(dateDebutCell) ||
                    "Erreur de chargement".equals(dateDebutCell) ||
                    "Aucun certificat".equals(dateDebutCell);

            setBackground(isSelected ? LIGHT_GRAY : Color.WHITE);

            btnView.setVisible(!isEmptyRow);
            btnEdit.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}