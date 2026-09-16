package ma.WhiteLab.mvc.controllers.dashboardModule.api;


import javax.swing.*;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;

public interface DashboardController {

    void showDashboard(UserPrincipal principal);

    // callbacks venant de la vue
    JComponent onNavigateRequested(ApplicationPages page);

    void onLogoutRequested();
    void onExitRequested();

    void onMarkAsReadRequested(Long userId, Long notificationId);
}
