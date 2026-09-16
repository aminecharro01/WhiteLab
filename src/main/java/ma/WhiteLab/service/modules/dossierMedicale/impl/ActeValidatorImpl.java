package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.service.modules.dossierMedicale.api.ActeValidator;
import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;

import java.util.HashMap;
import java.util.Map;

public class ActeValidatorImpl implements ActeValidator {

    @Override
    public Map<String, String> validate(ActeDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "ActeDTO cannot be null");
            return errors;
        }

        if (dto.getLibelle() == null || dto.getLibelle().isEmpty()) {
            errors.put("libelle", "Libelle is required");
        }

        if (dto.getCategorie() == null || dto.getCategorie().isEmpty()) {
            errors.put("categorie", "Categorie is required");
        }

        if (dto.getPrixDeBase() <= 0) {
            errors.put("prixDeBase", "Prix de base must be positive");
        }

        // Add more validation rules as needed

        return errors;
    }
}