package ma.WhiteLab.service.modules.notifications.api;

import ma.WhiteLab.service.modules.notifications.dto.NotificationDTO;

import java.util.Map;

public interface NotificationValidator {

    Map<String, String> validate(NotificationDTO dto);
}