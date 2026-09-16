package ma.WhiteLab.service.modules.agendas.impl;

import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelValidator;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;

import java.util.HashMap;
import java.util.Map;

public class AgendaMensuelValidatorImpl implements AgendaMensuelValidator {

    @Override
    public Map<String, String> validate(AgendaMensuelDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "AgendaMensuelDTO cannot be null");
            return errors;
        }

        if (dto.getMois() == null || dto.getMois().isEmpty()) {
            errors.put("mois", "Mois is required");
        }

        if (dto.getMedecinId() == null) {
            errors.put("medecinId", "Medecin ID is required");
        }

        // Add more validation rules as needed

        return errors;
    }
}