package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.DossierDetailPanel;
import ma.WhiteLab.mvc.ui.pages.panels.Consultation.ConsultationFormPanel;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.FlowLayout;

public class ConsultationsPanel extends JPanel implements DossierDetailPanel.RefreshablePanel {

    private final DossierMedical dossier;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionMedecinService;
    private final UserPrincipal principal;

    private DefaultTableModel model;
    private JTable table;

    public ConsultationsPanel(DossierMedical dossier, ConsultationService consultationService, InterventionMedecinService interventionMedecinService, UserPrincipal principal) {
        this.dossier = dossier;
        this.consultationService = consultationService;
        this.interventionMedecinService = interventionMedecinService;
        this.principal = principal;

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

        JLabel title = new JLabel("Consultations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(64, 120, 255));

        JButton addBtn = new JButton("Ajouter");
        addBtn.addActionListener(e -> addConsultation());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Date", "Status", "Notes", "Actions"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? ConsultationDTO.class : String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionsColumnRenderer());
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);


        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    Object value = model.getValueAt(row, col);
                    if (!(value instanceof ConsultationDTO)) return;
                    
                    ConsultationDTO consultation = (ConsultationDTO) value;
                    
                    Rectangle cellRect = table.getCellRect(row, col, true);
                    int relX = e.getPoint().x - cellRect.x;
                    int buttonWidth = cellRect.width / 3;

                    if (relX < buttonWidth) {
                        openConsultation(consultation);
                    } else if (relX < buttonWidth * 2) {
                        editConsultation(consultation);
                    } else {
                        deleteConsultation(consultation, row);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void loadData() {
        model.setRowCount(0);
        List<ConsultationDTO> consultations = consultationService.getConsultationsByDossierMedicalId(dossier.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (ConsultationDTO consultation : consultations) {
            model.addRow(new Object[]{
                    consultation.getDate() != null ? consultation.getDate().format(formatter) : "N/A",
                    consultation.getStatus(),
                    consultation.getNotes(),
                    consultation
            });
        }
    }

    private void addConsultation() {
        showConsultationDialog(null);
    }

    private void editConsultation(ConsultationDTO consultation) {
        showConsultationDialog(consultation);
    }

    private void showConsultationDialog(ConsultationDTO consultation) {
        String title = (consultation == null) ? "Nouvelle Consultation" : "Modifier Consultation";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        
        ConsultationFormPanel formPanel = new ConsultationFormPanel(consultation);
        
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            try {
                ConsultationDTO dto = formPanel.getConsultationDTO(dossier.getId(), principal.fullName());
                if (consultation == null) {
                    consultationService.createConsultation(dto);
                } else {
                    consultationService.updateConsultation(consultation.getId(), dto);
                }
                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteConsultation(ConsultationDTO consultation, int row) {
        int choice = JOptionPane.showConfirmDialog(this, "Supprimer cette consultation ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                consultationService.deleteConsultation(consultation.getId());
                model.removeRow(row);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openConsultation(ConsultationDTO consultation) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Interventions pour la consultation du " + consultation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
        
        InterventionsConsultationPanel panel = new InterventionsConsultationPanel(
            consultation.getId(),
            interventionMedecinService,
            principal,
            this::loadData,
            consultationService
        );

        dialog.add(panel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    @Override
    public void refresh() {
        loadData();
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

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(hoverBg);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
        });

        return btn;
    }

    private class ActionsColumnRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        private final JButton btnOpen;
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionsColumnRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);

            btnOpen   = createIconButton("/static/icons/open.png", new Color(200, 225, 255), "Ouvrir", "📂", 22);
            btnEdit   = createIconButton("/static/icons/edit.png", new Color(200, 225, 255), "Modifier", "✏️", 22);
            btnDelete = createIconButton("/static/icons/delete.png", new Color(255, 210, 210), "Supprimer", "🗑️", 22);

            add(btnOpen);
            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            boolean isEmptyRow = !(value instanceof ConsultationDTO);
            btnOpen.setVisible(!isEmptyRow);
            btnEdit.setVisible(!isEmptyRow);
            btnDelete.setVisible(!isEmptyRow);

            return this;
        }
    }
}
