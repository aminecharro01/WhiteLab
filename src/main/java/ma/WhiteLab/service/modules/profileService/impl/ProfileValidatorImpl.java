package ma.WhiteLab.service.modules.profileService.impl;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileUpdateRequest;
import ma.WhiteLab.service.modules.profileService.api.ProfileValidator;

public class ProfileValidatorImpl implements ProfileValidator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public Map<String, String> validate(ProfileUpdateRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (req == null || req.id() == null) {
            errors.put("_global", "Requête invalide.");
            return errors;
        }

        // nom/prenom
        if (isBlank(req.prenom())) errors.put("prenom", "Prénom obligatoire.");
        if (isBlank(req.nom())) errors.put("nom", "Nom obligatoire.");

        // email
        String email = trim(req.email());
        if (isBlank(email)) errors.put("email", "Email obligatoire.");
        else if (!EMAIL.matcher(email).matches()) errors.put("email", "Email invalide.");

        // tel (soft validation)
        if (!isBlank(req.tel()) && req.tel().length() > 30) errors.put("tel", "Téléphone trop long.");

        // cin (soft validation)
        if (!isBlank(req.cin()) && req.cin().length() > 30) errors.put("cin", "CIN trop long.");

        // sexe
        if (req.sexe() == null) errors.put("sexe", "Sexe obligatoire.");

        // staff
        if (req.salaire() != null && req.salaire() < 0) errors.put("salaire", "Salaire invalide.");
        if (req.prime() != null && req.prime() < 0) errors.put("prime", "Prime invalide.");
        if (req.soldeConge() != null && req.soldeConge() < 0) errors.put("soldeConge", "Solde congé invalide.");

        // médecin
        if (req.specialite() != null && req.specialite().length() > 150) {
            errors.put("specialite", "Spécialité trop longue.");
        }

        // secrétaire
        if (req.numCNSS() != null && req.numCNSS().length() > 50) {
            errors.put("numCNSS", "Num CNSS trop long.");
        }
        if (req.commission() != null && req.commission() < 0) {
            errors.put("commission", "Commission invalide.");
        }

        return errors;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private String trim(String s) { return s == null ? null : s.trim(); }
}
