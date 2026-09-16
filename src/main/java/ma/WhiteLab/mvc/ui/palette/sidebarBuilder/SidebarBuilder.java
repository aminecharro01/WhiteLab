package ma.WhiteLab.mvc.ui.palette.sidebarBuilder;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.buttons.PillNavButtonLight;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.auth.api.AuthorizationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public final class SidebarBuilder {

    private SidebarBuilder(){}

    public interface Navigator {
        //void go(MyButton source, String pageId);
       //  rendre  Navigator plus générique (pro)
        void go(AbstractButton source, String pageId);
    }

    public static JComponent build(
            Component parentForAlerts,
            UserPrincipal principal,
            AuthorizationService auth,
            List<NavSpec> items,
            Navigator navigator,
            boolean hideForbidden // true: cacher, false: désactiver
    ) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidebar.setBackground(Color.WHITE);

        JLabel title = new JLabel("Modules");
        title.setFont(new Font("Optima", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 6, 12, 0));
        sidebar.add(title);

        // group by section (garde l’ordre)
        Map<String, List<NavSpec>> bySection = new LinkedHashMap<>();

        //java.util.List<NavButton> allButtons = new java.util.ArrayList<>();

        java.util.List<PillNavButtonLight> allButtons = new java.util.ArrayList<>();

        for (NavSpec it : items) {
            bySection.computeIfAbsent(it.section(), k -> new ArrayList<>()).add(it);
        }

        for (var entry : bySection.entrySet()) {
            sidebar.add(sectionTitle(entry.getKey()));

            for (NavSpec spec : entry.getValue()) {

                boolean allowed = (spec.privilege() == null || spec.privilege().isBlank())
                                  || auth.hasPrivilege(principal, spec.privilege());

                if (!allowed && hideForbidden) continue;

                ImageIcon icon = safeIcon(spec.iconPath(), 28, 28);

                //old button
                //MyButton btn = new MyButton(spec.label(), icon, new Font("Optima", Font.BOLD, 16));
               // NavButton btn = new NavButton(spec.label(), icon);
                PillNavButtonLight btn = new PillNavButtonLight(spec.label(), icon);

                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
                btn.setAlignmentX(Component.LEFT_ALIGNMENT);

                btn.setEnabled(allowed);
/*
                btn.addActionListener(e -> {
                    if (!btn.isEnabled()) {
                        Alert.warning(parentForAlerts, "Accès refusé : privilège requis = " + spec.privilege());
                        return;
                    }
                    for (NavButton b : allButtons) b.setActive(false);
                    btn.setActive(true);

                    navigator.go(btn, spec.pageId());
                });

 */

                btn.addActionListener(e -> {
                    if (!btn.isEnabled()) {
                        Alert.warning(parentForAlerts, "Accès refusé : privilège requis = " + spec.privilege());
                        return;
                    }
                    for (PillNavButtonLight b : allButtons) b.setActive(false);
                    btn.setActive(true);

                    navigator.go(null, spec.pageId()); // ou adapte Navigator (voir ci-dessous)
                });


                sidebar.add(btn);
                allButtons.add(btn);

                sidebar.add(Box.createVerticalStrut(6));
            }

            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(sidebar);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(14);
        return sp;
    }

    private static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Optima", Font.BOLD, 14));
        l.setForeground(new Color(90, 90, 90));
        l.setBorder(new EmptyBorder(10, 6, 6, 0));
        return l;
    }

    private static ImageIcon safeIcon(String path, int w, int h) {
        try {
            return ImageTools.loadIcon(path, w, h);
        } catch (Exception ex) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }
}
