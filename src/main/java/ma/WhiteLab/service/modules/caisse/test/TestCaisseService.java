package ma.WhiteLab.service.modules.caisse.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.service.modules.caisse.api.CaisseService;
import ma.WhiteLab.service.modules.caisse.dto.CaisseReportDTO;

import java.time.LocalDate;

public class TestCaisseService {

    public static void main(String[] args) {

        System.out.println("========== Lancement du test de rapport pour CaisseService ==========");

        ApplicationContext context;
        CaisseService caisseService;

        try {
            // Using getInstance() instead of new ApplicationContext()
            context = ApplicationContext.getInstance();
            caisseService = ApplicationContext.getBean(CaisseService.class);
            if (caisseService == null) {
                throw new RuntimeException("CaisseService could not be loaded from the context.");
            }
        } catch (Exception e) {
            System.err.println("FATAL: Could not initialize ApplicationContext. Aborting tests.");
            e.printStackTrace();
            return;
        }

        Long cabinetId = 1L; // Assumes cabinet with ID 1 exists from Test.java
        // Using a date from the main Test.java file to ensure data exists for testing
        LocalDate testDate = LocalDate.now(); // Use current date or valid test data date

        System.out.println("\n--- Rapport pour le cabinet ID: " + cabinetId + " | Date de référence: " + testDate + " ---\n");

        // --- Fetching all reports ---
        try {
            CaisseReportDTO dailyReport = caisseService.getDailyCaisseReport(cabinetId, testDate);
            CaisseReportDTO weeklyReport = caisseService.getWeeklyCaisseReport(cabinetId, testDate);
            CaisseReportDTO monthlyReport = caisseService.getMonthlyCaisseReport(cabinetId, testDate.getYear(), testDate.getMonthValue());

            // --- Displaying the requested metrics in a clean format ---

            // Recette du Jour (Daily Revenue) & Dépenses du Jour (Daily Expenses)
            System.out.println("Recette du Jour (" + testDate + "): " + dailyReport.getTotalRevenus() + " DH");
            System.out.println("Dépenses du Jour: " + dailyReport.getTotalCharges() + " DH");
            System.out.println("--------------------------------------------------");

            // Recette de la semaine (Weekly Revenue) & Dépenses de la semaine (Weekly Expenses)
            System.out.println("Recette de la semaine (Semaine du " + testDate.with(java.time.DayOfWeek.MONDAY) + "): " + weeklyReport.getTotalRevenus() + " DH");
            System.out.println("Dépenses de la semaine: " + weeklyReport.getTotalCharges() + " DH");
            System.out.println("--------------------------------------------------");

            // Recette du mois (Monthly Revenue), Dépenses du mois (Monthly Expenses), Gain du mois (Monthly Profit)
            System.out.println("Recette du mois (Mois de " + testDate.getMonth() + "): " + monthlyReport.getTotalRevenus() + " DH");
            System.out.println("Dépenses du mois: " + monthlyReport.getTotalCharges() + " DH");
            System.out.println("Gain du mois: " + monthlyReport.getSolde() + " DH");
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
             System.err.println("Erreur lors de la génération des rapports: " + e.getMessage());
             e.printStackTrace();
        }

        System.out.println("\n✅ Test de rapport CaisseService terminé avec succès.");
    }
}