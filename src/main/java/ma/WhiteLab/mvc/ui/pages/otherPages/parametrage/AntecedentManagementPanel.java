package ma.WhiteLab.mvc.ui.pages.otherPages.parametrage;

import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.entities.enums.NiveauDeRisk;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.dto.AntecedentDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class AntecedentManagementPanel extends JPanel {

    private final AntecedentService service;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private JPanel listView;
    private JPanel formView;

    private JTable table;
    private DefaultTableModel tableModel;

    // Form
    private JTextField tfNom;
    private JComboBox<CategorieAntecedent> cbCat;
    private JComboBox<NiveauDeRisk> cbRisk;
    private JTextArea taDesc;
    private AntecedentDTO currentEditing;

    public AntecedentManagementPanel(AntecedentService service) {
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
        JButton btnAdd = new JButton("Ajouter Antécédent");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> openForm(null));

        bar.add(new JLabel("Catalogue Antécédents"), BorderLayout.WEST);
        bar.add(btnAdd, BorderLayout.EAST);

        listView.add(bar, BorderLayout.NORTH);
        listView.add(new JScrollPane(buildTable()), BorderLayout.CENTER);
    }

    private JTable buildTable() {
        String[] cols = {"ID", "Nom", "Catégorie", "Risque", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionsEditor());

        return table;
    }

    private void initFormView() {
        formView = new JPanel(new BorderLayout(0, 20));
        formView.setBackground(Color.WHITE);
        formView.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Détails Antécédent");
        title.setFont(new Font("Optima", Font.BOLD, 24));
        JButton btnBack = new JButton("Retour");
        btnBack.addActionListener(e -> cardLayout.show(container, "LIST"));
        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        formView.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(0, 2, 15, 15));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        tfNom = new JTextField();
        cbCat = new JComboBox<>(CategorieAntecedent.values());
        cbRisk = new JComboBox<>(NiveauDeRisk.values());
        taDesc = new JTextArea();
        taDesc.setRows(3);

        form.add(new JLabel("Nom:")); form.add(tfNom);
        form.add(new JLabel("Catégorie:")); form.add(cbCat);
        form.add(new JLabel("Niveau Risque:")); form.add(cbRisk);
        form.add(new JLabel("Description:")); form.add(new JScrollPane(taDesc));

        formView.add(new JScrollPane(form), BorderLayout.CENTER);

        // Footer
        JButton btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(52, 152, 219));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> save());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnSave);
        formView.add(footer, BorderLayout.SOUTH);
    }

    private void openForm(AntecedentDTO dto) {
        currentEditing = dto;
        boolean isNew = (dto == null);
        
        tfNom.setText(isNew ? "" : dto.getNom());
        taDesc.setText(isNew ? "" : dto.getDescription());
        if (!isNew && dto.getCategorie() != null) cbCat.setSelectedItem(CategorieAntecedent.valueOf(dto.getCategorie()));
        if (!isNew && dto.getNiveauRisque() != null) cbRisk.setSelectedItem(NiveauDeRisk.valueOf(dto.getNiveauRisque()));

        cardLayout.show(container, "FORM");
    }

    private void save() {
        try {
            AntecedentDTO target = (currentEditing == null) ? new AntecedentDTO() : currentEditing;
            target.setNom(tfNom.getText());
            target.setCategorie(((CategorieAntecedent)cbCat.getSelectedItem()).name());
            target.setNiveauRisque(((NiveauDeRisk)cbRisk.getSelectedItem()).name());
            target.setDescription(taDesc.getText());
            if(target.getId() == null) target.setCreePar("admin");
            target.setModifierPar("admin");

            if (target.getId() == null) service.createAntecedent(target);
            else service.updateAntecedent(target.getId(), target);

            Alert.success(this, "Antécédent enregistré !");
            loadData();
            cardLayout.show(container, "LIST");
        } catch (Exception ex) {
            Alert.error(this, ex.getMessage());
        }
    }

    private void delete(AntecedentDTO dto) {
        if (Alert.confirm(this, "Supprimer " + dto.getNom() + " ?")) {
            try {
                service.deleteAntecedent(dto.getId());
                loadData();
            } catch (Exception e) { Alert.error(this, e.getMessage()); }
        }
    }

    private void loadData() {
        populate(service.getAllAntecedents());
    }

    private void populate(List<AntecedentDTO> list) {
        tableModel.setRowCount(0);
        for (AntecedentDTO a : list) {
            tableModel.addRow(new Object[]{a.getId(), a.getNom(), a.getCategorie(), a.getNiveauRisque(), a});
        }
    }

    // ================= RENDERERS =================

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
        private AntecedentDTO current;
        ActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); delete(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (AntecedentDTO) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}