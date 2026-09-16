package ma.WhiteLab.service.modules.auth.api;

import ma.WhiteLab.mvc.dto.auth.AuthRequest;

import java.util.Map;

/**
 * Validateur des credentials pour l'authentification et le changement de mot de passe.
 * Retourne une Map des erreurs par champ (clé = nom du champ, valeur = message d'erreur).
 * Si la map est vide → validation réussie.
 */
public interface CredentialsValidator {

    /**
     * Valide les champs de la requête d'authentification (login/email et password).
     *
     * @param request la requête contenant login et password
     * @return Map vide si OK, sinon erreurs par champ (ex: "login" → "L'email est requis")
     */
    Map<String, String> validate(AuthRequest request);

    /**
     * Valide un nouveau mot de passe selon les règles de complexité définies
     * (longueur minimale, majuscule, chiffre, caractère spécial, etc.).
     *
     * @param newPassword le nouveau mot de passe à valider
     * @return Map vide si OK, sinon erreurs associées (ex: "newPassword" → "Doit contenir au moins 8 caractères")
     */
    Map<String, String> validateNewPassword(String newPassword);
}