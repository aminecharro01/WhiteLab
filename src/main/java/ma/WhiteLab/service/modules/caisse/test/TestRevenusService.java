package ma.WhiteLab.service.modules.caisse.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.service.modules.caisse.api.RevenusService;

import java.time.LocalDateTime;
import java.util.List;

public class TestRevenusService {

    public static void main(String[] args) {

        ApplicationContext context = ApplicationContext.getInstance();
        RevenusService revenusService = ApplicationContext.getBean(RevenusService.class);
        
        if (revenusService == null) {
            System.err.println("FATAL: RevenusService could not be loaded from the context.");
            return;
        }

        // ==========================
        // 1. Cabinet existant
        // ==========================
        CabinetMedicale cabinet = new CabinetMedicale();
        cabinet.setId(1L); // ⚠️ doit exister

        // ==========================
        // 2. Création
        // ==========================
        Revenus revenus = new Revenus();
        revenus.setTitre("Consultation médicale");
        revenus.setDescription("Consultation générale");
        revenus.setMontant(300.0);
        revenus.setDate(LocalDateTime.now());
        revenus.setCabinetMedicale(cabinet);
        revenus.setCreePar("admin");

        revenusService.create(revenus);
        System.out.println("✅ Revenu créé ID = " + revenus.getId());

        // ==========================
        // 3. Find by ID
        // ==========================
        Revenus fetched = revenusService.findById(revenus.getId());
        if (fetched != null) {
            System.out.println("📄 Revenu : " + fetched.getTitre() + " | " + fetched.getMontant());
        }

        // ==========================
        // 4. Update
        // ==========================
        if (fetched != null) {
            fetched.setMontant(350.0);
            fetched.setModifierPar("admin");
            revenusService.update(fetched);

            Revenus updated = revenusService.findById(fetched.getId());
            System.out.println("✏️ Montant mis à jour = " + updated.getMontant());
        }

        // ==========================
        // 5. Recherche par cabinet
        // ==========================
        List<Revenus> revenusCabinet = revenusService.findByCabinetId(cabinet.getId());
        System.out.println("📊 Revenus du cabinet = " + revenusCabinet.size());

        // ==========================
        // 6. Recherche par titre
        // ==========================
        List<Revenus> byTitre = revenusService.findByTitre("Consultation");
        System.out.println("🔍 Revenus trouvés par titre = " + byTitre.size());

        // ==========================
        // 7. Total revenus
        // ==========================
        double total = revenusService.calculateTotalRevenus(cabinet.getId());
        System.out.println("💰 Total revenus cabinet = " + total);

        // ==========================
        // 8. Suppression
        // ==========================
        revenusService.delete(revenus.getId());
        System.out.println("🗑️ Revenu supprimé ID = " + revenus.getId());

        System.out.println("✅ TEST RevenusService terminé avec succès");
    }
}