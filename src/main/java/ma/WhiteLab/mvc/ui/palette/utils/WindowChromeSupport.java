package ma.WhiteLab.mvc.ui.palette.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;


/**
 * C'est une classe utilitaire prête à brancher avec le HeaderWindowControls par ex qui te donne :
 *
 *      Toggle plein écran avec animation douce (via un Timer)
 *      Un Double-clic sur le header pour un fullscreen
 *      action de raccourcis clavier F11 pour le fullscreen
 *      une Icône dynamique pour agrandissement et réduction (⬜ / 🗗)
 *      Compatible avec notre HeaderBannerPanel et HeaderWindowControls utilisant le installWindowDrag(...)
 *
 *      NB. l’animation douce est beaucoup plus stable si on fait un “borderless maximized”
 *      (fenêtre undecorated qui prend tout l’écran utile) plutôt qu’un “vrai fullscreen device” (exclusive mode).
 *      Visuellement, c'est un plein écran, et c’est ce que font beaucoup d’apps Swing custom.
 */


public final class WindowChromeSupport {

    private WindowChromeSupport() {}

    public static final class Options {
        public boolean enableF11Shortcut = true;
        public boolean enableDoubleClickOnHeader = true;

        /** Animation douce (ms). 0 = pas d’animation */
        public int animationDurationMs = 180;

        /** Si true : on utilise l’écran "maximum window bounds" (sans taskbar/dock). */
        public boolean useMaximumWindowBounds = true;

        /** Texte/icônes (tu peux remplacer par des icônes ImageIcon si tu veux) */
        public String iconEnterFullscreen = "⬜";
        public String iconExitFullscreen  = "🗗";
    }

    public static final class Controller {
        private final JFrame frame;
        private final JComponent header;
        private final JButton fullscreenButton;
        private final Options opt;

        private boolean fullscreen = false;
        private Rectangle windowedBounds;

        private Controller(JFrame frame, JComponent header, JButton fullscreenButton, Options opt) {
            this.frame = Objects.requireNonNull(frame);
            this.header = Objects.requireNonNull(header);
            this.fullscreenButton = Objects.requireNonNull(fullscreenButton);
            this.opt = (opt != null ? opt : new Options());
        }

        public boolean isFullscreen() { return fullscreen; }

        public void toggleFullscreen() {
            if (!fullscreen) enterFullscreen();
            else exitFullscreen();
        }

        public void enterFullscreen() {
            if (fullscreen) return;

            // On mémorise l’état "fenêtre"
            windowedBounds = frame.getBounds();

            Rectangle target = computeTargetBounds();
            fullscreen = true;
            refreshFullscreenButtonIcon();

            if (opt.animationDurationMs > 0) {
                animateBounds(frame, windowedBounds, target, opt.animationDurationMs, () -> {
                    // Optionnel : forcer un state propre
                    frame.setExtendedState(Frame.NORMAL);
                    frame.setBounds(target);
                });
            } else {
                frame.setExtendedState(Frame.NORMAL);
                frame.setBounds(target);
            }
        }

        public void exitFullscreen() {
            if (!fullscreen) return;

            Rectangle from = frame.getBounds();
            Rectangle to = (windowedBounds != null ? windowedBounds : defaultWindowedBounds());

            fullscreen = false;
            refreshFullscreenButtonIcon();

            if (opt.animationDurationMs > 0) {
                animateBounds(frame, from, to, opt.animationDurationMs, () -> {
                    frame.setExtendedState(Frame.NORMAL);
                    frame.setBounds(to);
                });
            } else {
                frame.setExtendedState(Frame.NORMAL);
                frame.setBounds(to);
            }
        }

        private Rectangle defaultWindowedBounds() {
            Rectangle screen = computeTargetBounds();
            int w = Math.min(1100, screen.width);
            int h = Math.min(720, screen.height);
            int x = screen.x + (screen.width - w) / 2;
            int y = screen.y + (screen.height - h) / 2;
            return new Rectangle(x, y, w, h);
        }

        private Rectangle computeTargetBounds() {
            GraphicsConfiguration gc = frame.getGraphicsConfiguration();
            if (gc == null) {
                gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration();
            }

            if (opt.useMaximumWindowBounds) {
                // zone utilisable (sans taskbar / dock) sur l’écran courant
                Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

                // Si la fenêtre est sur un autre écran, maximumWindowBounds peut être écran principal.
                // On ajuste au screen device courant via gc bounds.
                Rectangle screenBounds = gc.getBounds();
                Rectangle intersect = max.intersection(screenBounds);
                if (!intersect.isEmpty()) return intersect;

                // fallback
                return max;
            } else {
                // plein écran brut (peut passer sous la taskbar)
                return gc.getBounds();
            }
        }

        private void refreshFullscreenButtonIcon() {
            fullscreenButton.setText(fullscreen ? opt.iconExitFullscreen : opt.iconEnterFullscreen);
        }
    }

    /**
     * Installe:
     * - bouton fullscreen (toggle + icône)
     * - double-clic sur header => toggle
     * - F11 => toggle
     */
    public static Controller install(JFrame frame, JComponent header, JButton fullscreenButton, Options options) {
        Controller c = new Controller(frame, header, fullscreenButton, options);

        // Icône initiale
        fullscreenButton.setText(c.fullscreen ? c.opt.iconExitFullscreen : c.opt.iconEnterFullscreen);

        // Click bouton fullscreen
        fullscreenButton.addActionListener(e -> c.toggleFullscreen());

        // Double-clic sur header
        if (c.opt.enableDoubleClickOnHeader) {
            header.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        c.toggleFullscreen();
                    }
                }
            });
        }

        // Raccourci F11
        if (c.opt.enableF11Shortcut) {
            JRootPane root = frame.getRootPane();
            root.registerKeyboardAction(
                    e -> c.toggleFullscreen(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        return c;
    }

    /* ===================== Animation douce ===================== */

    private static void animateBounds(Window w, Rectangle from, Rectangle to, int durationMs, Runnable onDone) {
        if (durationMs <= 0) {
            w.setBounds(to);
            if (onDone != null) onDone.run();
            return;
        }

        final long start = System.currentTimeMillis();
        final int delay = 1000 / 60; // ~60 fps

        Timer timer = new Timer(delay, null);
        timer.addActionListener(e -> {
            long now = System.currentTimeMillis();
            float t = (now - start) / (float) durationMs;
            if (t >= 1f) t = 1f;

            // easing doux (easeOutCubic)
            float eased = 1f - (float) Math.pow(1f - t, 3);

            int x = lerp(from.x, to.x, eased);
            int y = lerp(from.y, to.y, eased);
            int w1 = lerp(from.width, to.width, eased);
            int h1 = lerp(from.height, to.height, eased);

            w.setBounds(x, y, w1, h1);

            if (t >= 1f) {
                timer.stop();
                if (onDone != null) onDone.run();
            }
        });

        timer.setCoalesce(true);
        timer.start();
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }
}
