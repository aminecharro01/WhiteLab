package ma.WhiteLab.mvc.controllers.modules.patient.api;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

public interface PatientsController {
    JPanel getView(UserPrincipal principal);
}


