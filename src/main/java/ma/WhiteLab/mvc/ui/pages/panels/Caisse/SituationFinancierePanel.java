package ma.WhiteLab.mvc.ui.pages.panels.Caisse;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.caisse.api.FactureService;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

public class SituationFinancierePanel extends JPanel {

    private static final Color PRIMARY = new Color(0x0E, 0xA5, 0xA5); // Bleu unifié
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);

    private final SituationFinanciereService sfService;
    private final FactureService factureService;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionService;
    private final UserPrincipal principal;
    private final Runnable onOpenFactures;
    private final Runnable onBack;

    private DefaultTableModel model;
    private JTable table;

    public SituationFinancierePanel(SituationFinanciereService sfService,
                                    FactureService factureService,
                                    ConsultationService consultationService,
                                    InterventionMedecinService interventionService,
                                    UserPrincipal principal,
                                    Runnable onOpenFactures,
                                    Runnable onBack) {
        this.sfService = sfService;
        this.factureService = factureService;
        this.consultationService = consultationService;
        this.interventionService = interventionService;
        this.principal = principal;
        this.onOpenFactures = onOpenFactures;
        this.onBack = onBack;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionsPanel(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Situation Financière");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);

        JButton btnFactures = new JButton("Gérer les Factures");
        styleButton(btnFactures, new Color(255, 193, 7), Color.BLACK);
        btnFactures.addActionListener(e -> onOpenFactures.run());

        JButton btnBack = new JButton("← Retour");
        styleButton(btnBack, LIGHT_BG, Color.BLACK);
        btnBack.addActionListener(e -> onBack.run());

        right.add(btnFactures);
        right.add(btnBack);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Dossier ID", "Total Actes", "Payé", "Crédit", "Statut", "Actions"};

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 6 ? Long.class : String.class; }
        };

        table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(220, 230, 255));
        table.setSelectionForeground(Color.BLACK);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(60);
        cm.getColumn(1).setPreferredWidth(80);
        cm.getColumn(2).setPreferredWidth(100);
        cm.getColumn(3).setPreferredWidth(100);
        cm.getColumn(4).setPreferredWidth(100);
        cm.getColumn(5).setPreferredWidth(120);
        cm.getColumn(6).setPreferredWidth(140);

        ActionsRenderer actionsRenderer = new ActionsRenderer();
        cm.getColumn(6).setCellRenderer(actionsRenderer);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 6) return;

                Long id = (Long) model.getValueAt(row, 0);
                if (id == null) return;

                Rectangle cell = table.getCellRect(row, col, true);
                actionsRenderer.setSize(cell.width, cell.height);
                actionsRenderer.doLayout();
                int relX = e.getPoint().x - cell.x;
                int relY = e.getPoint().y - cell.y;
                Component clicked = actionsRenderer.getComponentAt(relX, relY);

                if (clicked == actionsRenderer.btnView) {
                    consultSF(id);
                } else if (clicked == actionsRenderer.btnEdit) {
                    editSF(id);
                } else if (clicked == actionsRenderer.btnDelete) {
                    deleteSF(id, row);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(Color.WHITE);

        JButton btnCreate = new JButton("Créer Situation");
        styleButton(btnCreate, SUCCESS, Color.WHITE);
        btnCreate.addActionListener(e -> createSF());

        JButton btnReset = new JButton("Réinitialiser");
        styleButton(btnReset, new Color(255, 193, 7), Color.BLACK);
        btnReset.addActionListener(e -> resetSF());

        JButton btnDelete = new JButton("Supprimer");
        styleButton(btnDelete, DANGER, Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelectedSF());

        panel.add(btnCreate);
        panel.add(btnReset);
        panel.add(btnDelete);
        return panel;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(160, 38));
    }

    private void loadData() {
        model.setRowCount(0);
        List<SituationFinanciere> list = sfService.findAll();
        for (SituationFinanciere sf : list) {
            model.addRow(new Object[]{
                    sf.getId(),
                    sf.getDossierMedical() != null ? sf.getDossierMedical().getId() : "-",
                    sf.getTotalDesActes(),
                    sf.getTotalPaye(),
                    sf.getCredit(),
                    sf.getStatus(),
                    sf.getId()  // caché pour actions
            });
        }
    }

    private void consultSF(Long id) {
        // À implémenter ou ouvrir un dialog de détails
        JOptionPane.showMessageDialog(this, "Consulter SF ID: " + id, "Détails", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editSF(Long id) {
        // À implémenter : ouvrir un dialog d'édition
        JOptionPane.showMessageDialog(this, "Éditer SF ID: " + id, "Édition", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedSF() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.");
            return;
        }
        Long id = (Long) model.getValueAt(row, 0);
        deleteSF(id, row);
    }

    private void deleteSF(Long id, int row) {
        if (JOptionPane.showConfirmDialog(this, "Supprimer cette SF ?", "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sfService.delete(id);
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "Supprimée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createSF() {
        String input = JOptionPane.showInputDialog(this, "ID du Dossier Médical :");
        if (input == null || input.trim().isEmpty()) return;

        try {
            Long dossierId = Long.parseLong(input.trim());
            double totalActes = calculateTotalForDossier(dossierId);

            SituationFinanciere sf = SituationFinanciere.builder()
                    .dossierMedical(DossierMedical.builder().id(dossierId).build())
                    .totalDesActes((float) totalActes)
                    .totalPaye(0f)
                    .credit((float) totalActes)
                    .status(Status.ACTIVE)
                    .enPromo(PromoStatus.AUCUNE)
                    .creePar(principal.fullName())
                    .build();

            sfService.create(sf);
            loadData();
            JOptionPane.showMessageDialog(this, "Créée ! Total: " + totalActes + " DH");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSF() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.");
            return;
        }

        Long id = (Long) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Réinitialiser paiements ?", "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            SituationFinanciere sf = sfService.findById(id);
            if (sf != null) {
                double totalActes = calculateTotalForDossier(sf.getDossierMedical().getId());
                sf.setTotalDesActes((float) totalActes);
                sf.setTotalPaye(0f);
                sf.setCredit((float) totalActes);
                sf.setStatus(Status.ACTIVE);
                sf.setModifierPar(principal.fullName());
                sfService.update(sf);
                loadData();
                JOptionPane.showMessageDialog(this, "Réinitialisée. Total: " + totalActes + " DH");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateTotalForDossier(Long dossierId) {
        try {
            List<ConsultationDTO> consultations = consultationService.getConsultationsByDossierMedicalId(dossierId);
            double total = 0;
            for (ConsultationDTO c : consultations) {
                List<InterventionDTO> acts = interventionService.getInterventionsByConsultationId(c.getId());
                for (InterventionDTO i : acts) total += i.getPrixDePatient();
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    // Renderer pour les actions avec icônes (comme ConsultationsPanel)
    private class ActionsRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnView;
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionsRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Espacement entre icônes
            setOpaque(true);

            btnView   = createIconButton("/static/icons/open.png",   PRIMARY, "Consulter",   "👁", 24);
            btnEdit   = createIconButton("/static/icons/edit.png",   SUCCESS, "Modifier",    "✏", 24);
            btnDelete = createIconButton("/static/icons/delete.png", DANGER,  "Supprimer",   "🗑", 24);

            add(btnView);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? new Color(220, 230, 255) : Color.WHITE);
            return this;
        }

        private JButton createIconButton(String path, Color hover, String tooltip, String fallback, int size) {
            JButton btn;
            URL url = getClass().getResource(path);
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
                public void mouseEntered(MouseEvent e) { btn.setOpaque(true); btn.setBackground(hover); }
                public void mouseExited(MouseEvent e) { btn.setOpaque(false); btn.setBackground(null); }
            });

            return btn;
        }
    }
}