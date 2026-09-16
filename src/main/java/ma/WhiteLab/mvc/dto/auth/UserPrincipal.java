package ma.WhiteLab.mvc.dto.auth;

import lombok.Getter;
import lombok.Setter;
import ma.WhiteLab.entities.enums.RoleR;

import java.io.Serializable;
import java.util.Set;

/**
 * Représentation sécurisée et immuable de l'utilisateur authentifié.
 * Ne contient ni mot de passe, ni entités JPA complètes.
 * Conforme au modèle du professeur.
 */

public record UserPrincipal(
        Long id,
        String email,
        String fullName,
        String login,                    // ← CE CHAMP DOIT ÊTRE PRÉSENT
        RoleR rolePrincipal,          // Premier rôle affecté = rôle principal
        Set<RoleR> roles,             // Tous les rôles sous forme d'enum
        Set<String> privileges        // Tous les privilèges distincts (String)
) implements Serializable {
}