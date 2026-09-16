package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.repository.modules.dossierMedical.api.MedicamentRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.OrdonnanceRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.PrescriptionRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrescriptionServiceImpl implements PrescriptionService {

    private final RepoFactory<PrescriptionRepository> prescriptionRepoFactory;
    private final RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory;
    private final RepoFactory<MedicamentRepository> medicamentRepoFactory;

    private final PrescriptionValidator validator;

    public PrescriptionServiceImpl(
            RepoFactory<PrescriptionRepository> prescriptionRepoFactory,
            RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory,
            RepoFactory<MedicamentRepository> medicamentRepoFactory) {

        this.prescriptionRepoFactory = prescriptionRepoFactory;
        this.ordonnanceRepoFactory = ordonnanceRepoFactory;
        this.medicamentRepoFactory = medicamentRepoFactory;

        // Le validateur reçoit aussi les factories
        this.validator = new PrescriptionValidatorImpl(
                ordonnanceRepoFactory,
                medicamentRepoFactory,
                prescriptionRepoFactory
        );
    }

    /* ================= CRUD AVEC VALIDATION ================= */

    @Override
    public void create(Prescription p) {
        Map<String, String> errors = validator.validateCreation(p);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation lors de la création : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            repo.create(p);
            return null;
        });
    }

    @Override
    public void update(Prescription p) {
        if (p == null || p.getId() == null) {
            throw new IllegalArgumentException("L'ID de la prescription est obligatoire pour une mise à jour.");
        }

        Map<String, String> errors = validator.validateUpdate(p);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation lors de la mise à jour : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            repo.update(p);
            return null;
        });
    }

    @Override
    public List<Prescription> getAll() {
        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public Prescription getById(Long id) {
        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la prescription est obligatoire pour la suppression.");
        }

        Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public Integer count() {
        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findAll().size(); // À améliorer avec count() dédié
        });
    }

    /* ================= MÉTHODES SPÉCIFIQUES ================= */

    @Override
    public List<Prescription> findByOrdonnanceId(Long ordonnanceId) {
        if (ordonnanceId == null) return List.of();

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findByOrdonnanceId(ordonnanceId);
        });
    }

    @Override
    public List<Prescription> findByMedicamentId(Long medicamentId) {
        if (medicamentId == null) return List.of();

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findByMedicamentId(medicamentId);
        });
    }

    @Override
    public List<Prescription> findByFrequence(String frequence) {
        if (frequence == null || frequence.trim().isEmpty()) return List.of();

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findByFrequence(frequence.trim());
        });
    }

    @Override
    public boolean exists(Long ordonnanceId, Long medicamentId) {
        if (ordonnanceId == null || medicamentId == null) return false;

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.exists(ordonnanceId, medicamentId);
        });
    }

    /* ================= MÉTHODES MÉTIERS ================= */

    @Override
    public List<Prescription> findByOrdonnanceIds(List<Long> ordonnanceIds) {
        if (ordonnanceIds == null || ordonnanceIds.isEmpty()) return List.of();

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return ordonnanceIds.stream()
                    .flatMap(id -> repo.findByOrdonnanceId(id).stream())
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<Prescription> findByMedicamentIds(List<Long> medicamentIds) {
        if (medicamentIds == null || medicamentIds.isEmpty()) return List.of();

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return medicamentIds.stream()
                    .flatMap(id -> repo.findByMedicamentId(id).stream())
                    .collect(Collectors.toList());
        });
    }

    @Override
    public int totalQuantitePrescriteForMedicament(Long medicamentId) {
        if (medicamentId == null) return 0;

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository repo = prescriptionRepoFactory.create(cnx);
            return repo.findByMedicamentId(medicamentId).stream()
                    .mapToInt(Prescription::getQte)
                    .sum();
        });
    }

    @Override
    public double totalPrixPrescritForMedicament(Long medicamentId) {
        if (medicamentId == null) return 0.0;

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);
            // Note : on suppose que Medicament est chargé avec le prix (sinon il faudra le charger via medicamentRepoFactory)
            return prescRepo.findByMedicamentId(medicamentId).stream()
                    .mapToDouble(p -> p.getQte() * (p.getMedicament() != null ? p.getMedicament().getPrixUnitaire() : 0.0))
                    .sum();
        });
    }

    @Override
    public List<Prescription> findRecent(int days) {
        if (days <= 0) return List.of();

        LocalDate threshold = LocalDate.now().minusDays(days);

        return Transaction.initTransaction(cnx -> {
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);

            return prescRepo.findAll().stream()
                    .filter(p -> {
                        if (p.getOrdonnance() == null || p.getOrdonnance().getId() == null) return false;
                        // On recharge l'ordonnance pour avoir la date (si elle n'est pas déjà chargée)
                        Ordonnance ord = ordRepo.findById(p.getOrdonnance().getId());
                        return ord != null && ord.getDateOrdonnance() != null && !ord.getDateOrdonnance().isBefore(threshold);
                    })
                    .collect(Collectors.toList());
        });
    }
}