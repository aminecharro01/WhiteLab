package ma.WhiteLab.service.modules.consultation.impl;

import ma.WhiteLab.service.modules.consultation.api.ConsultationValidator;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;

import java.util.HashMap;
import java.util.Map;

public class ConsultationValidatorImpl implements ConsultationValidator {

    @Override
    public Map<String, String> validate(ConsultationDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "ConsultationDTO cannot be null");
            return errors;
        }

        if (dto.getDate() == null) {
            errors.put("date", "Date is required");
        }

        if (dto.getStatus() == null || dto.getStatus().isEmpty()) {
            errors.put("status", "Status is required");
        }

        if (dto.getDossierMedicalId() == null) {
            errors.put("dossierMedicalId", "Dossier Medical ID is required");
        }

        // Add more validation rules as needed, e.g., for notes, observations, etc.

        return errors;
    }
}