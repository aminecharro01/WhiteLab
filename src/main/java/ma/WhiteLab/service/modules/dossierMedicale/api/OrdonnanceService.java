package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Ordonnance;

import java.time.LocalDate;
import java.util.List;

public interface OrdonnanceService {

    /* ================= CRUD ================= */
    List<Ordonnance> getAll();
    Ordonnance getById(Long id);
    void create(Ordonnance o);
    void update(Ordonnance o);
    void delete(Long id);
    Integer count();

    /* ================= Méthodes spécifiques ================= */
    List<Ordonnance> findByDossierId(Long dossierId);
    List<Ordonnance> findByConsultationId(Long consultationId);
    List<Ordonnance> findByDate(LocalDate date);
    List<Ordonnance> findBetweenDates(LocalDate start, LocalDate end);
    List<Ordonnance> findByMedecinId(Long medecinId);
    List<Ordonnance> findByPatientId(Long patientId);
    List<Ordonnance> findRecent(int days);

    /* ================= Téléchargement / Export ================= */
    byte[] downloadOrdonnance(Long ordonnanceId);
}