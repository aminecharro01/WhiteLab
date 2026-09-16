package ma.WhiteLab.service.modules.agendas.impl;

import ma.WhiteLab.service.modules.agendas.api.RendezVousValidator;
import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;

import java.util.HashMap;
import java.util.Map;

public class RendezVousValidatorImpl implements RendezVousValidator {

    @Override
    public Map<String, String> validate(RendezVousDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "RendezVousDTO cannot be null");
            return errors;
        }

        if (dto.getDate() == null) {
            errors.put("date", "Date is required");
        }

        if (dto.getStatus() == null || dto.getStatus().isEmpty()) {
            errors.put("status", "Status is required");
        }

        if (dto.getDossierMedId() == null) {
            errors.put("dossierMedId", "Dossier Medical ID is required");
        }

        // Add more validation rules as needed

        return errors;
    }
}