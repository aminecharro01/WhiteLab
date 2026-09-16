package ma.WhiteLab.service.modules.caisse.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;

import java.util.List;

public class TestSituationFinanciereService {

    public static void main(String[] args) {

        // ==========================
        // Initialisation du service
        // ==========================
        ApplicationContext context = ApplicationContext.getInstance();
        SituationFinanciereService sfService = ApplicationContext.getBean(SituationFinanciereService.class);

        if (sfService == null) {
            System.err.println("FATAL: SituationFinanciereService not found in context");
            return;
        }

        Long dossierId = 1L; // ⚠️ doit exister en base, a été créé par Test.java
        String user = "admin";

        // ==========================
        // 1. Création d'une situation financière
        // ==========================
        System.out.println("===== CREATION SITUATION FINANCIERE =====");

        SituationFinanciere sf = new SituationFinanciere();
        sf.setDossierMedical(new DossierMedical());
        sf.getDossierMedical().setId(dossierId);
        sf.setTotalDesActes(1500f);
        sf.setTotalPaye(500f);
        sf.setStatus(Status.ACTIVE);
        sf.setEnPromo(PromoStatus.AUCUNE);
        sf.setCreePar(user);

        sfService.create(sf);

        System.out.println("Situation créée avec ID : " + sf.getId());

        // ==========================
        // 2. Calcul du crédit
        // ==========================
        sfService.calculateCredit(sf);
        System.out.println("Crédit calculé : " + sf.getCredit());

        // ==========================
        // 3. Recherche par dossier
        // ==========================
        System.out.println("\n===== RECHERCHE PAR DOSSIER =====");
        List<SituationFinanciere> listByDossier = sfService.findByDossierId(dossierId);
        System.out.println("Nombre de situations pour le dossier " + dossierId + " : " + listByDossier.size());

        // ==========================
        // 4. Recherche par statut
        // ==========================
        System.out.println("\n===== RECHERCHE PAR STATUT =====");
        List<SituationFinanciere> listByStatus = sfService.findByStatus(Status.ACTIVE);
        System.out.println("Nombre de situations ACTIF : " + listByStatus.size());

        // ==========================
        // 5. Recherche par promo
        // ==========================
        System.out.println("\n===== RECHERCHE PAR PROMO =====");
        List<SituationFinanciere> listByPromo = sfService.findByPromo(PromoStatus.AUCUNE);
        System.out.println("Nombre de situations NON promo : " + listByPromo.size());

        System.out.println("\n✅ Test SituationFinanciereService terminé avec succès");
    }
}