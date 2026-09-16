/*
package ma.WhiteLab.service.modules.dossierMedicale.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.entities.enums.Sexe;
import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceService;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;

import java.time.LocalDate;
import java.util.List;

public class TestPrescriptionService {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   TEST COMPLET : PRESCRIPTION - WHITELAB 2025  ");
        System.out.println("=================================================");

        ApplicationContext context = new ApplicationContext("config/beans.properties");

        PatientRepository patientRepo = context.getBean(PatientRepository.class);
        DossierMedicalService dossierService = context.getBean(DossierMedicalService.class);
        OrdonnanceService ordonnanceService = context.getBean(OrdonnanceService.class);
        MedicamentService medicamentService = context.getBean(MedicamentService.class);
        PrescriptionService prescriptionService = context.getBean(PrescriptionService.class);

        try {
            // --- 1. Création du patient ---
            Patient pat = new Patient();
            pat.setNom("EL ALAMI");
            pat.setPrenom("Yassine");
            pat.setSexe(Sexe.HOMME);
            pat.setAssurance(Assurance.CNSS);
            pat.setDateNaissance(LocalDate.of(1995, 3, 12));
            pat.setTelephone("0661223344");
            pat.setEmail("yassine." + System.currentTimeMillis() + "@whitelab.ma");
            patientRepo.create(pat);

            // --- 2. Création du Dossier Médical ---
            DossierMedical dm = new DossierMedical();
            dm.setPat(pat);
            dm.setHistorique("Premier check-up complet.");
            Medecin medecinResponsable = new Medecin();
            medecinResponsable.setId(1L);
            dm.setMedecine(medecinResponsable);
            dossierService.create(dm);

            // --- 3. Création du Médicament ---
            Medicament med = new Medicament();
            med.setNom("Doliprane 1000");
            med.setLabo("SANOFI");
            med.setPrixUnitaire(15.50);
            med.setForme(Forme.COMPRIME);
            med.setRemboursable(true);
            med.setType("Antalgique");
            medicamentService.create(med);

            // --- 4. Création de l'Ordonnance ---
            Ordonnance ord = new Ordonnance();
            ord.setDateOrdonnance(LocalDate.now());
            ord.setDossierMedical(dm);
            ordonnanceService.create(ord);

            // --- 5. Création de la Prescription ---
            Prescription presc = new Prescription();
            presc.setOrdonnance(ord);
            presc.setMedicament(med);
            presc.setQte(2);
            presc.setDuree(5);
            presc.setFrequence("1 comprimé matin et soir");
            prescriptionService.create(presc);

            System.out.println("\n✅ Prescription créée : " + med.getNom());

            // --- 6. Récupération par ID ---
            Prescription found = prescriptionService.getById(presc.getId());
            System.out.println("Prescription trouvée par ID : " + (found != null ? found.getMedicament().getNom() : "Introuvable"));

            // --- 7. Test exists() ---
            boolean exists = prescriptionService.exists(ord.getId(), med.getId());
            System.out.println("Existence vérifiée : " + exists);

            // --- 8. Test findByOrdonnanceId() et findByMedicamentId() ---
            List<Prescription> byOrdonnance = prescriptionService.findByOrdonnanceId(ord.getId());
            System.out.println("Prescriptions pour l'ordonnance : " + byOrdonnance.size());

            List<Prescription> byMedicament = prescriptionService.findByMedicamentId(med.getId());
            System.out.println("Prescriptions pour le médicament : " + byMedicament.size());

            // --- 9. Test mise à jour ---
            presc.setQte(3);
            prescriptionService.update(presc);
            System.out.println("Prescription mise à jour, nouvelle quantité : " + prescriptionService.getById(presc.getId()).getQte());

            // --- 10. Test statistiques ---
            int totalQte = prescriptionService.totalQuantitePrescriteForMedicament(med.getId());
            double totalPrix = prescriptionService.totalPrixPrescritForMedicament(med.getId());
            System.out.println("Quantité totale prescrite : " + totalQte);
            System.out.println("Prix total prescrit : " + totalPrix);

            // --- 11. Test prescriptions récentes ---
            List<Prescription> recent = prescriptionService.findRecent(7);
            System.out.println("Prescriptions récentes (7 jours) : " + recent.size());

            // --- 12. Test suppression ---
            prescriptionService.delete(presc.getId());
            System.out.println("Prescription supprimée.");

        } catch (Exception e) {
            System.err.println("❌ ÉCHEC DU TEST :");
            e.printStackTrace();
        }

        System.out.println("\n=================================================");
        System.out.println("             FIN DU TEST PRESCRIPTION            ");
        System.out.println("=================================================");
    }
}


 */