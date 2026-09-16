package ma.WhiteLab.service.modules.profileService.impl;

import ma.WhiteLab.mvc.dto.profileDtos.ChangePasswordRequest;
import ma.WhiteLab.service.modules.profileService.api.ChangePasswordValidator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChangePasswordValidatorImpl implements ChangePasswordValidator {

    // Regex pour les règles de complexité
    private static final Pattern HAS_UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER   = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT   = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");

    // On définit les règles comme des constantes pour éviter d'appeler le Context au démarrage
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;
    private static final boolean REQUIRE_UPPER   = true;
    private static final boolean REQUIRE_LOWER   = true;
    private static final boolean REQUIRE_DIGIT   = true;
    private static final boolean REQUIRE_SPECIAL = true;

    /**
     * Constructeur vide.
     * IMPORTANT : Ne surtout pas appeler ApplicationContext.getInstance() ici.
     */
    public ChangePasswordValidatorImpl() {
    }

    @Override
    public Map<String, String> validate(ChangePasswordRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (req == null) {
            errors.put("_global", "Requête invalide : données manquantes.");
            return errors;
        }

        String current = req.currentPassword();
        String newPass = req.newPassword();
        String confirm = req.confirmPassword();
        Long userId    = req.userId();

        // === Vérifications de base ===
        if (userId == null || userId <= 0) {
            errors.put("_global", "Session utilisateur invalide.");
        }

        if (isBlank(current)) {
            errors.put("currentPassword", "Le mot de passe actuel est obligatoire.");
        }

        if (isBlank(newPass)) {
            errors.put("newPassword", "Le nouveau mot de passe est obligatoire.");
        } else {
            // Validation de la longueur
            if (newPass.length() < MIN_LENGTH) {
                errors.put("newPassword", "Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères.");
            }
            if (newPass.length() > MAX_LENGTH) {
                errors.put("newPassword", "Le mot de passe ne doit pas dépasser " + MAX_LENGTH + " caractères.");
            }

            // Validation de la complexité
            if (REQUIRE_UPPER && !HAS_UPPER.matcher(newPass).find()) {
                errors.put("newPassword", "Doit contenir au moins une majuscule (A-Z).");
            }
            if (REQUIRE_LOWER && !HAS_LOWER.matcher(newPass).find()) {
                errors.put("newPassword", "Doit contenir au moins une minuscule (a-z).");
            }
            if (REQUIRE_DIGIT && !HAS_DIGIT.matcher(newPass).find()) {
                errors.put("newPassword", "Doit contenir au moins un chiffre (0-9).");
            }
            if (REQUIRE_SPECIAL && !HAS_SPECIAL.matcher(newPass).find()) {
                errors.put("newPassword", "Doit contenir au moins un caractère spécial.");
            }
        }

        if (isBlank(confirm)) {
            errors.put("confirmPassword", "La confirmation du mot de passe est obligatoire.");
        } else if (newPass != null && !newPass.equals(confirm)) {
            errors.put("confirmPassword", "Les deux mots de passe ne correspondent pas.");
        }

        // Le nouveau mot de passe doit être différent de l'ancien
        if (current != null && newPass != null && current.equals(newPass)) {
            errors.put("newPassword", "Le nouveau mot de passe doit être différent de l'actuel.");
        }

        return errors;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}