package ma.WhiteLab.service.modules.consultation.api;

import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;

import java.util.Map;

public interface ConsultationValidator {

    Map<String, String> validate(ConsultationDTO dto);
}