package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrdonnanceRepository extends CrudRepository<Ordonnance, Long> {

    // Trouver toutes les ordonnances d'un dossier médical
    List<Ordonnance> findByDossierId(Long dossierId);

    // Trouver toutes les ordonnances d’une consultation
    List<Ordonnance> findByConsultationId(Long consultationId);

    // Rechercher par date
    List<Ordonnance> findByDate(LocalDate date);

    // Rechercher entre deux dates
    List<Ordonnance> findBetweenDates(LocalDate start, LocalDate end);
}
