package ma.WhiteLab.mvc.controllers.modules.notifications.api;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

import java.awt.*;

public interface NotificationsController {

    JPanel getView(UserPrincipal principal);

    void deleteNotification(Long notificationId, UserPrincipal principal);
}



