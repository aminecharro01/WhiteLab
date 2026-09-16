package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Prescription;

import java.util.List;

public interface PrescriptionService {

    /* ================= CRUD ================= */
    List<Prescription> getAll();
    Prescription getById(Long id);
    void create(Prescription p);
    void update(Prescription p);
    void delete(Long id);
    Integer count();

    /* ================= Méthodes spécifiques ================= */
    List<Prescription> findByOrdonnanceId(Long ordonnanceId);
    List<Prescription> findByMedicamentId(Long medicamentId);
    List<Prescription> findByFrequence(String frequence);
    boolean exists(Long ordonnanceId, Long medicamentId);

    /* ================= Méthodes métiers ================= */
    List<Prescription> findByOrdonnanceIds(List<Long> ordonnanceIds);
    List<Prescription> findByMedicamentIds(List<Long> medicamentIds);
    int totalQuantitePrescriteForMedicament(Long medicamentId);
    double totalPrixPrescritForMedicament(Long medicamentId);
    List<Prescription> findRecent(int days);
}