package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.repository.modules.dossierMedical.api.ActeMedicalRepository;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeValidator;
import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActeMedicalServiceImpl implements ActeMedicalService {

    private final RepoFactory<ActeMedicalRepository> acteRepoFactory;
    private final ActeValidator validator = new ActeValidatorImpl();

    public ActeMedicalServiceImpl(
            RepoFactory<ActeMedicalRepository> acteRepoFactory) {

        this.acteRepoFactory = acteRepoFactory;
    }

    private ActeDTO toDTO(ActeMedical entity) {
        if (entity == null) {
            return null;
        }
        ActeDTO dto = new ActeDTO();
        dto.setId(entity.getId());
        dto.setLibelle(entity.getLibelle());
        dto.setCategorie(entity.getCategorie());
        dto.setPrixDeBase(entity.getPrixDeBase());
        dto.setCreePar(entity.getCreePar());
        return dto;
    }

    private ActeMedical toEntity(ActeDTO dto) {
        if (dto == null) {
            return null;
        }
        ActeMedical entity = new ActeMedical();
        entity.setLibelle(dto.getLibelle());
        entity.setCategorie(dto.getCategorie());
        entity.setPrixDeBase(dto.getPrixDeBase());
        entity.setCreePar(dto.getCreePar());
        return entity;
    }

    @Override
    public List<ActeDTO> getAllActes() {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            List<ActeMedical> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ActeDTO getActeById(Long id) {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            ActeMedical entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public ActeDTO createActe(ActeDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);

            ActeMedical entity = toEntity(dto);

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public ActeDTO updateActe(Long id, ActeDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);

            ActeMedical existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Acte médical avec l'ID " + id + " n'existe pas.");
            }

            ActeMedical entity = toEntity(dto);
            entity.setId(id);

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteActe(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            ActeMedical entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Acte médical avec l'ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public ActeDTO getActeByLibelle(String libelle) {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            Optional<ActeMedical> optionalEntity = repo.findByLibelle(libelle);
            return optionalEntity.map(this::toDTO).orElse(null);
        });
    }

    @Override
    public String getMostFrequentActeNomCeMois(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            return repo.findMostFrequentActeNomCeMois(cabinetId);
        });
    }

    @Override
    public List<ActeDTO> getTop5ActesByPeriodeAndCabinet(LocalDate debutMois, LocalDate finMois, Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            List<ActeMedical> entities = repo.findTop5ActesByPeriodeAndCabinet(debutMois, finMois, cabinetId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }
}