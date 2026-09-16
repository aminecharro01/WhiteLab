package ma.WhiteLab.service.modules.caisse.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.repository.modules.cabinet.api.RevenusRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.caisse.api.RevenusService;

import java.time.LocalDateTime;
import java.util.List;

public class RevenusServiceImpl implements RevenusService {

    private final RepoFactory<RevenusRepository> revenusRepoFactory;

    public RevenusServiceImpl(RepoFactory<RevenusRepository> revenusRepoFactory) {
        this.revenusRepoFactory = revenusRepoFactory;
    }

    @Override
    public List<Revenus> findAll() {
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public Revenus findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid revenus ID");
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public void create(Revenus revenus) {
        if (revenus == null) throw new IllegalArgumentException("Revenus is null");
        Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            repo.create(revenus);
            return null;
        });
    }

    @Override
    public void update(Revenus revenus) {
        if (revenus == null || revenus.getId() == null) throw new IllegalArgumentException("Revenus or ID is null");
        Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            repo.update(revenus);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid revenus ID");
        Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<Revenus> findByCabinetId(Long cabinetId) {
        if (cabinetId == null || cabinetId <= 0) throw new IllegalArgumentException("Invalid cabinet ID");
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findByCabinetId(cabinetId);
        });
    }

    @Override
    public List<Revenus> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("Dates cannot be null");
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findByDateBetween(start, end);
        });
    }

    @Override
    public List<Revenus> findByTitre(String titre) {
        if (titre == null || titre.isBlank()) throw new IllegalArgumentException("Titre is empty");
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findByTitre(titre);
        });
    }

    @Override
    public double calculateTotalRevenus(Long cabinetId) {
        // Can reuse findByCabinetId logic
        return Transaction.initTransaction(cnx -> {
            RevenusRepository repo = revenusRepoFactory.create(cnx);
            return repo.findByCabinetId(cabinetId)
                    .stream()
                    .mapToDouble(r -> r.getMontant() != null ? r.getMontant() : 0.0)
                    .sum();
        });
    }
}