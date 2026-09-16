package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Prescription;

import java.util.Map;

public interface PrescriptionValidator {

    Map<String, String> validateCreation(Prescription prescription);

    Map<String, String> validateUpdate(Prescription prescription);

    Map<String, String> validatePrescription(Prescription prescription);
}