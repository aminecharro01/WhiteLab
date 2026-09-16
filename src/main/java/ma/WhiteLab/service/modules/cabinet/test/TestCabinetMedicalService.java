package ma.WhiteLab.service.modules.cabinet.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalService;
import ma.WhiteLab.service.modules.cabinet.impl.CabinetMedicalServiceImpl;

import java.util.List;
import java.util.Optional;

public class TestCabinetMedicalService {

    public static void main(String[] args) {

        // 1. Initialiser le conteneur IoC
        ApplicationContext context = ApplicationContext.getInstance();

        // 2. Récupérer le service via le conteneur
        CabinetMedicalService service = ApplicationContext.getBean(CabinetMedicalService.class);

        if (service == null) {
            System.err.println("Erreur: Impossible de récupérer CabinetMedicalService depuis le contexte. Vérifiez bean.props.");
            return;
        }


        // --- DÉBUT DES TESTS (Logique inchangée) ---

        // 1️⃣ Créer un nouveau cabinet
        CabinetMedicale cabinet = new CabinetMedicale();
        cabinet.setNom("WhiteLab");
        cabinet.setEmail("contact@whitelab.ma");
        cabinet.setTel1("0522-123456");
        cabinet.setCategorie("Dentaire");

        try {
            System.out.println("\n--- 1. CRÉATION ---");
            // Le service est injecté, on l'utilise
            CabinetMedicale created = service.create(cabinet, "admin@whitelab.ma");
            System.out.println("Cabinet créé avec ID : " + created.getId());
            cabinet.setId(created.getId()); // Mettre à jour l'ID pour les tests suivants
        } catch (RuntimeException e) {
            System.err.println("Erreur création : " + e.getMessage());
        }

        // 2️⃣ Lire tous les cabinets
        System.out.println("\n--- 2. LECTURE ---");
        List<CabinetMedicale> cabinets = service.findAll();
        System.out.println("Liste des cabinets chargés par le service injecté : ");
        cabinets.forEach(c -> System.out.println(c.getId() + " - " + c.getNom() + " (" + c.getCategorie() + ")"));

        // 3️⃣ Mettre à jour un cabinet
        System.out.println("\n--- 3. MISE À JOUR ---");
        Optional<CabinetMedicale> optCab = service.findByEmail("contact@whitelab.ma");
        if (optCab.isPresent()) {
            CabinetMedicale c = optCab.get();
            c.setTel2("0522-654321");
            service.update(c, "admin@whitelab.ma");
            System.out.println("Cabinet mis à jour : " + c.getNom() + ", nouveau tel2 = " + c.getTel2());
        } else {
            System.out.println("Cabinet non trouvé pour la mise à jour.");
        }

        // 4️⃣ Supprimer un cabinet (optionnel)
        /*
        if (cabinet.getId() != null) {
            System.out.println("\n--- 4. SUPPRESSION ---");
            service.deleteById(cabinet.getId());
            System.out.println("Cabinet (ID: " + cabinet.getId() + ") supprimé !");
        }
        */
    }
}
