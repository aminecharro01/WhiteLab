package ma.WhiteLab.mvc.controllers.otherModules.impl;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.controllers.otherModules.api.UsersController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.UsersPanel;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;
import ma.WhiteLab.service.modules.users.api.UserManagementService;

public class UsersControllerImpl implements UsersController {

    private final UserManagementService userService;
    private final RoleManagementService roleService;
    private UsersPanel cached;

    public UsersControllerImpl(UserManagementService userService, RoleManagementService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cached == null) {
            cached = new UsersPanel(principal, userService, roleService);
        }
        return cached;
    }
}
