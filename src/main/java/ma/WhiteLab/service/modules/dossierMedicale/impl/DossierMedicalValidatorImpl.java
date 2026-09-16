package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalValidator;

import java.util.HashMap;
import java.util.Map;

public class DossierMedicalValidatorImpl implements DossierMedicalValidator {

    @Override
    public Map<String, String> validateCreation(Long patientId, Long medecinId, String creePar) {
        Map<String, String> errors = new HashMap<>();

        if (patientId == null) {
            errors.put("patientId", "L'ID du patient est obligatoire.");
        }

        if (medecinId == null) {
            errors.put("medecinId", "L'ID du médecin est obligatoire.");
        }

        if (creePar == null || creePar.trim().isEmpty()) {
            errors.put("creePar", "Le champ 'créé par' est obligatoire.");
        } else if (creePar.trim().length() > 100) {
            errors.put("creePar", "Le champ 'créé par' ne peut pas dépasser 100 caractères.");
        }

        return errors;
    }

    @Override
    public Map<String, String> validateUpdateHistorique(Long dossierId, String nouveauHistorique, String modifierPar) {
        Map<String, String> errors = new HashMap<>();

        if (dossierId == null) {
            errors.put("dossierId", "L'ID du dossier médical est obligatoire.");
        }

        if (nouveauHistorique == null) {
            errors.put("nouveauHistorique", "L'historique ne peut pas être null (utilisez une chaîne vide si nécessaire).");
        } else if (nouveauHistorique.length() > 5000) {
            errors.put("nouveauHistorique", "L'historique ne peut pas dépasser 5000 caractères.");
        }

        if (modifierPar == null || modifierPar.trim().isEmpty()) {
            errors.put("modifierPar", "Le champ 'modifié par' est obligatoire.");
        } else if (modifierPar.trim().length() > 100) {
            errors.put("modifierPar", "Le champ 'modifié par' ne peut pas dépasser 100 caractères.");
        }

        return errors;
    }

    @Override
    public Map<String, String> validateDossier(DossierMedical dossier) {
        Map<String, String> errors = new HashMap<>();

        if (dossier == null) {
            errors.put("dossier", "Le dossier médical ne peut pas être null.");
            return errors;
        }

        if (dossier.getPat() == null) {
            errors.put("patient", "Le patient est obligatoire.");
        }

        if (dossier.getMedecine() == null) {
            errors.put("medecin", "Le médecin est obligatoire.");
        }

        if (dossier.getCreePar() == null || dossier.getCreePar().trim().isEmpty()) {
            errors.put("creePar", "Le champ 'créé par' est obligatoire.");
        }

        if (dossier.getHistorique() != null && dossier.getHistorique().length() > 5000) {
            errors.put("historique", "L'historique ne peut pas dépasser 5000 caractères.");
        }

        return errors;
    }
}