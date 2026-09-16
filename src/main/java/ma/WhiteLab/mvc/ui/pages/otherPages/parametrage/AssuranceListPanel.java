package ma.WhiteLab.mvc.ui.pages.otherPages.parametrage;

import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.mvc.ui.palette.alert.Alert;
import ma.WhiteLab.service.modules.referentiel.api.ReferentielService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AssuranceListPanel extends JPanel {

    private final ReferentielService service;
    private JPanel listPanel;

    public AssuranceListPanel(ReferentielService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        
        listPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        listPanel.setBackground(Color.WHITE);
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JButton btnSave = new JButton("Enregistrer Configuration");
        btnSave.setBackground(new Color(52, 152, 219));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> Alert.info(this, "Configuration sauvegardée (Simulation)."));
        add(btnSave, BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("Gestion des Assurances Acceptées");
        lbl.setFont(new Font("Optima", Font.BOLD, 20));
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private void loadData() {
        listPanel.removeAll();
        List<Assurance> all = service.getAllAssurances();
        
        for (Assurance a : all) {
            JCheckBox cb = new JCheckBox(a.name());
            cb.setOpaque(false);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cb.setSelected(true); // Default to all accepted for prototype
            listPanel.add(cb);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
