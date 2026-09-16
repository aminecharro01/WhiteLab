package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class CabinetsPanel extends JPanel {

    private final UserPrincipal principal;
    private final CabinetMedicalService service;
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    // Views
    private JPanel listView;
    private JPanel formView;
    private JPanel detailsView;

    // List Components
    private DefaultTableModel tableModel;
    private JTable table;

    // Form Components
    private JTextField tfNom, tfEmail, tfTel1, tfTel2, tfCategorie;
    private CabinetMedicale currentEditing;

    // Details Components
    private JLabel lblNom, lblEmail, lblTel, lblCat, lblRefData;

    public CabinetsPanel(UserPrincipal principal, CabinetMedicalService service) {
        this.principal = principal;
        this.service = service;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initListView();
        initFormView();
        initDetailsView();

        container.add(listView, "LIST");
        container.add(formView, "FORM");
        container.add(detailsView, "DETAILS");

        add(container, BorderLayout.CENTER);

        loadData();
    }

    private void initListView() {
        listView = new JPanel(new BorderLayout(0, 10));
        listView.setBackground(Color.WHITE);

        // Header
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        
        JLabel title = new JLabel("Gestion des Cabinets");
        title.setFont(new Font("Optima", Font.BOLD, 24));
        
        JButton btnAdd = new JButton("Nouveau Cabinet");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> openForm(null));

        bar.add(title, BorderLayout.WEST);
        bar.add(btnAdd, BorderLayout.EAST);

        listView.add(bar, BorderLayout.NORTH);
        listView.add(new JScrollPane(buildTable()), BorderLayout.CENTER);
    }

    private JTable buildTable() {
        String[] cols = {"ID", "Nom", "Catégorie", "Email", "Téléphone", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionsEditor());

        return table;
    }

    private void initFormView() {
        formView = new JPanel(new BorderLayout(0, 20));
        formView.setBackground(Color.WHITE);
        formView.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Formulaire Cabinet");
        title.setFont(new Font("Optima", Font.BOLD, 24));
        
        JButton btnBack = new JButton("Retour");
        btnBack.addActionListener(e -> cardLayout.show(container, "LIST"));
        
        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        
        formView.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(0, 2, 15, 15));
        form.setOpaque(false);

        tfNom = new JTextField();
        tfCategorie = new JTextField();
        tfEmail = new JTextField();
        tfTel1 = new JTextField();
        tfTel2 = new JTextField();

        form.add(new JLabel("Nom Cabinet:")); form.add(tfNom);
        form.add(new JLabel("Catégorie:")); form.add(tfCategorie);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Téléphone 1:")); form.add(tfTel1);
        form.add(new JLabel("Téléphone 2:")); form.add(tfTel2);

        formView.add(new JScrollPane(form), BorderLayout.CENTER);

        // Footer
        JButton btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(52, 152, 219));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> save());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnSave);
        formView.add(footer, BorderLayout.SOUTH);
    }

    private void initDetailsView() {
        detailsView = new JPanel(new BorderLayout(0, 20));
        detailsView.setBackground(Color.WHITE);
        detailsView.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Détails du Cabinet");
        title.setFont(new Font("Optima", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        
        JButton btnBack = new JButton("Retour");
        btnBack.setBackground(new Color(52, 152, 219));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> cardLayout.show(container, "LIST"));
        
        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        detailsView.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 5, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize Labels
        lblNom = new JLabel(); lblNom.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblCat = new JLabel(); lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblEmail = new JLabel(); lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTel = new JLabel(); lblTel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblRefData = new JLabel(); lblRefData.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Section 1: Info
        addSectionTitle(content, gbc, "Informations Générales", 0);
        addRow(content, gbc, "Nom du Cabinet:", lblNom, 1);
        addRow(content, gbc, "Catégorie:", lblCat, 2);
        addRow(content, gbc, "Email:", lblEmail, 3);
        addRow(content, gbc, "Téléphone:", lblTel, 4);

        // Section 2: Config
        addSectionTitle(content, gbc, "Configuration & Référentiel", 5);
        addRow(content, gbc, "Statut:", lblRefData, 6);
        
        // Push content to top
        gbc.weighty = 1.0;
        gbc.gridy = 7;
        content.add(new JLabel(), gbc);

        detailsView.add(new JScrollPane(content), BorderLayout.CENTER);
    }

    private void addSectionTitle(JPanel p, GridBagConstraints gbc, String text, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(new Color(52, 152, 219));
        l.setBorder(new EmptyBorder(20, 0, 10, 0));
        p.add(l, gbc);
        gbc.gridwidth = 1; // Reset
    }

    private void addRow(JPanel p, GridBagConstraints gbc, String key, JLabel val, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        JLabel lKey = new JLabel(key);
        lKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lKey.setForeground(Color.GRAY);
        p.add(lKey, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(val, gbc);
    }

    // Logic

    private void loadData() {
        tableModel.setRowCount(0);
        List<CabinetMedicale> list = service.findAll();
        for (CabinetMedicale c : list) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getNom(), c.getCategorie(), c.getEmail(), c.getTel1(), c
            });
        }
    }

    private void openForm(CabinetMedicale c) {
        currentEditing = c;
        boolean isNew = (c == null);
        
        tfNom.setText(isNew ? "" : c.getNom());
        tfCategorie.setText(isNew ? "" : c.getCategorie());
        tfEmail.setText(isNew ? "" : c.getEmail());
        tfTel1.setText(isNew ? "" : c.getTel1());
        tfTel2.setText(isNew ? "" : c.getTel2());

        cardLayout.show(container, "FORM");
    }

    private void showDetails(CabinetMedicale c) {
        lblNom.setText(c.getNom());
        lblCat.setText(c.getCategorie());
        lblEmail.setText(c.getEmail());
        String t = c.getTel1();
        if(c.getTel2() != null && !c.getTel2().isBlank()) t += " / " + c.getTel2();
        lblTel.setText(t);
        
        lblRefData.setText("<html>Ce cabinet utilise le <b>Référentiel Global</b>.<br>" +
                "<font color='gray'>Actes, Médicaments et Antécédents sont partagés avec tous les cabinets.</font></html>");

        cardLayout.show(container, "DETAILS");
    }

    private void save() {
        try {
            CabinetMedicale target = (currentEditing == null) ? new CabinetMedicale() : currentEditing;
            target.setNom(tfNom.getText());
            target.setCategorie(tfCategorie.getText());
            target.setEmail(tfEmail.getText());
            target.setTel1(tfTel1.getText());
            target.setTel2(tfTel2.getText());

            if (target.getId() == null) service.create(target, principal.login());
            else service.update(target, principal.login());

            Alert.success(this, "Cabinet enregistré !");
            loadData();
            cardLayout.show(container, "LIST");
        } catch (Exception e) {
            Alert.error(this, "Erreur: " + e.getMessage());
        }
    }

    private void delete(CabinetMedicale c) {
        if (Alert.confirm(this, "Supprimer le cabinet " + c.getNom() + " ?")) {
            try {
                service.deleteById(c.getId());
                loadData();
            } catch (Exception e) { Alert.error(this, e.getMessage()); }
        }
    }

    // Renderers

    class ActionsPanel extends JPanel {
        final JButton btnList = createBtn("/static/icons/open.png");
        final JButton btnEdit = createBtn("/static/icons/edit.png");
        final JButton btnDel = createBtn("/static/icons/delete.png");
        ActionsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(false);
            add(btnList); add(btnEdit); add(btnDel);
        }
        private JButton createBtn(String p) {
            JButton b = new JButton(ImageTools.loadIcon(p, 20, 20));
            b.setContentAreaFilled(false); b.setBorderPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    class ActionsRenderer extends DefaultTableCellRenderer {
        private final ActionsPanel p = new ActionsPanel();
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return p; }
    }

    class ActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final ActionsPanel p = new ActionsPanel();
        private CabinetMedicale current;
        ActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); showDetails(current); });
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); delete(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (CabinetMedicale) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}