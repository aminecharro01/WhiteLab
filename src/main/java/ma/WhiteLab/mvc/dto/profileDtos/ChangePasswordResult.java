package ma.WhiteLab.mvc.dto.profileDtos;


import java.util.Map;

public record ChangePasswordResult(
        boolean ok,
        String message,
        Map<String, String> fieldErrors
) {
    public static ChangePasswordResult success() {
        return new ChangePasswordResult(true, "Mot de passe modifié.", Map.of());
    }
    public static ChangePasswordResult failure(String msg, Map<String, String> errors) {
        return new ChangePasswordResult(false, msg, errors);
    }
}
