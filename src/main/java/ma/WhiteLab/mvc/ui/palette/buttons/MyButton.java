package ma.WhiteLab.mvc.ui.palette.buttons;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import lombok.Data;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;

@Data
public class MyButton extends JButton {

    private final ImageIcon normalIcon;
    private final ImageIcon hoverIcon;

    private final Color normalColor;
    private final Color hoverColor;

    public MyButton(String text, ImageIcon icon, Font font) {

        this.normalIcon = icon;
        this.normalColor = ImageTools.getDominantColor(icon);

        // Couleur hover (un peu plus vive)
        this.hoverColor = normalColor.brighter();

        // Icône hover colorisée
        this.hoverIcon = ImageTools.applyColorOverlay(icon, hoverColor, 0.35f);

        setText(text);
        setFont(font);

        setIcon(normalIcon);
        setForeground(normalColor);

        setFocusable(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setHorizontalAlignment(SwingConstants.LEFT);
        setMinimumSize(new Dimension(250, 50));
        setHorizontalTextPosition(JButton.RIGHT);
        setIconTextGap(8);
        setBorder(new javax.swing.border.EmptyBorder(0, 6, 0, 0));


        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(hoverIcon);
                setForeground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(normalIcon);
                setForeground(normalColor);
            }
        });
    }
}


