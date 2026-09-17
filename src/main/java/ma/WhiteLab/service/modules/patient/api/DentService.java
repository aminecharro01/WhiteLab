package ma.WhiteLab.service.modules.patient.api;

import ma.WhiteLab.entities.enums.EtatDent;
import ma.WhiteLab.service.modules.patient.dto.DentDTO;

import java.util.List;

public interface DentService {

    /** Returns all 32 teeth for a patient, synthesizing SAIN entries for any not yet recorded. */
    List<DentDTO> getOdontogramme(Long patientId);

    void updateEtat(Long patientId, int numero, EtatDent etat, String note, String modifiePar);
}
