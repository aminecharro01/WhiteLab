package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Ordonnance;

import java.util.Map;

public interface OrdonnanceValidator {

    /**
     * Valide les données lors de la création d'une ordonnance
     */
    Map<String, String> validateCreation(Ordonnance ordonnance);

    /**
     * Valide les données lors de la mise à jour d'une ordonnance
     */
    Map<String, String> validateUpdate(Ordonnance ordonnance);

    /**
     * Validation complète d'une ordonnance (utilisable pour création ou update)
     */
    Map<String, String> validateOrdonnance(Ordonnance ordonnance);
}