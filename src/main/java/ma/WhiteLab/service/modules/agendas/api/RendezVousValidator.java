package ma.WhiteLab.service.modules.agendas.api;

import ma.WhiteLab.service.modules.agendas.dto.RendezVousDTO;

import java.util.Map;

public interface RendezVousValidator {

    Map<String, String> validate(RendezVousDTO dto);
}