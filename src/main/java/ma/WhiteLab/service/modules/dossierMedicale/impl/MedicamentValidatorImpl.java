package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentValidator;

import java.util.HashMap;
import java.util.Map;

public class MedicamentValidatorImpl implements MedicamentValidator {

    private static final int MIN_NOM_LENGTH = 2;
    private static final int MAX_NOM_LENGTH = 150;
    private static final int MAX_LABO_LENGTH = 100;
    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public MedicamentValidatorImpl() {
    }

    @Override
    public Map<String, String> validateCreation(Medicament medicament) {
        return validateCommonFields(medicament, false);
    }

    @Override
    public Map<String, String> validateUpdate(Medicament medicament) {
        return validateCommonFields(medicament, true);
    }

    @Override
    public Map<String, String> validateCommonFields(Medicament medicament, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();

        if (medicament == null) {
            errors.put("global", "Le médicament ne peut pas être null");
            return errors;
        }

        // ID requis uniquement pour update
        if (isUpdate) {
            if (medicament.getId() == null || medicament.getId() <= 0) {
                errors.put("id", "L'ID est requis pour la mise à jour");
            }
        }

        // Nom (obligatoire)
        String nom = medicament.getNom();
        if (nom == null || nom.trim().isBlank()) {
            errors.put("nom", "Le nom du médicament est requis");
        } else {
            String trimmedNom = nom.trim();
            if (trimmedNom.length() < MIN_NOM_LENGTH) {
                errors.put("nom", "Le nom doit contenir au moins " + MIN_NOM_LENGTH + " caractères");
            }
            if (trimmedNom.length() > MAX_NOM_LENGTH) {
                errors.put("nom", "Le nom est trop long (max " + MAX_NOM_LENGTH + " caractères)");
            }
        }

        // Laboratoire
        String labo = medicament.getLabo();
        if (labo != null && !labo.trim().isBlank()) {
            String trimmedLabo = labo.trim();
            if (trimmedLabo.length() > MAX_LABO_LENGTH) {
                errors.put("labo", "Le laboratoire est trop long (max " + MAX_LABO_LENGTH + " caractères)");
            }
        }

        // Type
        String type = medicament.getType();
        if (type != null && !type.trim().isBlank()) {
            String trimmedType = type.trim();
            if (trimmedType.length() > MAX_TYPE_LENGTH) {
                errors.put("type", "Le type est trop long (max " + MAX_TYPE_LENGTH + " caractères)");
            }
        }

        // Forme (obligatoire ? selon ton modèle)
        if (medicament.getForme() == null) {
            errors.put("forme", "La forme pharmaceutique est requise");
        }

        // Prix unitaire
        if (medicament.getPrixUnitaire() <= 0) {
            errors.put("prix_unitaire", "Le prix unitaire doit être supérieur à 0");
        }

        // Description (facultative mais limitée)
        String description = medicament.getDescription();
        if (description != null && !description.trim().isBlank()) {
            String trimmedDesc = description.trim();
            if (trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
                errors.put("description", "La description est trop longue (max " + MAX_DESCRIPTION_LENGTH + " caractères)");
            }
        }

        // Remboursable : pas de validation particulière (boolean)

        return errors;
    }
}