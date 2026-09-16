package ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api;

import javax.swing.*;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;

import java.util.List;

public interface DossiersController {

    JPanel getView(UserPrincipal principal);

    JPanel getDossierDetailView(Long dossierId, UserPrincipal principal);

    JPanel getDossierByPatientId(Long patientId, UserPrincipal principal);

}