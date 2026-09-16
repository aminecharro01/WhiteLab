package ma.WhiteLab.service.modules.auth.api;

import ma.WhiteLab.mvc.dto.auth.AuthRequest;
import ma.WhiteLab.mvc.dto.auth.AuthResult;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

public interface AuthService {
    AuthResult authenticate(AuthRequest request);
    UserPrincipal loadUserPrincipalByLogin(String login);
    void changePassword(Long userId, String oldPassword, String newPassword);
    String forgotPassword(String email);
    void forceChangePassword(Long userId, String newPassword);
}