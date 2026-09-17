package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;
import ma.WhiteLab.service.modules.users.dto.RoleDto;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.security.Privileges;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RolesPanel extends JPanel {

    private final UserPrincipal principal;
    private final RoleManagementService roleService;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private JPanel listView;
    private JPanel editorView;

    private JTable table;
    private DefaultTableModel tableModel;

    // Editor Components
    private JTextField tfLibelle; // Or ComboBox if restricted to Enum
    private JTextField tfDesc;
    private JPanel privilegesCheckPanel;
    private RoleDto currentEditingRole; // null for create

    public RolesPanel(UserPrincipal principal, RoleManagementService roleService) {
        this.principal = principal;
        this.roleService = roleService;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initListView();
        initEditorView();

        container.add(listView, "LIST");
        container.add(editorView, "EDITOR");
        add(container, BorderLayout.CENTER);

        loadData();
    }

    private void initListView() {
        listView = new JPanel(new BorderLayout(0, 20));
        listView.setBackground(Color.WHITE);
        listView.setBorder(new EmptyBorder(22, 22, 22, 22));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Gestion des Rôles & Permissions");
        title.setFont(new Font("Optima", Font.BOLD, 28));

        JButton btnAdd = new JButton("Nouveau Rôle");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> openEditor(null));

        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

        listView.add(header, BorderLayout.NORTH);
        listView.add(buildTablePanel(), BorderLayout.CENTER);
    }

    private void initEditorView() {
        editorView = new JPanel(new BorderLayout(0, 20));
        editorView.setBackground(Color.WHITE);
        editorView.setBorder(new EmptyBorder(22, 22, 22, 22));
    }

    // ================= TABLE =================

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Libellé", "Description", "Permissions", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.getColumnModel().getColumn(4).setCellRenderer(new RoleActionsRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new RoleActionsEditor());

        return new JScrollPane(table);
    }

    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<RoleDto> roles = roleService.getAllRoles();
                populateTable(roles);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void populateTable(List<RoleDto> list) {
        tableModel.setRowCount(0);
        for (RoleDto r : list) {
            tableModel.addRow(new Object[]{
                    r.id(), r.libelle(), r.description(),
                    r.privileges() != null ? r.privileges().size() : 0, r
            });
        }
    }

    // ================= EDITOR VIEW =================

    private void openEditor(RoleDto dto) {
        currentEditingRole = dto;
        boolean isNew = (dto == null);

        editorView.removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(isNew ? "Créer un Rôle" : "Modifier le Rôle : " + dto.libelle());
        title.setFont(new Font("Optima", Font.BOLD, 24));
        JButton backBtn = new JButton("Retour");
        backBtn.addActionListener(e -> cardLayout.show(container, "LIST"));
        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        // Basic Info
        form.add(new JLabel("Libellé (RoleR):"), gc);

        // Use ComboBox for RoleR to avoid invalid strings, but allow custom if RoleR is just a suggestion?
        // No, entity uses Enum. Must be Enum.
        // If editing, we usually don't change the Enum key unless we want to map to another.
        // For simplicity, let's use a TextField that parses, or ComboBox. ComboBox is safer.
        JComboBox<RoleR> cbRole = new JComboBox<>(RoleR.values());
        if (!isNew) cbRole.setSelectedItem(dto.libelle());

        gc.gridx = 1;
        form.add(cbRole, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Description:"), gc);
        tfDesc = new JTextField(isNew ? "" : dto.description());
        gc.gridx = 1;
        form.add(tfDesc, gc);

        // Permissions Checkboxes
        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2;
        form.add(new JLabel("Permissions / Privilèges:"), gc);

        privilegesCheckPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        privilegesCheckPanel.setOpaque(false);
        privilegesCheckPanel.setBorder(BorderFactory.createTitledBorder("Cochez les droits"));

        List<String> allPrivs = getAllSystemPrivileges();
        List<String> currentPrivs = isNew ? new ArrayList<>() : dto.privileges();

        for (String p : allPrivs) {
            JCheckBox cb = new JCheckBox(p);
            cb.setOpaque(false);
            if (currentPrivs.contains(p)) cb.setSelected(true);
            privilegesCheckPanel.add(cb);
        }

        gc.gridy++;
        form.add(privilegesCheckPanel, gc);

        // Save Button
        JButton btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(0x0E, 0xA5, 0xA5));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.addActionListener(e -> saveRole(isNew, (RoleR) cbRole.getSelectedItem()));

        editorView.add(header, BorderLayout.NORTH);
        editorView.add(new JScrollPane(form), BorderLayout.CENTER);
        editorView.add(btnSave, BorderLayout.SOUTH);

        cardLayout.show(container, "EDITOR");
    }

    private void saveRole(boolean isNew, RoleR roleEnum) {
        try {
            String desc = tfDesc.getText();
            List<String> selectedPrivs = new ArrayList<>();
            for (Component c : privilegesCheckPanel.getComponents()) {
                if (c instanceof JCheckBox cb && cb.isSelected()) {
                    selectedPrivs.add(cb.getText());
                }
            }

            if (isNew) {
                RoleDto newRole = new RoleDto(null, roleEnum, desc, selectedPrivs);
                roleService.createRole(newRole, principal.email());
            } else {
                RoleDto updateRole = new RoleDto(currentEditingRole.id(), roleEnum, desc, selectedPrivs);
                roleService.updateRole(updateRole, principal.email());
            }

            Alert.success(this, "Rôle enregistré !");
            loadData();
            cardLayout.show(container, "LIST");

        } catch (Exception e) {
            Alert.error(this, e.getMessage());
        }
    }

    private List<String> getAllSystemPrivileges() {
        List<String> list = new ArrayList<>();
        for (Field field : Privileges.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
                try {
                    list.add((String) field.get(null));
                } catch (IllegalAccessException ignored) {}
            }
        }
        return list;
    }

    private void deleteRole(RoleDto dto) {
        if (Alert.confirm(this, "Supprimer le rôle " + dto.libelle() + " ?")) {
            roleService.deleteRole(dto.id());
            loadData();
        }
    }

    // ================= RENDERERS =================

    class RoleActionsPanel extends JPanel {
        final JButton btnEdit = createBtn("/static/icons/edit.png", new Color(241, 196, 15));
        final JButton btnList = createBtn("/static/icons/open.png", new Color(52, 152, 219));
        final JButton btnDel = createBtn("/static/icons/delete.png", new Color(231, 76, 60));
        RoleActionsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(false);
            add(btnList); add(btnEdit); add(btnDel);
        }
        private JButton createBtn(String p, Color h) {
            JButton b = new JButton(ImageTools.loadIcon(p, 20, 20));
            b.setContentAreaFilled(false); b.setBorderPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // Hover logic similar to other panels
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    b.setContentAreaFilled(true); b.setBackground(new Color(h.getRed(), h.getGreen(), h.getBlue(), 40));
                }
                public void mouseExited(java.awt.event.MouseEvent e) { b.setContentAreaFilled(false); }
            });
            return b;
        }
    }

    class RoleActionsRenderer extends DefaultTableCellRenderer {
        private final RoleActionsPanel p = new RoleActionsPanel();
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return p;
        }
    }

    class RoleActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final RoleActionsPanel p = new RoleActionsPanel();
        private RoleDto current;
        RoleActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); openEditor(current); }); // "List" effectively opens edit/view mode in this design
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); openEditor(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); deleteRole(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (RoleDto) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}