package ma.WhiteLab.repository.modules.user.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.user.*;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ma.WhiteLab.repository.common.RowMappers.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtilisateurRepositoryImpl implements UtilisateurRepository<Utilisateur> {

    private Connection connection; // Injectée automatiquement par ApplicationContext

    // ========================
    // CRUD de base
    // ========================

    @Override
    public List<Utilisateur> findAll() {
        String sql = """
            SELECT * FROM Utilisateur
            ORDER BY nom, prenom
            """;
        System.out.println("[JDBC UTILISATEUR SQL] " + sql);
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                utilisateurs.add(mapUtilisateur(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les utilisateurs", e);
        }

        return utilisateurs;
    }

    @Override
    public Utilisateur findById(Long id) {
        String sql = "SELECT * FROM Utilisateur WHERE id = ?";
        System.out.println("[JDBC UTILISATEUR SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur user = mapUtilisateur(rs);
                    // Manually map new fields as RowMapper is not updated
                    user.setTempPassword(rs.getString("temp_password"));
                    Timestamp expiryTs = rs.getTimestamp("temp_password_expiry");
                    if (expiryTs != null) {
                        user.setTempPasswordExpiry(expiryTs.toLocalDateTime());
                    }
                    user.setMustChangePassword(rs.getBoolean("must_change_password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par ID : " + id, e);
        }
        return null;
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        String sql = "SELECT * FROM Utilisateur WHERE email = ?";
        System.out.println("[JDBC UTILISATEUR SQL] " + sql + " | email=" + email);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur user = mapUtilisateur(rs);
                    // Manually map new fields as RowMapper is not updated
                    user.setTempPassword(rs.getString("temp_password"));
                    Timestamp expiryTs = rs.getTimestamp("temp_password_expiry");
                    if (expiryTs != null) {
                        user.setTempPasswordExpiry(expiryTs.toLocalDateTime());
                    }
                    user.setMustChangePassword(rs.getBoolean("must_change_password"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par email : " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilisateur> findByTelephone(String telephone) {
        String sql = "SELECT * FROM Utilisateur WHERE telephone = ?";
        System.out.println("[JDBC UTILISATEUR SQL] " + sql + " | telephone=" + telephone);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, telephone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUtilisateur(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par téléphone : " + telephone, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilisateur> findOne(String sql, String param) {
        System.out.println("[JDBC UTILISATEUR SQL] " + sql + " | param=" + param);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUtilisateur(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution de findOne avec SQL personnalisé", e);
        }
        return Optional.empty();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM Utilisateur";
        System.out.println("[JDBC UTILISATEUR SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des utilisateurs", e);
        }
    }

    @Override
    public void create(Utilisateur u) {
        String sql = """
            INSERT INTO Utilisateur (
                nom, prenom, email, adresse, cin, telephone, dateNaissance, sexe,
                motDePasse, type, salaire, prime, dateRecrutement, soldeConge,
                cabinetMedicale_id, specialite, numCNS, commission,
                dateCreation, creePar, temp_password, temp_password_expiry, must_change_password
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        System.out.println("[JDBC UTILISATEUR SQL] INSERT Utilisateur | nom=" + u.getNom() + ", email=" + u.getEmail());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getAdresse());
            ps.setString(5, u.getCin());
            ps.setString(6, u.getTelephone());
            ps.setObject(7, u.getDateNaissance() != null ? Date.valueOf(u.getDateNaissance()) : null);
            ps.setString(8, u.getSexe() != null ? u.getSexe().name() : null);
            ps.setString(9, u.getMotDePasse());
            ps.setString(10, determineType(u));

            ps.setObject(11, u instanceof Staff s ? s.getSalaire() : null);
            ps.setObject(12, u instanceof Staff s ? s.getPrime() : null);
            ps.setObject(13, u instanceof Staff s && s.getDateRecrutement() != null ? Date.valueOf(s.getDateRecrutement()) : null);
            ps.setObject(14, u instanceof Staff s ? s.getSoldeConge() : null);
            ps.setObject(15, u instanceof Staff s && s.getCabinetMedicale() != null ? s.getCabinetMedicale().getId() : null);

            ps.setString(16, u instanceof Medecin m ? m.getSpecialite() : null);
            ps.setString(17, u instanceof Secretaire sec ? sec.getNumCNS() : null);
            ps.setObject(18, u instanceof Secretaire sec ? sec.getCommission() : null);

            ps.setTimestamp(19, Timestamp.valueOf(u.getDateCreation() != null ? u.getDateCreation() : LocalDateTime.now()));
            ps.setString(20, u.getCreePar());
            
            ps.setString(21, u.getTempPassword());
            ps.setTimestamp(22, u.getTempPasswordExpiry() != null ? Timestamp.valueOf(u.getTempPasswordExpiry()) : null);
            ps.setBoolean(23, u.isMustChangePassword());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getLong(1));
                }
            }

            insertRolesForUtilisateur(u, connection);
            insertNotificationsForUtilisateur(u, connection);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    @Override
    public void update(Utilisateur u) {
        String sql = """
        UPDATE Utilisateur SET
            nom=?, prenom=?, email=?, adresse=?, cin=?, telephone=?, dateNaissance=?, sexe=?,
            motDePasse=?, salaire=?, prime=?, dateRecrutement=?, soldeConge=?,
            cabinetMedicale_id=?, specialite=?, numCNS=?, commission=?,
            dateMiseAJour=?, modifierPar=?,
            temp_password=?, temp_password_expiry=?, must_change_password=?
        WHERE id=?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getAdresse());
            ps.setString(5, u.getCin());
            ps.setString(6, u.getTelephone());
            ps.setObject(7, u.getDateNaissance() != null ? Date.valueOf(u.getDateNaissance()) : null);
            ps.setString(8, u.getSexe() != null ? u.getSexe().name() : null);
            ps.setString(9, u.getMotDePasse());

            ps.setObject(10, u instanceof Staff s ? s.getSalaire() : null);
            ps.setObject(11, u instanceof Staff s ? s.getPrime() : null);
            ps.setObject(12, u instanceof Staff s && s.getDateRecrutement() != null
                    ? Date.valueOf(s.getDateRecrutement()) : null);
            ps.setObject(13, u instanceof Staff s ? s.getSoldeConge() : null);
            ps.setObject(14, u instanceof Staff s && s.getCabinetMedicale() != null
                    ? s.getCabinetMedicale().getId() : null);

            ps.setString(15, u instanceof Medecin m ? m.getSpecialite() : null);
            ps.setString(16, u instanceof Secretaire sec ? sec.getNumCNS() : null);
            ps.setObject(17, u instanceof Secretaire sec ? sec.getCommission() : null);

            ps.setTimestamp(18, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(19, u.getModifierPar());
            
            ps.setString(20, u.getTempPassword());
            ps.setTimestamp(21, u.getTempPasswordExpiry() != null ? Timestamp.valueOf(u.getTempPasswordExpiry()) : null);
            ps.setBoolean(22, u.isMustChangePassword());
            
            ps.setLong(23, u.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour utilisateur", e);
        }
    }


    @Override
    public void delete(Utilisateur u) {
        if (u != null && u.getId() != null) {
            deleteById(u.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        System.out.println("[JDBC UTILISATEUR SQL] DELETE Utilisateur | id=" + id);
        try {
            deleteAllRolesForUtilisateur(id, connection);
            deleteAllNotificationsForUtilisateur(id, connection);

            String sql = "DELETE FROM Utilisateur WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur ID=" + id, e);
        }
    }

    // ========================
    // Gestion des Rôles
    // ========================

    @Override
    public void addRoleToUtilisateur(Long utilisateurId, Long roleId) {
        String sql = "INSERT IGNORE INTO Utilisateur_Role (utilisateur_id, role_id) VALUES (?, ?)";
        executeUpdate(sql, utilisateurId, roleId);
    }

    @Override
    public void removeRoleFromUtilisateur(Long utilisateurId, Long roleId) {
        String sql = "DELETE FROM Utilisateur_Role WHERE utilisateur_id = ? AND role_id = ?";
        executeUpdate(sql, utilisateurId, roleId);
    }

    @Override
    public List<Role> getRolesOfUtilisateur(Long utilisateurId) {
        return getRolesOfUtilisateur(utilisateurId, connection);
    }

    private List<Role> getRolesOfUtilisateur(Long utilisateurId, Connection c) {
        String sql = "SELECT r.* FROM Role r JOIN Utilisateur_Role ur ON r.id = ur.role_id WHERE ur.utilisateur_id = ?";
        List<Role> roles = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapRole(rs, c));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des rôles de l'utilisateur " + utilisateurId, e);
        }
        return roles;
    }

    @Override
    public void deleteAllRolesForUtilisateur(Long utilisateurId) {
        deleteAllRolesForUtilisateur(utilisateurId, connection);
    }

    private void deleteAllRolesForUtilisateur(Long utilisateurId, Connection c) {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM Utilisateur_Role WHERE utilisateur_id = ?")) {
            ps.setLong(1, utilisateurId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des rôles de l'utilisateur " + utilisateurId, e);
        }
    }

    // ========================
    // Gestion des Notifications
    // ========================

    @Override
    public void addNotificationToUtilisateur(Long utilisateurId, Long notificationId) {
        String sql = "INSERT IGNORE INTO utilisateur_notification (utilisateur_id, notification_id) VALUES (?, ?)";
        executeUpdate(sql, utilisateurId, notificationId);
    }

    @Override
    public void removeNotificationFromUtilisateur(Long utilisateurId, Long notificationId) {
        String sql = "DELETE FROM Utilisateur_Notification WHERE utilisateur_id = ? AND notification_id = ?";
        executeUpdate(sql, utilisateurId, notificationId);
    }

    @Override
    public List<Notification> getNotificationsOfUtilisateur(Long utilisateurId) {
        String sql = """
            SELECT n.* FROM Notification n
            JOIN Utilisateur_Notification un ON n.id = un.notification_id
            WHERE un.utilisateur_id = ?
            ORDER BY n.date DESC, n.time DESC
            """;
        List<Notification> notifications = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des notifications de l'utilisateur " + utilisateurId, e);
        }
        return notifications;
    }

    @Override
    public void deleteAllNotificationsForUtilisateur(Long utilisateurId) {
        deleteAllNotificationsForUtilisateur(utilisateurId, connection);
    }

    private void deleteAllNotificationsForUtilisateur(Long utilisateurId, Connection c) {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM Utilisateur_Notification WHERE utilisateur_id = ?")) {
            ps.setLong(1, utilisateurId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des notifications de l'utilisateur " + utilisateurId, e);
        }
    }

    @Override
    public void updatePassword(Long id, String encodedPassword) {
        String sql = "UPDATE Utilisateur SET motDePasse = ? WHERE id = ?";
        System.out.println("[JDBC UTILISATEUR SQL] UPDATE motDePasse | id=" + id);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, encodedPassword);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe", e);
        }
    }

    @Override
    public void updateTempPassword(Long userId, String tempPassword, LocalDateTime expiry, boolean mustChange) {
        String sql = """
            UPDATE Utilisateur SET
                temp_password = ?,
                temp_password_expiry = ?,
                must_change_password = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tempPassword);
            ps.setTimestamp(2, expiry != null ? Timestamp.valueOf(expiry) : null);
            ps.setBoolean(3, mustChange);
            ps.setLong(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe temporaire", e);
        }
    }

    public void updateLastLogin(Long userId) {
        String sql = "UPDATE Utilisateur SET dateMiseAJour = NOW() WHERE id = ?";
        System.out.println("[JDBC UTILISATEUR SQL] UPDATE dernière connexion | id=" + userId);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour dernière connexion : " + e.getMessage());
        }
    }

    @Override
    public List<Utilisateur> findAllMedecins() {
        String sql = "SELECT * FROM Utilisateur WHERE type = 'MEDECIN' ORDER BY nom, prenom";
        System.out.println("[JDBC UTILISATEUR SQL] " + sql);

        List<Utilisateur> medecins = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Medecin m = Medecin.builder()
                        .id(rs.getLong("id"))
                        .nom(rs.getString("nom"))
                        .prenom(rs.getString("prenom"))
                        .email(rs.getString("email"))
                        .specialite(rs.getString("specialite"))
                        .build();
                medecins.add(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des médecins", e);
        }

        return medecins;
    }



    // ========================
    // Méthodes utilitaires privées
    // ========================

    private String determineType(Utilisateur u) {
        if (u instanceof Admin) return "ADMIN";
        if (u instanceof Medecin) return "MEDECIN";
        if (u instanceof Secretaire) return "SECRETAIRE";
        throw new IllegalArgumentException("Type d'utilisateur non supporté : " + u.getClass().getSimpleName());
    }

    private void insertRolesForUtilisateur(Utilisateur u, Connection c) {
        if (u.getRoles() == null || u.getRoles().isEmpty()) return;

        String sql = "INSERT IGNORE INTO Utilisateur_Role (utilisateur_id, role_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Role role : u.getRoles()) {
                ps.setLong(1, u.getId());
                ps.setLong(2, role.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des rôles pour l'utilisateur " + u.getId(), e);
        }
    }

    private void insertNotificationsForUtilisateur(Utilisateur u, Connection c) {
        if (u.getNotifications() == null || u.getNotifications().isEmpty()) return;

        String sql = "INSERT IGNORE INTO Utilisateur_Notification (utilisateur_id, notification_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Notification notif : u.getNotifications()) {
                ps.setLong(1, u.getId());
                ps.setLong(2, notif.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des notifications pour l'utilisateur " + u.getId(), e);
        }
    }

    private void executeUpdate(String sql, Long... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setLong(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de relation", e);
        }
    }
}
