package ma.WhiteLab.service.modules.cabinet.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.repository.modules.cabinet.api.CabinetMedicaleRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalValidator;
import ma.WhiteLab.service.modules.cabinet.api.ParametrageService;

import java.time.LocalDateTime;
import java.util.List;

public class ParametrageServiceImpl implements ParametrageService {

    private final RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory;
    private final CabinetMedicalValidator validator;

    public ParametrageServiceImpl(
            RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory,
            CabinetMedicalValidator validator) {
        this.cabinetRepoFactory = cabinetRepoFactory;
        this.validator = validator;
    }

    @Override
    public CabinetMedicale getCabinetActuel() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            List<CabinetMedicale> cabinets = repo.findAll();
            if (cabinets.isEmpty()) {
                throw new RuntimeException("Aucun cabinet configuré");
            }
            // Mono-cabinet → retourne le premier
            return cabinets.get(0);
        });
    }

    @Override
    public CabinetMedicale updateParametresCabinet(CabinetMedicale cabinet, String modifierPar) {
        if (cabinet == null || cabinet.getId() == null) {
            throw new IllegalArgumentException("Cabinet invalide");
        }

        // Optional: validate
        // validator.validateForUpdate(cabinet);

        cabinet.setModifierPar(modifierPar);
        cabinet.setDateMiseAJour(LocalDateTime.now());

        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            repo.update(cabinet);
            return repo.findById(cabinet.getId());
        });
    }

    @Override
    public CabinetMedicale updateLogo(String logoPath, String modifierPar) {
        if (logoPath == null || logoPath.isBlank()) {
            throw new IllegalArgumentException("Logo invalide");
        }
        
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            List<CabinetMedicale> cabinets = repo.findAll();
            if (cabinets.isEmpty()) throw new RuntimeException("Aucun cabinet configuré");
            
            CabinetMedicale cabinet = cabinets.get(0);
            cabinet.setLogo(logoPath);
            cabinet.setModifierPar(modifierPar);
            cabinet.setDateMiseAJour(LocalDateTime.now());
            repo.update(cabinet);
            
            return cabinet;
        });
    }

    @Override
    public void removeLogo(String modifierPar) {
        Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            List<CabinetMedicale> cabinets = repo.findAll();
            if (!cabinets.isEmpty()) {
                CabinetMedicale cabinet = cabinets.get(0);
                cabinet.setLogo(null);
                cabinet.setModifierPar(modifierPar);
                cabinet.setDateMiseAJour(LocalDateTime.now());
                repo.update(cabinet);
            }
            return null;
        });
    }

    @Override
    public boolean isCabinetConfigured() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return !repo.findAll().isEmpty();
        });
    }

    @Override
    public CabinetMedicale initialiserCabinet(CabinetMedicale cabinet, String creePar) {
        if (cabinet == null) {
            throw new IllegalArgumentException("Cabinet ne peut pas être null");
        }
        if (creePar == null || creePar.isBlank()) {
            throw new IllegalArgumentException("Utilisateur créateur requis");
        }

        cabinet.setCreePar(creePar);
        cabinet.setDateCreation(LocalDateTime.now());
        
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            repo.create(cabinet);
            return cabinet;
        });
    }

    @Override
    public boolean isMonoCabinet() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findAll().size() <= 1;
        });
    }
}
