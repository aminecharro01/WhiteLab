package ma.WhiteLab.mvc.ui.pages.panels;

import ma.WhiteLab.entities.enums.Sexe;
import ma.WhiteLab.entities.patient.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * En-tête patient compact : photo (gauche) + infos (centre) + schéma dentaire discret (droite)
 */
public class PatientHeaderPanel extends JPanel {

    private static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 24);   // ← un peu plus petit
    private static final Font LABEL_FONT  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font VALUE_FONT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color PRIMARY   = new Color(0x0E, 0xA5, 0xA5);
    private static final Color LIGHT_BG  = new Color(245, 247, 250);
    private static final Color TEXT_DARK = new Color(40, 40, 40);
    private static final Color TEXT_GRAY = new Color(90, 90, 90);

    // Tailles beaucoup plus raisonnables
    private static final int PHOTO_SIZE          = 120;   // ← réduit de 140 → 120

    public PatientHeaderPanel(Patient patient) {
        setLayout(new BorderLayout(15, 0));           // espacement horizontal réduit
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // marges globales légères

        // Limiter la hauteur maximale de tout l'en-tête
        setMaximumSize(new Dimension(Short.MAX_VALUE, 200));

        // Photo patient à gauche
        PhotoPanel photo = new PhotoPanel(patient != null ? patient.getSexe() : null);
        photo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        add(photo, BorderLayout.WEST);

        // Bloc central : nom + infos (l'odontogramme complet est dans son propre onglet)
        add(createDetailsPanel(patient), BorderLayout.CENTER);
    }

    private JPanel createDetailsPanel(Patient patient) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel nameLabel = new JLabel(patient != null ? patient.getNomComplet() : "Patient inconnu");
        nameLabel.setFont(TITLE_FONT);
        nameLabel.setForeground(TEXT_DARK);
        panel.add(nameLabel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 6, 4, 20);   // marges réduites

        int age = calculateAge(patient);

        int row = 0;
        addRow(grid, gbc, row++, "Date de naissance", formatBirthDateAndAge(patient, age));
        addRow(grid, gbc, row++, "Genre",             getSexeDisplay(patient));
        addRow(grid, gbc, row++, "Téléphone",         patient != null ? patient.getTelephone() : "—");
        addRow(grid, gbc, row++, "Email",             patient != null ? patient.getEmail()     : "—");
        addRow(grid, gbc, row++, "Adresse",           patient != null ? patient.getAdresse()   : "—");
        addRow(grid, gbc, row,   "Assurance",         getAssuranceDisplay(patient));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void addRow(JPanel grid, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel lbl = new JLabel(label + " : ");
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_GRAY);
        grid.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel val = new JLabel(value);
        val.setFont(VALUE_FONT);
        val.setForeground(TEXT_DARK);
        grid.add(val, gbc);
    }

    private int calculateAge(Patient patient) {
        if (patient == null || patient.getDateNaissance() == null) return 0;
        return Period.between(patient.getDateNaissance(), LocalDate.now()).getYears();
    }

    private String formatBirthDateAndAge(Patient p, int age) {
        if (p == null || p.getDateNaissance() == null) return "—";
        return p.getDateNaissance().format(DATE_FORMATTER) + " (" + age + " ans)";
    }

    private String getSexeDisplay(Patient p) {
        return (p != null && p.getSexe() != null) ? p.getSexe().name() : "—";
    }

    private String getAssuranceDisplay(Patient p) {
        return (p != null && p.getAssurance() != null) ? p.getAssurance().name() : "—";
    }

    // Photo patient – taille réduite
    private static class PhotoPanel extends JPanel {
        private Image avatar;
        private static final int SIZE = PHOTO_SIZE;

        public PhotoPanel(Sexe sexe) {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMaximumSize(new Dimension(SIZE, SIZE));
            setOpaque(false);

            String path = switch (sexe) {
                case HOMME  -> "/static/icons/homme.png";
                case FEMME  -> "/static/icons/femme.png";
                default     -> "/static/avatars/default.png";
            };

            try {
                URL url = getClass().getResource(path);
                if (url != null) avatar = new ImageIcon(url).getImage();
            } catch (Exception e) {
                System.err.println("Impossible de charger l'avatar : " + path);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            g2.clip(new Ellipse2D.Float(x, y, diameter, diameter));

            if (avatar != null) {
                g2.drawImage(avatar, x, y, diameter, diameter, this);
            } else {
                g2.setColor(LIGHT_BG);
                g2.fill(new Ellipse2D.Float(x, y, diameter, diameter));
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("Pas de photo", x + 10, y + diameter / 2);
            }

            g2.setClip(null);
            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(new Ellipse2D.Float(x, y, diameter, diameter));

            g2.dispose();
        }
    }

}