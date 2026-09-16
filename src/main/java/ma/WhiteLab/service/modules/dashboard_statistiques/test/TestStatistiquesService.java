package ma.WhiteLab.service.modules.dashboard_statistiques.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.StatistiquesService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.*;

import java.time.LocalDate;
import java.util.List;

public class TestStatistiquesService {

    public static void main(String[] args) {
        System.out.println("========== Lancement du test pour StatistiquesService ==========");

        try {
            ApplicationContext context = ApplicationContext.getInstance();
            StatistiquesService service = ApplicationContext.getBean(StatistiquesService.class);

            if (service == null) {
                System.err.println("ERREUR: StatistiquesService n'a pas pu être injecté.");
                return;
            }

            LocalDate start = LocalDate.now().minusYears(1);
            LocalDate end = LocalDate.now();

            System.out.println("\n--- 1. Test: Revenue vs. Expense Trend ---");
            List<TimeSeriesDTO> trends = service.getRevenueVsExpenseTrend(start, end);
            trends.forEach(t -> System.out.println("  - " + t.getSeriesName() + ": " + t.getDataPoints().size() + " data points."));

            System.out.println("\n--- 2. Test: Revenue by Acte Category ---");
            List<BreakdownDTO> acteRevenue = service.getRevenueByActeCategory(start, end);
            acteRevenue.forEach(dto -> System.out.println("  - " + dto.getCategory() + ": " + dto.getValue() + " DH"));

            System.out.println("\n--- 3. Test: Unpaid Invoices Report ---");
            List<UnpaidInvoiceSummaryDTO> unpaid = service.getUnpaidInvoicesReport();
            System.out.println("  - Nombre de factures impayées: " + unpaid.size());
            unpaid.stream().limit(3).forEach(dto -> System.out.println("    -> ID: " + dto.getInvoiceId() + ", Patient: " + dto.getPatientName() + ", Montant: " + dto.getAmountDue()));

            System.out.println("\n--- 4. Test: Patient Age Distribution ---");
            List<BreakdownDTO> ageGroups = service.getPatientAgeDistribution();
            ageGroups.forEach(dto -> System.out.println("  - Groupe d'âge " + dto.getCategory() + ": " + dto.getValue().longValue() + " patients"));

            System.out.println("\n--- 5. Test: New Patient Acquisition Trend ---");
            TimeSeriesDTO newPatients = service.getNewPatientAcquisitionTrend(start, end);
            System.out.println("  - " + newPatients.getSeriesName() + ": " + newPatients.getDataPoints().size() + " data points.");

            System.out.println("\n--- 6. Test: Consultation Workload ---");
            List<BreakdownDTO> workload = service.getConsultationWorkload(start, end);
            workload.forEach(dto -> System.out.println("  - Dr. " + dto.getCategory() + ": " + dto.getValue().longValue() + " consultations"));

            System.out.println("\n--- 7. Test: Appointment Status Analysis ---");
            AppointmentStatusDTO apptStatus = service.getAppointmentStatusAnalysis(start, end);
            System.out.println("  - Total: " + apptStatus.getTotalAppointments() + ", Annulés ('AUCUNE'): " + apptStatus.getOtherCount() + " (" + String.format("%.2f", apptStatus.getCancellationRate()) + "%%)");

            System.out.println("\n--- 8. Test: Top 5 Prescribed Medications ---");
            List<BreakdownDTO> topMeds = service.getTopPrescribedMedications(start, end, 5);
            topMeds.forEach(dto -> System.out.println("  - " + dto.getCategory() + ": " + dto.getValue().longValue() + " unités"));

        } catch (Exception e) {
            System.err.println("\nUNE ERREUR CRITIQUE EST SURVENUE PENDANT LE TEST:");
            e.printStackTrace();
        }

        System.out.println("\n========== Test terminé ==========");
    }
}