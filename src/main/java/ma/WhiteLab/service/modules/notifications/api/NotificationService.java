package ma.WhiteLab.service.modules.notifications.api;

import ma.WhiteLab.service.modules.notifications.dto.NotificationDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> getAllNotifications();

    NotificationDTO getNotificationById(Long id);

    NotificationDTO createNotification(NotificationDTO dto) throws ValidationException;

    NotificationDTO updateNotification(Long id, NotificationDTO dto) throws ValidationException;

    void deleteNotification(Long id) throws ValidationException;

    List<NotificationDTO> getNotificationsForUser(Long utilisateurId);

    void addNotificationToUser(Long utilisateurId, Long notificationId) throws ValidationException;

    int countUnreadForUser(Long utilisateurId);

    void markAsRead(Long userId, Long notificationId);

    void deleteNotificationForUser(Long userId, Long notificationId);

    void createNotificationForUser(Long userId, ma.WhiteLab.entities.enums.TitreNotification titre, String message, String createdBy);

    // Methods for system-generated notifications
    void createNewPatientNotification(String patientName, String createdBy);
    void createConsultationAddedNotification(String patientName, String doctorName, String createdBy);


    // Deprecated or for internal use, not for UI
    NotificationDTO getNotificationByTitre(String titre);
    List<NotificationDTO> getNotificationsByType(String type);
    List<NotificationDTO> getNotificationsByPriorite(String priorite);
    long countNotifications();
    void removeNotificationFromUser(Long utilisateurId, Long notificationId) throws ValidationException;
    void deleteAllNotificationsForUser(Long utilisateurId) throws ValidationException;
    void assignToAllUsers(Long notificationId);

    void createNotificationForAllUsers(String titre, String message, String type, String creePar);

}