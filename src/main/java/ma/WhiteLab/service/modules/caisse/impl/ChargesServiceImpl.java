package ma.WhiteLab.service.modules.caisse.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.repository.modules.cabinet.api.ChargesRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.caisse.api.ChargesService;

import java.time.LocalDateTime;
import java.util.List;

public class ChargesServiceImpl implements ChargesService {

    private final RepoFactory<ChargesRepository> chargesRepoFactory;

    public ChargesServiceImpl(RepoFactory<ChargesRepository> chargesRepoFactory) {
        this.chargesRepoFactory = chargesRepoFactory;
    }

    @Override
    public List<Charges> findAll() {
        return Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public Charges findById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID invalide");
        return Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public List<Charges> findByCabinetId(Long cabinetId) {
        if (cabinetId == null) throw new IllegalArgumentException("Cabinet ID invalide");
        return Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            return repo.findByCabinetId(cabinetId);
        });
    }

    @Override
    public List<Charges> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("Dates invalides");
        return Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            return repo.findByDateBetween(start, end);
        });
    }

    @Override
    public void create(Charges charges) {
        if (charges == null) throw new IllegalArgumentException("Charge invalide");
        charges.setDateCreation(LocalDateTime.now());
        Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            repo.create(charges);
            return null;
        });
    }

    @Override
    public void update(Charges charges) {
        if (charges == null || charges.getId() == null) throw new IllegalArgumentException("Charge invalide");
        charges.setDateMiseAJour(LocalDateTime.now());
        Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            repo.update(charges);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID invalide");
        Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public double calculateTotalCharges(Long cabinetId) {
        if (cabinetId == null) throw new IllegalArgumentException("Cabinet ID invalide");
        return Transaction.initTransaction(cnx -> {
            ChargesRepository repo = chargesRepoFactory.create(cnx);
            return repo.findByCabinetId(cabinetId)
                    .stream()
                    .mapToDouble(c -> c.getMontant() != null ? c.getMontant() : 0.0)
                    .sum();
        });
    }
}