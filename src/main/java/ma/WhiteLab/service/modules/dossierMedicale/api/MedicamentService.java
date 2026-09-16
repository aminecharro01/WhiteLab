package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.service.modules.dossierMedicale.dto.MedicamentDashboardDTO;
import ma.WhiteLab.service.modules.dossierMedicale.dto.MedicamentPatientDTO;

import java.util.List;

public interface MedicamentService {

    /* ================= CRUD ================= */
    List<Medicament> getAll();
    Medicament getById(Long id);
    void create(Medicament m);
    void update(Medicament m);
    void delete(Long id);

    /* ================= Recherches avancées ================= */
    List<Medicament> searchByName(String nom);
    List<Medicament> searchByNameLike(String text);
    List<Medicament> findByLabo(String labo);
    List<Medicament> findRemboursables();
    List<Medicament> findByForme(Forme forme);
    List<Medicament> findByPrixBetween(double min, double max);

    /* ================= Méthodes métiers supplémentaires ================= */
    List<Medicament> findCheap(double maxPrice);
    List<Medicament> findExpensive(double minPrice);

    /* ================= Statistiques et Dashboard ================= */
    long countTotal();
    long countRemboursables();
    long countNonRemboursables();
    double getPrixMoyen();
    Medicament getMedicamentLePlusCher();
    Medicament getMedicamentLeMoinsCher();
    MedicamentDashboardDTO getDashboardStats();

    /* ================= Méthodes liées aux antécédents du patient ================= */
    MedicamentPatientDTO getMedicamentsPourPatient(Long patientId);
    List<Medicament> getMedicamentsCompatibles(List<Antecedent> antecedents);
    List<Medicament> getMedicamentsContreIndiques(List<Antecedent> antecedents);
}