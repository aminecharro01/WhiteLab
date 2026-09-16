package ma.WhiteLab.service.modules.auth.impl;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.service.modules.auth.api.AuthorizationService;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.service.modules.auth.exception.AuthorizationException;

import java.util.Arrays;

public class AuthorizationServiceImpl implements AuthorizationService {

    /* =========================
       ROLE CHECKS
       ========================= */

    @Override
    public boolean hasRole(UserPrincipal principal, RoleR role) {
        if (principal == null || principal.roles() == null || role == null) {
            return false;
        }
        return principal.roles().contains(role);
    }

    @Override
    public boolean hasAnyRole(UserPrincipal principal, RoleR... roles) {
        if (principal == null || principal.roles() == null || roles == null || roles.length == 0) {
            return false;
        }
        return principal.roles().stream()
                .anyMatch(role -> Arrays.asList(roles).contains(role));
    }


    /* =========================
       PRIVILEGE CHECKS
       ========================= */

    @Override
    public boolean hasPrivilege(UserPrincipal principal, String privilege) {
        if (principal == null || principal.privileges() == null || privilege == null || privilege.isBlank()) {
            return false;
        }
        return principal.privileges().stream()
                .anyMatch(p -> p.equalsIgnoreCase(privilege.trim()));
    }


    /* =========================
       THROWING CHECKS
       ========================= */

    @Override
    public void checkRole(UserPrincipal principal, RoleR role) {
        if (!hasRole(principal, role)) {
            throw new AuthorizationException("Accès refusé : rôle requis -> " + role);
        }
    }

    @Override
    public void checkAnyRole(UserPrincipal principal, RoleR... roles) {
        if (!hasAnyRole(principal, roles)) {
            throw new AuthorizationException(
                    "Accès refusé : au moins un des rôles suivants est requis -> " + Arrays.toString(roles)
            );
        }
    }


    @Override
    public void checkPrivilege(UserPrincipal principal, String privilege) {
        if (!hasPrivilege(principal, privilege)) {
            throw new AuthorizationException("Accès refusé : privilège requis -> " + privilege);
        }
    }

}