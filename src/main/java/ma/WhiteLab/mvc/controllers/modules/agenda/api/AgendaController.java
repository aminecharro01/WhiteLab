package ma.WhiteLab.mvc.controllers.modules.agenda.api;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;

import javax.swing.*;
import java.util.List;

public interface AgendaController {
    JPanel getView(UserPrincipal principal);

    List<AgendaMensuelDTO> getVisibleAgendas(UserPrincipal principal);

    void refreshCurrentView();
}
