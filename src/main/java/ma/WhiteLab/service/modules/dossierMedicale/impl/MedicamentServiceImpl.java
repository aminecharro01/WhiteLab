package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.MedicamentRepository;
import ma.WhiteLab.repository.modules.patient.api.AntecedentRepository;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentValidator;
import ma.WhiteLab.service.modules.dossierMedicale.dto.MedicamentDashboardDTO;
import ma.WhiteLab.service.modules.dossierMedicale.dto.MedicamentPatientDTO;
import ma.WhiteLab.service.common.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MedicamentServiceImpl implements MedicamentService {

    private final RepoFactory<MedicamentRepository> medicamentRepoFactory;
    private final MedicamentValidator validator;
    private final RepoFactory<DossierMedicalRepository> dossierMedicalRepoFactory;
    private final RepoFactory<AntecedentRepository> antecedentRepoFactory;

    public MedicamentServiceImpl(
            RepoFactory<MedicamentRepository> medicamentRepoFactory,
            MedicamentValidator validator,
            RepoFactory<DossierMedicalRepository> dossierMedicalRepoFactory,
            RepoFactory<AntecedentRepository> antecedentRepoFactory) {
        this.medicamentRepoFactory = medicamentRepoFactory;
        this.validator = validator;
        this.dossierMedicalRepoFactory = dossierMedicalRepoFactory;
        this.antecedentRepoFactory = antecedentRepoFactory;
    }

    /* ================= CRUD ================= */
    @Override
    public List<Medicament> getAll() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public Medicament getById(Long id) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public void create(Medicament m) {
        Map<String, String> errors = validator.validateCreation(m);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation lors de la création : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            repo.create(m);
            return null;
        });
    }

    @Override
    public void update(Medicament m) {
        Map<String, String> errors = validator.validateUpdate(m);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation lors de la mise à jour : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            repo.update(m);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            if (repo.findById(id) == null) {
                throw new IllegalArgumentException("Médicament introuvable (ID=" + id + ")");
            }
            repo.deleteById(id);
            return null;
        });
    }

    /* ================= Recherches ================= */
    @Override
    public List<Medicament> searchByName(String nom) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.searchByName(nom);
        });
    }

    @Override
    public List<Medicament> searchByNameLike(String text) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.searchByNameLike(text);
        });
    }

    @Override
    public List<Medicament> findByLabo(String labo) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findByLabo(labo);
        });
    }

    @Override
    public List<Medicament> findRemboursables() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findRemboursables();
        });
    }

    @Override
    public List<Medicament> findByForme(Forme forme) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findByForme(forme != null ? forme.name() : null);
        });
    }

    @Override
    public List<Medicament> findByPrixBetween(double min, double max) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findByPrixBetween(min, max);
        });
    }

    @Override
    public List<Medicament> findCheap(double maxPrice) {
        return findByPrixBetween(0, maxPrice);
    }

    @Override
    public List<Medicament> findExpensive(double minPrice) {
        return findByPrixBetween(minPrice, Double.MAX_VALUE);
    }

    /* ================= Statistiques ================= */
    @Override
    public long countTotal() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.count();
        });
    }

    @Override
    public long countRemboursables() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.countRemboursables();
        });
    }

    @Override
    public long countNonRemboursables() {
        return countTotal() - countRemboursables();
    }

    @Override
    public double getPrixMoyen() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.getPrixMoyen();
        });
    }

    @Override
    public Medicament getMedicamentLePlusCher() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findTopByPrixDesc();
        });
    }

    @Override
    public Medicament getMedicamentLeMoinsCher() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            return repo.findTopByPrixAsc();
        });
    }

    @Override
    public MedicamentDashboardDTO getDashboardStats() {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);
            List<Medicament> all = repo.findAll();

            long total = all.size();
            long remboursables = all.stream().filter(Medicament::isRemboursable).count();
            double prixMoyen = all.stream().mapToDouble(Medicament::getPrixUnitaire).average().orElse(0.0);
            Medicament plusCher = repo.findTopByPrixDesc();
            Medicament moinsCher = repo.findTopByPrixAsc();

            MedicamentDashboardDTO dto = new MedicamentDashboardDTO();
            dto.setTotal(total);
            dto.setRemboursables(remboursables);
            dto.setNonRemboursables(total - remboursables);
            dto.setPrixMoyen(prixMoyen);
            dto.setPlusCher(plusCher);
            dto.setMoinsCher(moinsCher);

            return dto;
        });
    }

    /* ================= Logique Patient & Antécédents ================= */
    @Override
    public MedicamentPatientDTO getMedicamentsPourPatient(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dmRepo = dossierMedicalRepoFactory.create(cnx);
            AntecedentRepository antRepo = antecedentRepoFactory.create(cnx);
            MedicamentRepository medRepo = medicamentRepoFactory.create(cnx);

            Optional<DossierMedical> dossierOpt = dmRepo.findByPatientId(patientId);
            if (dossierOpt.isEmpty()) {
                throw new RuntimeException("Dossier médical introuvable pour le patient ID=" + patientId);
            }

            Patient patient = dossierOpt.get().getPat();
            List<Antecedent> antecedents = antRepo.getAntecedentsByPatientId(patient.getId());

            List<Medicament> compatibles = getMedicamentsCompatibles(antecedents, medRepo);
            List<Medicament> contreIndiques = getMedicamentsContreIndiques(antecedents, medRepo);

            return new MedicamentPatientDTO(patient.getId(), compatibles, contreIndiques);
        });
    }

    @Override
    public List<Medicament> getMedicamentsCompatibles(List<Antecedent> antecedents) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);

            if (antecedents == null || antecedents.isEmpty()) {
                return repo.findAll(); // Tous les médicaments sont compatibles s'il n'y a pas d'antécédents
            }

            return repo.findAll().stream()
                    .filter(m -> {
                        for (Antecedent a : antecedents) {
                            switch (a.getCategorie()) {
                                case ALLERGIE:
                                    if ("antibiotique".equalsIgnoreCase(m.getType()) ||
                                            (m.getNom() != null && m.getNom().toLowerCase().contains(a.getNom().toLowerCase()))) {
                                        return false;
                                    }
                                    break;

                                case CONTRE_INDICATION:
                                    if (m.getNom() != null && m.getNom().toLowerCase().contains(a.getNom().toLowerCase())) {
                                        return false;
                                    }
                                    break;

                                case MALADIE_CHRONIQUE:
                                    // Exemple : éviter les anti-inflammatoires pour certaines maladies
                                    if ("anti-inflammatoire".equalsIgnoreCase(m.getType())) {
                                        return false;
                                    }
                                    break;

                                case TRAITEMENT_EN_COURS:
                                    // Éviter les doublons ou interactions avec traitement en cours
                                    if (m.getNom() != null && m.getNom().toLowerCase().contains(a.getNom().toLowerCase())) {
                                        return false;
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }
                        return true; // Compatible si aucune règle n'est violée
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<Medicament> getMedicamentsContreIndiques(List<Antecedent> antecedents) {
        return Transaction.initTransaction(cnx -> {
            MedicamentRepository repo = medicamentRepoFactory.create(cnx);

            if (antecedents == null || antecedents.isEmpty()) {
                return List.of(); // Aucun contre-indiqué s'il n'y a pas d'antécédents
            }

            List<Medicament> all = repo.findAll();
            List<Medicament> compatibles = getMedicamentsCompatibles(antecedents);

            return all.stream()
                    .filter(m -> !compatibles.contains(m))
                    .collect(Collectors.toList());
        });
    }

    private List<Medicament> getMedicamentsCompatibles(List<Antecedent> antecedents, MedicamentRepository repo) {
        if (antecedents == null || antecedents.isEmpty()) {
            return repo.findAll();
        }

        return repo.findAll().stream()
                .filter(m -> {
                    for (Antecedent a : antecedents) {
                        switch (a.getCategorie()) {
                            case ALLERGIE:
                                if ("antibiotique".equalsIgnoreCase(m.getType()) ||
                                        m.getNom().toLowerCase().contains(a.getNom().toLowerCase())) {
                                    return false;
                                }
                                break;
                            case CONTRE_INDICATION:
                                if (m.getNom().toLowerCase().contains(a.getNom().toLowerCase())) {
                                    return false;
                                }
                                break;
                            case MALADIE_CHRONIQUE:
                                if ("anti-inflammatoire".equalsIgnoreCase(m.getType())) {
                                    return false;
                                }
                                break;
                            case TRAITEMENT_EN_COURS:
                                if (m.getNom().toLowerCase().contains(a.getNom().toLowerCase())) {
                                    return false;
                                }
                                break;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Medicament> getMedicamentsContreIndiques(List<Antecedent> antecedents, MedicamentRepository repo) {
        List<Medicament> compatibles = getMedicamentsCompatibles(antecedents, repo);
        return repo.findAll().stream()
                .filter(m -> !compatibles.contains(m))
                .toList();
    }
}