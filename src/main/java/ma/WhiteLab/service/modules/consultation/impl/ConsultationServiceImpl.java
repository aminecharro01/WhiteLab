package ma.WhiteLab.service.modules.consultation.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.InterventionMedecinRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.ActeMedicalRepository;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationValidator;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;
import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsultationServiceImpl implements ConsultationService {

    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<InterventionMedecinRepository> interventionRepoFactory;
    private final RepoFactory<ActeMedicalRepository> acteRepoFactory;
    private final ConsultationValidator validator = new ConsultationValidatorImpl();

    public ConsultationServiceImpl(
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<InterventionMedecinRepository> interventionRepoFactory,
            RepoFactory<ActeMedicalRepository> acteRepoFactory) {

        this.consultationRepoFactory = consultationRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
        this.interventionRepoFactory = interventionRepoFactory;
        this.acteRepoFactory = acteRepoFactory;
    }

    private ConsultationDTO toDTO(Consultation entity) {
        if (entity == null) {
            return null;
        }
        ConsultationDTO dto = new ConsultationDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setNotes(entity.getNotes());
        dto.setObservationsMedecin(entity.getObservationsMedecin());
        dto.setDossierMedicalId(entity.getDossierMedical() != null ? entity.getDossierMedical().getId() : null);
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setCreePar(entity.getCreePar());
        dto.setModifierPar(entity.getModifierPar());
        return dto;
    }

    private Consultation toEntity(ConsultationDTO dto) {
        if (dto == null) {
            return null;
        }
        Consultation entity = new Consultation();
        entity.setDate(dto.getDate());
        entity.setStatus(dto.getStatus() != null ? Enum.valueOf(ma.WhiteLab.entities.enums.StatusConsultation.class, dto.getStatus()) : null);
        entity.setNotes(dto.getNotes());
        entity.setObservationsMedecin(dto.getObservationsMedecin());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setDateMiseAJour(dto.getDateMiseAJour());
        entity.setCreePar(dto.getCreePar());
        entity.setModifierPar(dto.getModifierPar());
        return entity;
    }

    private InterventionMedecin toInterventionEntity(InterventionDTO dto) {
        if (dto == null) {
            return null;
        }
        InterventionMedecin entity = new InterventionMedecin();
        entity.setPrixDePatient(dto.getPrixDePatient()); // Assuming DTO has this field
        entity.setNumDent(dto.getNumDent()); // Assuming DTO has this field
        entity.setDateCreation(LocalDateTime.now());
        entity.setCreePar(dto.getCreePar()); // Assuming DTO has this field
        // Assuming DTO has acteMedicalId; we'll set ActeMedical later if needed
        return entity;
    }

    private ActeMedical toActeEntity(ActeDTO dto) {
        if (dto == null) {
            return null;
        }
        ActeMedical entity = new ActeMedical();
        entity.setLibelle(dto.getLibelle()); // Assuming DTO has this field
        entity.setCategorie(dto.getCategorie()); // Assuming DTO has this field
        entity.setPrixDeBase(dto.getPrixDeBase()); // Assuming DTO has this field
        entity.setDateCreation(LocalDateTime.now());
        entity.setCreePar(dto.getCreePar()); // Assuming DTO has this field
        return entity;
    }

    @Override
    public List<ConsultationDTO> getAllConsultations() {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            List<Consultation> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ConsultationDTO getConsultationById(Long id) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            Consultation entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public ConsultationDTO createConsultation(ConsultationDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            Consultation entity = toEntity(dto);

            if (dto.getDossierMedicalId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedicalId());
                if (dossier == null) {
                    throw new IllegalArgumentException("Dossier médical avec l'ID " + dto.getDossierMedicalId() + " n'existe pas.");
                }
                entity.setDossierMedical(dossier);
            }

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public ConsultationDTO updateConsultation(Long id, ConsultationDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            Consultation existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Consultation avec l'ID " + id + " n'existe pas.");
            }

            Consultation entity = toEntity(dto);
            entity.setId(id);

            if (dto.getDossierMedicalId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedicalId());
                if (dossier == null) {
                    throw new IllegalArgumentException("Dossier médical avec l'ID " + dto.getDossierMedicalId() + " n'existe pas.");
                }
                entity.setDossierMedical(dossier);
            } else {
                entity.setDossierMedical(null);
            }

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteConsultation(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            Consultation entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Consultation avec l'ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<ConsultationDTO> getConsultationsByStatus(String status) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            List<Consultation> entities = repo.findByStatus(status);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<ConsultationDTO> getConsultationsByDossierMedicalId(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            List<Consultation> entities = repo.findByDossierMedicalId(dossierId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public long countByCabinet(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            return repo.countByCabinet(cabinetId);
        });
    }

    @Override
    public long countByCabinetAndMonth(Long cabinetId, int year, int month) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            return repo.countByCabinetAndMonth(cabinetId, year, month);
        });
    }

    @Override
    public long countByDateAndCabinet(LocalDate date, Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository repo = consultationRepoFactory.create(cnx);
            return repo.countByDateAndCabinet(date, cabinetId);
        });
    }

    @Override
    public void addIntervention(Long consultationId, InterventionDTO interventionDto) {
        Transaction.initTransaction(cnx -> {
            ConsultationRepository consultRepo = consultationRepoFactory.create(cnx);
            InterventionMedecinRepository interventionRepo = interventionRepoFactory.create(cnx);
            ActeMedicalRepository acteRepo = acteRepoFactory.create(cnx);

            Consultation consultation = consultRepo.findById(consultationId);
            if (consultation == null) {
                throw new IllegalArgumentException("Consultation avec l'ID " + consultationId + " n'existe pas.");
            }

            InterventionMedecin intervention = toInterventionEntity(interventionDto);
            intervention.setConsultation(consultation);

            // Assuming InterventionDTO has acteMedicalId
            if (interventionDto.getActeMedicalId() != null) {
                ActeMedical acte = acteRepo.findById(interventionDto.getActeMedicalId());
                if (acte == null) {
                    throw new IllegalArgumentException("Acte médical avec l'ID " + interventionDto.getActeMedicalId() + " n'existe pas.");
                }
                intervention.setActeMedical(acte);
            }

            interventionRepo.create(intervention);
            return null;
        });
    }

    @Override
    public void addActe(Long consultationId, ActeDTO acteDto) {
        Transaction.initTransaction(cnx -> {
            // Note: consultationId is provided but unused here, as ActeMedical is a catalog entry not directly tied to a consultation.
            // If the intent is to add an intervention based on this acte, use addIntervention instead.
            // Assuming this method creates a new ActeMedical in the catalog (independent of consultation).
            ActeMedicalRepository acteRepo = acteRepoFactory.create(cnx);

            ActeMedical acte = toActeEntity(acteDto);

            acteRepo.create(acte);
            return null;
        });
    }

    @Override
    public List<ActeMedical> getActesDisponibles() {
        return Transaction.initTransaction(cnx -> {
            ActeMedicalRepository repo = acteRepoFactory.create(cnx);
            return repo.findAll();           // ← implement findAll() if not already done
        });
    }
}