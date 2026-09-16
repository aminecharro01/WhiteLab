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

public class DoctorDashboardPanel extends JPanel {

    private final UserPrincipal principal;
    private final DashboardService dashboardService;
    private final ProfileService profileService;
    private final Consumer<ApplicationPages> navigator;

    private JPanel cardsPanel;
    private JPanel detailsPanel;

    public DoctorDashboardPanel(UserPrincipal principal, Consumer<ApplicationPages> navigator) {
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

        JLabel title = new JLabel("Dashboard — Médecin");
        title.setFont(new Font("Optima", Font.BOLD, 28));
        
        JButton statsBtn = new JButton("Mes Statistiques");
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

        // Cards
        cardsPanel = new JPanel(new GridLayout(1, 3, 14, 14));
        cardsPanel.setOpaque(false);
        cardsPanel.add(kpiCard("Patients Attente", "...", new Color(241, 196, 15)));
        cardsPanel.add(kpiCard("Consultations Jour", "...", new Color(52, 152, 219)));
        cardsPanel.add(kpiCard("Consultations Mois", "...", new Color(46, 204, 113)));
        
        body.add(cardsPanel, BorderLayout.NORTH);

        // Appointments Table
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        detailsPanel.add(new JLabel("Chargement...", SwingConstants.CENTER));
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
                detailsPanel.removeAll();
                JLabel err = new JLabel("Erreur: " + e.getMessage());
                err.setForeground(Color.RED);
                detailsPanel.add(err);
            }
        });
    }

    private void updateUI(DashboardDataDTO data) {
        cardsPanel.removeAll();
        cardsPanel.add(kpiCard("Patients Attente", data.getFileAttenteCount() + " patients", new Color(241, 196, 15)));
        cardsPanel.add(kpiCard("Consultations Jour", String.valueOf(data.getNbrConsultationsDuJour()), new Color(52, 152, 219)));
        cardsPanel.add(kpiCard("Consultations Mois", String.valueOf(data.getNbrConsultationsDuMois()), new Color(46, 204, 113)));
        cardsPanel.revalidate();
        cardsPanel.repaint();

        detailsPanel.removeAll();
        JLabel lbl = new JLabel("Mon Agenda du Jour");
        lbl.setFont(new Font("Optima", Font.BOLD, 18));
        lbl.setBorder(new EmptyBorder(0,0,10,0));
        detailsPanel.add(lbl, BorderLayout.NORTH);
        
        detailsPanel.add(buildRdvTable(data.getRendezVousDuJour()), BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private JScrollPane buildRdvTable(List<RendezVousDTO> rdvList) {
        String[] cols = {"Heure", "Patient", "Motif", "Statut"};
        Object[][] rows = new Object[rdvList.size()][4];
        for (int i = 0; i < rdvList.size(); i++) {
            RendezVousDTO r = rdvList.get(i);
            rows[i][0] = r.getHeure();
            rows[i][1] = r.getNomPatient();
            rows[i][2] = r.getMotif();
            rows[i][3] = r.getStatus();
        }
        JTable table = new JTable(rows, cols);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        return new JScrollPane(table);
    }

    private JComponent kpiCard(String title, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel t = new JLabel(title);
        t.setFont(new Font("Optima", Font.PLAIN, 14));
        t.setForeground(Color.GRAY);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Optima", Font.BOLD, 22));
        v.setForeground(color);
        
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
