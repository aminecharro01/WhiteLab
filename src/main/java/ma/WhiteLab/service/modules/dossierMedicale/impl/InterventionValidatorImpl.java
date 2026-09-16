package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionValidator;
import ma.WhiteLab.service.modules.consultation.dto.InterventionDTO;

import java.util.HashMap;
import java.util.Map;

public class InterventionValidatorImpl implements InterventionValidator {

    @Override
    public Map<String, String> validate(InterventionDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "InterventionDTO cannot be null");
            return errors;
        }

        if (dto.getPrixDePatient() <= 0) {
            errors.put("prixDePatient", "Prix de patient must be positive");
        }

        if (dto.getNumDent() < 0) {
            errors.put("numDent", "Num dent must be non-negative");
        }

        if (dto.getConsultationId() == null) {
            errors.put("consultationId", "Consultation ID is required");
        }

        if (dto.getActeMedicalId() == null) {
            errors.put("acteMedicalId", "Acte Medical ID is required");
        }

        // Add more validation rules as needed

        return errors;
    }
}