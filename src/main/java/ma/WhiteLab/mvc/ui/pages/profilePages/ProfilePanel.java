package ma.WhiteLab.mvc.ui.pages.profilePages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.enums.Sexe;
import ma.WhiteLab.mvc.dto.profileDtos.*;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.combos.CustomEnumComboBox;
import ma.WhiteLab.mvc.ui.palette.fields.CustomPasswordField;
import ma.WhiteLab.mvc.ui.palette.fields.CustomTextField;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static ma.WhiteLab.mvc.ui.palette.utils.ImageTools.loadIcon;

public class ProfilePanel extends JPanel {

    // ─── Thème ────────────────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(79, 70, 229);   // Indigo
    private static final Color BG_MAIN   = new Color(249, 250, 251);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color BORDER    = new Color(229, 231, 235);
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_GRAY = new Color(107, 114, 128);
    private static final Color ERROR_RED = new Color(220, 38, 38);

    private final ProfileService service;
    private ProfileData data;
    private final Consumer<ProfileData> onProfileSaved;

    // Composants UI
    private AvatarView avatarView;
    private JButton btnEditAvatar;
    private CustomTextField tfPrenom, tfNom, tfEmail, tfTel, tfAdresse, tfCin;
    private CustomTextField tfSpecialite, tfNumCNSS, tfCommission;
    private JTextField tfAvatarPath; // caché
    private CustomEnumComboBox<Sexe> cbSexe;
    private JLabel errPrenom, errNom, errEmail, errGlobal;
    private JLabel lblFullName, lblEmailHeader;

    public ProfilePanel(ProfileService service, ProfileData data, Consumer<ProfileData> onProfileSaved) {
        this.service = service;
        this.data = data;
        this.onProfileSaved = onProfileSaved;

        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(40, 60, 40, 60));

