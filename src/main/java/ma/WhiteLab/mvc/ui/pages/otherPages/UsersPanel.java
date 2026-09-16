package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;
import ma.WhiteLab.service.modules.users.api.UserManagementService;
import ma.WhiteLab.service.modules.users.dto.CreateUserRequest;
import ma.WhiteLab.service.modules.users.dto.RoleDto;
import ma.WhiteLab.service.modules.users.dto.UpdateUserRequest;
import ma.WhiteLab.service.modules.users.dto.UserDto;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.Sexe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class UsersPanel extends JPanel {

    private final UserPrincipal principal;
    private final UserManagementService userService;
    private final RoleManagementService roleService;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private JPanel listView;
    private JPanel detailView;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public UsersPanel(UserPrincipal principal, UserManagementService userService, RoleManagementService roleService) {
        this.principal = principal;
        this.userService = userService;
        this.roleService = roleService;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initListView();
        initDetailView();

        container.add(listView, "LIST");
        container.add(detailView, "DETAIL");
        add(container, BorderLayout.CENTER);

        loadData();
    }

    // ================= INIT VIEWS =================

    private void initListView() {
        listView = new JPanel(new BorderLayout(0, 20));
        listView.setBackground(Color.WHITE);
        listView.setBorder(new EmptyBorder(22, 22, 22, 22));

        listView.add(buildListHeader(), BorderLayout.NORTH);
        listView.add(buildTablePanel(), BorderLayout.CENTER);
    }

    private void initDetailView() {
        detailView = new JPanel(new BorderLayout());
        detailView.setBackground(Color.WHITE);
    }

    // ================= LIST VIEW =================

    private JPanel buildListHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Gestion des Utilisateurs");
        title.setFont(new Font("Optima", Font.BOLD, 28));

        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher un utilisateur...");

        JButton btnAdd = new JButton("Nouvel Utilisateur");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> showUserForm(null));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(searchField);
        actions.add(btnAdd);

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Nom complet", "Email", "Rôle Principal", "Dernière connexion", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.getColumnModel().getColumn(5).setCellRenderer(new UserActionsRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new UserActionsEditor());

        return new JScrollPane(table);
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<UserDto> users = userService.getAllUsers();
                populateTable(users);
            } catch (Exception e) {
                e.printStackTrace();
                Alert.error(this, "Erreur lors du chargement des utilisateurs : " + e.getMessage());
            }
        });
    }

    private void populateTable(List<UserDto> list) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (UserDto u : list) {
            tableModel.addRow(new Object[]{
                    u.id(),
                    u.prenom() + " " + u.nom(),
                    u.email(),
                    u.role(),
                    u.lastLoginDate() != null ? u.lastLoginDate().format(fmt) : "Jamais",
                    u
            });
        }
    }

    // ================= DETAIL VIEW =================

    private void showUserDetails(UserDto dto) {
        detailView.removeAll();
        detailView.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JButton backBtn = new JButton("Retour");
        backBtn.addActionListener(e -> cardLayout.show(container, "LIST"));

        JLabel title = new JLabel(dto.prenom() + " " + dto.nom());
        title.setFont(new Font("Optima", Font.BOLD, 28));

        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JPanel infoCard = new JPanel(new GridLayout(0, 2, 10, 10));
        infoCard.setBorder(BorderFactory.createTitledBorder("Informations"));
        infoCard.setBackground(Color.WHITE);

        infoCard.add(new JLabel("Email: " + dto.email()));
        infoCard.add(new JLabel("Rôle Principal: " + dto.role()));
        infoCard.add(new JLabel("Téléphone: " + (dto.telephone() != null ? dto.telephone() : "-")));
        infoCard.add(new JLabel("CIN: " + (dto.cin() != null ? dto.cin() : "-")));

        body.add(infoCard, gc);

        gc.gridy++;
        JPanel roleCard = new JPanel(new BorderLayout(10, 10));
        roleCard.setBorder(BorderFactory.createTitledBorder("Rôles & Permissions"));
        roleCard.setBackground(Color.WHITE);

        JTextArea rolesArea = new JTextArea("Rôles actuels: " + dto.assignedRoles());
        rolesArea.setEditable(false); rolesArea.setOpaque(false); rolesArea.setLineWrap(true);
        roleCard.add(rolesArea, BorderLayout.CENTER);

        JPanel roleBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        roleBtns.setOpaque(false);

        JButton btnManageRoles = new JButton("Modifier Rôles");
        btnManageRoles.addActionListener(e -> showRoleAssignmentDialog(dto));

        JButton btnViewPerms = new JButton("Voir Permissions Effectives");
        btnViewPerms.addActionListener(e -> {
            List<String> perms = roleService.getUserPermissions(dto.id());
            StringJoiner sj = new StringJoiner("\n");
            for (String perm : perms) sj.add(perm);
            JTextArea ta = new JTextArea(sj.toString());
            ta.setRows(15); ta.setColumns(40); ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Permissions de " + dto.nom(), JOptionPane.INFORMATION_MESSAGE);
        });

        roleBtns.add(btnManageRoles);
        roleBtns.add(btnViewPerms);
        roleCard.add(roleBtns, BorderLayout.SOUTH);

        body.add(roleCard, gc);

        gc.gridy++;
        JPanel actionCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        actionCard.setBorder(BorderFactory.createTitledBorder("Actions de Sécurité"));
        actionCard.setBackground(Color.WHITE);

        JButton btnReset = new JButton("Réinitialiser Mot de Passe");
        btnReset.setBackground(new Color(52, 152, 219));
        btnReset.setForeground(Color.WHITE);
        btnReset.addActionListener(e -> {
            String newPass = JOptionPane.showInputDialog(this, "Nouveau mot de passe :", "Réinitialisation de mot de passe", JOptionPane.PLAIN_MESSAGE);
            if (newPass != null && !newPass.isBlank()) {
                userService.resetUserPassword(dto.id(), newPass);
                Alert.success(this, "Mot de passe réinitialisé.");
            }
        });

        JButton btnToggle = new JButton(dto.active() ? "Désactiver Compte" : "Activer Compte");
        btnToggle.setBackground(dto.active() ? new Color(231, 76, 60) : new Color(46, 204, 113));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.addActionListener(e -> {
            userService.toggleUserAccountStatus(dto.id(), !dto.active());
            Alert.info(this, "Statut du compte modifié (Simulation).");
        });

        JButton btnHistory = new JButton("Historique Connexions");
        btnHistory.addActionListener(e -> {
            List<LocalDateTime> history = userService.getUserConnectionHistory(dto.id());
            String msg = history.isEmpty() ? "Aucune connexion." : "Dernière: " + history.get(0).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            JOptionPane.showMessageDialog(this, msg);
        });

        actionCard.add(btnReset);
        actionCard.add(btnToggle);
        actionCard.add(btnHistory);

        body.add(actionCard, gc);

        detailView.add(header, BorderLayout.NORTH);
        detailView.add(new JScrollPane(body), BorderLayout.CENTER);

        cardLayout.show(container, "DETAIL");
    }

    // ================= USER FORM =================

    private void showUserForm(UserDto dto) {
        boolean isNew = (dto == null);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isNew ? "Créer Utilisateur" : "Modifier Utilisateur", true);
        d.setSize(450, 600);
        d.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField tfNom = new JTextField(isNew ? "" : dto.nom());
        JTextField tfPrenom = new JTextField(isNew ? "" : dto.prenom());
        JTextField tfEmail = new JTextField(isNew ? "" : dto.email());
        JPasswordField pfPass = new JPasswordField();
        JComboBox<RoleR> cbRole = new JComboBox<>(RoleR.values());
        if (!isNew) cbRole.setSelectedItem(dto.role());

        form.add(new JLabel("Nom:")); form.add(tfNom);
        form.add(new JLabel("Prénom:")); form.add(tfPrenom);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        if(isNew) { form.add(new JLabel("Mot de passe:")); form.add(pfPass); }
        form.add(new JLabel("Type / Rôle (Principal):")); form.add(cbRole);

        JButton btnSave = new JButton("Enregistrer");
        btnSave.addActionListener(e -> {
            try {
                RoleR role = (RoleR) cbRole.getSelectedItem();
                if (isNew) {
                    CreateUserRequest req = new CreateUserRequest(
                            tfNom.getText().trim(),
                            tfPrenom.getText().trim(),
                            tfEmail.getText().trim(),
                            new String(pfPass.getPassword()),
                            "", "", "", Sexe.HOMME, null,
                            role,
                            0.0, 0.0, null, 0, "", "", 0.0
                    );
                    userService.createUser(req, principal.email());
                } else {
                    UpdateUserRequest req = new UpdateUserRequest(
                            dto.id(),
                            tfNom.getText().trim(),
                            tfPrenom.getText().trim(),
                            tfEmail.getText().trim(),
                            "", "", "", Sexe.HOMME, null, null, null, null, null, null, null, null
                    );
                    userService.updateUser(req, principal.email());
                }
                d.dispose();
                loadData();
                Alert.success(this, "Utilisateur enregistré.");
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert.error(this, "Erreur : " + ex.getMessage());
            }
        });

        d.add(form, BorderLayout.CENTER);
        d.add(btnSave, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void deleteUser(UserDto dto) {
        if (Alert.confirm(this, "Supprimer l'utilisateur " + dto.email() + " ?")) {
            userService.deleteUser(dto.id());
            loadData();
        }
    }

    // ================= ROLE ASSIGNMENT =================

    private void showRoleAssignmentDialog(UserDto user) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assigner Rôles pour " + user.nom(), true);
        d.setSize(400, 500);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,10,10,10));

        List<RoleDto> allRoles = roleService.getAllRoles();
        JPanel checkList = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> boxes = new ArrayList<>();

        for(RoleDto r : allRoles) {
            JCheckBox cb = new JCheckBox(r.libelle().name());
            cb.setSelected(user.assignedRoles().contains(r.libelle().name()));
            boxes.add(cb);
            checkList.add(cb);
        }

        JButton save = new JButton("Enregistrer");
        save.addActionListener(e -> {
            for(JCheckBox cb : boxes) {
                try {
                    RoleR rName = RoleR.valueOf(cb.getText());
                    if (cb.isSelected() && !user.assignedRoles().contains(rName.name())) {
                        roleService.assignRoleToUser(user.id(), rName);
                    } else if (!cb.isSelected() && user.assignedRoles().contains(rName.name())) {
                        if (!rName.name().equals(user.role().name())) {
                            roleService.removeRoleFromUser(user.id(), rName);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            d.dispose();
            UserDto fresh = userService.getUserById(user.id());
            showUserDetails(fresh);
        });

        p.add(new JScrollPane(checkList), BorderLayout.CENTER);
        p.add(save, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }

    // ================= TABLE ACTIONS =================

    class UserActionsPanel extends JPanel {
        final JButton btnEdit = createBtn("/static/icons/edit.png", new Color(241, 196, 15));
        final JButton btnList = createBtn("/static/icons/open.png", new Color(52, 152, 219));
        final JButton btnDel = createBtn("/static/icons/delete.png", new Color(231, 76, 60));

        UserActionsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(false);
            add(btnList); add(btnEdit); add(btnDel);
        }

        private JButton createBtn(String path, Color hover) {
            JButton b = new JButton(ImageTools.loadIcon(path, 20, 20));
            b.setContentAreaFilled(false); b.setBorderPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    b.setContentAreaFilled(true);
                    b.setBackground(new Color(hover.getRed(), hover.getGreen(), hover.getBlue(), 40));
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    b.setContentAreaFilled(false);
                }
            });
            return b;
        }
    }

    class UserActionsRenderer extends DefaultTableCellRenderer {
        private final UserActionsPanel p = new UserActionsPanel();
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return p;
        }
    }

    class UserActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final UserActionsPanel p = new UserActionsPanel();
        private UserDto current;
        UserActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); showUserDetails(current); });
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); showUserForm(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); deleteUser(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (UserDto) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}
