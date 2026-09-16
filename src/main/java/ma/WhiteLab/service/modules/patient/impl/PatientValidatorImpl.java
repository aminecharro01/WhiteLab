package ma.WhiteLab.service.modules.patient.impl;

import ma.WhiteLab.service.modules.patient.api.PatientValidator;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;

import java.util.HashMap;
import java.util.Map;

public class PatientValidatorImpl implements PatientValidator {

    @Override
    public Map<String, String> validate(PatientDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "PatientDTO cannot be null");
            return errors;
        }

        if (dto.getNom() == null || dto.getNom().isEmpty()) {
            errors.put("nom", "Nom is required");
        }

        if (dto.getPrenom() == null || dto.getPrenom().isEmpty()) {
            errors.put("prenom", "Prenom is required");
        }

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            errors.put("email", "Email is required");
        }

        if (dto.getTelephone() == null || dto.getTelephone().isEmpty()) {
            errors.put("telephone", "Telephone is required");
        }

        if (dto.getSexe() == null || dto.getSexe().isEmpty()) {
            errors.put("sexe", "Sexe is required");
        }

        if (dto.getAssurance() == null || dto.getAssurance().isEmpty()) {
            errors.put("assurance", "Assurance is required");
        }

        // Add more validation rules as needed, e.g., email format

        return errors;
    }
}