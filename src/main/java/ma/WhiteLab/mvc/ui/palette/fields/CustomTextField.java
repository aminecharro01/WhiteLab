package ma.WhiteLab.mvc.ui.palette.fields;


import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lombok.Data;

@Data
public class CustomTextField extends JTextField {

    private String hint = "Enter text"; // Texte du placeholder (hint)
    private int arcWidth = 20; // Largeur des coins arrondis
    private int arcHeight = 20; // Hauteur des coins arrondis
    private Color borderColor = new Color(100, 150, 255); // Couleur de la bordure
    private Color backgroundColor = new Color(240, 240, 240); // Couleur de fond
    private Color gainColor = new Color(7, 28, 73); // Couleur du texte au focus
    private Color lostColor = Color.GRAY; // Couleur du texte hors focus
    private Font gainFont, lostFont; // Polices au focus et hors focus

    public CustomTextField(String hint) {
        this.hint = hint;
        // Initialisation des propriétés
        setOpaque(false); // Rendre le champ transparent pour dessiner le fond
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Espacement intérieur
        lostFont = new Font("Optima", Font.ITALIC, 15) ;// Utiliser la police par défaut comme `lostFont`
        gainFont = lostFont.deriveFont(Font.BOLD, 18); // Police en gras pour le focus
        setForeground(lostColor); // Couleur par défaut du texte
        setBackgroundColor(backgroundColor);
        setHorizontalAlignment(JTextField.CENTER);

        // Ajouter un FocusListener pour gérer le focus
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(hint)) {
                    setText(""); // Effacer le hint
                }
                setFont(gainFont);
                setForeground(gainColor);
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(hint); // Réafficher le hint
                    setFont(lostFont);
                    setForeground(lostColor);
                } else {
                    setFont(gainFont); // Maintenir le style si ce n'est pas le hint
                    setForeground(gainColor);
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Activer l'anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dessiner le fond avec des coins arrondis
        drawBackground(g2);

        // Dessiner la bordure
        drawBorder(g2);

        // Dessiner le hint si nécessaire
        if (getText().isEmpty() && !isFocusOwner()) {
            drawHint(g2);
        }

        // Appeler le parent pour gérer le texte saisi
        super.paintComponent(g);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
    }

    private void drawBorder(Graphics2D g2) {
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
    }

    private void drawHint(Graphics2D g2) {
        g2.setColor(lostColor);
        g2.setFont(lostFont);
        g2.drawString(hint, getInsets().left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 3);
    }

    // Méthodes pour configurer les propriétés dynamiquement
    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    public void setArcWeight(int arcWeight) {
        this.arcWidth = this.arcHeight = arcWeight;
        repaint();
    }
    public void setLostColor(Color lostColor) {
        this.lostColor = lostColor;
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
    public void setGainColor(Color gainColor) {
        this.gainColor = gainColor;
        setForeground(gainColor);
        repaint();
    }
}
