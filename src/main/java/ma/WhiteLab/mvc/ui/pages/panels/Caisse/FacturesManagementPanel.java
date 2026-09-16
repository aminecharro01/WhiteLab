package ma.WhiteLab.mvc.ui.pages.panels.Caisse;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.caisse.api.FactureService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.List;

public class FacturesManagementPanel extends JPanel {

    private final FactureService factureService;
    private final UserPrincipal principal;
    private final Runnable onBack;
    private DefaultTableModel model;
    private JTable table;

    public FacturesManagementPanel(FactureService factureService, UserPrincipal principal, Runnable onBack) {
        this.factureService = factureService;
        this.principal = principal;
        this.onBack = onBack;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Gestion des Factures");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(64, 120, 255));
        
        JButton btnBack = new JButton("← Retour");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.addActionListener(e -> onBack.run());

        header.add(title, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Date", "Total", "Payé", "Reste", "Statut", "Consultation ID"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        return new JScrollPane(table);
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);

        JButton btnDelete = new JButton("Supprimer");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteFacture());
        
        JButton btnEdit = new JButton("Modifier");
        btnEdit.addActionListener(e -> editFacture());

        JButton btnView = new JButton("Consulter");
        btnView.addActionListener(e -> viewFacture());

        JButton btnPrint = new JButton("Imprimer");
        btnPrint.setBackground(new Color(40, 167, 69));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> printFacture());

        panel.add(btnDelete);
        panel.add(btnEdit);
        panel.add(btnView);
        panel.add(btnPrint);
        return panel;
    }

    private void loadData() {
        model.setRowCount(0);
        List<Facture> list = factureService.findAll();
        for (Facture f : list) {
            model.addRow(new Object[]{
                f.getId(), f.getDate(), f.getTotalFact(), f.getTotalPaye(), f.getReste(), f.getStatut(),
                f.getConsultation() != null ? f.getConsultation().getId() : "-"
            });
        }
    }

    private void deleteFacture() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        Long id = (Long) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Supprimer ?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                factureService.delete(id);
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
            }
        }
    }

    private void editFacture() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une facture.");
            return;
        }
        Long id = (Long) model.getValueAt(row, 0);
        Facture f = factureService.findById(id);
        if (f == null) return;

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JTextField txtTotal = new JTextField(String.valueOf(f.getTotalFact()));
        txtTotal.setEditable(false); // Read-only to ensure integrity with medical acts
        JTextField txtPaye = new JTextField(String.valueOf(f.getTotalPaye()));
        
        panel.add(new JLabel("Total Facturé (DH) :"));
        panel.add(txtTotal);
        panel.add(new JLabel("Montant Payé (DH) :"));
        panel.add(txtPaye);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Modifier Facture #" + f.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                float newTotal = Float.parseFloat(txtTotal.getText().trim());
                float newPaye = Float.parseFloat(txtPaye.getText().trim());

                f.setTotalFact(newTotal);
                f.setTotalPaye(newPaye);
                f.setModifierPar(principal.fullName());
                
                factureService.update(f);
                loadData();
                JOptionPane.showMessageDialog(this, "Facture mise à jour.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Montants invalides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewFacture() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une facture.");
            return;
        }
        Long id = (Long) model.getValueAt(row, 0);
        Facture f = factureService.findById(id);
        if (f != null) {
            String info = "Facture #" + f.getId() + "\n" +
                    "Date: " + f.getDate() + "\n" +
                    "Consultation: " + (f.getConsultation() != null ? f.getConsultation().getId() : "N/A") + "\n" +
                    "-----------------" + "\n" +
                    "Total: " + f.getTotalFact() + " DH\n" +
                    "Payé: " + f.getTotalPaye() + " DH\n" +
                    "Reste: " + f.getReste() + " DH\n" +
                    "Statut: " + f.getStatut();
            JOptionPane.showMessageDialog(this, info, "Détails Facture", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void printFacture() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une facture à imprimer.");
            return;
        }
        Long id = (Long) model.getValueAt(row, 0);
        Facture f = factureService.findById(id);
        if (f == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("Facture_" + id + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
                
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(path));
                document.open();
                
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                document.add(new Paragraph("FACTURE #" + f.getId(), titleFont));
                document.add(new Paragraph(" ")); // spacer
                document.add(new Paragraph("Date d'émission: " + (f.getDate() != null ? f.getDate() : "N/A")));
                document.add(new Paragraph("Consultation ID: " + (f.getConsultation() != null ? f.getConsultation().getId() : "N/A")));
                document.add(new Paragraph("------------------------------------------------"));
                document.add(new Paragraph("Montant Total: " + f.getTotalFact() + " DH"));
                document.add(new Paragraph("Montant Payé:  " + f.getTotalPaye() + " DH"));
                document.add(new Paragraph("Reste à Payer: " + f.getReste() + " DH"));
                document.add(new Paragraph("------------------------------------------------"));
                document.add(new Paragraph("Statut: " + f.getStatut()));
                
                document.close();
                
                JOptionPane.showMessageDialog(this, "PDF généré : " + path);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur impression PDF: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
