package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.enums.EtatDent;
import ma.WhiteLab.mvc.ui.palette.utils.AppTheme;
import ma.WhiteLab.service.modules.patient.api.DentService;
import ma.WhiteLab.service.modules.patient.dto.DentDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.*;

/**
 * Interactive odontogram (dental chart): 32 permanent teeth drawn as
 * anatomically-shaped crowns (incisor/canine/premolar/molar silhouettes)
 * arranged in the real four-quadrant arch layout, FDI numbering. Click a
 * tooth to set its clinical state via {@link DentService}.
 */
public class OdontogramPanel extends JPanel {

    // Design canvas size (reference geometry drawn at this scale, then auto-fit).
    // Each arch (upper / lower) is built as its own self-contained horseshoe using
    // MIRROR_X/MIRROR_Y, then the lower arch is shifted right by ARCH_GAP_X so the
    // two arches sit side by side (upper = left, lower = right) instead of the
    // Y-mirror landing them in the same band, which used to merge them into one ring.
    private static final double MIRROR_X = 860;
    private static final double MIRROR_Y = 620;
    private static final double ARCH_GAP_X = 900;
    private static final double ARCH_GAP_Y = -106; // re-centers the lower arch's bbox onto the upper arch's
    private static final double BASE_W = 1750, BASE_H = 540;

    private static final Color BG         = new Color(250, 250, 251);
    private static final Color LINE       = new Color(138, 148, 188);
    private static final Color HOVER_FILL = new Color(232, 234, 246);

    private static final Map<EtatDent, Color> COLORS = Map.of(
            EtatDent.SAIN, BG,
            EtatDent.CARIE, new Color(0xEF, 0x44, 0x44),
            EtatDent.PLOMBAGE, new Color(0xF5, 0x9E, 0x0B),
            EtatDent.COURONNE, new Color(0xEA, 0xB3, 0x08),
            EtatDent.DEVITALISE, new Color(0x8B, 0x5C, 0xF6),
            EtatDent.IMPLANT, new Color(0x38, 0xBD, 0xF8),
            EtatDent.EXTRAIT, new Color(0x9C, 0xA3, 0xAF),
            EtatDent.ABSENT, new Color(0xE5, 0xE7, 0xEB)
    );

    enum Type { CENTRAL, LATERAL, CANINE, PREMOLAR, MOLAR }

    /** Geometry for quadrant 1 (upper right), mirrored into the other three. */
    private static final Object[][] Q1 = {
        //  tooth, type,            cx,  cy,   w,   h,  angle
        {1, Type.CENTRAL,  393, 66,   73,  88,   0},
        {2, Type.LATERAL,  325, 74,   63,  77, -20},
        {3, Type.CANINE,   278, 104,  64,  76, -40},
        {4, Type.PREMOLAR, 243, 148,  60,  84, -58},
        {5, Type.PREMOLAR, 217, 199,  58,  86, -70},
        {6, Type.MOLAR,    192, 271,  94, 106, -85},
        {7, Type.MOLAR,    181, 362,  84, 100, -90},
        {8, Type.MOLAR,    177, 446,  78,  92, -92},
    };

    private static class Tooth {
        final int fdi;
        final Shape outline, inner;
        final List<Shape> details = new ArrayList<>();
        Tooth(int fdi, Shape outline, Shape inner) { this.fdi = fdi; this.outline = outline; this.inner = inner; }
    }

    private final Long patientId;
    private final DentService dentService;
    private final boolean readOnly;
    private final List<Tooth> teeth = new ArrayList<>();
    private final Map<Integer, EtatDent> etats = new LinkedHashMap<>();
    private Integer hovered = null;
    private AffineTransform view = new AffineTransform();
    private final JLabel status = new JLabel(" ");

    public OdontogramPanel(Long patientId) {
        this(patientId, false);
    }

