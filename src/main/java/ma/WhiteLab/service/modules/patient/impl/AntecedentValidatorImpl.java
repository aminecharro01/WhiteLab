package ma.WhiteLab.service.modules.patient.impl;

import ma.WhiteLab.service.modules.patient.api.AntecedentValidator;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;

import java.util.HashMap;
import java.util.Map;

public class AntecedentValidatorImpl implements AntecedentValidator {

    @Override
    public Map<String, String> validate(AntecedentDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "AntecedentDTO cannot be null");
            return errors;
        }

        if (dto.getNom() == null || dto.getNom().isEmpty()) {
            errors.put("nom", "Nom is required");
        }

        if (dto.getCategorie() == null || dto.getCategorie().isEmpty()) {
            errors.put("categorie", "Categorie is required");
        }

        if (dto.getNiveauRisque() == null || dto.getNiveauRisque().isEmpty()) {
            errors.put("niveauRisque", "Niveau de risque is required");
        }

        // Add more validation rules as needed

        return errors;
    }
}