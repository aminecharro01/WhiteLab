package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.OrdonnanceRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdonnanceRepositoryImpl implements OrdonnanceRepository {

    private Connection connection; // Injectée par ApplicationContext

    // ------------------------------------------------------
    //                      CRUD
    // ------------------------------------------------------

    @Override
    public List<Ordonnance> findAll() {
        String sql = "SELECT * FROM ordonnance";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql);

        List<Ordonnance> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapOrdonnance(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les ordonnances", e);
        }

        return out;
    }

    @Override
    public Ordonnance findById(Long id) {
        String sql = "SELECT * FROM ordonnance WHERE id = ?";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapOrdonnance(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'ordonnance par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Ordonnance o) {
        String sql = """
            INSERT INTO ordonnance(date_ordonnance, consultation_id, dossier_med_id)
            VALUES (?, ?, ?)
            """;
        System.out.println("[JDBC ORDONNANCE SQL] INSERT Ordonnance | date=" + o.getDateOrdonnance());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, o.getDateOrdonnance() != null ? Date.valueOf(o.getDateOrdonnance()) : null);

            ps.setObject(2, o.getConsultation() != null ? o.getConsultation().getId() : null, Types.BIGINT);
            ps.setObject(3, o.getDossierMedical() != null ? o.getDossierMedical().getId() : null, Types.BIGINT);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    o.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'ordonnance", e);
        }
    }

    @Override
    public void update(Ordonnance o) {
        String sql = """
            UPDATE ordonnance
            SET date_ordonnance=?, consultation_id=?, dossier_med_id=?
            WHERE id=?
            """;
        System.out.println("[JDBC ORDONNANCE SQL] UPDATE Ordonnance | id=" + o.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDate(1, o.getDateOrdonnance() != null ? Date.valueOf(o.getDateOrdonnance()) : null);

            ps.setObject(2, o.getConsultation() != null ? o.getConsultation().getId() : null, Types.BIGINT);
            ps.setObject(3, o.getDossierMedical() != null ? o.getDossierMedical().getId() : null, Types.BIGINT);
            ps.setLong(4, o.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'ordonnance ID=" + o.getId(), e);
        }
    }

    @Override
    public void delete(Ordonnance o) {
        if (o != null && o.getId() != null) {
            deleteById(o.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM ordonnance WHERE id = ?";
        System.out.println("[JDBC ORDONNANCE SQL] DELETE Ordonnance | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'ordonnance ID=" + id, e);
        }
    }

    // ------------------------------------------------------
    //              MÉTHODES SPÉCIFIQUES
    // ------------------------------------------------------

    @Override
    public List<Ordonnance> findByDossierId(Long dossierId) {
        String sql = "SELECT * FROM ordonnance WHERE dossier_med_id = ?";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql + " | dossierId=" + dossierId);

        List<Ordonnance> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, dossierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapOrdonnance(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ordonnances par dossier médical ID=" + dossierId, e);
        }

        return out;
    }

    @Override
    public List<Ordonnance> findByConsultationId(Long consultationId) {
        String sql = "SELECT * FROM ordonnance WHERE consultation_id = ?";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql + " | consultationId=" + consultationId);

        List<Ordonnance> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, consultationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapOrdonnance(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ordonnances par consultation ID=" + consultationId, e);
        }

        return out;
    }

    @Override
    public List<Ordonnance> findByDate(LocalDate date) {
        String sql = "SELECT * FROM ordonnance WHERE date_ordonnance = ?";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql + " | date=" + date);

        List<Ordonnance> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapOrdonnance(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ordonnances par date : " + date, e);
        }

        return out;
    }

    @Override
    public List<Ordonnance> findBetweenDates(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM ordonnance WHERE date_ordonnance BETWEEN ? AND ?";
        System.out.println("[JDBC ORDONNANCE SQL] " + sql + " | du " + start + " au " + end);

        List<Ordonnance> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapOrdonnance(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ordonnances entre les dates " + start + " et " + end, e);
        }

        return out;
    }
}