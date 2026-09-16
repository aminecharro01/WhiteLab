package ma.WhiteLab.service.modules.auth.impl;

import ma.WhiteLab.service.modules.auth.api.CredentialsValidator;
import ma.WhiteLab.mvc.dto.auth.AuthRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CredentialsValidatorImpl implements CredentialsValidator {

    // Configuration des règles de mot de passe
    private static final int MIN_PASSWORD_LENGTH = 8;

    // Regex email robuste (conforme à la plupart des standards)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Règles de complexité du mot de passe
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    @Override
    public Map<String, String> validate(AuthRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request == null) {
            errors.put("global", "Requête d'authentification nulle");
            return errors;
        }

        String login = request.getLogin();
        String password = request.getPassword();

        // Validation du login (email)
        if (login == null || login.isBlank()) {
            errors.put("login", "L'email est requis");
        } else {
            String trimmedLogin = login.trim();
            if (!EMAIL_PATTERN.matcher(trimmedLogin).matches()) {
                errors.put("login", "Format d'email invalide");
            }
        }

        // Validation du mot de passe (pour login)
        if (password == null || password.isBlank()) {
            errors.put("password", "Le mot de passe est requis");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.put("password", "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
        }

        return errors;
    }

    @Override
    public Map<String, String> validateNewPassword(String newPassword) {
        Map<String, String> errors = new HashMap<>();

        if (newPassword == null || newPassword.isBlank()) {
            errors.put("newPassword", "Le nouveau mot de passe est requis");
            return errors;
        }

        String pwd = newPassword.trim();

        if (pwd.length() < MIN_PASSWORD_LENGTH) {
            errors.put("newPassword", "Doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
        }

        if (!HAS_UPPERCASE.matcher(pwd).matches()) {
            errors.put("newPassword", "Doit contenir au moins une majuscule");
        }

        if (!HAS_LOWERCASE.matcher(pwd).matches()) {
            errors.put("newPassword", "Doit contenir au moins une minuscule");
        }

        if (!HAS_DIGIT.matcher(pwd).matches()) {
            errors.put("newPassword", "Doit contenir au moins un chiffre");
        }

        if (!HAS_SPECIAL_CHAR.matcher(pwd).matches()) {
            errors.put("newPassword", "Doit contenir au moins un caractère spécial (!@#$%^&*...)");
        }

        // Optionnel : empêcher que le mot de passe contienne des espaces
        if (pwd.contains(" ")) {
            errors.put("newPassword", "Ne doit pas contenir d'espaces");
        }

        return errors;
    }
}