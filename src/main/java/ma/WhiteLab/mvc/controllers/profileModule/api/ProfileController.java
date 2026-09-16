package ma.WhiteLab.mvc.controllers.profileModule.api;



import java.util.function.Consumer;
import javax.swing.*;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileData;

public interface ProfileController {
    JPanel getView(UserPrincipal principal);
    JPanel getView(UserPrincipal principal, Consumer<ProfileData> onProfileSaved);
}
