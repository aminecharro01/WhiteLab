package ma.WhiteLab.mvc.controllers.otherModules.api;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

public interface UsersController {
    JPanel getView(UserPrincipal principal);
}

