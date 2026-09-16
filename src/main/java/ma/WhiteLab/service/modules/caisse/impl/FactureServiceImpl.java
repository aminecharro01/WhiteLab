package ma.WhiteLab.service.modules.caisse.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.entities.enums.StatutFacture;
import ma.WhiteLab.repository.modules.dossierMedical.api.FactureRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.caisse.api.FactureService;

import java.time.LocalDate;
import java.util.List;

public class FactureServiceImpl implements FactureService {

    private final RepoFactory<FactureRepository> factureRepoFactory;

    public FactureServiceImpl(RepoFactory<FactureRepository> factureRepoFactory) {
        this.factureRepoFactory = factureRepoFactory;
    }

    @Override
    public List<Facture> findAll() {
        return Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public Facture findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid facture ID");
        return Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public void create(Facture facture) {
        if (facture == null) throw new IllegalArgumentException("Facture is null");
        calculateReste(facture);
        Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            repo.create(facture);
            return null;
        });
    }

    @Override
    public void update(Facture facture) {
        if (facture == null || facture.getId() == null) throw new IllegalArgumentException("Facture or ID is null");
        calculateReste(facture);
        Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            repo.update(facture);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid facture ID");
        Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<Facture> findByConsultationId(Long consultationId) {
        if (consultationId == null || consultationId <= 0) throw new IllegalArgumentException("Invalid consultation ID");
        return Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            return repo.findByConsultationId(consultationId);
        });
    }

    @Override
    public List<Facture> findByStatut(StatutFacture statut) {
        if (statut == null) throw new IllegalArgumentException("Statut is null");
        return Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            return repo.findByStatut(statut.name());
        });
    }

    @Override
    public List<Facture> findByDateBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) throw new IllegalArgumentException("Dates cannot be null");
        return Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            return repo.findByDateBetween(start, end);
        });
    }

    @Override
    public void markAsPaid(Long factureId) {
        Transaction.initTransaction(cnx -> {
            FactureRepository repo = factureRepoFactory.create(cnx);
            Facture facture = repo.findById(factureId);
            if (facture != null) {
                facture.setTotalPaye(facture.getTotalFact());
                facture.setReste(0f);
                facture.setStatut(StatutFacture.PAYEE);
                repo.update(facture);
            }
            return null;
        });
    }

    @Override
    public void calculateReste(Facture facture) {
        if (facture == null) return;
        float totalFact = facture.getTotalFact();
        float totalPaye = facture.getTotalPaye();
        float reste = totalFact - totalPaye;
        facture.setReste(reste);
        if (reste <= 0f) {
            facture.setStatut(StatutFacture.PAYEE);
        } else {
            facture.setStatut(StatutFacture.EN_ATTENTE);
        }
    }
}