package ma.WhiteLab.repository.modules.patient.api;

import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.repository.common.CrudRepository;
import ma.WhiteLab.entities.patient.Antecedent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
//auteur : Aymane Akarbich

public interface PatientRepository extends CrudRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByTelephone(String telephone);
    List<Patient> searchByNomPrenom(String keyword); // LIKE %keyword%
    boolean existsById(Long id);
    long count();
    List<Patient> findPage(int limit, int offset);

    // ---- Liaison Many-to-Many ----
    void addAntecedentToPatient(Long patientId, Long antecedentId);
    void removeAntecedentFromPatient(Long patientId, Long antecedentId);
    void removeAllAntecedentsFromPatient(Long patientId);
    List<Antecedent> getAntecedentsOfPatient(Long patientId);
    List<Patient> getPatientsByAntecedent(Long antecedentId);

    long countPatientsByCabinet(Long cabinetId);
    long countPatientsByCabinetAndMonth(Long cabinetId, int year, int month);

    long countByCabinetId(Long cabinetId);

    int countNouveauxPatientsDuMois(Long cabinetId, int year, int monthValue);

    List<Object[]> getRepartitionPatientsParTrancheAge(Long cabinetId);
}
