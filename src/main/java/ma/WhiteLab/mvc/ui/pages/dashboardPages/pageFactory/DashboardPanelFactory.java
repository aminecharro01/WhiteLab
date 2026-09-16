package ma.WhiteLab.mvc.ui.pages.dashboardPages.pageFactory;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;

import javax.swing.*;
import ma.WhiteLab.mvc.ui.pages.dashboardPages.AdminDashboardPanel;
import ma.WhiteLab.mvc.ui.pages.dashboardPages.DefaultDashboardPanel;
import ma.WhiteLab.mvc.ui.pages.dashboardPages.DoctorDashboardPanel;
import ma.WhiteLab.mvc.ui.pages.dashboardPages.SecretaryDashboardPanel;
import java.util.function.Consumer;

public final class DashboardPanelFactory {

    private DashboardPanelFactory(){}

    // Version avec navigator
    public static JComponent create(UserPrincipal principal, java.util.function.Consumer<ApplicationPages> navigator) {
        RoleR role = (principal != null) ? principal.rolePrincipal() : null;

        if (role == null) {
            return new DefaultDashboardPanel(principal);
        }

        return switch (role) {
            case ADMIN      -> new AdminDashboardPanel(principal, navigator);
            case MEDECIN    -> new DoctorDashboardPanel(principal, navigator);
            case SECRETAIRE -> new SecretaryDashboardPanel(principal, navigator);
            default         -> new DefaultDashboardPanel(principal);
        };
    }

    // Surcharge sans navigator pour compatibilité
    public static JComponent create(UserPrincipal principal) {
        return create(principal, page -> {
            // Par défaut, rien ne se passe si pas de navigator fourni
        });
    }
}
