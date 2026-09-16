package ma.WhiteLab.service.modules.agendas.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.api.RendezVousValidator;
import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des rendez-vous
 */
public class RendezVousServiceImpl implements RendezVousService {

    private final RepoFactory<RendezVousRepository> rdvRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RendezVousValidator validator = new RendezVousValidatorImpl();

    public RendezVousServiceImpl(
            RepoFactory<RendezVousRepository> rdvRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory) {

        this.rdvRepoFactory = rdvRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
    }

    // =============================================================================
    //                             Conversions
    // =============================================================================

    private RendezVousDTO toDTO(RendezVous entity) {
        if (entity == null) return null;

        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setTime(entity.getTime());
        dto.setMotif(entity.getMotif());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setNoteMedecin(entity.getNoteMedecin());
        dto.setDossierMedId(entity.getDossierMed() != null ? entity.getDossierMed().getId() : null);
        dto.setConsultationId(entity.getConsultation() != null ? entity.getConsultation().getId() : null);
        return dto;
    }

    private RendezVous toEntity(RendezVousDTO dto) {
        if (dto == null) return null;

        RendezVous entity = new RendezVous();
        entity.setDate(dto.getDate());
        entity.setTime(dto.getTime());
        entity.setMotif(dto.getMotif());
        entity.setStatus(dto.getStatus() != null ? Status.valueOf(dto.getStatus()) : null);
        entity.setNoteMedecin(dto.getNoteMedecin());
        return entity;
    }

    // =============================================================================
    //                             CRUD Operations
    // =============================================================================

    @Override
    public List<RendezVousDTO> getAllRendezVous() {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            return repo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public RendezVousDTO getRendezVousById(Long id) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            RendezVous entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public RendezVousDTO createRendezVous(RendezVousDTO dto) throws ValidationException {
        validate(dto);

        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            ConsultationRepository consultRepo = consultationRepoFactory.create(cnx);

            RendezVous entity = toEntity(dto);

            if (dto.getDossierMedId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedId());
                if (dossier == null) {
                    throw new IllegalArgumentException("Dossier médical ID " + dto.getDossierMedId() + " introuvable");
                }
                entity.setDossierMed(dossier);
            }

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultRepo.findById(dto.getConsultationId());
                if (consultation == null) {
                    throw new IllegalArgumentException("Consultation ID " + dto.getConsultationId() + " introuvable");
                }
                entity.setConsultation(consultation);
            }

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public RendezVousDTO updateRendezVous(Long id, RendezVousDTO dto) throws ValidationException {
        validate(dto);

        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            ConsultationRepository consultRepo = consultationRepoFactory.create(cnx);

            RendezVous existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Rendez-vous ID " + id + " introuvable");
            }

            RendezVous entity = toEntity(dto);
            entity.setId(id);

            if (dto.getDossierMedId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedId());
                if (dossier == null) throw new IllegalArgumentException("Dossier médical introuvable");
                entity.setDossierMed(dossier);
            } else {
                entity.setDossierMed(null);
            }

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultRepo.findById(dto.getConsultationId());
                if (consultation == null) throw new IllegalArgumentException("Consultation introuvable");
                entity.setConsultation(consultation);
            } else {
                entity.setConsultation(null);
            }

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteRendezVous(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            if (repo.findById(id) == null) {
                throw new ValidationException("Rendez-vous ID " + id + " introuvable");
            }
            repo.deleteById(id);
            return null;
        });
    }

    // =============================================================================
    //                             Search / Filter
    // =============================================================================

    @Override
    public List<RendezVousDTO> getRendezVousByDossierMedId(Long dossierMedId) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            return repo.findByDossierMedId(dossierMedId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<RendezVousDTO> getRendezVousByConsultationId(Long consultationId) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            return repo.findByConsultationId(consultationId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<RendezVousDTO> getRendezVousByStatus(String status) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            return repo.findByStatus(status).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    // =============================================================================
    //                          Statistics / Dashboard
    // =============================================================================

    @Override
    public long countRendezVousByCabinet(Long cabinetId) {
        return executeCount(c -> c.countByCabinet(cabinetId));
    }

    @Override
    public long countRendezVousByCabinetAndMonth(Long cabinetId, int year, int month) {
        return executeCount(c -> c.countByCabinetAndMonth(cabinetId, year, month));
    }

    @Override
    public long countRendezVousByDateAndCabinet(LocalDate date, Long cabinetId) {
        return executeCount(c -> c.countByDateAndCabinet(date, cabinetId));
    }

    @Override
    public long countRdvBetweenDatesAndCabinet(LocalDate debut, LocalDate fin, Long cabinetId) {
        return executeCount(c -> c.countRdvBetweenDatesAndCabinet(debut, fin, cabinetId));
    }

    @Override
    public double calculateTauxAnnulationCeMois(Long cabinetId) {
        return Transaction.initTransaction(cnx ->
                rdvRepoFactory.create(cnx).calculateTauxAnnulationCeMois(cabinetId));
    }

    @Override
    public long countCreneauxDisponiblesSemaine(Long cabinetId) {
        return executeCount(c -> c.countCreneauxDisponiblesSemaine(cabinetId));
    }

    // =============================================================================
    //                    AGENDA & AVAILABILITY (new methods)
    // =============================================================================

    @Override
    public List<LocalDate> getDaysWithAppointments(Long medecinId, LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx ->
                rdvRepoFactory.create(cnx).findDaysWithAppointments(medecinId, start, end));
    }

    @Override
    public Map<LocalDate, Long> getAppointmentCountPerDay(Long medecinId, LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            List<Object[]> results = repo.countAppointmentsPerDay(medecinId, start, end);

            return results.stream()
                    .collect(Collectors.toMap(
                            row -> (LocalDate) row[0],
                            row -> (Long) row[1],
                            (a, b) -> a, // merge function (shouldn't be needed)
                            LinkedHashMap::new
                    ));
        });
    }

    @Override
    public boolean existsAppointmentAt(Long medecinId, LocalDateTime dateTime) {
        return Transaction.initTransaction(cnx ->
                rdvRepoFactory.create(cnx).existsByMedecinIdAndDateTime(medecinId, dateTime));
    }

    @Override
    public boolean isAvailableForAppointment(Long medecinId, LocalDateTime proposedDateTime, int maxSlotsPerDay) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);

            // 1. Créneau exact déjà pris ?
            if (repo.existsByMedecinIdAndDateTime(medecinId, proposedDateTime)) {
                return false;
            }

            // 2. Limite quotidienne dépassée ?
            if (maxSlotsPerDay > 0) {
                LocalDate date = proposedDateTime.toLocalDate();
                long countThatDay = repo.countByMedecinIdAndDate(medecinId, date);
                if (countThatDay >= maxSlotsPerDay) {
                    return false;
                }
            }

            // 3. Vérification des jours non disponibles (AgendaMensuel) ?
            // → À ajouter plus tard quand vous injecterez AgendaMensuelService/Repository

            return true;
        });
    }

    @Override
    public List<RendezVous> getRendezVousByMedecinAndPeriod(Long medecinId, LocalDate start, LocalDate end) {
        // Validation rapide des paramètres
        if (medecinId == null || start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            // The repository now returns fully populated objects with patient data
            return repo.getRendezVousByMedecinAndPeriod(medecinId, start, end);
        });
    }

    // =============================================================================
    //                             Helpers
    // =============================================================================

    private void validate(RendezVousDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }
    }

    private long executeCount(java.util.function.Function<RendezVousRepository, Long> countOperation) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository repo = rdvRepoFactory.create(cnx);
            return countOperation.apply(repo);
        });
    }
}