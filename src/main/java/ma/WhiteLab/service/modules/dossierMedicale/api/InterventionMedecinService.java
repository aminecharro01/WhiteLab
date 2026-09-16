package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;

import java.util.List;

public interface InterventionMedecinService {

    /* ================= CRUD ================= */

    List<InterventionDTO> getAllInterventions();

    InterventionDTO getInterventionById(Long id);

    InterventionDTO createIntervention(InterventionDTO dto) throws ValidationException;

    InterventionDTO updateIntervention(Long id, InterventionDTO dto) throws ValidationException;

    void deleteIntervention(Long id) throws ValidationException;

    /* ================= Recherche ================= */

    List<InterventionDTO> getInterventionsByNumDent(int numDent);

    List<InterventionDTO> getInterventionsByConsultationId(Long consultationId);

    List<InterventionDTO> getInterventionsByActeMedicalId(Long acteMedicalId);
}
