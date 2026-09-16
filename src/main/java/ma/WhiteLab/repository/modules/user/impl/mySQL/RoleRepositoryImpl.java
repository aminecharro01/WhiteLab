package ma.WhiteLab.repository.modules.user.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.user.Role;
import ma.WhiteLab.entities.user.RolePrivilege;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.repository.modules.user.api.RoleRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private Connection connection; // Injectée automatiquement par ApplicationContext

    @Override
    public List<Role> findAll() {
        String sql = """
            SELECT r.id, r.libelle, r.description, r.dateCreation, r.dateMiseAJour, r.creePar, r.modifierPar, rp.privilege
            FROM role r
            LEFT JOIN Role_Privilege rp ON r.id = rp.role_id
            ORDER BY r.libelle
            """;
        System.out.println("[JDBC ROLE SQL] " + sql);

        List<Role> out = new ArrayList<>();
        Role currentRole = null;
        Long currentId = null;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long roleId = rs.getLong("id");

                if (currentRole == null || !roleId.equals(currentId)) {
                    currentRole = new Role();
                    currentRole.setId(roleId);
                    currentRole.setLibelle(mapEnum(rs, "libelle", RoleR.class));
                    currentRole.setDescription(rs.getString("description"));
                    currentRole.setDateCreation(getLocalDateTime(rs, "dateCreation"));
                    currentRole.setDateMiseAJour(getLocalDateTime(rs, "dateMiseAJour"));
                    currentRole.setCreePar(rs.getString("creePar"));
                    currentRole.setModifierPar(rs.getString("modifierPar"));
                    currentRole.setPrivileges(new ArrayList<>());

                    out.add(currentRole);
                    currentId = roleId;
                }

                String privilegeName = rs.getString("privilege");
                if (privilegeName != null) {
                    RolePrivilege rp = new RolePrivilege();
                    rp.setPrivilege(privilegeName);
                    rp.setRole(currentRole);
                    currentRole.getPrivileges().add(rp);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Role", e);
        }

        return out;
    }

    @Override
    public Role findById(Long id) {
        String sql = """
            SELECT r.id, r.libelle, r.description, r.dateCreation, r.dateMiseAJour, r.creePar, r.modifierPar, rp.privilege
            FROM role r
            LEFT JOIN Role_Privilege rp ON r.id = rp.role_id
            WHERE r.id = ?
            """;
        System.out.println("[JDBC ROLE SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                Role currentRole = null;
                while (rs.next()) {
                    if (currentRole == null) {
                        currentRole = new Role();
                        currentRole.setId(rs.getLong("id"));
                        currentRole.setLibelle(mapEnum(rs, "libelle", RoleR.class));
                        currentRole.setDescription(rs.getString("description"));
                        currentRole.setDateCreation(getLocalDateTime(rs, "dateCreation"));
                        currentRole.setDateMiseAJour(getLocalDateTime(rs, "dateMiseAJour"));
                        currentRole.setCreePar(rs.getString("creePar"));
                        currentRole.setModifierPar(rs.getString("modifierPar"));
                        currentRole.setPrivileges(new ArrayList<>());
                    }

                    String privilegeName = rs.getString("privilege");
                    if (privilegeName != null) {
                        RolePrivilege rp = new RolePrivilege();
                        rp.setPrivilege(privilegeName);
                        rp.setRole(currentRole);
                        currentRole.getPrivileges().add(rp);
                    }
                }
                return currentRole;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById Role", e);
        }
    }

    @Override
    public Optional<Role> findByLibelle(RoleR libelle) {
        String sql = """
            SELECT r.id, r.libelle, r.description, r.dateCreation, r.dateMiseAJour, r.creePar, r.modifierPar, rp.privilege
            FROM role r
            LEFT JOIN Role_Privilege rp ON r.id = rp.role_id
            WHERE r.libelle = ?
            """;
        System.out.println("[JDBC ROLE SQL] " + sql + " | libelle=" + libelle);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, libelle.name());

            try (ResultSet rs = ps.executeQuery()) {
                Role currentRole = null;
                while (rs.next()) {
                    if (currentRole == null) {
                        currentRole = new Role();
                        currentRole.setId(rs.getLong("id"));
                        currentRole.setLibelle(mapEnum(rs, "libelle", RoleR.class));
                        currentRole.setDescription(rs.getString("description"));
                        currentRole.setDateCreation(getLocalDateTime(rs, "dateCreation"));
                        currentRole.setDateMiseAJour(getLocalDateTime(rs, "dateMiseAJour"));
                        currentRole.setCreePar(rs.getString("creePar"));
                        currentRole.setModifierPar(rs.getString("modifierPar"));
                        currentRole.setPrivileges(new ArrayList<>());
                    }

                    String privilegeName = rs.getString("privilege");
                    if (privilegeName != null) {
                        RolePrivilege rp = new RolePrivilege();
                        rp.setPrivilege(privilegeName);
                        rp.setRole(currentRole);
                        currentRole.getPrivileges().add(rp);
                    }
                }
                return Optional.ofNullable(currentRole);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByLibelle Role", e);
        }
    }

    @Override
    public boolean existsByLibelle(RoleR libelle) {
        String sql = "SELECT 1 FROM role WHERE libelle = ?";
        System.out.println("[JDBC ROLE SQL] " + sql + " | libelle=" + libelle);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, libelle.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur existsByLibelle Role", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM role";
        System.out.println("[JDBC ROLE SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur count Role", e);
        }
    }

    @Override
    public void create(Role r) {
        String sql = "INSERT INTO role(libelle, description, dateCreation, creePar) VALUES(?,?,?,?)";
        System.out.println("[JDBC ROLE SQL] INSERT Role | libelle=" + r.getLibelle());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getLibelle().name());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateCreation() != null ? r.getDateCreation() : LocalDateTime.now()));
            ps.setString(4, r.getCreePar());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }

            insertPrivileges(r, connection);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur create Role", e);
        }
    }

    @Override
    public void update(Role r) {
        String sql = "UPDATE role SET libelle=?, description=?, dateMiseAJour=?, modifierPar=? WHERE id=?";
        System.out.println("[JDBC ROLE SQL] UPDATE Role | id=" + r.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getLibelle().name());
            ps.setString(2, r.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, r.getModifierPar());
            ps.setLong(5, r.getId());
            ps.executeUpdate();

            deletePrivileges(r, connection);
            insertPrivileges(r, connection);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Role", e);
        }
    }

    @Override
    public void delete(Role r) {
        if (r != null) {
            deleteById(r.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM role WHERE id = ?";
        System.out.println("[JDBC ROLE SQL] DELETE Role | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Role", e);
        }
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        String sql = """
            SELECT r.id, r.libelle, r.description, r.dateCreation, r.dateMiseAJour, r.creePar, r.modifierPar, rp.privilege
            FROM role r
            JOIN Utilisateur_Role ur ON r.id = ur.role_id
            LEFT JOIN Role_Privilege rp ON r.id = rp.role_id
            WHERE ur.utilisateur_id = ?
            ORDER BY r.libelle
            """;
        System.out.println("[JDBC ROLE SQL] " + sql + " | userId=" + userId);

        List<Role> roles = new ArrayList<>();
        Role currentRole = null;
        Long currentId = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long roleId = rs.getLong("id");

                    if (currentRole == null || !roleId.equals(currentId)) {
                        currentRole = new Role();
                        currentRole.setId(roleId);
                        currentRole.setLibelle(mapEnum(rs, "libelle", RoleR.class));
                        currentRole.setDescription(rs.getString("description"));
                        currentRole.setDateCreation(getLocalDateTime(rs, "dateCreation"));
                        currentRole.setDateMiseAJour(getLocalDateTime(rs, "dateMiseAJour"));
                        currentRole.setCreePar(rs.getString("creePar"));
                        currentRole.setModifierPar(rs.getString("modifierPar"));
                        currentRole.setPrivileges(new ArrayList<>());

                        roles.add(currentRole);
                        currentId = roleId;
                    }

                    String privilegeName = rs.getString("privilege");
                    if (privilegeName != null) {
                        RolePrivilege rp = new RolePrivilege();
                        rp.setPrivilege(privilegeName);
                        rp.setRole(currentRole);
                        currentRole.getPrivileges().add(rp);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des rôles de l'utilisateur ID=" + userId, e);
        }

        return roles;
    }

    // ========================= Méthodes privées (sans throws SQLException) =========================

    private void insertPrivileges(Role r, Connection c) {
        if (r.getPrivileges() == null || r.getPrivileges().isEmpty()) return;

        String sqlPriv = "INSERT INTO Role_Privilege(role_id, privilege) VALUES(?,?)";
        try (PreparedStatement psPriv = c.prepareStatement(sqlPriv)) {
            for (RolePrivilege rp : r.getPrivileges()) {
                psPriv.setLong(1, r.getId());
                psPriv.setString(2, rp.getPrivilege());
                psPriv.addBatch();
            }
            psPriv.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion des privilèges pour le rôle ID=" + r.getId(), e);
        }
    }

    private void deletePrivileges(Role r, Connection c) {
        try (PreparedStatement psDel = c.prepareStatement("DELETE FROM Role_Privilege WHERE role_id = ?")) {
            psDel.setLong(1, r.getId());
            psDel.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des privilèges du rôle ID=" + r.getId(), e);
        }
    }

    // ========================= Utilitaires =========================

    private static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static <E extends Enum<E>> E mapEnum(ResultSet rs, String column, Class<E> enumClass) throws SQLException {
        String value = rs.getString(column);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }
}