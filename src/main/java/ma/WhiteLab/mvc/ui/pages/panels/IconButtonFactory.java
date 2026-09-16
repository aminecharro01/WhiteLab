package ma.WhiteLab.mvc.ui.pages.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class IconButtonFactory {

    public static JButton createAddButton(Color hoverBg, java.awt.event.ActionListener listener) {
        return createIconButton("/static/icons/add.png", hoverBg, "Ajouter une nouvelle ordonnance", "➕", 30, listener);
    }

    public static JButton createIconButton(String iconPath, Color hoverBg, String tooltip, String fallback, int size,
                                           java.awt.event.ActionListener listener) {
        JButton btn;
        URL url = IconButtonFactory.class.getResource(iconPath);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallback);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        }

        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(hoverBg);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
        });

        if (listener != null) {
            btn.addActionListener(listener);
        }

        return btn;
    }
}