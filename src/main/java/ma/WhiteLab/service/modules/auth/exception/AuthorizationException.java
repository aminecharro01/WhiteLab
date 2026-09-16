package ma.WhiteLab.service.modules.auth.exception;

import ma.WhiteLab.entities.enums.RoleR;

public class AuthorizationException extends RuntimeException {

    private final String requiredRole;
    private final String requiredPrivilege;

    public AuthorizationException(String message) {
        super(message);
        this.requiredRole = null;
        this.requiredPrivilege = null;
    }

    public static AuthorizationException forRole(RoleR role) {
        return new AuthorizationException("Rôle requis : " + role);
    }

    public static AuthorizationException forPrivilege(String privilege) {
        return new AuthorizationException("Privilège requis : " + privilege);
    }
}