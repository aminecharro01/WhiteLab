package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.StatistiquesService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.*;
import ma.WhiteLab.mvc.ui.palette.charts.SimpleChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {

    private final UserPrincipal principal;
    private final StatistiquesService statsService;

    public StatisticsPanel(UserPrincipal principal) {
        this.principal = principal;
        this.statsService = ApplicationContext.getInstance().getBean(StatistiquesService.class);

        setLayout(new BorderLayout(16, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("Statistiques Détaillées");
        title.setFont(new Font("Optima", Font.BOLD, 28));
        title.setForeground(new Color(35, 35, 35));
        return title;
    }

    private JComponent buildBody() {
        // Main container with scroll
        JPanel container = new JPanel(new GridLayout(0, 2, 20, 20)); // 2 columns
        container.setBackground(Color.WHITE);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(12);

        SwingUtilities.invokeLater(() -> {
            try {
                String role = principal.rolePrincipal().name();

                // ================= ADMIN VIEW =================
                if (role.equals("ADMIN")) {
                    // 1. Users by Role (Pie)
                    List<BreakdownDTO> users = statsService.getUsersByRoleDistribution();
                    SimpleChart userChart = new SimpleChart("Répartition Utilisateurs", SimpleChart.Type.PIE);
                    userChart.setData(toMap(users));
                    container.add(userChart);

                    // 2. Cabinets by Category (Bar)
                    List<BreakdownDTO> cabinets = statsService.getCabinetsByCategoryDistribution();
                    SimpleChart cabChart = new SimpleChart("Cabinets par Catégorie", SimpleChart.Type.BAR);
                    cabChart.setData(toMap(cabinets));
                    container.add(cabChart);

                    // 3. System Growth (Patient Acquisition Trend)
                    TimeSeriesDTO growth = statsService.getNewPatientAcquisitionTrend(start, end);
                    SimpleChart growthChart = new SimpleChart("Croissance (Nouveaux Patients)", SimpleChart.Type.BAR);
                    growthChart.setData(timeSeriesToMap(growth));
                    container.add(growthChart);
                }

                // ================= MEDECIN VIEW =================
                else if (role.equals("MEDECIN")) {
                    // 1. Revenue by Acte (Bar)
                    List<BreakdownDTO> revActs = statsService.getRevenueByActeCategory(start, end);
                    SimpleChart revChart = new SimpleChart("Revenus par Acte", SimpleChart.Type.BAR);
                    revChart.setData(toMap(revActs));
                    container.add(revChart);

                    // 2. Top Meds (Bar)
                    List<BreakdownDTO> meds = statsService.getTopPrescribedMedications(start, end, 5);
                    SimpleChart medChart = new SimpleChart("Top 5 Médicaments", SimpleChart.Type.BAR);
                    medChart.setData(toMap(meds));
                    container.add(medChart);

                    // 3. Workload (Who works most?)
                    List<BreakdownDTO> workload = statsService.getConsultationWorkload(start, end);
                    SimpleChart workChart = new SimpleChart("Charge de travail (Consultations)", SimpleChart.Type.PIE);
                    workChart.setData(toMap(workload));
                    container.add(workChart);
                    
                    // 4. Age Dist
                    List<BreakdownDTO> ages = statsService.getPatientAgeDistribution();
                    SimpleChart ageChart = new SimpleChart("Âge Patients", SimpleChart.Type.BAR);
                    ageChart.setData(toMap(ages));
                    container.add(ageChart);
                }

                // ================= SECRETAIRE VIEW =================
                else if (role.equals("SECRETAIRE")) {
                    // 1. Appointment Status (Pie)
                    AppointmentStatusDTO status = statsService.getAppointmentStatusAnalysis(start, end);
                    Map<String, Double> statusMap = new HashMap<>();
                    statusMap.put("Actifs", (double) status.getActiveCount());
                    statusMap.put("Annulés", (double) status.getOtherCount());
                    statusMap.put("Terminés", (double) status.getInactiveCount());
                    
                    SimpleChart statusChart = new SimpleChart("Statuts Rendez-vous", SimpleChart.Type.PIE);
                    statusChart.setData(statusMap);
                    container.add(statusChart);

                    // 2. Financial Trend (Bar)
                    List<TimeSeriesDTO> trends = statsService.getRevenueVsExpenseTrend(start, end);
                    if (!trends.isEmpty()) {
                        SimpleChart finChart = new SimpleChart("Tendance Financière", SimpleChart.Type.BAR);
                        finChart.setData(timeSeriesToMap(trends.get(0))); // Just Revenue for simplicity
                        container.add(finChart);
                    }

                    // 3. Top Actes (Revenue Source)
                    List<BreakdownDTO> revActs = statsService.getRevenueByActeCategory(start, end);
                    SimpleChart actChart = new SimpleChart("Top Actes (Revenus)", SimpleChart.Type.BAR);
                    actChart.setData(toMap(revActs));
                    container.add(actChart);

                    // 4. Unpaid Invoices (Table)
                    List<UnpaidInvoiceSummaryDTO> unpaid = statsService.getUnpaidInvoicesReport();
                    container.add(buildUnpaidTable(unpaid));
                }

                container.revalidate();
                container.repaint();

            } catch (Exception e) {
                e.printStackTrace();
                container.add(new JLabel("Erreur chargement stats: " + e.getMessage()));
            }
        });

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private Map<String, Double> toMap(List<BreakdownDTO> list) {
        return list.stream().collect(Collectors.toMap(BreakdownDTO::getCategory, BreakdownDTO::getValue));
    }

    private Map<String, Double> timeSeriesToMap(TimeSeriesDTO ts) {
        return ts.getDataPoints().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getMonth().name().substring(0, 3) + "-" + e.getKey().getYear(),
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // merge
                        java.util.LinkedHashMap::new // preserve order if possible (though map implies unsorted)
                ));
    }

    private JPanel buildUnpaidTable(List<UnpaidInvoiceSummaryDTO> list) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Factures Impayées"));

        String[] cols = {"Patient", "Montant", "Date", "Retard (Jours)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        for (UnpaidInvoiceSummaryDTO u : list) {
            model.addRow(new Object[]{
                u.getPatientName(),
                String.format("%.2f DH", u.getAmountDue()),
                u.getInvoiceDate(),
                u.getDaysOutstanding()
            });
        }

        JTable t = new JTable(model);
        t.setFillsViewportHeight(true);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setRowHeight(25);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }
}