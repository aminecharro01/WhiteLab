package ma.WhiteLab.repository.modules.cabinet.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.cabinet.Statistiques;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.cabinet.api.StatistiquesRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class StatistiquesRepositoryImpl implements StatistiquesRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<Statistiques> findAll() {
        String sql = "SELECT * FROM statistiques ORDER BY dateCalcul DESC";
        System.out.println("[JDBC STATISTIQUES SQL] " + sql);

        List<Statistiques> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapStatistiques(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les statistiques", e);
        }

        return out;
    }

    @Override
    public Statistiques findById(Long id) {
        String sql = "SELECT * FROM statistiques WHERE id = ?";
        System.out.println("[JDBC STATISTIQUES SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapStatistiques(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la statistique par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Statistiques s) {
        String sql = """
            INSERT INTO statistiques(
                nom, categorie, chiffre, dateCalcul, cabinet_id, dateCreation, creePar
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC STATISTIQUES SQL] INSERT Statistiques | nom=" + s.getNom() + ", chiffre=" + s.getChiffre());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getNom());
            ps.setString(2, s.getCategorie() != null ? s.getCategorie().name() : null);
            ps.setDouble(3, s.getChiffre() != null ? s.getChiffre() : 0.0);

            if (s.getDateCalcul() != null) {
                ps.setDate(4, Date.valueOf(s.getDateCalcul()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            Long cabinetId = s.getCabinetMedicale() != null ? s.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(s.getDateCreation() != null ? s.getDateCreation() : LocalDateTime.now()));
            ps.setString(7, s.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la statistique pour le cabinet ID=" +
                    (s.getCabinetMedicale() != null ? s.getCabinetMedicale().getId() : "null"), e);
        }
    }

    @Override
    public void update(Statistiques s) {
        String sql = """
            UPDATE statistiques
            SET nom=?, categorie=?, chiffre=?, dateCalcul=?, cabinet_id=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        System.out.println("[JDBC STATISTIQUES SQL] UPDATE Statistiques | id=" + s.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setString(2, s.getCategorie() != null ? s.getCategorie().name() : null);
            ps.setDouble(3, s.getChiffre() != null ? s.getChiffre() : 0.0);

            if (s.getDateCalcul() != null) {
                ps.setDate(4, Date.valueOf(s.getDateCalcul()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            Long cabinetId = s.getCabinetMedicale() != null ? s.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, s.getModifierPar());
            ps.setLong(8, s.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la statistique ID=" + s.getId(), e);
        }
    }

    @Override
    public void delete(Statistiques s) {
        if (s != null && s.getId() != null) {
            deleteById(s.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM statistiques WHERE id = ?";
        System.out.println("[JDBC STATISTIQUES SQL] DELETE Statistiques | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la statistique ID=" + id, e);
        }
    }

    @Override
    public List<Statistiques> findByCabinetId(Long cabinetId) {
        String sql = "SELECT * FROM statistiques WHERE cabinet_id = ? ORDER BY dateCalcul DESC";
        System.out.println("[JDBC STATISTIQUES SQL] " + sql + " | cabinetId=" + cabinetId);

        List<Statistiques> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapStatistiques(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des statistiques par cabinet ID=" + cabinetId, e);
        }

        return out;
    }
}