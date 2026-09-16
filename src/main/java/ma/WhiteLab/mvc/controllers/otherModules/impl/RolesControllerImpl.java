package ma.WhiteLab.mvc.controllers.otherModules.impl;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.controllers.otherModules.api.RolesController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.RolesPanel;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;

public class RolesControllerImpl implements RolesController {

    private final RoleManagementService roleService;
    private RolesPanel cached;

    public RolesControllerImpl(RoleManagementService roleService) {
        this.roleService = roleService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cached == null) {
            cached = new RolesPanel(principal, roleService);
        }
        return cached;
    }
}
