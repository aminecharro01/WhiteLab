package ma.WhiteLab.mvc.ui.pages.otherPages.parametrage;

import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.mvc.ui.palette.utils.ImageTools;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeMedicalService;
import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class ActeManagementPanel extends JPanel {

    private final ActeMedicalService service;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private JPanel listView;
    private JPanel formView;

    private JTable table;
    private DefaultTableModel tableModel;
    
    // Form
    private JTextField tfLib, tfCat, tfPrix;
    private ActeDTO currentEditing;

    public ActeManagementPanel(ActeMedicalService service) {
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
        JButton btnAdd = new JButton("Ajouter Acte");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> openForm(null));

        bar.add(new JLabel("Catalogue Actes Médicaux"), BorderLayout.WEST);
        bar.add(btnAdd, BorderLayout.EAST);

        listView.add(bar, BorderLayout.NORTH);
        listView.add(new JScrollPane(buildTable()), BorderLayout.CENTER);
    }

    private JTable buildTable() {
        String[] cols = {"ID", "Libellé", "Catégorie", "Prix Base (DH)", "Actions"};
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
        JLabel title = new JLabel("Détails Acte");
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

        tfLib = new JTextField();
        tfCat = new JTextField();
        tfPrix = new JTextField();

        form.add(new JLabel("Libellé:")); form.add(tfLib);
        form.add(new JLabel("Catégorie:")); form.add(tfCat);
        form.add(new JLabel("Prix Base:")); form.add(tfPrix);

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

    private void openForm(ActeDTO dto) {
        currentEditing = dto;
        boolean isNew = (dto == null);
        tfLib.setText(isNew ? "" : dto.getLibelle());
        tfCat.setText(isNew ? "" : dto.getCategorie());
        tfPrix.setText(isNew ? "" : String.valueOf(dto.getPrixDeBase()));
        cardLayout.show(container, "FORM");
    }

    private void save() {
        try {
            ActeDTO target = (currentEditing == null) ? new ActeDTO() : currentEditing;
            target.setLibelle(tfLib.getText());
            target.setCategorie(tfCat.getText());
            target.setPrixDeBase(Float.parseFloat(tfPrix.getText()));
            // Mock creator
            target.setCreePar("admin"); 

            if (target.getId() == null) service.createActe(target);
            else service.updateActe(target.getId(), target);

            Alert.success(this, "Acte enregistré !");
            loadData();
            cardLayout.show(container, "LIST");
        } catch (Exception ex) {
            Alert.error(this, ex.getMessage());
        }
    }

    private void delete(ActeDTO dto) {
        if (Alert.confirm(this, "Supprimer l'acte " + dto.getLibelle() + " ?")) {
            try {
                service.deleteActe(dto.getId());
                loadData();
            } catch (Exception e) { Alert.error(this, e.getMessage()); }
        }
    }

    private void loadData() {
        populate(service.getAllActes());
    }

    private void populate(List<ActeDTO> list) {
        tableModel.setRowCount(0);
        for (ActeDTO a : list) {
            tableModel.addRow(new Object[]{a.getId(), a.getLibelle(), a.getCategorie(), a.getPrixDeBase(), a});
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
        private ActeDTO current;
        ActionsEditor() {
            p.btnList.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnEdit.addActionListener(e -> { fireEditingStopped(); openForm(current); });
            p.btnDel.addActionListener(e -> { fireEditingStopped(); delete(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            current = (ActeDTO) v; return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }
}