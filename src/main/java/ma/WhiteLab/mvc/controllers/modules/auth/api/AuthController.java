package ma.WhiteLab.mvc.controllers.modules.auth.api;


import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

public interface AuthController {

    void showLoginView();

    // callbacks venant de la vue
    void onLoginRequested(String login, String password);
    void onForgotPasswordRequested();
    void onCancelRequested();

    void onLoginSuccess(UserPrincipal principal);
}