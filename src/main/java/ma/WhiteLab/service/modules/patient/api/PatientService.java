package ma.WhiteLab.service.modules.patient.api;

import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.util.List;

public interface PatientService {

    List<PatientDTO> getAllPatients();

    PatientDTO getPatientById(Long id);

    PatientDTO createPatient(PatientDTO dto, UserPrincipal principal) throws ValidationException;

    PatientDTO updatePatient(Long id, PatientDTO dto) throws ValidationException;


    void deletePatient(Long id, UserPrincipal principal) throws ValidationException;

    PatientDTO getPatientByEmail(String email);

    PatientDTO getPatientByTelephone(String telephone);

    List<PatientDTO> searchPatientsByNomPrenom(String keyword);

    boolean existsPatientById(Long id);

    long countPatients();

    List<PatientDTO> getPatientsPage(int limit, int offset);

    void addAntecedentToPatient(Long patientId, Long antecedentId) throws ValidationException;

    void removeAntecedentFromPatient(Long patientId, Long antecedentId) throws ValidationException;

    void removeAllAntecedentsFromPatient(Long patientId) throws ValidationException;

    List<Antecedent> getAntecedentsOfPatient(Long patientId);

    List<PatientDTO> getPatientsByAntecedent(Long antecedentId);

    long countPatientsByCabinetId(Long cabinetId);

    int countNouveauxPatientsDuMois(Long cabinetId, int annee, int mois);

    List<Object[]> getRepartitionPatientsParTrancheAge(Long cabinetId);

    long countPatientsByCabinet(Long cabinetId);

    long countPatientsByCabinetAndMonth(Long cabinetId, int year, int month);
}