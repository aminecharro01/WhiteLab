package ma.WhiteLab.service.modules.agendas.api;

import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;

import java.util.Map;

public interface AgendaMensuelValidator {

    Map<String, String> validate(AgendaMensuelDTO dto);
}