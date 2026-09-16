package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.repository.modules.dossierMedical.api.ActeMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.InterventionMedecinRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InterventionMedecinServiceImpl implements InterventionMedecinService {

    private final RepoFactory<InterventionMedecinRepository> interventionRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<ActeMedicalRepository> acteRepoFactory;
    private final SituationFinanciereService situationFinanciereService;
    private final InterventionValidator validator = new InterventionValidatorImpl();

    public InterventionMedecinServiceImpl(
            RepoFactory<InterventionMedecinRepository> interventionRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<ActeMedicalRepository> acteRepoFactory,
            SituationFinanciereService situationFinanciereService) {

        this.interventionRepoFactory = interventionRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.acteRepoFactory = acteRepoFactory;
        this.situationFinanciereService = situationFinanciereService;
    }

    private InterventionDTO toDTO(InterventionMedecin entity) {
        if (entity == null) {
            return null;
        }
        InterventionDTO dto = new InterventionDTO();
        dto.setId(entity.getId());
        dto.setPrixDePatient(entity.getPrixDePatient());
        dto.setNumDent(entity.getNumDent());
        dto.setConsultationId(entity.getConsultation() != null ? entity.getConsultation().getId() : null);
        dto.setActeMedicalId(entity.getActeMedical() != null ? entity.getActeMedical().getId() : null);
        dto.setDateCreation(entity.getDateCreation());
        dto.setCreePar(entity.getCreePar());
        return dto;
    }

    private InterventionMedecin toEntity(InterventionDTO dto) {
        if (dto == null) {
            return null;
        }
        InterventionMedecin entity = new InterventionMedecin();
        entity.setPrixDePatient(dto.getPrixDePatient());
        entity.setNumDent(dto.getNumDent());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setCreePar(dto.getCreePar());
        return entity;
    }

    @Override
    public List<InterventionDTO> getAllInterventions() {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            List<InterventionMedecin> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public InterventionDTO getInterventionById(Long id) {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            InterventionMedecin entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public InterventionDTO createIntervention(InterventionDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            ActeMedicalRepository acteRepo = acteRepoFactory.create(cnx);

            InterventionMedecin entity = toEntity(dto);

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultationRepo.findById(dto.getConsultationId());
                if (consultation == null) {
                    throw new IllegalArgumentException("Consultation avec l'ID " + dto.getConsultationId() + " n'existe pas.");
                }
                entity.setConsultation(consultation);
            }

            if (dto.getActeMedicalId() != null) {
                ActeMedical acte = acteRepo.findById(dto.getActeMedicalId());
                if (acte == null) {
                    throw new IllegalArgumentException("Acte médical avec l'ID " + dto.getActeMedicalId() + " n'existe pas.");
                }
                entity.setActeMedical(acte);
            }

            repo.create(entity);
            dto.setId(entity.getId());

            if (entity.getConsultation() != null && entity.getConsultation().getDossierMedical() != null) {
                Long dossierId = entity.getConsultation().getDossierMedical().getId();
                situationFinanciereService.recalculateForDossier(dossierId);
            }

            return dto;
        });
    }

    @Override
    public InterventionDTO updateIntervention(Long id, InterventionDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            ActeMedicalRepository acteRepo = acteRepoFactory.create(cnx);

            InterventionMedecin existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Intervention avec l'ID " + id + " n'existe pas.");
            }

            Long oldDossierId = null;
            if (existing.getConsultation() != null && existing.getConsultation().getDossierMedical() != null) {
                oldDossierId = existing.getConsultation().getDossierMedical().getId();
            }

            InterventionMedecin entity = toEntity(dto);
            entity.setId(id);

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultationRepo.findById(dto.getConsultationId());
                if (consultation == null) {
                    throw new IllegalArgumentException("Consultation avec l'ID " + dto.getConsultationId() + " n'existe pas.");
                }
                entity.setConsultation(consultation);
            } else {
                entity.setConsultation(null);
            }

            if (dto.getActeMedicalId() != null) {
                ActeMedical acte = acteRepo.findById(dto.getActeMedicalId());
                if (acte == null) {
                    throw new IllegalArgumentException("Acte médical avec l'ID " + dto.getActeMedicalId() + " n'existe pas.");
                }
                entity.setActeMedical(acte);
            } else {
                entity.setActeMedical(null);
            }

            repo.update(entity);

            Long newDossierId = null;
            if (entity.getConsultation() != null && entity.getConsultation().getDossierMedical() != null) {
                newDossierId = entity.getConsultation().getDossierMedical().getId();
            }

            if (oldDossierId != null && !Objects.equals(oldDossierId, newDossierId)) {
                situationFinanciereService.recalculateForDossier(oldDossierId);
            }

            if (newDossierId != null) {
                situationFinanciereService.recalculateForDossier(newDossierId);
            }

            return toDTO(entity);
        });
    }

    @Override
    public void deleteIntervention(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            InterventionMedecin entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Intervention avec l'ID " + id + " n'existe pas.");
            }

            Long dossierId = null;
            if (entity.getConsultation() != null && entity.getConsultation().getDossierMedical() != null) {
                dossierId = entity.getConsultation().getDossierMedical().getId();
            }

            repo.deleteById(id);

            if (dossierId != null) {
                situationFinanciereService.recalculateForDossier(dossierId);
            }
            return null;
        });
    }

    @Override
    public List<InterventionDTO> getInterventionsByNumDent(int numDent) {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            List<InterventionMedecin> entities = repo.findByNumDent(numDent);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<InterventionDTO> getInterventionsByConsultationId(Long consultationId) {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            List<InterventionMedecin> entities = repo.findByConsultationId(consultationId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<InterventionDTO> getInterventionsByActeMedicalId(Long acteMedicalId) {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository repo = interventionRepoFactory.create(cnx);
            List<InterventionMedecin> entities = repo.findByActeMedicalId(acteMedicalId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }
}