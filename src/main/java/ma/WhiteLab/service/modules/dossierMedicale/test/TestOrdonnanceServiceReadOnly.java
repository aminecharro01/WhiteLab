/*
package ma.WhiteLab.service.modules.dossierMedicale.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceService;

import java.util.List;

public class TestOrdonnanceServiceReadOnly {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("TEST ORDONNANCE - READ ONLY");
        System.out.println("==============================================");

        ApplicationContext context = new ApplicationContext("config/beans.properties");
        OrdonnanceService ordonnanceService = context.getBean(OrdonnanceService.class);

        try {
            // Test récupération par ID existant
            Long existingOrdonnanceId = 2L; // Assurez-vous qu'il existe en base
            Ordonnance ord = ordonnanceService.getById(existingOrdonnanceId);
            if (ord != null) {
                System.out.println("Ordonnance trouvée : ID=" + ord.getId() + ", prescriptions=" + ord.getPrescriptions().size());
            } else {
                System.out.println("Aucune ordonnance trouvée pour ID=" + existingOrdonnanceId);
            }

            // Test findByPatientId
            Long patientId = 1L; // ID d’un patient existant
            List<Ordonnance> byPatient = ordonnanceService.findByPatientId(patientId);
            System.out.println("Ordonnances du patient ID=" + patientId + " : " + byPatient.size());

            // Test findRecent
            List<Ordonnance> recent = ordonnanceService.findRecent(30);
            System.out.println("Ordonnances récentes (30 jours) : " + recent.size());

            // Test téléchargement PDF
            if (ord != null) {
                byte[] pdf = ordonnanceService.downloadOrdonnance(ord.getId());
                System.out.println("PDF généré : " + (pdf != null ? pdf.length + " bytes" : "Erreur"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("==============================================");
        System.out.println("FIN DU TEST READ-ONLY");
        System.out.println("==============================================");
    }
}


 */