package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.panels.Caisse.FacturesManagementPanel;
import ma.WhiteLab.mvc.ui.pages.panels.Caisse.SituationFinancierePanel;
import ma.WhiteLab.service.modules.caisse.api.CaisseService;
import ma.WhiteLab.service.modules.caisse.api.ChargesService;
import ma.WhiteLab.service.modules.caisse.api.FactureService;
import ma.WhiteLab.service.modules.caisse.api.RevenusService;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.caisse.dto.CaisseReportDTO;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CaissePanel extends JPanel {

    private final UserPrincipal principal;
    private final CaisseService caisseService;
    private final RevenusService revenusService;
    private final ChargesService chargesService;
    private final FactureService factureService;
    private final SituationFinanciereService sfService;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionService;

    // Navigation
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private static final String VIEW_DASHBOARD = "DASHBOARD";
    private static final String VIEW_SF = "SF";
    private static final String VIEW_FACTURES = "FACTURES";

    // Dashboard Components
    private JPanel journalPanel;
    private JPanel revenusPanel;
    private JPanel chargesPanel;
    private JPanel facturesPanel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CaissePanel(UserPrincipal principal,
                       CaisseService caisseService,
                       RevenusService revenusService,
                       ChargesService chargesService,
                       FactureService factureService,
                       SituationFinanciereService sfService,
                       ConsultationService consultationService,
                       InterventionMedecinService interventionService) {
        this.principal = principal;
        this.caisseService = caisseService;
        this.revenusService = revenusService;
        this.chargesService = chargesService;
        this.factureService = factureService;
        this.sfService = sfService;
        this.consultationService = consultationService;
        this.interventionService = interventionService;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        // Add Dashboard View (Default)
        rootPanel.add(buildDashboardView(), VIEW_DASHBOARD);

        add(rootPanel, BorderLayout.CENTER);

        refreshAll();
    }

    // ================== NAVIGATION ==================

    private void showDashboard() {
        refreshAll();
        cardLayout.show(rootPanel, VIEW_DASHBOARD);
    }

    private void openSFPanel() {
        SituationFinancierePanel panel = new SituationFinancierePanel(
                sfService,
                factureService,
                consultationService,
                interventionService,
                principal,
                this::openFacturesPanel, // onOpenFactures
                this::showDashboard      // onBack
        );
        rootPanel.add(panel, VIEW_SF);
        cardLayout.show(rootPanel, VIEW_SF);
    }

    private void openFacturesPanel() {
        FacturesManagementPanel panel = new FacturesManagementPanel(
                factureService,
                principal,
                () -> cardLayout.show(rootPanel, VIEW_SF) // onBack -> Go back to SF
        );
        rootPanel.add(panel, VIEW_FACTURES);
        cardLayout.show(rootPanel, VIEW_FACTURES);
    }

    // ================== DASHBOARD VIEW ==================

    private JPanel buildDashboardView() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(new EmptyBorder(20, 20, 20, 20));

        dashboard.add(buildHeader(), BorderLayout.NORTH);
        dashboard.add(buildTabs(), BorderLayout.CENTER);

        return dashboard;
    }

    private boolean isReadOnly() {
        return principal.roles().contains(RoleR.MEDECIN);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Gestion de la Caisse");
        title.setFont(new Font("Optima", Font.BOLD, 28));
        title.setForeground(new Color(35, 35, 35));

        JButton btnSF = new JButton("Gérer Situations Financières");
        btnSF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSF.setBackground(new Color(64, 120, 255));
        btnSF.setForeground(Color.WHITE);
        btnSF.setFocusPainted(false);
        btnSF.addActionListener(e -> openSFPanel());

        header.add(title, BorderLayout.WEST);
        header.add(btnSF, BorderLayout.EAST);
        return header;
    }

    private JComponent buildTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        journalPanel = new JPanel(new BorderLayout());
        revenusPanel = new JPanel(new BorderLayout());
        chargesPanel = new JPanel(new BorderLayout());
        facturesPanel = new JPanel(new BorderLayout());

        tabbedPane.addTab("Tableau de Bord", journalPanel);
        tabbedPane.addTab("Revenus", revenusPanel);
        tabbedPane.addTab("Dépenses", chargesPanel);
        tabbedPane.addTab("Factures", facturesPanel);

        return tabbedPane;
    }

    private void refreshAll() {
        SwingUtilities.invokeLater(() -> {
            refreshJournal();
            refreshRevenus();
            refreshCharges();
            refreshFactures();
        });
    }

    // ================== JOURNAL ==================

    private void refreshJournal() {
        journalPanel.removeAll();
        journalPanel.setOpaque(false);

        Long cabinetId = 1L;
        LocalDate today = LocalDate.now();

        CaisseReportDTO daily = caisseService.getDailyCaisseReport(cabinetId, today);
        CaisseReportDTO weekly = caisseService.getWeeklyCaisseReport(cabinetId, today);
        CaisseReportDTO monthly = caisseService.getMonthlyCaisseReport(cabinetId, today.getYear(), today.getMonthValue());

        JPanel cardsContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel row1 = new JPanel(new GridLayout(1, 3, 15, 0));
        row1.setOpaque(false);
        row1.add(kpiCard("Entrées (Jour)", formatMoney(daily.getTotalRevenus()), new Color(46, 204, 113)));
        row1.add(kpiCard("Sorties (Jour)", formatMoney(daily.getTotalCharges()), new Color(231, 76, 60)));
        row1.add(kpiCard("Solde (Jour)", formatMoney(daily.getSolde()), new Color(52, 152, 219)));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 15, 0));
        row2.setOpaque(false);
        row2.add(kpiCard("Recette Semaine", formatMoney(weekly.getSolde()), new Color(155, 89, 182)));
        row2.add(kpiCard("Recette Mois", formatMoney(monthly.getSolde()), new Color(241, 196, 15)));
        row2.add(new JLabel(""));

        cardsContainer.add(row1);
        cardsContainer.add(row2);

        journalPanel.add(cardsContainer, BorderLayout.NORTH);

        List<Revenus> revs = revenusService.findAll();
        List<Charges> chgs = chargesService.findAll();

        List<Object[]> rows = new ArrayList<>();
        for (Revenus r : revs) {
            if (r.getDate() != null && r.getDate().toLocalDate().isEqual(today))
                rows.add(new Object[]{r.getDate(), "REVENU", r.getTitre(), r.getMontant(), null});
        }
        for (Charges c : chgs) {
            if (c.getDate() != null && c.getDate().toLocalDate().isEqual(today))
                rows.add(new Object[]{c.getDate(), "DEPENSE", c.getTitre(), null, c.getMontant()});
        }

        rows.sort(Comparator.comparing(o -> ((java.time.LocalDateTime)o[0]), Comparator.reverseOrder()));

        String[] cols = {"Date", "Type", "Libellé", "Crédit (+)", "Débit (-)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for(Object[] r : rows) {
            model.addRow(new Object[]{
                    ((java.time.LocalDateTime)r[0]).format(DATE_FMT),
                    r[1],
                    r[2],
                    r[3] != null ? formatMoney(BigDecimal.valueOf((Double)r[3])) : "",
                    r[4] != null ? formatMoney(BigDecimal.valueOf((Double)r[4])) : ""
            });
        }

        JTable table = createTable(model);
        journalPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        journalPanel.revalidate();
        journalPanel.repaint();
    }

    // ================== REVENUS ==================

    private void refreshRevenus() {
        revenusPanel.removeAll();
        List<Revenus> list = revenusService.findAll();
        boolean readOnly = isReadOnly();
        String[] cols = readOnly ? new String[]{"Date", "Titre", "Montant"} : new String[]{"Date", "Titre", "Montant", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        for(Revenus r : list) {
            List<Object> row = new ArrayList<>();
            row.add(r.getDate() != null ? r.getDate().format(DATE_FMT) : "-");
            row.add(r.getTitre());
            row.add(formatMoney(BigDecimal.valueOf(r.getMontant())));
            if (!readOnly) row.add("Modifier");
            model.addRow(row.toArray());
        }
        JTable table = createTable(model);
        revenusPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        revenusPanel.revalidate();
        revenusPanel.repaint();
    }

    // ================== CHARGES ==================

    private void refreshCharges() {
        chargesPanel.removeAll();
        List<Charges> list = chargesService.findAll();
        boolean readOnly = isReadOnly();
        String[] cols = readOnly ? new String[]{"Date", "Titre", "Montant"} : new String[]{"Date", "Titre", "Montant", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        for(Charges c : list) {
            List<Object> row = new ArrayList<>();
            row.add(c.getDate() != null ? c.getDate().format(DATE_FMT) : "-");
            row.add(c.getTitre());
            row.add(formatMoney(BigDecimal.valueOf(c.getMontant())));
            if (!readOnly) row.add("Modifier");
            model.addRow(row.toArray());
        }
        JTable table = createTable(model);
        chargesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        chargesPanel.revalidate();
        chargesPanel.repaint();
    }

    // ================== FACTURES ==================

    private void refreshFactures() {
        facturesPanel.removeAll();
        List<Facture> list = factureService.findAll();
        boolean readOnly = isReadOnly();
        String[] cols = readOnly ? new String[]{"ID", "Date", "Total", "Payé", "Reste", "Statut"} : new String[]{"ID", "Date", "Total", "Payé", "Reste", "Statut", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        for(Facture f : list) {
            List<Object> row = new ArrayList<>();
            row.add(f.getId());
            row.add(f.getDate() != null ? f.getDate() : "-");
            row.add(formatMoney(BigDecimal.valueOf(f.getTotalFact())));
            row.add(formatMoney(BigDecimal.valueOf(f.getTotalPaye())));
            row.add(formatMoney(BigDecimal.valueOf(f.getReste())));
            row.add(f.getStatut());
            if (!readOnly) row.add("Payer");
            model.addRow(row.toArray());
        }
        JTable table = createTable(model);
        facturesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        facturesPanel.revalidate();
        facturesPanel.repaint();
    }

    // ================== HELPERS ==================

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.setEnabled(!isReadOnly());
        return table;
    }

    private JComponent kpiCard(String title, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Optima", Font.PLAIN, 14));
        t.setForeground(Color.GRAY);
        t.setBorder(new EmptyBorder(15, 15, 5, 15));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Optima", Font.BOLD, 24));
        v.setForeground(color);
        v.setBorder(new EmptyBorder(0, 15, 15, 15));
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private String formatMoney(BigDecimal bd) {
        return (bd != null ? String.format("%.2f DH", bd) : "0.00 DH");
    }
}