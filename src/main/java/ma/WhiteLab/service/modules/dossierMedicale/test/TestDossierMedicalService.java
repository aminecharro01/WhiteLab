/*
package ma.WhiteLab.service.modules.dossierMedicale.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.user.Role;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.auth.dto.*;

import java.util.List;
import java.util.Optional;

public class TestDossierMedicalService {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println(" TEST DOSSIER MÉDICAL SERVICE - WHITELAB 2025 ");
        System.out.println("==============================================");

        // --- 1. Initialisation du conteneur IoC ---
        ApplicationContext context = new ApplicationContext("config/beans.properties");

        // --- 2. Récupération du service ---
        DossierMedicalService dossierService = context.getBean(DossierMedicalService.class);

        if (dossierService == null) {
            System.err.println("❌ DossierMedicalService introuvable dans le contexte");
            return;
        }

        // --- 3. Création d'un UserPrincipal pour les tests ---
        UserPrincipal currentUser = UserPrincipal.builder()
                .id(100L)
                .username("admin")
                .fullName("Admin WHITELAB")
                .roles(List.of(createRole(RoleR.ADMIN)))   // ici on crée le Role correctement
                .privileges(List.of("DOSSIER_CREATE", "DOSSIER_READ", "DOSSIER_UPDATE"))
                .build();


        try {
            // --- 4. Préparation des données de test ---
            Long patientId = 1L;
            Long medecinId = 2L;

            Patient patient = new Patient();
            patient.setId(patientId);
            patient.setNom("ALAOUI");

            Medecin medecin = new Medecin();
            medecin.setId(medecinId);

            // --- 5. Test de Création avec Validation ---
            System.out.println("\n--- 1. Création d'un nouveau dossier ---");
            DossierMedical dm = new DossierMedical();
            dm.setPat(patient);
            dm.setMedecine(medecin);
            dm.setHistorique("Antécédents de carie dentaire et gingivite.");

            dossierService.create(dm, currentUser);
            System.out.println("✅ Dossier créé avec succès pour le patient ID: " + patientId);

            // --- 6. Test de la règle de gestion (Un seul dossier par patient) ---
            System.out.println("\n--- 2. Test doublon (Règle Métier) ---");
            try {
                DossierMedical dmDoublon = new DossierMedical();
                dmDoublon.setPat(patient);
                dossierService.create(dmDoublon, currentUser);
                System.out.println("❌ Échec : Le système a accepté un doublon !");
            } catch (RuntimeException e) {
                System.out.println("✅ Succès : Le validateur a bien bloqué le doublon : " + e.getMessage());
            }

            // --- 7. Test de recherche et Statistiques ---
            System.out.println("\n--- 3. Recherche et Statistiques ---");
            Optional<DossierMedical> found = dossierService.findByPatientId(patientId, currentUser);
            found.ifPresent(d -> System.out.println("• Dossier trouvé, Historique : " + d.getHistorique()));

            long countMedecin = dossierService.countByMedecin(medecinId, currentUser);
            System.out.println("• Nombre de dossiers gérés par le Dr (ID:" + medecinId + ") : " + countMedecin);

            // --- 8. Test d'export PDF (iText) ---
            System.out.println("\n--- 4. Génération du PDF ---");
            byte[] pdf = dossierService.telechargerDossierMedical(patientId, currentUser);
            if (pdf != null && pdf.length > 0) {
                System.out.println("✅ PDF généré avec succès (" + pdf.length + " bytes)");
            } else {
                System.out.println("❌ Erreur : Le contenu du PDF est vide");
            }

        } catch (Exception e) {
            System.err.println("\n❌ Une erreur inattendue est survenue :");
            e.printStackTrace();
        }

        System.out.println("\n==============================================");
        System.out.println("            FIN DES TESTS DOSSIER             ");
        System.out.println("==============================================");
    }

    private static Role createRole(RoleR roleEnum) {
        Role role = new Role();
        role.setLibelle(roleEnum); // setter pour RoleR
        return role;
    }

}


 */