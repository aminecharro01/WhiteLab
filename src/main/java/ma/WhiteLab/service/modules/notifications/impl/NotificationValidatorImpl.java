package ma.WhiteLab.service.modules.notifications.impl;

import ma.WhiteLab.service.modules.notifications.api.NotificationValidator;
import ma.WhiteLab.service.modules.notifications.dto.NotificationDTO;

import java.util.HashMap;
import java.util.Map;

public class NotificationValidatorImpl implements NotificationValidator {

    @Override
    public Map<String, String> validate(NotificationDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("dto", "NotificationDTO cannot be null");
            return errors;
        }

        if (dto.getTitre() == null || dto.getTitre().isEmpty()) {
            errors.put("titre", "Titre is required");
        }

        if (dto.getMessage() == null || dto.getMessage().isEmpty()) {
            errors.put("message", "Message is required");
        }

        if (dto.getType() == null || dto.getType().isEmpty()) {
            errors.put("type", "Type is required");
        }

        if (dto.getPriorite() == null || dto.getPriorite().isEmpty()) {
            errors.put("priorite", "Priorite is required");
        }

        // Add more validation rules as needed, e.g., for date, time

        return errors;
    }
}