package ma.WhiteLab.service.modules.patient.api;

import ma.WhiteLab.service.modules.patient.dto.PatientDTO;

import java.util.Map;

public interface PatientValidator {

    Map<String, String> validate(PatientDTO dto);
}