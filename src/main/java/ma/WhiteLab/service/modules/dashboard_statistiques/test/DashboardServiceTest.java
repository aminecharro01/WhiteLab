package ma.WhiteLab.service.modules.dashboard_statistiques.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardDataDTO;

public class DashboardServiceTest {

    public static void main(String[] args) {
        System.out.println("========== Lancement du test pour DashboardService ==========");

        try {
            // 1. Initialiser le contexte de l'application
            ApplicationContext context = ApplicationContext.getInstance();

            // 2. Récupérer le service DashboardService depuis le contexte
            DashboardService dashboardService = ApplicationContext.getBean(DashboardService.class);

            if (dashboardService == null) {
                System.err.println("ERREUR: DashboardService n'a pas pu être injecté. Vérifiez beans.properties.");
                return;
            }

            // 3. Appeler la méthode pour récupérer les données du dashboard
            // On utilise l'ID du cabinet 1L, comme créé dans le Test principal
            Long cabinetIdForTest = 1L;
            System.out.println("\nRécupération des données pour le cabinet ID: " + cabinetIdForTest);
            DashboardDataDTO dashboardData = dashboardService.getDashboardData(cabinetIdForTest);

            // 4. Afficher les résultats de manière lisible
            System.out.println("\nDonnées du Dashboard récupérées avec succès !");
            System.out.println(dashboardData.toString());

        } catch (Exception e) {
            System.err.println("\nUNE ERREUR CRITIQUE EST SURVENUE PENDANT LE TEST:");
            e.printStackTrace();
        }

        System.out.println("\n========== Test terminé ==========");
    }
}