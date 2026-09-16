package ma.WhiteLab.repository.modules.agenda.api;

import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousRepository extends CrudRepository<RendezVous, Long> {

    List<RendezVous> findByDossierMedId(Long dossierMedId);
    List<RendezVous> findByConsultationId(Long consultationId);
    List<RendezVous> findByStatus(String status);

    long countByCabinet(Long cabinetId);
    long countByCabinetAndMonth(Long cabinetId, int year, int month);
    long countByDateAndCabinet(LocalDate localDate, Long cabinetId);
    double calculateTauxAnnulationCeMois(Long cabinetId);
    long countRdvBetweenDatesAndCabinet(LocalDate lundi, LocalDate dimanche, Long cabinetId);
    long countCreneauxDisponiblesSemaine(Long cabinetId);

    List<LocalDate> findDaysWithAppointments(Long medecinId, LocalDate start, LocalDate end);

    List<Object[]> countAppointmentsPerDay(Long medecinId, LocalDate start, LocalDate end);

    Boolean existsByMedecinIdAndDateTime(Long medecinId, LocalDateTime dateTime);

    long countByMedecinIdAndDate(Long medecinId, LocalDate date);

    List<RendezVous> getRendezVousByMedecinAndPeriod(Long restrictedMedecinId, LocalDate start, LocalDate end);

    List<RendezVous> findByDossierIdsAndDateBetween(List<Long> dossierIds, LocalDateTime start, LocalDateTime end);

}