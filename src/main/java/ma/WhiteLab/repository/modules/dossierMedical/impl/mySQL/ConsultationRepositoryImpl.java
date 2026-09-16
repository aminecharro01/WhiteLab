package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationRepositoryImpl implements ConsultationRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String TABLE = "consultation";

    @Override
    public List<Consultation> findAll() {
        String sql = "SELECT * FROM " + TABLE + " ORDER BY date DESC";
        System.out.println("[JDBC CONSULTATION SQL] " + sql);

        List<Consultation> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(RowMappers.mapConsultation(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les consultations", e);
        }

        return result;
    }

    @Override
    public Consultation findById(Long id) {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";
        System.out.println("[JDBC CONSULTATION SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapConsultation(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la consultation ID=" + id, e);
        }
    }

    @Override
    public void create(Consultation c) {
        String sql = """
            INSERT INTO consultation 
            (date, status, notes, observations_medecin,
             dossier_medical_id,
             dateCreation, dateMiseAJour, creePar, modifierPar)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC CONSULTATION SQL] INSERT Consultation | dossierMedicalId=" +
                (c.getDossierMedical() != null ? c.getDossierMedical().getId() : "null"));

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            ps.setTimestamp(1, c.getDate() != null ? Timestamp.valueOf(c.getDate()) : null);
            ps.setString(2, c.getStatus() != null ? c.getStatus().name() : null);
            ps.setString(3, c.getNotes());
            ps.setString(4, c.getObservationsMedecin());

            if (c.getDossierMedical() != null && c.getDossierMedical().getId() != null) {
                ps.setLong(5, c.getDossierMedical().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(c.getDateCreation() != null ? c.getDateCreation() : now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.setString(8, c.getCreePar());
            ps.setString(9, c.getModifierPar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la consultation", e);
        }
    }

    @Override
    public void update(Consultation c) {
        String sql = """
            UPDATE consultation SET 
            date = ?, 
            status = ?, 
            notes = ?, 
            observations_medecin = ?, 
            dossier_medical_id = ?,
            dateMiseAJour = ?, 
            modifierPar = ?
            WHERE id = ?
            """;
        System.out.println("[JDBC CONSULTATION SQL] UPDATE Consultation | id=" + c.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            ps.setTimestamp(1, c.getDate() != null ? Timestamp.valueOf(c.getDate()) : null);
            ps.setString(2, c.getStatus() != null ? c.getStatus().name() : null);
            ps.setString(3, c.getNotes());
            ps.setString(4, c.getObservationsMedecin());

            if (c.getDossierMedical() != null && c.getDossierMedical().getId() != null) {
                ps.setLong(5, c.getDossierMedical().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setString(7, c.getModifierPar() != null ? c.getModifierPar() : "SYSTEM");
            ps.setLong(8, c.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Consultation non trouvée pour mise à jour, ID=" + c.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la consultation ID=" + c.getId(), e);
        }
    }

    @Override
    public void delete(Consultation c) {
        if (c != null && c.getId() != null) {
            deleteById(c.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        System.out.println("[JDBC CONSULTATION SQL] DELETE Consultation | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la consultation ID=" + id, e);
        }
    }

    @Override
    public List<Consultation> findByStatus(String status) {
        String sql = "SELECT * FROM " + TABLE + " WHERE status = ? ORDER BY date DESC";
        System.out.println("[JDBC CONSULTATION SQL] " + sql + " | status=" + status);

        List<Consultation> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(RowMappers.mapConsultation(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par statut : " + status, e);
        }

        return result;
    }

    @Override
    public List<Consultation> findByDossierMedicalId(Long dossierId) {
        String sql = """
            SELECT * FROM consultation 
            WHERE dossier_medical_id = ? 
            ORDER BY date DESC
            """;
        System.out.println("[JDBC CONSULTATION SQL] " + sql + " | dossierId=" + dossierId);

        List<Consultation> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, dossierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(RowMappers.mapConsultation(rs));
                }
            }

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unknown column 'dossier_medical_id'")) {
                System.err.println("AVERTISSEMENT : Colonne 'dossier_medical_id' manquante dans la table consultation");
                return new ArrayList<>();
            }
            throw new RuntimeException("Erreur lors de la recherche par dossier médical ID=" + dossierId, e);
        }

        return result;
    }

    @Override
    public long countByCabinet(Long cabinetId) {
        String sql = """
            SELECT COUNT(c.id)
            FROM consultation c
            JOIN dossiermedical d ON c.dossier_id = d.id
            JOIN staff s ON d.medcine_id = s.id
            WHERE s.cabinetMedicale_id = ?
            """;
        System.out.println("[JDBC CONSULTATION SQL] COUNT by cabinet | cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des consultations par cabinet ID=" + cabinetId, e);
        }
    }

    @Override
    public long countByCabinetAndMonth(Long cabinetId, int year, int month) {
        String sql = """
            SELECT COUNT(c.id)
            FROM consultation c
            JOIN dossiermedical d ON c.dossier_id = d.id
            JOIN staff s ON d.medcine_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND YEAR(c.date_consultation) = ?
              AND MONTH(c.date_consultation) = ?
            """;
        System.out.println("[JDBC CONSULTATION SQL] COUNT by cabinet and month | cabinetId=" + cabinetId +
                ", année=" + year + ", mois=" + month);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des consultations par cabinet et mois", e);
        }
    }

    @Override
    public long countByDateAndCabinet(LocalDate date, Long cabinetId) {
        String sql = """
            SELECT COUNT(c.id)
            FROM consultation c
            JOIN dossiermedical d ON c.dossier_medical_id = d.id
            JOIN staff s ON d.medcine_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND DATE(c.date) = ?
            """;
        System.out.println("[JDBC CONSULTATION SQL] COUNT by date and cabinet | date=" + date + ", cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des consultations par date et cabinet", e);
        }
    }
}