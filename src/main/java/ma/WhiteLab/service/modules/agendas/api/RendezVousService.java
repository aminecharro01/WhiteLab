package ma.WhiteLab.service.modules.agendas.api;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RendezVousService {

    /* ================= CRUD ================= */

    List<RendezVousDTO> getAllRendezVous();

    RendezVousDTO getRendezVousById(Long id);

    RendezVousDTO createRendezVous(RendezVousDTO dto) throws ValidationException;

    RendezVousDTO updateRendezVous(Long id, RendezVousDTO dto) throws ValidationException;

    void deleteRendezVous(Long id) throws ValidationException;

    /* ================= Recherche ================= */

    List<RendezVousDTO> getRendezVousByDossierMedId(Long dossierMedId);

    List<RendezVousDTO> getRendezVousByConsultationId(Long consultationId);

    List<RendezVousDTO> getRendezVousByStatus(String status);

    /* ================= Statistiques ================= */

    long countRendezVousByCabinet(Long cabinetId);

    long countRendezVousByCabinetAndMonth(Long cabinetId, int year, int month);

    long countRendezVousByDateAndCabinet(LocalDate date, Long cabinetId);

    long countRdvBetweenDatesAndCabinet(
            LocalDate debut,
            LocalDate fin,
            Long cabinetId
    );

    double calculateTauxAnnulationCeMois(Long cabinetId);

    long countCreneauxDisponiblesSemaine(Long cabinetId);

    List<LocalDate> getDaysWithAppointments(Long medecinId, LocalDate start, LocalDate end);

    Map<LocalDate, Long> getAppointmentCountPerDay(Long medecinId, LocalDate start, LocalDate end);

    boolean existsAppointmentAt(Long medecinId, LocalDateTime dateTime);

    boolean isAvailableForAppointment(Long medecinId, LocalDateTime proposedDateTime, int maxSlotsPerDay);

    List<RendezVous> getRendezVousByMedecinAndPeriod(Long restrictedMedecinId, LocalDate start, LocalDate end);
}
