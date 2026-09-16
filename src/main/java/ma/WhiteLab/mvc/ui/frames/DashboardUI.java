package ma.WhiteLab.mvc.ui.frames;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.dashboardModule.api.DashboardController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;
import ma.WhiteLab.mvc.ui.palette.menu.MyMenuBar;
import ma.WhiteLab.mvc.ui.pages.commonPages.CenterPanel;
import ma.WhiteLab.mvc.ui.pages.commonPages.FooterPanel;
import ma.WhiteLab.mvc.ui.pages.commonPages.HeaderBannerPanel;
import ma.WhiteLab.mvc.ui.palette.utils.HeaderWindowControls;
import ma.WhiteLab.mvc.ui.palette.notification.NotificationLevel;
import ma.WhiteLab.mvc.ui.palette.sidebarBuilder.NavigationSpecs;
import ma.WhiteLab.mvc.ui.palette.sidebarBuilder.SidebarBuilder;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.auth.api.AuthorizationService;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;
import ma.WhiteLab.common.consoleLog.ConsoleLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class DashboardUI extends JFrame {

    private final DashboardController controller;
    private final AuthorizationService authorizationService;

    private UserPrincipal principal;

    // UI Components
    private HeaderBannerPanel headerBanner;
    private CenterPanel center;
    private FooterPanel footer;

    // Pour le drag de la fenêtre sans bordure
    private Point initialClick;

    public DashboardUI(DashboardController controller,
                       AuthorizationService authorizationService,
                       UserPrincipal principal) {

        this.controller = controller;
        this.authorizationService = authorizationService;
        this.principal = principal;

        initUI();
        loadInitialProfileData();
    }

    private void initUI() {
        setSize(1620, 1020);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        setJMenuBar(buildMenuBar());
        setContentPane(buildRootPanel());

        navigateTo(ApplicationPages.DASHBOARD);

        showNotificationsCount(5, NotificationLevel.INFO);

        pack();
        setVisible(true);
    }

    // ======================== API View ← Controller ========================

    public void refreshSession(UserPrincipal newPrincipal) {
        if (newPrincipal == null) return;
        this.principal = newPrincipal;
        if (headerBanner != null) {
            headerBanner.refresh(newPrincipal);
        }
    }

    public void refreshHeaderFromProfile(ProfileData profile) {
        if (profile == null) return;

        String fullName = ((profile.prenom() != null ? profile.prenom().trim() : "") +
                " " +
                (profile.nom() != null ? profile.nom().trim().toUpperCase() : "")).trim();

        if (fullName.isBlank()) fullName = principal.email();

        this.principal = new UserPrincipal(
                principal.id(),
                fullName,
                profile.email() != null ? profile.email() : principal.email(),
                principal.login(),
                principal.rolePrincipal(),
                principal.roles(),
                principal.privileges()
        );

        SwingUtilities.invokeLater(() -> {
            if (headerBanner != null) {
                headerBanner.refresh(principal);
                try {
                    ImageIcon avatarIcon = ImageTools.loadAvatarFromProfilePath(profile.avatar(), 48, 48);
                    headerBanner.setAvatarIcon(avatarIcon);
                } catch (Exception ignored) {}
            }
        });
    }

    public void navigateTo(ApplicationPages page) {
        openPage(page);
    }

    public void showNotificationsCount(int count) {
        if (headerBanner != null) {
            headerBanner.setNotificationCount(count);
        }
    }

    public void showNotificationsCount(int count, NotificationLevel level) {
        if (headerBanner != null) {
            headerBanner.setNotificationCount(count, level);
        }
    }

    // ======================== Navigation ========================

    private void openPage(ApplicationPages page) {
        if (page == null || center == null) {
            ConsoleLogger.warn("Tentative de navigation vers une page nulle ou center non initialisé.");
            return;
        }

        ConsoleLogger.info("Navigation demandée vers : " + page);

        JComponent pageView = controller.onNavigateRequested(page);
        if (pageView == null) {
            ConsoleLogger.warn("Aucune vue retournée pour la page : " + page);
            return;
        }

        center.upsertPage(page, pageView);
        center.showPage(page);

        SwingUtilities.invokeLater(() -> {
            center.revalidate();
            center.repaint();
            ConsoleLogger.info("Page affichée avec succès : " + page);
        });
    }

    // ======================== UI Construction ========================

    private JMenuBar buildMenuBar() {
        return new MyMenuBar(
                e -> controller.onLogoutRequested(),
                e -> controller.onExitRequested()
        );
    }

    private JPanel buildRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSideBar(), BorderLayout.WEST);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        headerBanner = new HeaderBannerPanel(principal, this::openPage);
        JComponent windowControls = new HeaderWindowControls(this, headerBanner);

        enableWindowDrag(headerBanner);
        enableWindowDrag(topBar);

        topBar.add(headerBanner, BorderLayout.CENTER);
        topBar.add(windowControls, BorderLayout.EAST);

        return topBar;
    }

    private JComponent buildSideBar() {

        var items = NavigationSpecs.forPrincipal(principal);

        return SidebarBuilder.build(
                this,
                principal,
                authorizationService,
                items,
                (source, pageId) -> {
                    try {
                        ApplicationPages page = ApplicationPages.from(pageId);
                        openPage(page);
                    } catch (IllegalArgumentException e) {
                        ConsoleLogger.warn(e.getMessage());
                    }
                },
                true
        );
    }


    private JComponent buildCenter() {
        center = new CenterPanel();
        return center;
    }

    private JComponent buildFooter() {
        footer = new FooterPanel();
        return footer;
    }

    // ======================== Chargement initial du profil ========================

    private void loadInitialProfileData() {
        SwingUtilities.invokeLater(() -> {
            try {
                ProfileService profileService = ApplicationContext.getInstance().getBean(ProfileService.class);
                if (profileService != null) {
                    ProfileData profile = profileService.loadByUserId(principal.id());
                    if (profile != null) {
                        refreshHeaderFromProfile(profile);
                    }
                }
            } catch (Exception e) {
                System.err.println("Impossible de charger le profil au démarrage : " + e.getMessage());
            }
        });
    }

    // ======================== Drag & Drop Fenêtre ========================

    private void enableWindowDrag(Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        component.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int deltaX = e.getX() - initialClick.x;
                    int deltaY = e.getY() - initialClick.y;
                    Point location = getLocation();
                    setLocation(location.x + deltaX, location.y + deltaY);
                }
            }
        });
    }
}