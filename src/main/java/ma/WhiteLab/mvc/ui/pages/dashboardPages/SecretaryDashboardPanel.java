package ma.WhiteLab.mvc.ui.pages.dashboardPages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardDataDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.RendezVousDTO;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class SecretaryDashboardPanel extends JPanel {

    private final UserPrincipal principal;
    private final DashboardService dashboardService;
    private final ProfileService profileService;
    private final Consumer<ApplicationPages> navigator;

    private JPanel cardsPanel;
    private JPanel listPanel;

    public SecretaryDashboardPanel(UserPrincipal principal, Consumer<ApplicationPages> navigator) {
        this.principal = principal;
        this.navigator = navigator;
        
        this.dashboardService = ApplicationContext.getInstance().getBean(DashboardService.class);
        this.profileService = ApplicationContext.getInstance().getBean(ProfileService.class);

        setLayout(new BorderLayout(16, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadData();
    }
    
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard — Secrétariat");
        title.setFont(new Font("Optima", Font.BOLD, 28));
        
        JButton statsBtn = new JButton("Stats Caisse & RDV");
        statsBtn.setFont(new Font("Optima", Font.BOLD, 14));
        statsBtn.setBackground(new Color(64, 120, 255));
        statsBtn.setForeground(Color.WHITE);
        statsBtn.setFocusPainted(false);
        statsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statsBtn.addActionListener(e -> navigator.accept(ApplicationPages.STATISTICS));

        header.add(title, BorderLayout.WEST);
        header.add(statsBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);

        // Cards
        cardsPanel = new JPanel(new GridLayout(1, 3, 14, 14));
        cardsPanel.setOpaque(false);
        cardsPanel.add(card("Rendez-vous", "...", new Color(52, 152, 219))); // Blue
        cardsPanel.add(card("File d'attente", "...", new Color(241, 196, 15))); // Yellow
        cardsPanel.add(card("Recette Jour", "...", new Color(46, 204, 113))); // Green

        content.add(cardsPanel, BorderLayout.NORTH);

        // List
        listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.add(new JLabel("Chargement...", SwingConstants.CENTER));
        content.add(listPanel, BorderLayout.CENTER);

        return content;
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                ProfileData p = profileService.loadByUserId(principal.id());
                Long cabId = (p != null && p.cabinetId() != null) ? p.cabinetId() : 1L;
                DashboardDataDTO data = dashboardService.getDashboardData(cabId);
                updateUI(data);
            } catch (Exception e) {
                listPanel.removeAll();
                listPanel.add(new JLabel("Erreur: " + e.getMessage()));
            }
        });
    }

    private void updateUI(DashboardDataDTO data) {
        cardsPanel.removeAll();
        cardsPanel.add(card("Rendez-vous (Jour)", String.valueOf(data.getRendezVousDuJour().size()), new Color(52, 152, 219)));
        cardsPanel.add(card("File d'attente", data.getFileAttenteCount() + " patients", new Color(241, 196, 15)));
        cardsPanel.add(card("Recette Jour", formatMoney(data.getRecetteDuJour()), new Color(46, 204, 113)));
        cardsPanel.revalidate();
        cardsPanel.repaint();

        listPanel.removeAll();
        JLabel header = new JLabel("Agenda Aujourd'hui");
        header.setFont(new Font("Optima", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0,0,10,0));
        listPanel.add(header, BorderLayout.NORTH);
        
        listPanel.add(buildTable(data.getRendezVousDuJour()), BorderLayout.CENTER);
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JScrollPane buildTable(List<RendezVousDTO> rdv) {
        String[] cols = {"Heure", "Patient", "Motif", "Statut"};
        Object[][] rows = new Object[rdv.size()][4];
        for (int i = 0; i < rdv.size(); i++) {
            rows[i][0] = rdv.get(i).getHeure();
            rows[i][1] = rdv.get(i).getNomPatient();
            rows[i][2] = rdv.get(i).getMotif();
            rows[i][3] = rdv.get(i).getStatus();
        }
        JTable table = new JTable(rows, cols);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245,245,245));
        return new JScrollPane(table);
    }

    private JComponent card(String t, String v, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        
        JLabel title = new JLabel(t);
        title.setFont(new Font("Optima", Font.PLAIN, 14));
        title.setForeground(Color.GRAY);
        title.setBorder(new EmptyBorder(15, 15, 5, 15));
        
        JLabel val = new JLabel(v);
        val.setFont(new Font("Optima", Font.BOLD, 24));
        val.setForeground(c);
        val.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        p.add(title, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private String formatMoney(BigDecimal bd) {
        return (bd != null ? String.format("%.2f DH", bd) : "0.00 DH");
    }
}
