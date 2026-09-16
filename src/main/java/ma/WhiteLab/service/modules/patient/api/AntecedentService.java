package ma.WhiteLab.service.modules.patient.api;

import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.util.List;

public interface AntecedentService {

    List<AntecedentDTO> getAllAntecedents();

    AntecedentDTO getAntecedentById(Long id);

    AntecedentDTO createAntecedent(AntecedentDTO dto) throws ValidationException;

    AntecedentDTO updateAntecedent(Long id, AntecedentDTO dto) throws ValidationException;

    void deleteAntecedent(Long id) throws ValidationException;

    AntecedentDTO getAntecedentByNom(String nom);

    List<AntecedentDTO> getAntecedentsByCategorie(String categorie);

    List<AntecedentDTO> getAntecedentsByNiveauRisque(String niveau);

    boolean existsAntecedentById(Long id);

    long countAntecedents();

    List<AntecedentDTO> getAntecedentsPage(int limit, int offset);

    List<Patient> getPatientsHavingAntecedent(Long antecedentId);

    List<AntecedentDTO> getAntecedentsByPatientId(Long patientId);
}