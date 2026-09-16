package ma.WhiteLab.mvc.ui.pages.commonPages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;
import ma.WhiteLab.mvc.ui.palette.notification.NotificationBadgeSupport;
import ma.WhiteLab.mvc.ui.palette.notification.NotificationLevel;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class HeaderBannerPanel extends JPanel {

    private final UserPrincipal principal; // Champ stocké pour accès partout

    private final JLabel logoLabel = new JLabel();

    private final JLabel lblUserName = new JLabel("");
    private final JLabel lblRole = new JLabel("");

    private final JButton btNotif = new JButton();
    private NotificationBadgeSupport.Controller notifBadge;

    private final JLabel avatarLabel = new JLabel();

    private final Consumer<ApplicationPages> onNavigate;

    private static final String LOGO_PATH = "/static/icons/logo.png";
    private static final String AVATAR_PATH = "/static/icons/user.png";

    // Service injecté une fois au démarrage (pas besoin de le récupérer à chaque appel)
    private final NotificationService notificationService;

    public HeaderBannerPanel(UserPrincipal principal, Consumer<ApplicationPages> onNavigate) {
        this.principal = principal;
        this.onNavigate = onNavigate;

        // Injection du service une seule fois
        this.notificationService = ApplicationContext.getInstance().getBean(NotificationService.class);

        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setBackground(Color.WHITE);
        setOpaque(true);

        add(buildLeftLogo(), BorderLayout.WEST);
        add(buildCenterTitle(), BorderLayout.CENTER);
        add(buildRightUserAndNotif(), BorderLayout.EAST);

        refresh(principal);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                updateLogoSize();
            }
        });
        SwingUtilities.invokeLater(this::updateLogoSize);

        // Mise à jour initiale du badge
        updateNotificationCount();

        // Rafraîchissement automatique toutes les 30 secondes (30 000 ms)
        Timer timer = new Timer(1000, e -> updateNotificationCount());
        timer.setRepeats(true);
        timer.start();
    }

    private JComponent buildLeftLogo() {
        logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(logoLabel, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildCenterTitle() {
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Optima", Font.BOLD, 22));
        title.setForeground(new Color(35, 35, 35));

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(title, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildRightUserAndNotif() {
        // Notifications button
        btNotif.setIcon(ImageTools.loadIcon("/static/icons/topbar/notification.png", 45, 45));
        btNotif.setToolTipText("Notifications");
        btNotif.setFocusPainted(false);
        btNotif.setBorderPainted(false);
        btNotif.setContentAreaFilled(false);
        btNotif.setOpaque(false);
        btNotif.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btNotif.addActionListener(e -> onNavigate.accept(ApplicationPages.NOTIFICATIONS));

        notifBadge = NotificationBadgeSupport.install(btNotif);

        JPanel notifBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        notifBox.setOpaque(false);
        notifBox.add(btNotif);

        // Avatar + info
        avatarLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        setDefaultAvatar();

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        lblUserName.setFont(new Font("Optima", Font.BOLD, 16));
        lblRole.setFont(new Font("Optima", Font.PLAIN, 13));
        lblRole.setForeground(new Color(110, 110, 110));

        info.add(lblUserName);
        info.add(lblRole);

        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        userBox.setOpaque(false);
        userBox.add(avatarLabel);
        userBox.add(info);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);
        right.add(userBox);
        right.add(notifBox);

        return right;
    }

    public void setAvatarIcon(ImageIcon icon) {
        if (icon == null) {
            avatarLabel.setIcon(ImageTools.loadIcon(AVATAR_PATH, 40, 40));
        } else {
            avatarLabel.setIcon(icon);
        }
        avatarLabel.revalidate();
        avatarLabel.repaint();
    }

    private void updateLogoSize() {
        int h = Math.max(1, getHeight());
        int size = Math.max(28, Math.min(52, h - 22));
        logoLabel.setIcon(ImageTools.loadIcon(LOGO_PATH, size, size));
    }

    public void refresh(UserPrincipal principal) {
        if (principal == null) return;
        lblUserName.setText(safe(principal.fullName()));
        lblRole.setText("Rôle: " + (principal.rolePrincipal() != null ? principal.rolePrincipal().name() : "—"));

        updateNotificationCount();
    }

    public void refreshFromProfile(ProfileData profile) {
        if (profile == null) return;

        String fullName = (safe(profile.prenom()) + " " + safe(profile.nom())).trim();
        lblUserName.setText(fullName.isBlank() ? "—" : fullName);

        String role = (profile.rolePrincipal() != null ? profile.rolePrincipal().name() : "—");
        lblRole.setText("Rôle: " + role);

        BufferedImage img = loadAvatarImage(profile.avatar());
        if (img != null) {
            avatarLabel.setIcon(new ImageIcon(makeCircleAvatar(img, 40)));
        } else {
            setDefaultAvatar();
        }

        revalidate();
        repaint();

        updateNotificationCount();
    }

    private void setDefaultAvatar() {
        ImageIcon ico = ImageTools.loadIcon(AVATAR_PATH, 40, 40);
        avatarLabel.setIcon(ico);
    }

    /**
     * Met à jour le badge avec le nombre réel de notifications non lues
     * Appelé au démarrage, au login, après refresh profil, et périodiquement
     */
    public void updateNotificationCount() {
        if (principal == null || principal.id() == null) {
            notifBadge.setCount(0, NotificationLevel.INFO);
            return;
        }

        // Exécution asynchrone pour ne pas bloquer l'UI pendant la requête
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                try {
                    return notificationService.countUnreadForUser(principal.id());
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void done() {
                try {
                    int unreadCount = get();
                    if (unreadCount > 0) {
                        notifBadge.setCount(unreadCount, NotificationLevel.WARNING);
                    } else {
                        notifBadge.setCount(0, NotificationLevel.INFO);
                    }
                } catch (Exception e) {
                    notifBadge.setCount(0, NotificationLevel.INFO);
                }
            }
        };
        worker.execute();
    }

    public void setNotificationCount(int count) {
        notifBadge.setCount(count, NotificationLevel.INFO);
    }

    public void setNotificationCount(int count, NotificationLevel level) {
        notifBadge.setCount(count, level);
    }

    private String safe(String s) { return s == null ? "" : s; }

    // ========= Helpers Avatar =========

    private BufferedImage loadAvatarImage(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;

        try {
            Path p = Paths.get(storedPath);
            if (p.isAbsolute() && Files.exists(p)) {
                return ImageIO.read(p.toFile());
            }

            String fn = storedPath.replace("\\", "/");
            if (fn.contains("/")) fn = fn.substring(fn.lastIndexOf('/') + 1);

            Path dir = resolveAvatarDir();
            Path file = dir.resolve(fn);
            if (!Files.exists(file)) return null;

            return ImageIO.read(file.toFile());
        } catch (Exception e) {
            return null;
        }
    }

    private Path resolveAvatarDir() {
        String dir = ApplicationContext.getInstance()
                .getProperty("profile.avatars.dir", "data/avatars");
        return Paths.get(dir);
    }

    private static BufferedImage makeCircleAvatar(BufferedImage src, int size) {
        BufferedImage scaled = scaleToFill(src, size, size);

        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape clip = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(clip);
        g2.drawImage(scaled, 0, 0, null);
        g2.setClip(null);

        g2.setColor(new Color(220, 220, 220));
        g2.drawOval(0, 0, size - 1, size - 1);

        g2.dispose();
        return out;
    }

    private static BufferedImage scaleToFill(BufferedImage src, int w, int h) {
        int sw = src.getWidth(), sh = src.getHeight();
        if (sw <= 0 || sh <= 0) return src;

        double rSrc = (double) sw / sh;
        double rDst = (double) w / h;

        int cw, ch, cx, cy;
        if (rSrc > rDst) {
            ch = sh;
            cw = (int) (sh * rDst);
            cx = (sw - cw) / 2;
            cy = 0;
        } else {
            cw = sw;
            ch = (int) (sw / rDst);
            cx = 0;
            cy = (sh - ch) / 2;
        }

        BufferedImage cropped = src.getSubimage(Math.max(0, cx), Math.max(0, cy),
                Math.min(cw, sw), Math.min(ch, sh));

        Image scaled = cropped.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return out;
    }
}