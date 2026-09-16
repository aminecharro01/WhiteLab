package ma.WhiteLab.mvc.controllers.dashboardModule.impl;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.dashboardModule.api.DashboardController;
import ma.WhiteLab.mvc.controllers.modules.agenda.api.AgendaController;
import ma.WhiteLab.mvc.controllers.modules.auth.api.AuthController;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.controllers.modules.notifications.api.NotificationsController;
import ma.WhiteLab.mvc.controllers.otherModules.api.*;
import ma.WhiteLab.mvc.controllers.profileModule.api.ProfileController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.frames.DashboardUI;
import ma.WhiteLab.mvc.ui.pages.dashboardPages.pageFactory.DashboardPanelFactory;
import ma.WhiteLab.mvc.ui.pages.otherPages.CabinetPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.NotificationsPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.StatisticsPanel;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.service.modules.auth.api.AuthorizationService;
import ma.WhiteLab.mvc.controllers.modules.patient.api.PatientsController;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;

import javax.swing.*;
import java.awt.*;

import static ma.WhiteLab.security.Privileges.*;

public class DashboardControllerImpl implements DashboardController {

    private final AuthorizationService authorizationService;
    private final AuthController authController;

    private DashboardUI view;
    private UserPrincipal principal;

    public DashboardControllerImpl(AuthorizationService authorizationService,
                                   AuthController authController) {
        this.authorizationService = authorizationService;
        this.authController = authController;
    }

    @Override
    public void showDashboard(UserPrincipal principal) {
        if (principal == null) return;

        this.principal = principal;

        Runnable show = () -> {
            if (view != null) {
                view.dispose();
            }
            view = new DashboardUI(this, authorizationService, principal);
            view.setVisible(true);
            view.navigateTo(ApplicationPages.DASHBOARD);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeLater(show);
        }
    }

    @Override
    public JComponent onNavigateRequested(ApplicationPages page) {
        if (principal == null || page == null) {
            return emptyPanel("Page invalide.");
        }

        if (!canAccess(page)) {
            SwingUtilities.invokeLater(() ->
                    Alert.error(view, "Accès refusé : vous n'avez pas les privilèges nécessaires.")
            );
            return forbiddenPanel("Accès refusé");
        }

        return switch (page) {
            case DASHBOARD -> DashboardPanelFactory.create(principal);

            case STATISTICS -> new StatisticsPanel(principal);


            case ROLES ->
                    ApplicationContext.getInstance().getBean(RolesController.class).getView(principal);

            case PATIENTS ->
                    ApplicationContext.getInstance().getBean(PatientsController.class).getView(principal);

            case CAISSE ->
                    ApplicationContext.getInstance().getBean(CaisseController.class).getView(principal);

            case USERS ->
                    ApplicationContext.getInstance().getBean(UsersController.class).getView(principal);

            case CABINETS ->
                    ApplicationContext.getInstance().getBean(CabinetsController.class).getView(principal);

            case DOSSIERS_MEDICAUX ->
                    ApplicationContext.getInstance().getBean(DossiersController.class).getView(principal);

            case PARAMETRAGE ->
                    ApplicationContext.getInstance().getBean(ParametrageController.class).getView(principal);

            case PROFILE ->
                    ApplicationContext.getInstance().getBean(ProfileController.class).getView(principal);

            case AGENDA ->
                    ApplicationContext.getInstance().getBean(AgendaController.class).getView(principal);  // ← CORRECTION ICI !

            case NOTIFICATIONS ->
                    ApplicationContext.getInstance().getBean(NotificationsController.class).getView(principal);


            case CABINET -> new CabinetPanel(principal);

            default -> emptyPanel("Page non implémentée.");
        };
    }

    private boolean canAccess(ApplicationPages page) {
        return switch (page) {
            case DASHBOARD, PROFILE, NOTIFICATIONS, CABINET -> true;

            case PATIENTS ->
                    authorizationService.hasPrivilege(principal, PATIENT_ACCESS) ||
                            authorizationService.hasPrivilege(principal, DOSSIER_ACCESS);
            case ROLES ->
                    authorizationService.hasPrivilege(principal, USERS_ACCESS); // ← tu peux ajuster selon tes besoins

            case AGENDA ->
                    authorizationService.hasPrivilege(principal, AGENDA_ACCESS); // ← tu peux ajuster selon tes besoins

            case CAISSE ->
                    authorizationService.hasPrivilege(principal, CAISSE_ACCESS);

            case USERS ->
                    authorizationService.hasPrivilege(principal, USERS_ACCESS);

            case CABINETS ->
                    authorizationService.hasPrivilege(principal, CABINET_ACCESS);

            case DOSSIERS_MEDICAUX ->
                    authorizationService.hasPrivilege(principal, DOSSIER_ACCESS);

            case PARAMETRAGE ->
                    authorizationService.hasPrivilege(principal, USERS_ACCESS);

            default -> false;
        };
    }

    @Override
    public void onLogoutRequested() {
        if (view == null) return;

        boolean confirm = Alert.confirm(view, "Confirmer la déconnexion ?");
        if (!confirm) return;

        view.dispose();
        view = null;
        principal = null;

        Runnable showLogin = () -> authController.showLoginView();

        if (SwingUtilities.isEventDispatchThread()) {
            showLogin.run();
        } else {
            SwingUtilities.invokeLater(showLogin);
        }
    }

    @Override
    public void onExitRequested() {
        if (view == null) return;

        boolean confirm = Alert.confirm(view, "Voulez-vous quitter l'application ?");
        if (confirm) {
            System.exit(0);
        }
    }

    @Override
    public void onMarkAsReadRequested(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) return;

        try {
            NotificationService notificationService = ApplicationContext.getInstance().getBean(NotificationService.class);
            notificationService.markAsRead(userId, notificationId);
            ma.WhiteLab.common.consoleLog.ConsoleLogger.info("Notification " + notificationId + " marquée comme lue.");
        } catch (Exception e) {
            ma.WhiteLab.common.consoleLog.ConsoleLogger.error("Erreur lors du marquage comme lu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== Helpers ========================

    private JComponent emptyPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }

    private JComponent forbiddenPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("<html><div style='text-align:center; color:red; font-size:16px;'>" +
                message + "<br><small>Contactez votre administrateur.</small></div></html>");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(label);
        return panel;
    }
}