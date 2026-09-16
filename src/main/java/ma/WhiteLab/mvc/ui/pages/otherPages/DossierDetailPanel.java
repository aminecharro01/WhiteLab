package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.panels.*;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.certificat.api.CertificatService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.api.PatientService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DossierDetailPanel extends JPanel {

    // --- UI Constants ---
    private static final Color PRIMARY = new Color(64, 120, 255);
    private static final Font TAB_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Services ---
    private final DossierMedical dossier;
    private final DossierMedicalService dossierService;
    private final PrescriptionService prescriptionService;
    private final CertificatService certificatService;
    private final AntecedentService antecedentService;
    private final RendezVousService rendezVousService;
    private final PatientService patientService;
    private final MedicamentService medicamentService;
    private final DossiersController controller;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionMedecinService;
    private final SituationFinanciereService situationFinanciereService;
    private final UserPrincipal principal;
    private final boolean isReadOnly;

    private JTabbedPane tabbedPane;

    public DossierDetailPanel(
            DossierMedical dossier,
            DossierMedicalService dossierService,
            PrescriptionService prescriptionService,
            CertificatService certificatService,
            AntecedentService antecedentService,
            RendezVousService rendezVousService,
            PatientService patientService,
            MedicamentService medicamentService,
            DossiersController controller,
            ConsultationService consultationService,
            InterventionMedecinService interventionMedecinService,
            SituationFinanciereService situationFinanciereService,
            UserPrincipal principal,
            boolean readOnly
    ) {
        this.dossier = dossier;
        this.dossierService = dossierService;
        this.prescriptionService = prescriptionService;
        this.certificatService = certificatService;
        this.antecedentService = antecedentService;
        this.rendezVousService = rendezVousService;
        this.patientService = patientService;
        this.medicamentService = medicamentService;
        this.controller = controller;
        this.consultationService = consultationService;
        this.interventionMedecinService = interventionMedecinService;
        this.situationFinanciereService = situationFinanciereService;
        this.principal = principal;
        this.isReadOnly = readOnly;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    /* ================= INIT ================= */

    private void initComponents() {
        add(buildHeader(), BorderLayout.NORTH);
        tabbedPane = buildTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    /* ================= HEADER ================= */

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        header.add(new PatientHeaderPanel(dossier.getPat()), BorderLayout.CENTER);

        JButton shareBtn = createIconButton(
                "/static/icons/share.png",
                "Télécharger le dossier médical (PDF)"
        );

        shareBtn.addActionListener(e -> downloadPdf());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);
        actions.add(shareBtn);

        header.add(actions, BorderLayout.EAST);
        return header;
    }

    /* ================= TABS ================= */

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(TAB_FONT);

        tabs.addTab("Rendez-vous",
                new RendezVousPanel(dossier, rendezVousService, consultationService));
        tabs.addTab("Antécédents",
                new AntecedentsPanel(dossier, antecedentService, patientService));
        tabs.addTab("Situation Financière",
                new SituationFinancierePanel(dossier, situationFinanciereService, isReadOnly));

        if (!isReadOnly) {
            tabs.addTab("Consultations",
                    new ConsultationsPanel(dossier, consultationService,
                            interventionMedecinService, principal));
            tabs.addTab("Ordonnances",
                    new OrdonnancesPanel(dossier, dossierService,
                            prescriptionService, medicamentService, controller));
            tabs.addTab("Certificats",
                    new CertificatsPanel(dossier, certificatService));
        }

        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof RefreshablePanel r) {
                r.refresh();
            }
        });

        return tabs;
    }

    /* ================= PDF DOWNLOAD ================= */

    private void downloadPdf() {
        try {
            byte[] pdf = dossierService.exportDossierToPdf(dossier.getId());

            if (pdf == null || pdf.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Impossible de générer le PDF",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(
                    new File("dossier_medical_" + dossier.getId() + ".pdf")
            );

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try (FileOutputStream fos =
                         new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(pdf);
            }

            JOptionPane.showMessageDialog(this,
                    "Dossier médical téléchargé avec succès",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur téléchargement PDF :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================= UI HELPERS ================= */

    private JButton createIconButton(String iconPath, String tooltip) {
        JButton btn;
        java.net.URL url = getClass().getResource(iconPath);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(img));
        } else {
            btn = new JButton("⬇");
        }

        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    /* ================= UTIL ================= */

    private <T> List<T> safeList(Supplier<List<T>> loader) {
        try {
            List<T> list = loader.get();
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public interface RefreshablePanel {
        void refresh();
    }
}
