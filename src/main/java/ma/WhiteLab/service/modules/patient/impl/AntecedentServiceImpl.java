package ma.WhiteLab.service.modules.patient.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.entities.enums.NiveauDeRisk;
import ma.WhiteLab.repository.modules.patient.api.AntecedentRepository;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.api.AntecedentValidator;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AntecedentServiceImpl implements AntecedentService {

    private final RepoFactory<AntecedentRepository> antecedentRepoFactory;
    private final AntecedentValidator validator = new AntecedentValidatorImpl();

    public AntecedentServiceImpl(
            RepoFactory<AntecedentRepository> antecedentRepoFactory) {

        this.antecedentRepoFactory = antecedentRepoFactory;
    }

    private AntecedentDTO toDTO(Antecedent entity) {
        if (entity == null) {
            return null;
        }
        AntecedentDTO dto = new AntecedentDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setDescription(entity.getDescription());
        dto.setCategorie(entity.getCategorie() != null ? entity.getCategorie().name() : null);
        dto.setNiveauRisque(entity.getNiveauDeRisk() != null ? entity.getNiveauDeRisk().name() : null);
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setCreePar(entity.getCreePar());
        dto.setModifierPar(entity.getModifierPar());
        return dto;
    }

    private Antecedent toEntity(AntecedentDTO dto) {
        if (dto == null) {
            return null;
        }
        Antecedent entity = new Antecedent();
        entity.setNom(dto.getNom());
        entity.setDescription(dto.getDescription());
        entity.setCategorie(dto.getCategorie() != null ? CategorieAntecedent.valueOf(dto.getCategorie()) : null);
        entity.setNiveauDeRisk(dto.getNiveauRisque() != null ? NiveauDeRisk.valueOf(dto.getNiveauRisque()) : null);
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setDateMiseAJour(dto.getDateMiseAJour());
        entity.setCreePar(dto.getCreePar());
        entity.setModifierPar(dto.getModifierPar());
        return entity;
    }

    @Override
    public List<AntecedentDTO> getAllAntecedents() {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            List<Antecedent> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public AntecedentDTO getAntecedentById(Long id) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            Antecedent entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public AntecedentDTO createAntecedent(AntecedentDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);

            Antecedent entity = toEntity(dto);

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public AntecedentDTO updateAntecedent(Long id, AntecedentDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);

            Antecedent existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Antecedent avec l'ID " + id + " n'existe pas.");
            }

            Antecedent entity = toEntity(dto);
            entity.setId(id);

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteAntecedent(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            Antecedent entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Antecedent avec l'ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public AntecedentDTO getAntecedentByNom(String nom) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            Optional<Antecedent> optionalEntity = repo.findByNom(nom);
            return optionalEntity.map(this::toDTO).orElse(null);
        });
    }

    @Override
    public List<AntecedentDTO> getAntecedentsByCategorie(String categorie) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            List<Antecedent> entities = repo.findByCategorie(CategorieAntecedent.valueOf(categorie));
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<AntecedentDTO> getAntecedentsByNiveauRisque(String niveau) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            List<Antecedent> entities = repo.findByNiveauRisque(NiveauDeRisk.valueOf(niveau));
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean existsAntecedentById(Long id) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            return repo.existsById(id);
        });
    }

    @Override
    public long countAntecedents() {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            return repo.count();
        });
    }

    @Override
    public List<AntecedentDTO> getAntecedentsPage(int limit, int offset) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            List<Antecedent> entities = repo.findPage(limit, offset);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<Patient> getPatientsHavingAntecedent(Long antecedentId) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            return repo.getPatientsHavingAntecedent(antecedentId);
        });
    }

    @Override
    public List<AntecedentDTO> getAntecedentsByPatientId(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            AntecedentRepository repo = antecedentRepoFactory.create(cnx);
            List<Antecedent> entities = repo.getAntecedentsByPatientId(patientId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }
}