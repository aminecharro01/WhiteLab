package ma.WhiteLab.mvc.dto.auth;

import java.util.Collections;
import java.util.Map;

public class AuthResult {

    private final boolean success;
    private final String message;
    private final Map<String, String> fieldErrors;
    private final UserPrincipal userPrincipal;
    private final boolean mustChangePassword;

    private AuthResult(boolean success, String message, Map<String, String> fieldErrors, UserPrincipal userPrincipal, boolean mustChangePassword) {
        this.success = success;
        this.message = message;
        this.fieldErrors = (fieldErrors != null) ? fieldErrors : Collections.emptyMap();
        this.userPrincipal = userPrincipal;
        this.mustChangePassword = mustChangePassword;
    }

    public static AuthResult success(UserPrincipal principal, String message) {
        return new AuthResult(true, message, null, principal, false);
    }

    public static AuthResult success(UserPrincipal principal, String message, boolean mustChangePassword) {
        return new AuthResult(true, message, null, principal, mustChangePassword);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null, null, false);
    }

    public static AuthResult validationError(Map<String, String> errors) {
        return new AuthResult(false, "Erreurs de validation", errors, null, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    public UserPrincipal getUserPrincipal() {
        return userPrincipal;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
}
