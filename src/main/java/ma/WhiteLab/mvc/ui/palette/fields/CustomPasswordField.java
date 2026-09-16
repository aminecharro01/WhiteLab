package ma.WhiteLab.mvc.ui.palette.fields;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lombok.Data;

@Data
public class CustomPasswordField extends JPasswordField {
    private String hint             = "Enter password"; // Le texte du placeholder (hint)
    private int arcWidth            = 20; // Largeur des coins arrondis
    private int arcHeight           = 20; // Hauteur des coins arrondis
    private Color borderColor       = new Color(100, 150, 255); // Couleur de la bordure
    private Color backgroundColor   = new Color(240, 240, 240); // Couleur de fond
    private Font lostFont, gainFont;
    private Color gainColor         = new Color(7, 28, 73); // Couleur du texte au focus
    private Color lostColor         = Color.GRAY; // Couleur du texte hors focus


    public void setGainColor(Color gainColor) {
        this.gainColor = gainColor;
        setForeground(gainColor);
        repaint();
    }
    public void setLostFont(Font lostFont) {
        this.lostFont = lostFont;
        repaint();
    }
    public void setGainFont(Font gainFont) {
        this.gainFont = gainFont;
        setFont(gainFont);
        repaint();
    }

    public CustomPasswordField(String hint) {
        this.hint = hint;
        lostFont = new Font("Optima", Font.ITALIC, 15) ;// Utiliser la police par défaut comme `lostFont`
        gainFont = lostFont.deriveFont(Font.BOLD, 18); // Police en gras pour le focus
        setOpaque(false); // Rendre le champ transparent pour dessiner le fond nous-mêmes
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Espacement intérieur
        setHorizontalAlignment(JTextField.CENTER);

    }
    public CustomPasswordField() {
        setOpaque(false); // Rendre le champ transparent pour dessiner le fond nous-mêmes
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Espacement intérieur
    }

    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    public void setArc(int arcWidth, int arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);

        // Bordure
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);

        // Hint centré
        if (getPassword().length == 0 && !isFocusOwner()) {
            g2.setFont(lostFont);
            g2.setColor(lostColor);

            FontMetrics fm = g2.getFontMetrics();

            // Centrage horizontal
            int textWidth = fm.stringWidth(hint);
            int x = (getWidth() - textWidth) / 2;

            // Centrage vertical (baseline correcte)
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(hint, x, y);
        } else {
            super.paintComponent(g);
        }

        g2.dispose();
    }

}

