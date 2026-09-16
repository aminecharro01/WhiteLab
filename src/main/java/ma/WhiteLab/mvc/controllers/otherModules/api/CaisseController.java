package ma.WhiteLab.mvc.controllers.otherModules.api;

import javax.swing.*;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;


public interface CaisseController {
    JPanel getView(UserPrincipal principal);
}