    public OdontogramPanel(Long patientId, boolean readOnly) {
        this.patientId = patientId;
        this.readOnly = readOnly;
        this.dentService = ApplicationContext.getInstance().getBean(DentService.class);

        setLayout(new BorderLayout(0, 6));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        buildTeeth();

        JPanel canvasWrap = new JPanel(new BorderLayout());
        canvasWrap.setOpaque(false);
        Canvas canvas = new Canvas();
        canvasWrap.add(canvas, BorderLayout.CENTER);

        status.setHorizontalAlignment(SwingConstants.CENTER);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(AppTheme.TEXT_SECONDARY);

        add(canvasWrap, BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);

        loadData();
    }

    // ------------------------------------------------------------------ canvas

    /** The drawing surface: anatomical tooth shapes, auto-scaled to fit. */
    private class Canvas extends JPanel {
        Canvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(360, 260));
            setToolTipText(""); // enable tooltip dispatch

            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (readOnly) return;
                    Tooth t = hit(e.getPoint());
                    if (t == null) return;
                    showStateMenu(t.fdi, e.getX(), e.getY());
                }
                @Override public void mouseMoved(MouseEvent e) {
                    Tooth t = hit(e.getPoint());
                    Integer h = t == null ? null : t.fdi;
                    if (!Objects.equals(h, hovered)) {
                        hovered = h;
                        setCursor(Cursor.getPredefinedCursor(
                                (h == null || readOnly) ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                        status.setText(h == null ? " " : "Dent " + h + " — " + labelFor(etats.getOrDefault(h, EtatDent.SAIN)));
                        repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    hovered = null;
                    status.setText(" ");
                    repaint();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            Tooth t = hit(e.getPoint());
            if (t == null) return null;
            return "FDI " + t.fdi + " — " + labelFor(etats.getOrDefault(t.fdi, EtatDent.SAIN));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            updateView(this);
            render(g2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ geometry

    private void buildTeeth() {
        for (int q = 1; q <= 4; q++) {
            AffineTransform mirror = new AffineTransform();
            boolean fx = (q == 2 || q == 3), fy = (q == 3 || q == 4);
            if (fx) { mirror.translate(MIRROR_X, 0); mirror.scale(-1, 1); }
            if (fy) { mirror.translate(0, MIRROR_Y); mirror.scale(1, -1); }
            // Lower arch (q=3,4): keep the correct anatomical mirroring above, then
            // slide the whole arch sideways so it renders as its own horseshoe next
            // to the upper one instead of overlapping it.
            if (fy) { mirror.preConcatenate(AffineTransform.getTranslateInstance(ARCH_GAP_X, ARCH_GAP_Y)); }

            for (Object[] r : Q1) {
                int n = (int) r[0];
                Type type = (Type) r[1];
                AffineTransform at = new AffineTransform(mirror);
                at.translate(num(r[2]), num(r[3]));
                at.rotate(Math.toRadians(num(r[6])));
                at.scale(num(r[4]), num(r[5]));

                Path2D body = outline(type);
                AffineTransform shrink = AffineTransform.getScaleInstance(0.80, 0.80);
                Tooth t = new Tooth(q * 10 + n,
                        at.createTransformedShape(body),
                        at.createTransformedShape(shrink.createTransformedShape(body)));
                for (Path2D d : details(type)) t.details.add(at.createTransformedShape(d));
                teeth.add(t);
            }
        }
    }

    private static double num(Object o) { return ((Number) o).doubleValue(); }

    /* Shapes drawn in a unit box (-0.5..0.5). -Y faces the lips (outside of the arch). */
    private static Path2D outline(Type type) {
        Path2D p = new Path2D.Double();
        switch (type) {
            case CENTRAL -> {
                p.moveTo(-0.49, -0.34);
                p.curveTo(-0.50, -0.48, -0.42, -0.50, 0.00, -0.50);
                p.curveTo(0.42, -0.50, 0.50, -0.48, 0.49, -0.34);
                p.curveTo(0.47, -0.04, 0.28, 0.30, 0.10, 0.45);
                p.quadTo(0.00, 0.52, -0.10, 0.45);
                p.curveTo(-0.28, 0.30, -0.47, -0.04, -0.49, -0.34);
            }
            case LATERAL -> {
                p.moveTo(-0.48, -0.28);
                p.curveTo(-0.50, -0.48, -0.34, -0.50, 0.00, -0.50);
                p.curveTo(0.36, -0.50, 0.50, -0.46, 0.48, -0.26);
                p.curveTo(0.46, 0.08, 0.34, 0.42, 0.04, 0.48);
                p.curveTo(-0.30, 0.52, -0.46, 0.20, -0.48, -0.28);
            }
            case CANINE -> {
                p.moveTo(-0.46, -0.18);
                p.curveTo(-0.48, -0.42, -0.25, -0.50, 0.00, -0.50);
                p.curveTo(0.26, -0.50, 0.48, -0.40, 0.47, -0.14);
                p.curveTo(0.46, 0.16, 0.26, 0.46, 0.00, 0.50);
                p.curveTo(-0.26, 0.46, -0.44, 0.16, -0.46, -0.18);
            }
            case PREMOLAR -> {
                p.moveTo(-0.48, -0.14);
                p.curveTo(-0.50, -0.44, -0.30, -0.50, 0.00, -0.50);
                p.curveTo(0.30, -0.50, 0.50, -0.42, 0.48, -0.10);
                p.curveTo(0.46, 0.26, 0.38, 0.48, 0.00, 0.48);
                p.curveTo(-0.40, 0.48, -0.46, 0.20, -0.48, -0.14);
            }
            case MOLAR -> {
                p.moveTo(-0.47, -0.24);
                p.curveTo(-0.50, -0.45, -0.30, -0.50, -0.05, -0.50);
                p.curveTo(0.20, -0.50, 0.40, -0.45, 0.47, -0.30);
                p.curveTo(0.52, -0.18, 0.43, -0.05, 0.46, 0.12);
                p.curveTo(0.49, 0.32, 0.30, 0.50, 0.05, 0.48);
                p.curveTo(-0.25, 0.46, -0.45, 0.40, -0.48, 0.16);
                p.curveTo(-0.50, 0.02, -0.43, -0.10, -0.47, -0.24);
            }
        }
        p.closePath();
        return p;
    }

    private static List<Path2D> details(Type type) {
        List<Path2D> list = new ArrayList<>();
        Path2D p = new Path2D.Double();
        switch (type) {
            case CENTRAL -> {
                p.moveTo(-0.38, -0.26);
                p.curveTo(-0.28, -0.37, 0.28, -0.37, 0.38, -0.26);
            }
            case LATERAL -> {
                p.moveTo(-0.34, 0.02);
                p.curveTo(-0.38, -0.26, -0.28, -0.34, 0.10, -0.34);
            }
            case CANINE -> {
                p.moveTo(-0.32, 0.06);
                p.curveTo(-0.36, -0.24, -0.28, -0.34, 0.12, -0.34);
            }
            case PREMOLAR -> {
                p.moveTo(-0.32, 0.10);
                p.curveTo(-0.36, -0.18, -0.26, -0.32, 0.16, -0.32);
            }
            case MOLAR -> {
                p.moveTo(-0.30, -0.02);
                p.quadTo(-0.06, -0.01, 0.00, 0.12);
                p.quadTo(0.06, -0.01, 0.30, -0.02);
                p.moveTo(-0.30, -0.02); p.quadTo(-0.36, -0.06, -0.40, -0.14);
                p.moveTo(-0.30, -0.02); p.quadTo(-0.36, 0.03, -0.40, 0.12);
                p.moveTo(0.30, -0.02);  p.quadTo(0.36, -0.06, 0.40, -0.14);
                p.moveTo(0.30, -0.02);  p.quadTo(0.36, 0.03, 0.40, 0.12);
            }
        }
        list.add(p);
        return list;
    }

    // ------------------------------------------------------------------ painting

    private void updateView(Component c) {
        double s = Math.min(c.getWidth() / BASE_W, c.getHeight() / BASE_H);
        view = new AffineTransform();
        view.translate((c.getWidth() - BASE_W * s) / 2, (c.getHeight() - BASE_H * s) / 2);
        view.scale(s, s);
    }

    private Tooth hit(Point pt) {
        try {
            var base = view.inverseTransform(pt, null);
            for (Tooth t : teeth) if (t.outline.contains(base)) return t;
        } catch (Exception ignored) { }
        return null;
    }

    private void render(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.transform(view);

        BasicStroke thin  = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke thick = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        for (Tooth t : teeth) {
            EtatDent etat = etats.getOrDefault(t.fdi, EtatDent.SAIN);
            boolean marked = etat != EtatDent.SAIN;
            Color base = COLORS.get(etat);

            if (marked) {
                g2.setColor(tint(base, 0.82));
                g2.fill(t.outline);
                g2.setColor(tint(base, 0.35));
                g2.fill(t.inner);
                g2.setColor(base.darker());
                g2.setStroke(thick);
                g2.draw(t.outline);
                g2.setStroke(thin);
                for (Shape d : t.details) g2.draw(d);
            } else {
                g2.setColor(Objects.equals(hovered, t.fdi) ? HOVER_FILL : BG);
                g2.fill(t.outline);
                g2.setColor(LINE);
                g2.setStroke(thin);
                g2.draw(t.outline);
                for (Shape d : t.details) g2.draw(d);
            }

            if (Objects.equals(hovered, t.fdi) && !readOnly) {
                g2.setColor(new Color(64, 120, 255, 140));
                g2.setStroke(thick);
                g2.draw(t.outline);
            }
        }
    }

    private static Color tint(Color base, double whiteRatio) {
        int r = (int) Math.round(base.getRed()   * (1 - whiteRatio) + 255 * whiteRatio);
        int g = (int) Math.round(base.getGreen() * (1 - whiteRatio) + 255 * whiteRatio);
        int b = (int) Math.round(base.getBlue()  * (1 - whiteRatio) + 255 * whiteRatio);
        return new Color(r, g, b);
    }

    private void showStateMenu(int numero, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        for (EtatDent etat : EtatDent.values()) {
            JMenuItem item = new JMenuItem(labelFor(etat));
            item.addActionListener(e -> updateTooth(numero, etat));
            menu.add(item);
        }
        menu.show(this, x, y);
    }

    private void updateTooth(int numero, EtatDent etat) {
        try {
            dentService.updateEtat(patientId, numero, etat, null, "UI");
            etats.put(numero, etat);
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de mettre à jour la dent " + numero + " :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try {
            List<DentDTO> chart = dentService.getOdontogramme(patientId);
            for (DentDTO d : chart) {
                etats.put(d.getNumero(), EtatDent.valueOf(d.getEtat()));
            }
        } catch (Exception ex) {
            System.err.println("Odontogramme: chargement impossible — " + ex.getMessage());
        }
    }

    private JComponent buildLegend() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(status, BorderLayout.NORTH);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        legend.setOpaque(false);
        for (EtatDent etat : EtatDent.values()) {
            JLabel dot = new JLabel("●");
            dot.setForeground(etat == EtatDent.SAIN ? AppTheme.BORDER : COLORS.get(etat));
            dot.setFont(new Font("Segoe UI", Font.BOLD, 11));
            JLabel text = new JLabel(labelFor(etat));
            text.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            text.setForeground(AppTheme.TEXT_SECONDARY);
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            item.setOpaque(false);
            item.add(dot);
            item.add(text);
            legend.add(item);
        }
        wrap.add(legend, BorderLayout.CENTER);
        return wrap;
    }

    private String labelFor(EtatDent etat) {
        return switch (etat) {
            case SAIN -> "Sain";
            case CARIE -> "Carie";
            case PLOMBAGE -> "Plombage";
            case COURONNE -> "Couronne";
            case DEVITALISE -> "Dévitalisée";
            case IMPLANT -> "Implant";
            case EXTRAIT -> "Extraite";
            case ABSENT -> "Absente";
        };
    }
}
