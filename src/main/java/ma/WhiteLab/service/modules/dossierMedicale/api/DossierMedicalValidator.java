package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;

import java.util.Map;

public interface DossierMedicalValidator {

    Map<String, String> validateCreation(Long patientId, Long medecinId, String creePar);


    Map<String, String> validateUpdateHistorique(Long dossierId, String nouveauHistorique, String modifierPar);

    /**
     * Valide un dossier médical complet (utilisé pour update global si besoin futur).
     *
     * @param dossier le dossier à valider
     * @return Map des erreurs (vide si OK)
     */
    Map<String, String> validateDossier(DossierMedical dossier);
}