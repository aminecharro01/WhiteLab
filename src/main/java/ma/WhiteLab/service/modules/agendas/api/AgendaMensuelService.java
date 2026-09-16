package ma.WhiteLab.service.modules.agendas.api;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;

import java.util.List;

public interface AgendaMensuelService {

    /* ================= CRUD ================= */

    List<AgendaMensuelDTO> getAllAgendasMensuels();

    AgendaMensuelDTO getAgendaMensuelById(Long id);

    AgendaMensuelDTO createAgendaMensuel(AgendaMensuelDTO dto) throws ValidationException;

    AgendaMensuelDTO updateAgendaMensuel(Long id, AgendaMensuelDTO dto) throws ValidationException;

    void deleteAgendaMensuel(Long id) throws ValidationException;

    /* ================= Recherche ================= */

    List<AgendaMensuelDTO> getAgendasMensuelsByMedecinId(Long medecinId);

    List<AgendaMensuelDTO> getAgendasMensuelsByMedecinIdAndMois(Long medecinId, String mois);

    List<UserPrincipal> getAllMedecins();
}
