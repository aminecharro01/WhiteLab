package ma.WhiteLab.repository.modules.user.api;

import ma.WhiteLab.entities.user.Notification;
import ma.WhiteLab.entities.user.Role;
import ma.WhiteLab.entities.user.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository<T extends Utilisateur> {
    List<T> findAll();
    T findById(Long id);
    Optional<T> findByEmail(String email);
    Optional<T> findByTelephone(String telephone);
    Optional<T> findOne(String sql, String param);
    long count();
    void create(T entity);
    void update(T entity);
    void delete(T entity);
    void deleteById(Long id);
    void addRoleToUtilisateur(Long utilisateurId, Long roleId);
    void removeRoleFromUtilisateur(Long utilisateurId, Long roleId);
    List<Role> getRolesOfUtilisateur(Long utilisateurId);
    void deleteAllRolesForUtilisateur(Long utilisateurId);
    void addNotificationToUtilisateur(Long utilisateurId, Long notificationId);
    void removeNotificationFromUtilisateur(Long utilisateurId, Long notificationId);
    List<Notification> getNotificationsOfUtilisateur(Long utilisateurId);
    void deleteAllNotificationsForUtilisateur(Long utilisateurId);
    void updatePassword(Long id, String encodedPassword);
    void updateTempPassword(Long userId, String tempPassword, LocalDateTime expiry, boolean mustChange);
    void updateLastLogin(Long userId);
    List<Utilisateur> findAllMedecins();
}