package ma.WhiteLab.mvc.ui.pages.dashboardPages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;
import ma.WhiteLab.mvc.ui.palette.charts.SimpleChart;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardDataDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.UserSummaryDTO;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

public class AdminDashboardPanel extends JPanel {

    private final UserPrincipal principal;
    private final DashboardService dashboardService;
    private final ProfileService profileService;
    private final Consumer<ApplicationPages> navigator;

    private JPanel cardsPanel;
    private JPanel detailsPanel;

    public AdminDashboardPanel(UserPrincipal principal, Consumer<ApplicationPages> navigator) {
        this.principal = principal;
        this.navigator = navigator;
        
        this.dashboardService = ApplicationContext.getInstance().getBean(DashboardService.class);
        this.profileService = ApplicationContext.getInstance().getBean(ProfileService.class);

        setLayout(new BorderLayout(16, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadData();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard — Administrateur");
        title.setFont(new Font("Optima", Font.BOLD, 28));
        title.setForeground(new Color(35, 35, 35));

        JButton statsBtn = new JButton("Voir Stats Avancées");
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

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);

        // 1. KPI Cards
        cardsPanel = new JPanel(new GridLayout(1, 2, 14, 14)); // Changed to 2 columns
        cardsPanel.setOpaque(false);
        cardsPanel.add(kpiCard("Utilisateurs", "...", new Color(46, 204, 113)));
        cardsPanel.add(kpiCard("Status Système", "Actif", new Color(241, 196, 15)));
        body.add(cardsPanel, BorderLayout.NORTH);

        // 2. Details (Split: Chart + List)
        detailsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        detailsPanel.setOpaque(false);
        // Placeholders
        detailsPanel.add(new JLabel("Chargement graphique...", SwingConstants.CENTER));
        detailsPanel.add(new JLabel("Chargement liste...", SwingConstants.CENTER));
        
        body.add(detailsPanel, BorderLayout.CENTER);

        return body;
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                ProfileData profile = profileService.loadByUserId(principal.id());
                Long cabinetId = (profile != null && profile.cabinetId() != null) ? profile.cabinetId() : 1L;
                DashboardDataDTO data = dashboardService.getDashboardData(cabinetId);
                updateUI(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateUI(DashboardDataDTO data) {
        // Cards
        cardsPanel.removeAll();
        // Removed Cabinets card
        cardsPanel.add(kpiCard("Utilisateurs Actifs", String.valueOf(data.getTotalUsers()), new Color(46, 204, 113)));
        cardsPanel.add(kpiCard("Status Système", "En ligne", new Color(39, 174, 96)));
        cardsPanel.revalidate();
        cardsPanel.repaint();

        // Details
        detailsPanel.removeAll();

        // Left: Chart
        SimpleChart roleChart = new SimpleChart("Répartition des Rôles", SimpleChart.Type.PIE);
        roleChart.setData(data.getUsersByRole());
        detailsPanel.add(roleChart);

        // Right: List
        JPanel listWrap = new JPanel(new BorderLayout());
        listWrap.setOpaque(false);
        listWrap.setBorder(BorderFactory.createTitledBorder("Derniers Utilisateurs Ajoutés"));
        listWrap.setBackground(Color.WHITE);
        
        listWrap.add(buildUsersTable(data.getLatestUsers()), BorderLayout.CENTER);
        detailsPanel.add(listWrap);

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JScrollPane buildUsersTable(List<UserSummaryDTO> users) {
        String[] cols = {"ID", "Nom", "Email", "Rôle"};
        Object[][] rows = new Object[users.size()][4];
        
        for (int i = 0; i < users.size(); i++) {
            UserSummaryDTO u = users.get(i);
            rows[i][0] = u.getId();
            rows[i][1] = u.getNomComplet();
            rows[i][2] = u.getEmail();
            rows[i][3] = u.getRole();
        }

        JTable table = new JTable(rows, cols);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.setEnabled(false);

        return new JScrollPane(table);
    }

    private JComponent kpiCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(accent);
        p.add(bar, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 5));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 15, 0, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Optima", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Optima", Font.BOLD, 24));
        lblValue.setForeground(new Color(35, 35, 35));

        content.add(lblTitle);
        content.add(lblValue);
        p.add(content, BorderLayout.CENTER);

        return p;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00 DH";
        return String.format("%.2f DH", amount);
    }
}