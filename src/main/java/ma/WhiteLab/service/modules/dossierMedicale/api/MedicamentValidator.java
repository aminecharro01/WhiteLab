package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.dossierMedical.Medicament;

import java.util.Map;

/**
 * Validateur dédié aux opérations sur les médicaments du référentiel.
 * Retourne une Map des erreurs par champ (clé = nom du champ logique, valeur = message).
 * Map vide = validation OK.
 */
public interface MedicamentValidator {

    /**
     * Valide la création d'un nouveau médicament.
     *
     * @param medicament le médicament à créer
     * @return Map des erreurs (vide si OK)
     */
    Map<String, String> validateCreation(Medicament medicament);

    /**
     * Valide la mise à jour d'un médicament existant.
     *
     * @param medicament le médicament à mettre à jour (doit avoir un ID)
     * @return Map des erreurs (vide si OK)
     */
    Map<String, String> validateUpdate(Medicament medicament);

    /**
     * Valide les champs communs (utilisé par création et mise à jour).
     *
     * @param medicament le médicament
     * @param isUpdate   true si mise à jour (l'ID est requis), false pour création
     * @return Map des erreurs
     */
    Map<String, String> validateCommonFields(Medicament medicament, boolean isUpdate);
}