package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;

import java.util.Map;

public interface InterventionValidator {

    Map<String, String> validate(InterventionDTO dto);
}