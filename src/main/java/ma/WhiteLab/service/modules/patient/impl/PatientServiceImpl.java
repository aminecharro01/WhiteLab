package ma.WhiteLab.service.modules.patient.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.enums.TitreNotification;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.entities.enums.Sexe;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.repository.modules.patient.api.AntecedentRepository;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.api.PatientValidator;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatientServiceImpl implements PatientService {

    private final RepoFactory<PatientRepository> patientRepoFactory;
    private final RepoFactory<AntecedentRepository> antecedentRepoFactory;
    private final PatientValidator validator = new PatientValidatorImpl();

    public PatientServiceImpl(
            RepoFactory<PatientRepository> patientRepoFactory,
            RepoFactory<AntecedentRepository> antecedentRepoFactory) {

        this.patientRepoFactory = patientRepoFactory;
        this.antecedentRepoFactory = antecedentRepoFactory;
    }

    private PatientDTO toDTO(Patient entity) {
        if (entity == null) {
            return null;
        }
        PatientDTO dto = new PatientDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setAdresse(entity.getAdresse());
        dto.setTelephone(entity.getTelephone());
        dto.setEmail(entity.getEmail());
        dto.setDateNaissance(entity.getDateNaissance());
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setSexe(entity.getSexe() != null ? entity.getSexe().name() : null);
        dto.setAssurance(entity.getAssurance() != null ? entity.getAssurance().name() : null);
        return dto;
    }

    private Patient toEntity(PatientDTO dto) {
        if (dto == null) {
            return null;
        }
        Patient entity = new Patient();
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setAdresse(dto.getAdresse());
        entity.setTelephone(dto.getTelephone());
        entity.setEmail(dto.getEmail());
        entity.setDateNaissance(dto.getDateNaissance());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setDateMiseAJour(dto.getDateMiseAJour());
        entity.setSexe(dto.getSexe() != null ? Sexe.valueOf(dto.getSexe()) : null);
        entity.setAssurance(dto.getAssurance() != null ? Assurance.valueOf(dto.getAssurance()) : null);
        return entity;
    }

    @Override
    public List<PatientDTO> getAllPatients() {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            List<Patient> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public PatientDTO getPatientById(Long id) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            Patient entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public PatientDTO createPatient(PatientDTO dto, UserPrincipal principal) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);

            Patient entity = toEntity(dto);

            repo.create(entity);
            dto.setId(entity.getId());

            // Create notification
            if (principal != null) {
                NotificationService notificationService = ApplicationContext.getInstance().getBean(NotificationService.class);
                String notificationMessage = "Le patient '" + dto.getNom() + " " + dto.getPrenom() + "' a été créé.";
                notificationService.createNotificationForUser(principal.id(), TitreNotification.INFO, notificationMessage, principal.fullName());
            }

            return dto;
        });
    }

    @Override
    public PatientDTO updatePatient(Long id, PatientDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);

            Patient existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Patient avec l'ID " + id + " n'existe pas.");
            }

            Patient entity = toEntity(dto);
            entity.setId(id);

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deletePatient(Long id, UserPrincipal principal) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            Patient entity = repo.findById(id);

            if (entity == null) {
                throw new ValidationException("Patient avec l'ID " + id + " n'existe pas.");
            }

            repo.deleteById(id);

            // Create notification
            if (principal != null) {
                NotificationService notificationService = ApplicationContext.getInstance().getBean(NotificationService.class);
                String notificationMessage = "Le patient '" + entity.getNom() + " " + entity.getPrenom() + "' a été supprimé.";
                notificationService.createNotificationForUser(principal.id(), TitreNotification.INFO, notificationMessage, principal.fullName());
            }

            return null;
        });
    }


    @Override
    public PatientDTO getPatientByEmail(String email) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            Optional<Patient> optionalEntity = repo.findByEmail(email);
            return optionalEntity.map(this::toDTO).orElse(null);
        });
    }

    @Override
    public PatientDTO getPatientByTelephone(String telephone) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            Optional<Patient> optionalEntity = repo.findByTelephone(telephone);
            return optionalEntity.map(this::toDTO).orElse(null);
        });
    }

    @Override
    public List<PatientDTO> searchPatientsByNomPrenom(String keyword) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            List<Patient> entities = repo.searchByNomPrenom(keyword);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean existsPatientById(Long id) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.existsById(id);
        });
    }

    @Override
    public long countPatients() {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.count();
        });
    }

    @Override
    public List<PatientDTO> getPatientsPage(int limit, int offset) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            List<Patient> entities = repo.findPage(limit, offset);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void addAntecedentToPatient(Long patientId, Long antecedentId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            AntecedentRepository antecedentRepo = antecedentRepoFactory.create(cnx);

            if (patientRepo.findById(patientId) == null) {
                throw new ValidationException("Patient avec l'ID " + patientId + " n'existe pas.");
            }

            if (antecedentRepo.findById(antecedentId) == null) {
                throw new ValidationException("Antecedent avec l'ID " + antecedentId + " n'existe pas.");
            }

            patientRepo.addAntecedentToPatient(patientId, antecedentId);
            return null;
        });
    }

    @Override
    public void removeAntecedentFromPatient(Long patientId, Long antecedentId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            repo.removeAntecedentFromPatient(patientId, antecedentId);
            return null;
        });
    }

    @Override
    public void removeAllAntecedentsFromPatient(Long patientId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            repo.removeAllAntecedentsFromPatient(patientId);
            return null;
        });
    }

    @Override
    public List<Antecedent> getAntecedentsOfPatient(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.getAntecedentsOfPatient(patientId);
        });
    }

    @Override
    public List<PatientDTO> getPatientsByAntecedent(Long antecedentId) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            List<Patient> entities = repo.getPatientsByAntecedent(antecedentId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public long countPatientsByCabinetId(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.countByCabinetId(cabinetId);
        });
    }

    @Override
    public int countNouveauxPatientsDuMois(Long cabinetId, int annee, int mois) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.countNouveauxPatientsDuMois(cabinetId, annee, mois);
        });
    }

    @Override
    public List<Object[]> getRepartitionPatientsParTrancheAge(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.getRepartitionPatientsParTrancheAge(cabinetId);
        });
    }

    @Override
    public long countPatientsByCabinet(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.countPatientsByCabinet(cabinetId);
        });
    }

    @Override
    public long countPatientsByCabinetAndMonth(Long cabinetId, int year, int month) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository repo = patientRepoFactory.create(cnx);
            return repo.countPatientsByCabinetAndMonth(cabinetId, year, month);
        });
    }
}