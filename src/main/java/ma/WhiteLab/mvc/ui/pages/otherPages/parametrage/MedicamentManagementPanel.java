package ma.WhiteLab.mvc.ui.pages.otherPages.parametrage;

import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.enums.Forme;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class MedicamentManagementPanel extends JPanel {

    private final MedicamentService service;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    // Views
    private JPanel listView;
    private JPanel formView;

    // List Components
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // Form Components
    private JTextField tfNom, tfLabo, tfType, tfPrix;
    private JComboBox<Forme> cbForme;
    private JCheckBox chkRem;
    private Medicament currentEditing; // null if new

    public MedicamentManagementPanel(MedicamentService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initListView();
        initFormView();

        container.add(listView, "LIST");
        container.add(formView, "FORM");
        add(container, BorderLayout.CENTER);

        loadData();
    }

    private void initListView() {
        listView = new JPanel(new BorderLayout(0, 10));
        listView.setBackground(Color.WHITE);
        listView.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher médicament...");
        searchField.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isBlank()) loadData();
            else populate(service.searchByNameLike(q));
        });

        JButton btnAdd = new JButton("Ajouter Médicament");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.addActionListener(e -> openForm(null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(searchField);
        right.add(btnAdd);

        bar.add(new JLabel("Catalogue Médicaments"), BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        listView.add(bar, BorderLayout.NORTH);
        listView.add(new JScrollPane(buildTable()), BorderLayout.CENTER);
    }

    private JTable buildTable() {
        String[] cols = {"ID", "Nom", "Laboratoire", "Type", "Forme", "Prix (DH)", "Remboursable", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionsEditor());

        return table;
    }

    private void initFormView() {
        formView = new JPanel(new BorderLayout(0, 20));
        formView.setBackground(Color.WHITE);
        formView.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Détails Médicament");
        title.setFont(new Font("Optima", Font.BOLD, 24));
        
        JButton btnBack = new JButton("Retour");
        btnBack.addActionListener(e -> cardLayout.show(container, "LIST"));
        
        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        
        formView.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(0, 2, 15, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        tfNom = new JTextField();
        tfLabo = new JTextField();
        tfType = new JTextField();
        cbForme = new JComboBox<>(Forme.values());
        tfPrix = new JTextField();
        chkRem = new JCheckBox("Remboursable");
        chkRem.setOpaque(false);

        form.add(new JLabel("Nom:")); form.add(tfNom);
        form.add(new JLabel("Laboratoire:")); form.add(tfLabo);
        form.add(new JLabel("Type:")); form.add(tfType);
        form.add(new JLabel("Forme:")); form.add(cbForme);
        form.add(new JLabel("Prix Unitaire (DH):")); form.add(tfPrix);
        form.add(new JLabel("Statut:")); form.add(chkRem);

        formView.add(new JScrollPane(form), BorderLayout.CENTER);

        // Footer Actions
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

    private void openForm(Medicament m) {
        currentEditing = m;
        boolean isNew = (m == null);
        
        tfNom.setText(isNew ? "" : m.getNom());
        tfLabo.setText(isNew ? "" : m.getLabo());
        tfType.setText(isNew ? "" : m.getType());
        tfPrix.setText(isNew ? "" : String.valueOf(m.getPrixUnitaire()));
        cbForme.setSelectedItem(isNew ? Forme.COMPRIME : m.getForme());
        chkRem.setSelected(!isNew && m.isRemboursable());

        cardLayout.show(container, "FORM");
    }

    private void save() {
        try {
            Medicament target = (currentEditing == null) ? new Medicament() : currentEditing;
            target.setNom(tfNom.getText());
            target.setLabo(tfLabo.getText());
            target.setType(tfType.getText());
            target.setForme((Forme) cbForme.getSelectedItem());
            target.setPrixUnitaire(Double.parseDouble(tfPrix.getText()));
            target.setRemboursable(chkRem.isSelected());

            if (target.getId() == null) service.create(target);
            else service.update(target);

            Alert.success(this, "Médicament enregistré !");
            loadData();
            cardLayout.show(container, "LIST");
        } catch (Exception ex) {
            Alert.error(this, "Erreur: " + ex.getMessage());
        }
    }

    private void delete(Medicament m) {
        if (Alert.confirm(this, "Voulez-vous vraiment supprimer " + m.getNom() + " ?")) {
            service.delete(m.getId());
            loadData();
        }
    }

    private void loadData() {
        populate(service.getAll());
    }

    private void populate(List<Medicament> list) {
        tableModel.setRowCount(0);
        for (Medicament m : list) {
            tableModel.addRow(new Object[]{
                    m.getId(), m.getNom(), m.getLabo(), m.getType(),
                    m.getForme(), m.getPrixUnitaire(), m.isRemboursable() ? "Oui" : "Non", m
            });
        }
    }

    // ================= RENDERERS & EDITORS =================

    class ActionsPanel extends JPanel {
        final JButton btnList = createBtn("/static/icons/open.png", new Color(52, 152, 219));
        final JButton btnEdit = createBtn("/static/icons/edit.png", new Color(241, 196, 15));
        final JButton btnDel = createBtn("/static/icons/delete.png", new Color(231, 76, 60));

        ActionsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(false);
            add(btnList); add(btnEdit); add(btnDel);
        }

        private JButton createBtn(String path, Color bg) {
            JButton b = new JButton(ImageTools.loadIcon(path, 20, 20));
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    class ActionsRenderer extends DefaultTableCellRenderer {
        private final ActionsPanel p = new ActionsPanel();
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return p;
        }
    }

    class ActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final ActionsPanel p = new ActionsPanel();
        private Medicament current;
        ActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); delete(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (Medicament) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}