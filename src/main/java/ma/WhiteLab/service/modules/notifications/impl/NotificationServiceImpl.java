package ma.WhiteLab.service.modules.notifications.impl;

import ma.WhiteLab.common.consoleLog.ConsoleLogger;
import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.enums.PrioriteNotification;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.TitreNotification;
import ma.WhiteLab.entities.enums.TypeNotification;
import ma.WhiteLab.entities.user.Notification;
import ma.WhiteLab.entities.user.Utilisateur;
import ma.WhiteLab.repository.modules.notifications.api.NotificationRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;
import ma.WhiteLab.service.modules.notifications.api.NotificationValidator;
import ma.WhiteLab.service.modules.notifications.dto.NotificationDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NotificationServiceImpl implements NotificationService {

    private final RepoFactory<NotificationRepository> notificationRepoFactory;
    private final RepoFactory<UtilisateurRepository> utilisateurRepoFactory;
    private final NotificationValidator validator = new NotificationValidatorImpl();

    public NotificationServiceImpl(
            RepoFactory<NotificationRepository> notificationRepoFactory,
            RepoFactory<UtilisateurRepository> utilisateurRepoFactory) {
        this.notificationRepoFactory = notificationRepoFactory;
        this.utilisateurRepoFactory = utilisateurRepoFactory;
    }

    private NotificationDTO toDTO(Notification entity, boolean isRead) {
        if (entity == null) return null;

        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setTitre(entity.getTitre() != null ? entity.getTitre().name() : null);
        dto.setType(entity.getType() != null ? entity.getType().name() : null);
        dto.setPriorite(entity.getPriorite() != null ? entity.getPriorite().name() : null);
        dto.setMessage(entity.getMessage());
        dto.setDate(entity.getDate());
        dto.setTime(entity.getTime());
        dto.setRead(isRead); // Set read status
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setCreePar(entity.getCreePar());
        dto.setModifierPar(entity.getModifierPar());
        return dto;
    }
    
    private NotificationDTO toDTO(Notification entity) {
        // Overloaded method for contexts where read status is unknown, defaults to read.
        return toDTO(entity, true);
    }

    private <E extends Enum<E>> E safeEnumValueOf(Class<E> enumClass, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            ConsoleLogger.error("Invalid enum value for " + enumClass.getSimpleName() + ": " + value);
            return null;
        }
    }

    private Notification toEntity(NotificationDTO dto) {
        if (dto == null) return null;

        Notification entity = new Notification();
        entity.setId(dto.getId());
        entity.setTitre(safeEnumValueOf(TitreNotification.class, dto.getTitre()));
        entity.setType(safeEnumValueOf(TypeNotification.class, dto.getType()));
        entity.setPriorite(safeEnumValueOf(PrioriteNotification.class, dto.getPriorite()));
        entity.setMessage(dto.getMessage());
        entity.setDate(dto.getDate());
        entity.setTime(dto.getTime());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setCreePar(dto.getCreePar());
        return entity;
    }

    @Override
    public List<NotificationDTO> getNotificationsForUser(Long utilisateurId) {
        if (utilisateurId == null) return Collections.emptyList();

        return Transaction.initTransaction(cnx -> {
            NotificationRepository notifRepo = notificationRepoFactory.create(cnx);

            List<Notification> entities = notifRepo.findForUser(utilisateurId);
            Set<Long> unreadIds = notifRepo.findUnreadIdsForUser(utilisateurId);

            return entities.stream()
                    .map(entity -> toDTO(entity, !unreadIds.contains(entity.getId())))
                    .sorted((n1, n2) -> {
                        if (n1.isRead() != n2.isRead()) {
                            return n1.isRead() ? 1 : -1;
                        }
                        LocalDateTime dt1 = LocalDateTime.of(n1.getDate(), n1.getTime());
                        LocalDateTime dt2 = LocalDateTime.of(n2.getDate(), n2.getTime());
                        return dt2.compareTo(dt1);
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Transaction.initTransaction(cnx -> {
            notificationRepoFactory.create(cnx).markAsRead(userId, notificationId);
            return null;
        });
    }

    @Override
    public void deleteNotificationForUser(Long userId, Long notificationId) {
        Transaction.initTransaction(cnx -> {
            notificationRepoFactory.create(cnx).markAsDeletedForUser(userId, notificationId);
            return null;
        });
    }

    @Override
    public void createNotificationForUser(Long userId, TitreNotification titre, String message, String createdBy) {
        Transaction.initTransaction(cnx -> {
            NotificationRepository notifRepo = notificationRepoFactory.create(cnx);
            UtilisateurRepository userRepo = utilisateurRepoFactory.create(cnx);

            NotificationDTO dto = new NotificationDTO();
            dto.setTitre(titre.name());
            dto.setMessage(message);
            dto.setType(TypeNotification.INFO.name());
            dto.setPriorite(PrioriteNotification.BASSE.name());
            dto.setCreePar(createdBy);
            dto.setDate(LocalDate.now());
            dto.setTime(LocalTime.now());

            Map<String, String> errors = validator.validate(dto);
            if (!errors.isEmpty()) {
                throw new ValidationException("Validation errors for notification: " + errors);
            }

            Notification entity = toEntity(dto);
            notifRepo.create(entity);

            if (userRepo.findById(userId) != null) {
                userRepo.addNotificationToUtilisateur(userId, entity.getId());
            } else {
                ConsoleLogger.error("User with ID " + userId + " not found. Cannot assign notification.");
                // Depending on requirements, you might want to throw an exception here
            }
            return null;
        });
    }

    @Override
    public int countUnreadForUser(Long userId) {
        if (userId == null) return 0;
        try {
            return Transaction.initTransaction(cnx ->
                notificationRepoFactory.create(cnx).countUnreadForUser(userId)
            );
        } catch (Exception e) {
            ConsoleLogger.error("Silent notification count error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public void createNewPatientNotification(String patientName, String createdBy) {
        NotificationDTO dto = new NotificationDTO();
        dto.setTitre(TitreNotification.INFO.name());
        dto.setMessage("Le patient '" + patientName + "' a été ajouté au système.");
        dto.setType(TypeNotification.INFO.name());
        dto.setPriorite(PrioriteNotification.BASSE.name());
        dto.setCreePar(createdBy);
        dto.setDate(LocalDate.now());
        dto.setTime(LocalTime.now());

        createAndAssign(dto, List.of(RoleR.ADMIN, RoleR.SECRETAIRE));
    }

    @Override
    public void createConsultationAddedNotification(String patientName, String doctorName, String createdBy) {
        NotificationDTO dto = new NotificationDTO();
        dto.setTitre(TitreNotification.INFO.name());
        dto.setMessage("Une nouvelle consultation pour '" + patientName + "' a été planifiée avec Dr. " + doctorName);
        dto.setType(TypeNotification.INFO.name());
        dto.setPriorite(PrioriteNotification.MOYENNE.name());
        dto.setCreePar(createdBy);
        dto.setDate(LocalDate.now());
        dto.setTime(LocalTime.now());

        createAndAssign(dto, List.of(RoleR.ADMIN, RoleR.SECRETAIRE, RoleR.MEDECIN));
    }

    private NotificationDTO createAndAssign(NotificationDTO dto, List<RoleR> roles) {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository notifRepo = notificationRepoFactory.create(cnx);
            UtilisateurRepository userRepo = utilisateurRepoFactory.create(cnx);

            Map<String, String> errors = validator.validate(dto);
            if (!errors.isEmpty()) {
                throw new ValidationException("Validation errors: " + errors);
            }
            Notification entity = toEntity(dto);
            notifRepo.create(entity);
            dto.setId(entity.getId());

            List<Utilisateur> usersToNotify = ((List<Utilisateur>)userRepo.findAll()).stream()
                    .filter(user -> {
                        List<ma.WhiteLab.entities.user.Role> userRoles = userRepo.getRolesOfUtilisateur(user.getId());
                        if (userRoles == null) return false;
                        return userRoles.stream().anyMatch(role -> roles.contains(role.getLibelle()));
                    })
                    .collect(Collectors.toList());

            for (Utilisateur user : usersToNotify) {
                try {
                    userRepo.addNotificationToUtilisateur(user.getId(), entity.getId());
                } catch (Exception e) {
                    ConsoleLogger.error("Failed to assign notification " + entity.getId() + " to user " + user.getId() + ": " + e.getMessage());
                }
            }
            return dto;
        });
    }

    @Override
    public List<NotificationDTO> getAllNotifications() {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            return repo.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public NotificationDTO getNotificationById(Long id) {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            Notification entity = repo.findById(id);
            return toDTO(entity);
        });
    }

    @Override
    public NotificationDTO createNotification(NotificationDTO dto) throws ValidationException {
         return Transaction.initTransaction(cnx -> {
            Map<String, String> errors = validator.validate(dto);
            if (!errors.isEmpty()) {
                throw new ValidationException("Erreurs de validation : " + errors);
            }
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            Notification entity = toEntity(dto);
            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public NotificationDTO updateNotification(Long id, NotificationDTO dto) throws ValidationException {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            if (repo.findById(id) == null) {
                throw new IllegalArgumentException("Notification ID " + id + " does not exist.");
            }
            Notification entity = toEntity(dto);
            entity.setId(id);
            repo.update(entity);
            return toDTO(entity);
        });
    }
    
    @Override
    public void deleteNotification(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            if (repo.findById(id) == null) {
                throw new ValidationException("Notification ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }
    
    @Override
    public void addNotificationToUser(Long utilisateurId, Long notificationId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            UtilisateurRepository userRepo = utilisateurRepoFactory.create(cnx);
            NotificationRepository notifRepo = notificationRepoFactory.create(cnx);

            if (notifRepo.findById(notificationId) == null) {
                throw new ValidationException("Notification ID " + notificationId + " n'existe pas.");
            }
            if (userRepo.findById(utilisateurId) == null) {
                throw new ValidationException("Utilisateur ID " + utilisateurId + " n'existe pas.");
            }
            userRepo.addNotificationToUtilisateur(utilisateurId, notificationId);
            return null;
        });
    }
    
    @Override
    public NotificationDTO getNotificationByTitre(String titre) {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            Optional<Notification> optionalEntity = repo.findByTitre(titre);
            return optionalEntity.map(this::toDTO).orElse(null);
        });
    }

    @Override
    public List<NotificationDTO> getNotificationsByType(String type) {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            List<Notification> entities = repo.findByType(type);
            return entities.stream().map(this::toDTO).collect(Collectors.toList());
        });
    }

    @Override
    public List<NotificationDTO> getNotificationsByPriorite(String priorite) {
        return Transaction.initTransaction(cnx -> {
            NotificationRepository repo = notificationRepoFactory.create(cnx);
            List<Notification> entities = repo.findByPriorite(priorite);
            return entities.stream().map(this::toDTO).collect(Collectors.toList());
        });
    }

    @Override
    public long countNotifications() {
        return Transaction.initTransaction(cnx -> notificationRepoFactory.create(cnx).count());
    }

    @Override
    public void removeNotificationFromUser(Long utilisateurId, Long notificationId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            utilisateurRepoFactory.create(cnx).removeNotificationFromUtilisateur(utilisateurId, notificationId);
            return null;
        });
    }

    @Override
    public void deleteAllNotificationsForUser(Long utilisateurId) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            utilisateurRepoFactory.create(cnx).deleteAllNotificationsForUtilisateur(utilisateurId);
            return null;
        });
    }
    
    @Override
    public void assignToAllUsers(Long notificationId) {
        Transaction.initTransaction(cnx -> {
            NotificationRepository notifRepo = notificationRepoFactory.create(cnx);
            UtilisateurRepository userRepo = utilisateurRepoFactory.create(cnx);
            if (notifRepo.findById(notificationId) == null) {
                throw new IllegalArgumentException("Notification ID " + notificationId + " n'existe pas");
            }
            List<Utilisateur> allUsers = userRepo.findAll();
            for (Utilisateur user : allUsers) {
                try {
                    userRepo.addNotificationToUtilisateur(user.getId(), notificationId);
                } catch (Exception e) {
                    ConsoleLogger.error("Échec assignation à user " + user.getId() + ": " + e.getMessage());
                }
            }
            return null;
        });
    }

    @Override
    public void createNotificationForAllUsers(String titre, String message, String type, String creePar) {

        NotificationDTO dto = new NotificationDTO();
        dto.setTitre(titre);
        dto.setMessage(message);
        dto.setType(type);
        dto.setPriorite(PrioriteNotification.BASSE.name());
        dto.setCreePar(creePar);
        dto.setDate(LocalDate.now());
        dto.setTime(LocalTime.now());

        createAndAssign(dto, List.of(RoleR.ADMIN));
    }

}
