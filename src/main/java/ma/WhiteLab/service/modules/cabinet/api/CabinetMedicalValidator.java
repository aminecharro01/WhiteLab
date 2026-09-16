package ma.WhiteLab.service.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.CabinetMedicale;

/**
 * Interface dédiée à la validation métier des cabinets médicaux.
 * Sépare clairement la logique de validation du service principal (bonne pratique).
 */
public interface CabinetMedicalValidator {

    /**
     * Valide un cabinet avant création
     */
    void validateForCreate(CabinetMedicale cabinet);

    /**
     * Valide un cabinet avant mise à jour
     */
    void validateForUpdate(CabinetMedicale cabinet);

    /**
     * Validation des champs communs à la création et modification
     */
    void validateCommonFields(CabinetMedicale cabinet);

    /**
     * Vérifie l'unicité de l'email (exclut le cabinet avec excludeId s'il existe)
     */
    void checkEmailUnique(String email, Long excludeId);

    /**
     * Vérifie l'unicité du nom
     */
    void checkNomUnique(String nom, Long excludeId);
}