package ma.WhiteLab.service.modules.cabinet.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.repository.modules.cabinet.api.CabinetMedicaleRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalService;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalValidator;
import ma.WhiteLab.service.modules.cabinet.dto.CabinetSummaryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des cabinets médicaux.
 */
public class CabinetMedicalServiceImpl implements CabinetMedicalService {

    private final RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory;
    private final CabinetMedicalValidator cabinetValidator;

    public CabinetMedicalServiceImpl(
            RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory,
            CabinetMedicalValidator cabinetValidator) {
        this.cabinetRepoFactory = cabinetRepoFactory;
        this.cabinetValidator = cabinetValidator;
    }

    // ============================
    // READ
    // ============================

    @Override
    public List<CabinetMedicale> findAll() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public List<CabinetMedicale> findAllOrderByNom() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findAllOrderByNom();
        });
    }

    @Override
    public List<CabinetSummaryDTO> getAllSummaries() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findAll().stream()
                    .map(cabinet -> new CabinetSummaryDTO(
                            cabinet.getId(),
                            cabinet.getNom(),
                            cabinet.getCategorie(),
                            cabinet.getEmail(),
                            cabinet.getTel1(),
                            cabinet.getTel2(),
                            cabinet.getLogo() != null && !cabinet.getLogo().trim().isEmpty(),
                            cabinet.getSiteWeb() != null && !cabinet.getSiteWeb().trim().isEmpty(),
                            (cabinet.getInstagram() != null && !cabinet.getInstagram().trim().isEmpty()) ||
                                    (cabinet.getFacebook() != null && !cabinet.getFacebook().trim().isEmpty())
                    ))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public Optional<CabinetMedicale> findById(Long id) {
        if (id == null) return Optional.empty();
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return Optional.ofNullable(repo.findById(id));
        });
    }

    @Override
    public Optional<CabinetMedicale> findByNom(String nom) {
        if (nom == null || nom.trim().isBlank()) return Optional.empty();
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findByNom(nom.trim());
        });
    }

    @Override
    public Optional<CabinetMedicale> findByEmail(String email) {
        if (email == null || email.trim().isBlank()) return Optional.empty();
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findByEmail(email.trim());
        });
    }

    @Override
    public List<CabinetMedicale> findByCategorie(String categorie) {
        if (categorie == null || categorie.trim().isBlank()) return List.of();
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findByCategorie(categorie.trim());
        });
    }

    @Override
    public List<CabinetMedicale> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String key = keyword.trim().toLowerCase();

        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            return repo.findAll().stream()
                    .filter(c ->
                            (c.getNom() != null && c.getNom().toLowerCase().contains(key)) ||
                                    (c.getEmail() != null && c.getEmail().toLowerCase().contains(key)) ||
                                    (c.getTel1() != null && c.getTel1().contains(key)) ||
                                    (c.getTel2() != null && c.getTel2().contains(key))
                    )
                    .collect(Collectors.toList());
        });
    }

    @Override
    public long count() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            try {
                return repo.count();
            } catch (UnsupportedOperationException e) {
                return (long) repo.findAll().size();
            }
        });
    }

    // ============================
    // WRITE
    // ============================

    @Override
    public CabinetMedicale create(CabinetMedicale cabinet, String creePar) {
        if (cabinet == null) throw new IllegalArgumentException("Le cabinet ne peut pas être null");
        if (creePar == null || creePar.isBlank()) throw new IllegalArgumentException("Utilisateur créateur requis");

        cabinetValidator.validateForCreate(cabinet);

        cabinet.setDateCreation(LocalDateTime.now());
        cabinet.setCreePar(creePar);

        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            repo.create(cabinet);
            return cabinet;
        });
    }

    @Override
    public CabinetMedicale update(CabinetMedicale cabinet, String modifierPar) {
        if (cabinet == null || cabinet.getId() == null)
            throw new IllegalArgumentException("Cabinet invalide ou ID manquant");
        if (modifierPar == null || modifierPar.isBlank())
            throw new IllegalArgumentException("Utilisateur modificateur requis");

        cabinetValidator.validateForUpdate(cabinet);

        cabinet.setDateMiseAJour(LocalDateTime.now());
        cabinet.setModifierPar(modifierPar);

        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            if (repo.findById(cabinet.getId()) == null) {
                throw new IllegalArgumentException("Cabinet introuvable avec l'ID : " + cabinet.getId());
            }
            repo.update(cabinet);
            return cabinet;
        });
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID requis pour la suppression");
        Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository repo = cabinetRepoFactory.create(cnx);
            if (repo.findById(id) == null) {
                throw new IllegalArgumentException("Cabinet introuvable avec l'ID : " + id);
            }
            repo.deleteById(id);
            return null;
        });
    }
}
