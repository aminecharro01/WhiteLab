package ma.WhiteLab.repository.modules.cabinet.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.cabinet.api.CabinetMedicaleRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabinetMedicaleRepositoryImpl implements CabinetMedicaleRepository {

    private Connection connection; // Injectée par ApplicationContext


    @Override
    public List<CabinetMedicale> findAll() {
        String sql = "SELECT * FROM CabinetMedicale";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql);

        List<CabinetMedicale> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapCabinetMedical(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les cabinets médicaux", e);
        }

        return out;
    }

    @Override
    public CabinetMedicale findById(Long id) {
        String sql = "SELECT * FROM CabinetMedicale WHERE id = ?";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapCabinetMedical(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du cabinet médical par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(CabinetMedicale c) {
        String sql = """
            INSERT INTO CabinetMedicale(
                nom, email, logo, categorie, tel1, tel2, siteWeb, instagram, facebook, description, dateCreation, creePar
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC CABINET MEDICAL SQL] INSERT CabinetMedicale | nom=" + c.getNom());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getLogo());
            ps.setString(4, c.getCategorie());
            ps.setString(5, c.getTel1());
            ps.setString(6, c.getTel2());
            ps.setString(7, c.getSiteWeb());
            ps.setString(8, c.getInstagram());
            ps.setString(9, c.getFacebook());
            ps.setString(10, c.getDescription());
            ps.setTimestamp(11, Timestamp.valueOf(c.getDateCreation() != null ? c.getDateCreation() : LocalDateTime.now()));
            ps.setString(12, c.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du cabinet médical", e);
        }
    }

    @Override
    public void update(CabinetMedicale c) {
        String sql = """
            UPDATE CabinetMedicale
            SET nom=?, email=?, logo=?, categorie=?, tel1=?, tel2=?, siteWeb=?, instagram=?, facebook=?, description=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        System.out.println("[JDBC CABINET MEDICAL SQL] UPDATE CabinetMedicale | id=" + c.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getLogo());
            ps.setString(4, c.getCategorie());
            ps.setString(5, c.getTel1());
            ps.setString(6, c.getTel2());
            ps.setString(7, c.getSiteWeb());
            ps.setString(8, c.getInstagram());
            ps.setString(9, c.getFacebook());
            ps.setString(10, c.getDescription());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(12, c.getModifierPar());
            ps.setLong(13, c.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du cabinet médical ID=" + c.getId(), e);
        }
    }

    @Override
    public void delete(CabinetMedicale c) {
        if (c != null && c.getId() != null) {
            deleteById(c.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM CabinetMedicale WHERE id = ?";
        System.out.println("[JDBC CABINET MEDICAL SQL] DELETE CabinetMedicale | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du cabinet médical ID=" + id, e);
        }
    }

    @Override
    public Optional<CabinetMedicale> findByNom(String nom) {
        String sql = "SELECT * FROM CabinetMedicale WHERE nom = ?";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql + " | nom=" + nom);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapCabinetMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du cabinet par nom : " + nom, e);
        }

        return Optional.empty();
    }

    @Override
    public List<CabinetMedicale> findByCategorie(String categorie) {
        String sql = "SELECT * FROM CabinetMedicale WHERE categorie = ?";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql + " | categorie=" + categorie);

        List<CabinetMedicale> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorie);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapCabinetMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des cabinets par catégorie : " + categorie, e);
        }

        return out;
    }

    @Override
    public Optional<CabinetMedicale> findByEmail(String email) {
        String sql = "SELECT * FROM CabinetMedicale WHERE email = ?";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql + " | email=" + email);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapCabinetMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du cabinet par email : " + email, e);
        }

        return Optional.empty();
    }

    @Override
    public List<CabinetMedicale> findAllOrderByNom() {
        String sql = "SELECT * FROM CabinetMedicale ORDER BY nom";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql);

        List<CabinetMedicale> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapCabinetMedical(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des cabinets triés par nom", e);
        }

        return out;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM CabinetMedicale";
        System.out.println("[JDBC CABINET MEDICAL SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des cabinets médicaux", e);
        }

        return 0;
    }
}