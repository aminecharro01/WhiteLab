package ma.WhiteLab.service.modules.caisse.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.service.modules.caisse.api.ChargesService;

import java.time.LocalDateTime;
import java.util.List;

public class TestChargesService {

    public static void main(String[] args) {

        // Use getInstance() for singleton access
        ApplicationContext context = ApplicationContext.getInstance();
        ChargesService chargesService = ApplicationContext.getBean(ChargesService.class);

        if (chargesService == null) {
            System.err.println("FATAL: ChargesService could not be loaded from the context.");
            return;
        }

        // ==========================
        // 1. Création d'un cabinet (ID déjà existant en DB)
        // ==========================
        CabinetMedicale cabinet = new CabinetMedicale();
        cabinet.setId(1L); // ⚠️ DOIT exister dans la base

        // ==========================
        // 2. Création d'une charge
        // ==========================
        Charges charge = new Charges();
        charge.setTitre("Loyer cabinet");
        charge.setDescription("Loyer mensuel");
        charge.setMontant(1500.0);
        charge.setDate(LocalDateTime.now());
        charge.setCabinetMedicale(cabinet);
        charge.setCreePar("admin");

        chargesService.create(charge);
        System.out.println("✅ Charge créée avec ID : " + charge.getId());

        // ==========================
        // 3. Lecture par ID
        // ==========================
        Charges fetched = chargesService.findById(charge.getId());
        if (fetched != null) {
            System.out.println("📄 Charge récupérée : " + fetched.getTitre() + " | " + fetched.getMontant());
        }

        // ==========================
        // 4. Mise à jour
        // ==========================
        if (fetched != null) {
            fetched.setMontant(1600.0);
            fetched.setModifierPar("admin");
            chargesService.update(fetched);

            Charges updated = chargesService.findById(fetched.getId());
            System.out.println("✏️ Montant mis à jour : " + updated.getMontant());
        }

        // ==========================
        // 5. Recherche par cabinet
        // ==========================
        List<Charges> chargesCabinet = chargesService.findByCabinetId(cabinet.getId());
        System.out.println("📊 Charges du cabinet : " + chargesCabinet.size());

        // ==========================
        // 6. Calcul total des charges
        // ==========================
        double total = chargesService.calculateTotalCharges(cabinet.getId());
        System.out.println("💰 Total charges cabinet = " + total);

        // ==========================
        // 7. Suppression
        // ==========================
        chargesService.delete(charge.getId());
        System.out.println("🗑️ Charge supprimée : " + charge.getId());

        System.out.println("✅ TEST ChargesService terminé avec succès");
    }
}