        JScrollPane scroll = new JScrollPane(createMainContent());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        fillFormFromData();
    }

    private JPanel createMainContent() {
        JPanel container = new JPanel(new BorderLayout(0, 30));
        container.setOpaque(false);

        container.add(buildHeader(), BorderLayout.NORTH);
        container.add(buildProfileCard(), BorderLayout.CENTER);
        container.add(buildFooter(), BorderLayout.SOUTH);

        return container;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                              HEADER
    // ════════════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);

        // Infos utilisateur
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        infoPanel.setOpaque(false);

        lblFullName = new JLabel("Chargement...");
        lblFullName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblFullName.setForeground(TEXT_DARK);

        lblEmailHeader = new JLabel("—");
        lblEmailHeader.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblEmailHeader.setForeground(TEXT_GRAY);

        infoPanel.add(lblFullName);
        infoPanel.add(lblEmailHeader);

        // Avatar + bouton edit
        JPanel avatarContainer = new JPanel();
        avatarContainer.setOpaque(false);
        avatarContainer.setLayout(new OverlayLayout(avatarContainer));
        avatarContainer.setPreferredSize(new Dimension(110, 110));

        avatarView = new AvatarView(110);
        btnEditAvatar = createEditAvatarButton();

        avatarContainer.add(btnEditAvatar);
        avatarContainer.add(avatarView);

        installHoverEffect(avatarContainer, btnEditAvatar);

        header.add(infoPanel, BorderLayout.WEST);
        header.add(avatarContainer, BorderLayout.EAST);

        return header;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                            CARD PROFIL
    // ════════════════════════════════════════════════════════════════════════════

    private JPanel buildProfileCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(35, 40, 35, 40)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int row = 0;

        // Titre section
        gc.gridy = row++; gc.gridx = 0; gc.gridwidth = 2;
        card.add(createSectionTitle("Informations personnelles"), gc);

        // Champs
        tfPrenom = createTextField("Prénom");
        tfNom = createTextField("Nom");
        errPrenom = createErrorLabel(); errNom = createErrorLabel();

        addTwoFieldsRow(card, gc, row++, "Prénom", tfPrenom, errPrenom, "Nom", tfNom, errNom);

        tfEmail = createTextField("Email");
        tfTel = createTextField("Téléphone");
        errEmail = createErrorLabel();

        addTwoFieldsRow(card, gc, row++, "Email", tfEmail, errEmail, "Téléphone", tfTel, null);

        tfAdresse = createTextField("Adresse");
        tfCin = createTextField("CIN");

        addTwoFieldsRow(card, gc, row++, "Adresse", tfAdresse, null, "CIN", tfCin, null);

        cbSexe = new CustomEnumComboBox<>(Sexe.class, "Sélectionner...");
        cbSexe.setPreferredSize(new Dimension(300, 42));

        gc.gridy = row++; gc.gridx = 0; gc.gridwidth = 2;
        card.add(createFieldLabel("Sexe"), gc);
        gc.gridy++; card.add(cbSexe, gc);

        // Section professionnelle (conditionnelle)
        if (data != null && data.rolePrincipal() != null) {
            gc.gridy = row++; gc.insets = new Insets(35,12,15,12);
            card.add(createSectionTitle("Informations professionnelles"), gc);
            gc.insets = new Insets(12,12,12,12);

            switch (data.rolePrincipal()) {
                case MEDECIN -> {
                    tfSpecialite = createTextField("Spécialité");
                    gc.gridy = row++; gc.gridwidth = 2;
                    card.add(createFieldLabel("Spécialité"), gc);
                    gc.gridy++; card.add(tfSpecialite, gc);
                }
                case SECRETAIRE -> {
                    tfNumCNSS = createTextField("Numéro CNSS");
                    tfCommission = createTextField("Commission (%)");
                    addTwoFieldsRow(card, gc, row++, "Num CNSS", tfNumCNSS, null,
                            "Commission", tfCommission, null);
                }
            }
        }

        // Erreur globale
        tfAvatarPath = new JTextField();
        tfAvatarPath.setVisible(false);

        gc.gridy = row; gc.gridx = 0; gc.gridwidth = 2;
        errGlobal = createErrorLabel();
        errGlobal.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(errGlobal, gc);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                               FOOTER
    // ════════════════════════════════════════════════════════════════════════════

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setOpaque(false);

        JButton btnChangePwd = createStyledButton("Changer mot de passe", CARD_BG, TEXT_DARK);
        btnChangePwd.setBorder(new LineBorder(BORDER, 1, true));
        btnChangePwd.addActionListener(e -> openChangePasswordDialog());

        JButton btnSave = createStyledButton("Enregistrer", PRIMARY, Color.WHITE);
        btnSave.addActionListener(e -> saveProfile());

        footer.add(btnChangePwd);
        footer.add(btnSave);

        return footer;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                          Helpers UI
    // ════════════════════════════════════════════════════════════════════════════

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private CustomTextField createTextField(String hint) {
        CustomTextField tf = new CustomTextField(hint);
        tf.setPreferredSize(new Dimension(300, 44));
        tf.setBackground(new Color(250, 250, 252));
        return tf;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(ERROR_RED);
        return lbl;
    }

    private void addTwoFieldsRow(JPanel panel, GridBagConstraints gc, int row,
                                 String label1, JComponent field1, JLabel err1,
                                 String label2, JComponent field2, JLabel err2) {
        gc.gridy = row; gc.gridwidth = 1;

        // Colonne gauche
        gc.gridx = 0;
        panel.add(createFieldLabel(label1), gc);
        gc.gridy++; panel.add(field1, gc);
        if (err1 != null) { gc.gridy++; panel.add(err1, gc); }

        // Colonne droite
        gc.gridy = row; gc.gridx = 1;
        panel.add(createFieldLabel(label2), gc);
        gc.gridy++; panel.add(field2, gc);
        if (err2 != null) { gc.gridy++; panel.add(err2, gc); }
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(220, 48));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createEditAvatarButton() {
        JButton btn = new JButton("✎");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.WHITE, 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> chooseAvatar());
        btn.setVisible(false);
        return btn;
    }

    private void installHoverEffect(JComponent container, JComponent button) {
        var adapter = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setVisible(true); }
            public void mouseExited(java.awt.event.MouseEvent e) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, container);
                if (!container.contains(p)) button.setVisible(false);
            }
        };
        container.addMouseListener(adapter);
        button.addMouseListener(adapter);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                          Logique métier
    // ════════════════════════════════════════════════════════════════════════════

    private void saveProfile() {
        clearErrors();

        if (data == null || data.id() == null) {
            Alert.error(this, "Profil non chargé.");
            return;
        }

        var request = ProfileUpdateRequest.builder()
                .id(data.id())
                .prenom(readValue(tfPrenom))
                .nom(readValue(tfNom))
                .email(readValue(tfEmail))
                .tel(readValue(tfTel))
                .adresse(readValue(tfAdresse))
                .cin(readValue(tfCin))
                .avatar(tfAvatarPath.getText())
                .sexe(cbSexe.getSelectedEnum())
                .dateNaissance(data.dateNaissance())
                .salaire(data.salaire())
                .prime(data.prime())
                .dateRecrutement(data.dateRecrutement())
                .soldeConge(data.soldeConge())
                .specialite(tfSpecialite != null ? readValue(tfSpecialite) : null)
                .numCNSS(tfNumCNSS != null ? readValue(tfNumCNSS) : null)
                .commission(tfCommission != null ? parseDoubleOrNull(readValue(tfCommission)) : null)
                .build();

        var result = service.update(request);

        if (!result.ok()) {
            showFieldErrors(result.fieldErrors(), result.message());
            return;
        }

        this.data = result.data();
        fillFormFromData();
        if (onProfileSaved != null) onProfileSaved.accept(data);
        Alert.success(this, "Profil mis à jour avec succès.");
    }

    private void chooseAvatar() {
        if (data == null || data.id() == null) {
            Alert.error(this, "Profil non chargé.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) throw new IOException("Format non reconnu");

            Path dir = getAvatarsDir();
            Files.createDirectories(dir);

            String ext = getExtension(file.getName());
            if (ext == null) ext = "png";

            String filename = "u" + data.id() + "_" + UUID.randomUUID() + "." + ext;
            Path destination = dir.resolve(filename);

            Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            tfAvatarPath.setText("avatars/" + filename);
            avatarView.setImage(img);

        } catch (Exception ex) {
            Alert.error(this, "Erreur lors du chargement de l'image :\n" + ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                          Helpers utilitaires
    // ════════════════════════════════════════════════════════════════════════════

    private String readValue(CustomTextField tf) {
        if (tf == null) return null;
        String text = tf.getText();
        return (text == null || text.equals(tf.getHint())) ? "" : text.trim();
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearErrors() {
        if (errPrenom != null) errPrenom.setText(" ");
        if (errNom != null)    errNom.setText(" ");
        if (errEmail != null)  errEmail.setText(" ");
        if (errGlobal != null) errGlobal.setText(" ");
    }

    private void showFieldErrors(Map<String, String> errors, String globalMsg) {
        if (errors == null) {
            errGlobal.setText(globalMsg != null ? globalMsg : "Erreur inconnue");
            return;
        }
        errPrenom.setText(errors.getOrDefault("prenom", " "));
        errNom.setText(errors.getOrDefault("nom", " "));
        errEmail.setText(errors.getOrDefault("email", " "));
        errGlobal.setText(errors.getOrDefault("_global", globalMsg));
    }

    private void fillFormFromData() {
        if (data == null) return;

        setFieldValue(tfPrenom, data.prenom());
        setFieldValue(tfNom, data.nom());
        setFieldValue(tfEmail, data.email());
        setFieldValue(tfTel, data.tel());
        setFieldValue(tfAdresse, data.adresse());
        setFieldValue(tfCin, data.cin());

        tfAvatarPath.setText(safe(data.avatar()));
        cbSexe.setSelectedEnum(data.sexe());

        if (tfSpecialite != null) setFieldValue(tfSpecialite, data.specialite());
        if (tfNumCNSS != null)    setFieldValue(tfNumCNSS, data.numCNSS());
        if (tfCommission != null) setFieldValue(tfCommission,
                data.commission() != null ? String.format("%.2f", data.commission()) : "");

        String fullName = (safe(data.prenom()) + " " + safe(data.nom())).trim();
        lblFullName.setText(fullName.isBlank() ? "Profil" : fullName);
        lblEmailHeader.setText(safe(data.email()));

        avatarView.setImage(loadAvatar(data.avatar()));
    }

    private void setFieldValue(CustomTextField field, String value) {
        if (field == null) return;
        String text = safe(value).trim();
        field.setText(text.isEmpty() ? field.getHint() : text);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private Path getAvatarsDir() {
        return Paths.get(ApplicationContext.getInstance().getProperty("profile.avatars.dir", "avatars"));
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1).toLowerCase() : null;
    }

    private BufferedImage loadAvatar(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        try {
            Path filePath = getAvatarsDir().resolve(Paths.get(path).getFileName());
            return Files.exists(filePath) ? ImageIO.read(filePath.toFile()) : null;
        } catch (IOException e) {
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                            AvatarView
    // ════════════════════════════════════════════════════════════════════════════

    private static class AvatarView extends JComponent {
        private final int size;
        private BufferedImage image;

        AvatarView(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
        }

        void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape circle = new Ellipse2D.Double(0, 0, size, size);
            g2.setColor(new Color(243, 244, 246));
            g2.fill(circle);

            if (image != null) {
                g2.setClip(circle);
                g2.drawImage(image, 0, 0, size, size, null);
            } else {
                g2.setColor(TEXT_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                String text = "?";
                int x = (size - fm.stringWidth(text)) / 2;
                int y = (size + fm.getAscent()) / 2 - 5;
                g2.drawString(text, x, y);
            }

            g2.setClip(null);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(circle);

            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //                  Dialogue changement mot de passe (simplifié)
    // ════════════════════════════════════════════════════════════════════════════
    private void openChangePasswordDialog() {
        if (data == null || data.id() == null) {
            Alert.error(this, "Profil non chargé.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Changer le mot de passe", true);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ─── Contenu ───
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;

        CustomPasswordField pfCurrent = new CustomPasswordField("Mot de passe actuel");
        CustomPasswordField pfNew      = new CustomPasswordField("Nouveau mot de passe");
        CustomPasswordField pfConfirm  = new CustomPasswordField("Confirmer le nouveau");

        pfCurrent.setPreferredSize(new Dimension(360, 44));
        pfNew.setPreferredSize(new Dimension(360, 44));
        pfConfirm.setPreferredSize(new Dimension(360, 44));

        JLabel errCurrent = createErrorLabel();
        JLabel errNew     = createErrorLabel();
        JLabel errConfirm = createErrorLabel();
        JLabel errGlobal  = createErrorLabel();
        errGlobal.setHorizontalAlignment(SwingConstants.CENTER);

        int row = 0;
        gc.gridy = row++;
        form.add(createFieldLabel("Mot de passe actuel"), gc);
        gc.gridy = row++;
        form.add(pfCurrent, gc);
        gc.gridy = row++;
        form.add(errCurrent, gc);

        gc.gridy = row++;
        form.add(createFieldLabel("Nouveau mot de passe"), gc);
        gc.gridy = row++;
        form.add(pfNew, gc);
        gc.gridy = row++;
        form.add(errNew, gc);

        gc.gridy = row++;
        form.add(createFieldLabel("Confirmer le nouveau mot de passe"), gc);
        gc.gridy = row++;
        form.add(pfConfirm, gc);
        gc.gridy = row++;
        form.add(errConfirm, gc);

        gc.gridy = row++;
        gc.insets = new Insets(20, 0, 0, 0);
        form.add(errGlobal, gc);

        // ─── Boutons ───
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnCancel = createStyledButton("Annuler", CARD_BG, TEXT_DARK);
        btnCancel.setBorder(new LineBorder(BORDER, 1, true));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnValidate = createStyledButton("Valider", PRIMARY, Color.WHITE);
        btnValidate.addActionListener(e -> {
            // Reset erreurs
            errCurrent.setText(" ");
            errNew.setText(" ");
            errConfirm.setText(" ");
            errGlobal.setText(" ");

            String current = new String(pfCurrent.getPassword());
            String newPwd  = new String(pfNew.getPassword());
            String confirm = new String(pfConfirm.getPassword());

            if (current.isBlank() || newPwd.isBlank() || confirm.isBlank()) {
                errGlobal.setText("Tous les champs sont obligatoires.");
                return;
            }

            if (!newPwd.equals(confirm)) {
                errConfirm.setText("Les mots de passe ne correspondent pas.");
                return;
            }

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .userId(data.id())
                    .currentPassword(current)
                    .newPassword(newPwd)
                    .confirmPassword(confirm)
                    .build();

            ChangePasswordResult result = service.changePassword(request);

            if (!result.ok()) {
                Map<String, String> errors = result.fieldErrors();
                if (errors != null) {
                    errCurrent.setText(errors.getOrDefault("currentPassword", " "));
                    errNew.setText(errors.getOrDefault("newPassword", " "));
                    errConfirm.setText(errors.getOrDefault("confirmPassword", " "));
                    errGlobal.setText(errors.getOrDefault("_global", result.message() != null ? result.message() : "Erreur"));
                } else {
                    errGlobal.setText(result.message() != null ? result.message() : "Une erreur est survenue.");
                }
                return;
            }

            Alert.success(dialog, "Mot de passe modifié avec succès !");
            dialog.dispose();
        });

        footer.add(btnCancel);
        footer.add(btnValidate);

        // ─── Assemblage ───
        mainPanel.add(form, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(480, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);

        // Support clavier : Enter = Valider, Escape = Annuler
        dialog.getRootPane().setDefaultButton(btnValidate);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        dialog.getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // Focus sur le premier champ
        SwingUtilities.invokeLater(pfCurrent::requestFocusInWindow);

        dialog.setVisible(true);
    }
}