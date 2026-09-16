package ma.WhiteLab.service.modules.cabinet.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.service.modules.cabinet.api.ParametrageService;

public class TestParametrageService {

    public static void main(String[] args) {

        // 1. Initialiser le conteneur IoC
        ApplicationContext context = ApplicationContext.getInstance();

        // 2. Récupérer le service via le conteneur
        ParametrageService service = ApplicationContext.getBean(ParametrageService.class);

        if (service == null) {
            System.err.println("Erreur: Impossible de récupérer ParametrageService depuis le contexte. Vérifiez config/beans.properties.");
            return;
        }

        // --- DÉBUT DES TESTS (Logique inchangée, utilise le service injecté) ---

        System.out.println("--- 1. VÉRIFICATION DE LA CONFIGURATION ---");
        // Vérifier si un cabinet est configuré
        System.out.println("Cabinet configuré ? " + service.isCabinetConfigured());

        // Récupérer le cabinet actuel
        try {
            System.out.println("\n--- 2. LECTURE DU CABINET ACTUEL ---");
            CabinetMedicale current = service.getCabinetActuel();
            System.out.println("Cabinet actuel : " + current.getNom() + " (" + current.getEmail() + ")");

            // Mettre à jour les paramètres généraux
            System.out.println("\n--- 3. MISE À JOUR DES PARAMÈTRES ---");
            current.setTel1("0522-000000");
            current.setSiteWeb("www.nouveau-site.ma");
            CabinetMedicale updated = service.updateParametresCabinet(current, "admin@whitelab.ma");
            System.out.println("✅ Cabinet mis à jour : tel1=" + updated.getTel1() + ", siteWeb=" + updated.getSiteWeb());

            // Mettre à jour uniquement le logo
            System.out.println("\n--- 4. MISE À JOUR DU LOGO ---");
            CabinetMedicale updatedLogo = service.updateLogo("/images/logo-new.png", "admin@whitelab.ma");
            System.out.println("Logo mis à jour : " + updatedLogo.getLogo());

        } catch (Exception e) {
            System.err.println("Erreur lors des opérations sur le cabinet : " + e.getMessage());
        }
    }
}
