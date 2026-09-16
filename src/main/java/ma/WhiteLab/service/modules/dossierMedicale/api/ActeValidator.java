package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;

import java.util.Map;

public interface ActeValidator {

    Map<String, String> validate(ActeDTO dto);
}