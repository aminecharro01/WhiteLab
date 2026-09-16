package ma.WhiteLab.repository.modules.notifications.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import ma.WhiteLab.entities.user.Notification;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.notifications.api.NotificationRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@AllArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final Connection connection; // Injectée par Transaction ou ApplicationContext

    // =========================
    // CRUD
    // =========================

    @Override
    public List<Notification> findAll() {
        String sql = "SELECT * FROM notification ORDER BY date DESC, time DESC";
        List<Notification> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(RowMappers.mapNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les notifications", e);
        }

        return out;
    }

    @Override
    public Notification findById(Long id) {
        String sql = "SELECT * FROM notification WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return RowMappers.mapNotification(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la notification par ID : " + id, e);
        }
        return null;
    }

    @Override
    public void create(Notification n) {
        String sql = """
            INSERT INTO notification(titre, message, date, time, type, priorite, dateCreation, creePar)
            VALUES(?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitre().name());
            ps.setString(2, n.getMessage());
            ps.setDate(3, n.getDate() != null ? Date.valueOf(n.getDate()) : null);
            ps.setTime(4, n.getTime() != null ? Time.valueOf(n.getTime()) : null);
            ps.setString(5, n.getType().name());
            ps.setString(6, n.getPriorite().name());
            ps.setTimestamp(7, Timestamp.valueOf(n.getDateCreation() != null ? n.getDateCreation() : LocalDateTime.now()));
            ps.setString(8, n.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) n.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }

    @Override
    public void update(Notification n) {
        String sql = """
            UPDATE notification
            SET titre=?, message=?, date=?, time=?, type=?, priorite=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, n.getTitre().name());
            ps.setString(2, n.getMessage());
            ps.setDate(3, n.getDate() != null ? Date.valueOf(n.getDate()) : null);
            ps.setTime(4, n.getTime() != null ? Time.valueOf(n.getTime()) : null);
            ps.setString(5, n.getType().name());
            ps.setString(6, n.getPriorite().name());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, n.getModifierPar());
            ps.setLong(9, n.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la notification ID=" + n.getId(), e);
        }
    }

    @Override
    public void delete(Notification n) {
        if (n != null && n.getId() != null) deleteById(n.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM notification WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la notification ID=" + id, e);
        }
    }

    // =========================
    // Recherches spécifiques
    // =========================

    @Override
    public Optional<Notification> findByTitre(String titre) {
        String sql = "SELECT * FROM notification WHERE titre = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, titre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(RowMappers.mapNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par titre : " + titre, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findByType(String type) {
        String sql = "SELECT * FROM notification WHERE type = ? ORDER BY date DESC, time DESC";
        List<Notification> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(RowMappers.mapNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par type : " + type, e);
        }
        return out;
    }

    @Override
    public List<Notification> findByPriorite(String priorite) {
        String sql = "SELECT * FROM notification WHERE priorite = ? ORDER BY date DESC, time DESC";
        List<Notification> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, priorite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(RowMappers.mapNotification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par priorité : " + priorite, e);
        }
        return out;
    }

    @Override
    public List<Notification> findForUser(Long userId) {
        String sql = """
            SELECT n.* 
            FROM notification n
            JOIN Utilisateur_Notification un ON n.id = un.notification_id
            WHERE un.utilisateur_id = ?
              AND (un.deleted_by_user = FALSE OR un.deleted_by_user IS NULL)
            ORDER BY n.date DESC, n.time DESC
        """;
        List<Notification> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des notifications pour l'utilisateur " + userId, e);
        }
        return out;
    }

    @Override
    public Set<Long> findUnreadIdsForUser(Long userId) {
        String sql = """
        SELECT un.notification_id 
        FROM Utilisateur_Notification un
        WHERE un.utilisateur_id = ?
          AND (un.is_read = false OR un.is_read IS NULL)
        """;
        Set<Long> out = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getLong("notification_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findUnreadIdsForUser(" + userId + ")", e);
        }
        return out;
    }


    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM notification";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des notifications", e);
        }
    }

    @Override
    public int countUnreadForUser(Long userId) {
        String sql = """
        SELECT COUNT(*) 
        FROM Utilisateur_Notification un
        WHERE un.utilisateur_id = ?
          AND (un.is_read = false OR un.is_read IS NULL)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur countUnreadForUser(" + userId + ")", e);
        }
    }


    @Override
    public void markAsRead(Long userId, Long notificationId) {
        String sql = "UPDATE Utilisateur_Notification SET is_read = 1 WHERE utilisateur_id = ? AND notification_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur markAsRead(" + userId + "," + notificationId + ")", e);
        }
    }


    @Override
    public void markAsDeletedForUser(Long userId, Long notificationId) {
        String sql = """
        UPDATE Utilisateur_Notification
        SET deleted_by_user = TRUE
        WHERE utilisateur_id = ?
          AND notification_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur markAsDeletedForUser(" + userId + "," + notificationId + ")", e
            );
        }
    }

}

