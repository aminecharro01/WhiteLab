package ma.WhiteLab.service.modules.patient.api;

import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;

import java.util.Map;

public interface AntecedentValidator {

    Map<String, String> validate(AntecedentDTO dto);
}