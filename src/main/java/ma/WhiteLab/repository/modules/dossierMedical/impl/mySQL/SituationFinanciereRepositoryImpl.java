package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.SituationFinanciereRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SituationFinanciereRepositoryImpl implements SituationFinanciereRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String INSERT_SQL = """
        INSERT INTO SituationFinanciere (
            totalDesActes, totalPaye, credit, enPromo, status,
            dossierMedical_id, dateCreation, creePar
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE SituationFinanciere
        SET totalDesActes = ?, totalPaye = ?, credit = ?,
            enPromo = ?, status = ?, dossierMedical_id = ?,
            dateMiseAJour = ?, modifierPar = ?
        WHERE id = ?
        """;

    @Override
    public List<SituationFinanciere> findAll() {
        String sql = "SELECT * FROM SituationFinanciere ORDER BY dateCreation DESC";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] " + sql);

        List<SituationFinanciere> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(RowMappers.mapSituationFinanciere(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les situations financières", e);
        }

        return list;
    }

    @Override
    public SituationFinanciere findById(Long id) {
        String sql = "SELECT * FROM SituationFinanciere WHERE id = ?";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapSituationFinanciere(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la situation financière par ID : " + id, e);
        }
    }

    @Override
    public void create(SituationFinanciere sf) {
        System.out.println("[JDBC SITUATION FINANCIERE SQL] INSERT SituationFinanciere | totalDesActes=" + sf.getTotalDesActes());

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setFloat(1, sf.getTotalDesActes());
            ps.setFloat(2, sf.getTotalPaye());
            ps.setFloat(3, sf.getCredit());

            ps.setString(4, sf.getEnPromo() != null ? sf.getEnPromo().name() : null);
            ps.setString(5, sf.getStatus() != null ? sf.getStatus().name() : null);

            Long dossierId = sf.getDossierMedical() != null ? sf.getDossierMedical().getId() : null;
            if (dossierId != null) {
                ps.setLong(6, dossierId);
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(7, Timestamp.valueOf(sf.getDateCreation() != null ? sf.getDateCreation() : now));
            ps.setString(8, sf.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    sf.setId(keys.getLong(1));
                }
            }

            if (sf.getDateCreation() == null) {
                sf.setDateCreation(now);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la situation financière pour le dossier ID=" +
                    (sf.getDossierMedical() != null ? sf.getDossierMedical().getId() : "null"), e);
        }
    }

    @Override
    public void update(SituationFinanciere sf) {
        System.out.println("[JDBC SITUATION FINANCIERE SQL] UPDATE SituationFinanciere | id=" + sf.getId());

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setFloat(1, sf.getTotalDesActes());
            ps.setFloat(2, sf.getTotalPaye());
            ps.setFloat(3, sf.getCredit());

            ps.setString(4, sf.getEnPromo() != null ? sf.getEnPromo().name() : null);
            ps.setString(5, sf.getStatus() != null ? sf.getStatus().name() : null);

            Long dossierId = sf.getDossierMedical() != null ? sf.getDossierMedical().getId() : null;
            if (dossierId != null) {
                ps.setLong(6, dossierId);
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, sf.getModifierPar());
            ps.setLong(9, sf.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Situation financière non trouvée pour mise à jour (id=" + sf.getId() + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la situation financière ID=" + sf.getId(), e);
        }
    }

    @Override
    public void delete(SituationFinanciere sf) {
        if (sf != null && sf.getId() != null) {
            deleteById(sf.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM SituationFinanciere WHERE id = ?";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] DELETE SituationFinanciere | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la situation financière ID=" + id, e);
        }
    }

    @Override
    public List<SituationFinanciere> findByDossierId(Long dossierId) {
        String sql = "SELECT * FROM SituationFinanciere WHERE dossierMedical_id = ? ORDER BY dateCreation DESC";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] " + sql + " | dossierId=" + dossierId);

        List<SituationFinanciere> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, dossierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapSituationFinanciere(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des situations financières par dossier ID=" + dossierId, e);
        }

        return list;
    }

    @Override
    public List<SituationFinanciere> findByStatus(String status) {
        String sql = "SELECT * FROM SituationFinanciere WHERE status = ? ORDER BY dateCreation DESC";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] " + sql + " | status=" + status);

        List<SituationFinanciere> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapSituationFinanciere(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des situations financières par statut : " + status, e);
        }

        return list;
    }

    @Override
    public List<SituationFinanciere> findByPromo(String promoStatus) {
        String sql = "SELECT * FROM SituationFinanciere WHERE enPromo = ? ORDER BY dateCreation DESC";
        System.out.println("[JDBC SITUATION FINANCIERE SQL] " + sql + " | enPromo=" + promoStatus);

        List<SituationFinanciere> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, promoStatus);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapSituationFinanciere(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des situations financières par promo : " + promoStatus, e);
        }

        return list;
    }
}