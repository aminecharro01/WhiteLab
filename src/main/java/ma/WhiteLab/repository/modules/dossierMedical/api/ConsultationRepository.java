package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationRepository extends CrudRepository<Consultation, Long> {

    // Recherche par status
    List<Consultation> findByStatus(String status);

    // Eventuellement, recherche par dossier médical
    List<Consultation> findByDossierMedicalId(Long dossierId);
    long countByCabinet(Long cabinetId);
    long countByCabinetAndMonth(Long cabinetId, int year, int month);

    long countByDateAndCabinet(LocalDate aujourdHui, Long cabinetId);
}
