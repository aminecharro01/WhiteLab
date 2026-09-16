/*
package ma.WhiteLab.service.modules.dossierMedicale.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.CredentialValidatorMedicament;
import ma.WhiteLab.service.modules.dossierMedicale.dto.MedicamentPatientDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestMedicamentService {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println(" TEST MEDICAMENT SERVICE - WHITELAB 2025 ");
        System.out.println("==============================================");

        // --- 1. Initialisation du contexte IoC ---
        ApplicationContext context = new ApplicationContext("config/beans.properties");
        MedicamentService service = context.getBean(MedicamentService.class);
        CredentialValidatorMedicament validator = context.getBean(CredentialValidatorMedicament.class);

        if (service == null || validator == null) {
            System.err.println("❌ MedicamentService ou Validator introuvable dans le contexte");
            return;
        }

        try {
            // --- 2. Création d'un médicament ---
            System.out.println("\n--- 1. Création d'un nouveau médicament ---");
            Medicament med = new Medicament();
            med.setNom("Paracetamol");
            med.setType("analgésique");
            med.setForme(Forme.COMPRIME);
            med.setLabo("SANOFI"); // <--- ASSUREZ-VOUS QUE CETTE LIGNE EST PRÉSENTE ET NON VIDE
            med.setPrixUnitaire(5.0);
            med.setRemboursable(true);

            service.create(med);
            System.out.println("✅ Médicament créé avec succès : " + med.getNom());

            // --- 3. Test récupération par ID ---
            System.out.println("\n--- 2. Récupération du médicament par ID ---");
            Medicament found = service.getById(med.getId());
            if (found != null) {
                System.out.println("• Médicament trouvé : " + found.getNom() + ", Prix : " + found.getPrixUnitaire());
            } else {
                System.out.println("❌ Médicament introuvable !");
            }

            // --- 4. Test logique compatible / contre-indiqué pour un patient ---
            System.out.println("\n--- 3. Test compatibilité avec antécédents patient ---");
            Patient patient = new Patient();
            patient.setId(1L);
            patient.setNom("John Doe");

            Antecedent allergy = new Antecedent();
            allergy.setCategorie(CategorieAntecedent.ALLERGIE);
            allergy.setNom("Paracetamol");

            MedicamentPatientDTO dto = service.getMedicamentsPourPatient(patient.getId());
            List<Medicament> compatibles = dto.getMedicamentsCompatibles();
            List<Medicament> contreIndiques = dto.getMedicamentsContreIndiques();

            System.out.println("• Médicaments compatibles : " + (compatibles.isEmpty() ? "aucun" : compatibles));
            System.out.println("• Médicaments contre-indiqués : " + (contreIndiques.isEmpty() ? "aucun" : contreIndiques));

            // --- 5. Test statistiques ---
            System.out.println("\n--- 4. Statistiques ---");
            System.out.println("• Total médicaments : " + service.countTotal());
            System.out.println("• Médicaments remboursables : " + service.countRemboursables());
            System.out.println("• Prix moyen : " + service.getPrixMoyen());

        } catch (Exception e) {
            System.err.println("\n❌ Une erreur inattendue est survenue :");
            e.printStackTrace();
        }

        System.out.println("\n==============================================");
        System.out.println("            FIN DES TESTS MEDICAMENT          ");
        System.out.println("==============================================");
    }
}


 */