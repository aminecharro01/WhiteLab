package ma.WhiteLab.mvc.controllers.modules.dossierMedicale.impl;

import ma.WhiteLab.entities.dossierMedical.*;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.DossierDetailPanel;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.certificat.api.CertificatService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.dossierMedicale.api.*;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.api.PatientService;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Implementation of the DossiersController interface.
 */
public class DossiersControllerImpl implements DossiersController {

    private final DossierMedicalService dossierMedicalService;
    private final OrdonnanceService ordonnanceService;
    private final PrescriptionService prescriptionService;
    private final CertificatService certificatService;
    private final AntecedentService antecedentService;
    private final RendezVousService rendezVousService;
    private final MedicamentService medicamentService;
    private final PatientService patientService;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionMedecinService;
    private final SituationFinanciereService situationFinanciereService;

    public DossiersControllerImpl(
            DossierMedicalService dossierMedicalService,
            OrdonnanceService ordonnanceService,
            PrescriptionService prescriptionService,
            CertificatService certificatService,
            AntecedentService antecedentService,
            RendezVousService rendezVousService,
            MedicamentService medicamentService,
            PatientService patientService,
            ConsultationService consultationService,
            InterventionMedecinService interventionMedecinService, SituationFinanciereService situationFinanciereService) {

        this.dossierMedicalService = dossierMedicalService;
        this.ordonnanceService = ordonnanceService;
        this.prescriptionService = prescriptionService;
        this.certificatService = certificatService;
        this.antecedentService = antecedentService;
        this.rendezVousService = rendezVousService;
        this.medicamentService = medicamentService;
        this.patientService = patientService;
        this.consultationService = consultationService;
        this.interventionMedecinService = interventionMedecinService;
        this.situationFinanciereService = situationFinanciereService;
    }

    // ===================== VIEWS =====================

    @Override
    public JPanel getView(UserPrincipal principal) {
        return createInfoPanel(
                "<div style='font-size:20px; color:#3498db;'>Gestion des Dossiers Médicaux</div><br>" +
                        "La liste globale des dossiers a été supprimée pour des raisons de confidentialité.<br><br>" +
                        "Utilisez la recherche ou la liste des patients pour ouvrir un dossier."
        );
    }

    @Override
    public JPanel getDossierDetailView(Long dossierId, UserPrincipal principal) {
        if (principal == null) {
            return createErrorPanel("Session expirée ou utilisateur non identifié.");
        }

        try {
            Optional<DossierMedical> dossierOpt = dossierMedicalService.getDossierById(dossierId);
            if (dossierOpt.isEmpty()) {
                return createErrorPanel("Dossier médical introuvable (ID: " + dossierId + ")");
            }

            DossierMedical dossier = dossierOpt.get();

            boolean isAdmin      = principal.roles().contains(RoleR.ADMIN);
            boolean isMedecin    = principal.roles().contains(RoleR.MEDECIN);
            boolean isSecretaire = principal.roles().contains(RoleR.SECRETAIRE);
            boolean isOwner      = dossier.getMedecine() != null &&
                    dossier.getMedecine().getId().equals(principal.id());

            boolean hasAccess = isAdmin || isMedecin || isOwner || isSecretaire;
            if (!hasAccess) {
                return createErrorPanel("Accès refusé.<br>Vous n'êtes pas autorisé à consulter ce dossier.");
            }

            boolean readOnly = !isMedecin;

            return new DossierDetailPanel(
                    dossier,
                    dossierMedicalService,
                    prescriptionService,
                    certificatService,
                    antecedentService,
                    rendezVousService,
                    patientService,
                    medicamentService,
                    this,
                    consultationService,          // ✅ ADDED
                    interventionMedecinService,
                    situationFinanciereService,
                    principal,
                    readOnly
            );

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorPanel("Erreur lors du chargement du dossier :<br>" + e.getMessage());
        }
    }

    @Override
    public JPanel getDossierByPatientId(Long patientId, UserPrincipal principal) {
        if (principal == null) {
            return createErrorPanel("Utilisateur non authentifié.");
        }

        try {
            Optional<DossierMedical> dossierOpt = dossierMedicalService.getDossierByPatientId(patientId);

            DossierMedical dossier = dossierOpt.orElseGet(() -> {
                if (!principal.roles().contains(RoleR.MEDECIN)) {
                    throw new SecurityException("Seul un médecin peut créer un nouveau dossier médical.");
                }
                return dossierMedicalService.createDossierForPatient(
                        patientId,
                        principal.id(),
                        principal.fullName()
                );
            });

            return getDossierDetailView(dossier.getId(), principal);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorPanel(
                    "Impossible d'ouvrir ou créer le dossier du patient :<br>" +
                            e.getMessage().replace("\n", "<br>")
            );
        }
    }

    // ===================== CRUD CALLBACKS =====================

    public void createOrdonnance(Ordonnance o) {
        ordonnanceService.create(o);
    }

    public void updateOrdonnance(Ordonnance o) {
        ordonnanceService.update(o);
    }

    public void deleteOrdonnance(Long id) {
        ordonnanceService.delete(id);
    }

    public void createPrescription(Prescription p) {
        prescriptionService.create(p);
    }

    public void updatePrescription(Prescription p) {
        prescriptionService.update(p);
    }

    public void deletePrescription(Long id) {
        prescriptionService.delete(id);
    }

    // ===================== UTILS =====================

    private JPanel createErrorPanel(String message) {
        return createStyledPanel(message, "#e74c3c");
    }

    private JPanel createInfoPanel(String message) {
        return createStyledPanel(message, "#3498db");
    }

    private JPanel createStyledPanel(String htmlMessage, String color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JLabel label = new JLabel(
                "<html><div style='text-align:center; font-size:17px; line-height:1.6; color:" +
                        color + ";'>" + htmlMessage + "</div></html>",
                SwingConstants.CENTER
        );

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
