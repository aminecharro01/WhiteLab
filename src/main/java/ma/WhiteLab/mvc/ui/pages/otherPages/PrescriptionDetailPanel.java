package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrescriptionDetailPanel extends JPanel {

    private final JTable prescriptionsTable;
    private final DefaultTableModel model;

    public PrescriptionDetailPanel(Ordonnance ordonnance, PrescriptionService prescriptionService) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Prescriptions pour l'ordonnance du " + (ordonnance.getDateOrdonnance() != null ? ordonnance.getDateOrdonnance().toString() : "-"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(64, 120, 255));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Médicament", "Quantité", "Fréquence", "Durée (jours)", "Prix Unitaire", "Prix Total"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        prescriptionsTable = new JTable(model);
        prescriptionsTable.setRowHeight(40);
        prescriptionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        prescriptionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        prescriptionsTable.getTableHeader().setBackground(new Color(64, 120, 255));
        prescriptionsTable.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(prescriptionsTable), BorderLayout.CENTER);

        loadPrescriptions(ordonnance, prescriptionService);
    }

    private void loadPrescriptions(Ordonnance ordonnance, PrescriptionService service) {
        List<Prescription> prescriptions = ordonnance.getPrescriptions();

        if (prescriptions == null || prescriptions.isEmpty()) {
            model.addRow(new Object[]{"Aucune prescription", "-", "-", "-", "-", "-"});
            return;
        }

        for (Prescription p : prescriptions) {
            String medic = p.getMedicament() != null ? p.getMedicament().getNom() : "Médicament inconnu";
            double prixUnitaire = p.getMedicament() != null ? p.getMedicament().getPrixUnitaire() : 0.0;
            double prixTotal = p.getQte() * prixUnitaire;

            model.addRow(new Object[]{
                    medic,
                    p.getQte(),
                    p.getFrequence(),
                    p.getDuree(),
                    String.format("%.2f", prixUnitaire),
                    String.format("%.2f", prixTotal)
            });
        }
    }
}
