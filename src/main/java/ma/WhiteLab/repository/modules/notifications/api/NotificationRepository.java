package ma.WhiteLab.repository.modules.notifications.api;

import ma.WhiteLab.entities.user.Notification;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

    Optional<Notification> findByTitre(String titre);

    List<Notification> findByType(String type);

    List<Notification> findByPriorite(String priorite);

    long count();

    int countUnreadForUser(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAsDeletedForUser(Long userId, Long notificationId);

    List<Notification> findForUser(Long userId);

    Set<Long> findUnreadIdsForUser(Long userId);
}

