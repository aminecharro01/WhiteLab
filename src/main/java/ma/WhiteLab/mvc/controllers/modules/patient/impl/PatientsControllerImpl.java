package ma.WhiteLab.mvc.controllers.modules.patient.impl;

import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.controllers.modules.patient.api.PatientsController;
import ma.WhiteLab.mvc.ui.pages.otherPages.PatientFormPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.PatientsPanel;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.dto.PatientDTO;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PatientsControllerImpl implements PatientsController {

    private final PatientService patientService;
    private final DossiersController dossiersController;
    private final DossierMedicalService dossierMedicalService;

    private PatientsPanel cachedPanel;
    private List<PatientDTO> patients = new ArrayList<>();

    public PatientsControllerImpl(
            PatientService patientService,
            DossiersController dossiersController,
            DossierMedicalService dossierMedicalService) {
        this.patientService = patientService;
        this.dossiersController = dossiersController;
        this.dossierMedicalService = dossierMedicalService;
    }

    // ================= VIEW =================

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cachedPanel == null) {
            patients = patientService.getAllPatients();

            cachedPanel = new PatientsPanel(
                    this,
                    patientService,
                    dossierMedicalService,
                    patients,
                    principal
            );
            cachedPanel.setName("PatientsPanel");
        }
        return cachedPanel;
    }

    // ================= CRUD NAVIGATION =================

    /**
     * Ouvrir le formulaire de création d'un patient
     */
    public void openCreatePatient(UserPrincipal principal) {
        if (principal == null) return;

        PatientFormPanel formPanel = new PatientFormPanel(
                null,                       // dto = null → création
                principal,
                patientService,
                dossierMedicalService,
                this
        );

        switchView(formPanel);
    }

    /**
     * Ouvrir le formulaire d'édition d'un patient
     */
    public void openEditPatient(Long patientId, UserPrincipal principal) {
        if (principal == null || patientId == null) return;

        PatientDTO dto = patientService.getPatientById(patientId);
        if (dto == null) {
            JOptionPane.showMessageDialog(
                    cachedPanel,
                    "Patient introuvable.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        PatientFormPanel formPanel = new PatientFormPanel(
                dto,                    // dto existant → édition
                principal,
                patientService,
                dossierMedicalService,
                this
        );

        switchView(formPanel);
    }

    /**
     * Ouvrir le dossier médical du patient
     */
    public void openPatientDossier(Long patientId, UserPrincipal principal) {
        if (principal == null || patientId == null) return;

        JPanel dossierPanel = dossiersController.getDossierByPatientId(patientId, principal);
        switchView(dossierPanel);
    }

    // ================= REFRESH =================

    public void refresh() {
        patients = patientService.getAllPatients();
        if (cachedPanel != null) {
            cachedPanel.refreshData(patients);
        }
    }

    // ================= NAVIGATION CORE =================

    /**
     * Remplace le contenu du container parent par un nouveau panel
     */
    private void switchView(JPanel newPanel) {
        SwingUtilities.invokeLater(() -> {
            if (cachedPanel == null) return;

            Container parent = cachedPanel.getParent();
            if (parent instanceof JPanel container) {
                container.removeAll();
                container.add(newPanel, BorderLayout.CENTER);
                container.revalidate();
                container.repaint();
            }
        });
    }

    // ================= CLEAN =================

    public void dispose() {
        cachedPanel = null;
        patients.clear();
    }
}