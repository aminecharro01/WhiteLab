package ma.WhiteLab.service.modules.caisse.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.service.modules.caisse.api.FactureService;

import java.time.LocalDate;
import java.util.List;

public class TestFactureService {

    public static void main(String[] args) {
        System.out.println("========== Lancement du test pour FactureService ==========");
        ApplicationContext context = ApplicationContext.getInstance();
        FactureService factureService = ApplicationContext.getBean(FactureService.class);

        if (factureService == null) {
            System.err.println("FATAL: FactureService could not be loaded from the context.");
            return;
        }

        // ======================
        // Step 1: Create a test invoice to ensure the service works
        // ======================
        System.out.println("\n--- Phase 1: Création et modification d'une facture test ---");
        Consultation consultation = new Consultation();
        consultation.setId(1L); // Assumes consultation with ID 1 exists from Test.java

        Facture testFacture = new Facture();
        testFacture.setConsultation(consultation);
        testFacture.setTotalFact(1250f);
        testFacture.setTotalPaye(400f);
        testFacture.setDate(LocalDate.now());
        testFacture.setCreePar("test_admin");

        factureService.create(testFacture);
        System.out.println("✅ Facture de test créée avec ID = " + testFacture.getId());

        testFacture.setTotalPaye(800f);
        factureService.update(testFacture);
        System.out.println("✅ Facture de test mise à jour.");

        // ======================
        // Step 2: Fetch all invoices and display them in a formatted table
        // ======================
        System.out.println("\n--- Phase 2: Affichage du rapport des factures ---");
        List<Facture> allInvoices = factureService.findAll();
        printInvoicesTable(allInvoices);


        System.out.println("\n✅ TEST FactureService terminé.");
    }

    /**
     * Prints a list of invoices in a formatted table to the console.
     * @param invoices The list of Facture objects to print.
     */
    private static void printInvoicesTable(List<Facture> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            System.out.println("Aucune facture à afficher.");
            return;
        }

        // --- Table Header ---
        String header = String.format("| %-10s | %-12s | %-25s | %-15s | %-15s |",
                "N° Facture", "Date", "Nom Complet du Patient", "N° Consultation", "Montant");
        String separator = "-".repeat(header.length());

        System.out.println(separator);
        System.out.println(header);
        System.out.println(separator);

        // --- Table Rows ---
        for (Facture facture : invoices) {
            String patientName = "N/A";
            if (facture.getConsultation() != null &&
                facture.getConsultation().getDossierMedical() != null &&
                facture.getConsultation().getDossierMedical().getPat() != null) {
                patientName = facture.getConsultation().getDossierMedical().getPat().getNomComplet();
            }

            Long consultationId = (facture.getConsultation() != null) ? facture.getConsultation().getId() : null;

            String row = String.format("| %-10d | %-12s | %-25s | %-15s | %-15.2f |",
                    facture.getId(),
                    facture.getDate(),
                    patientName,
                    (consultationId != null) ? consultationId.toString() : "N/A",
                    facture.getTotalFact());
            System.out.println(row);
        }

        System.out.println(separator);
    }
}