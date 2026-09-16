package ma.WhiteLab.repository.modules.agenda.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RendezVousRepositoryImpl implements RendezVousRepository {

    private final Connection connection;

    // =========================
    //   Base SELECT with JOIN
    // =========================
    private static final String BASE_SELECT =
            """
            SELECT
                r.*,
                d.id AS d_id,
                p.id AS p_id,
                p.nom AS p_nom,
                p.prenom AS p_prenom
            FROM rendezvous r
            LEFT JOIN dossiermedical d ON r.dossier_med_id = d.id
            LEFT JOIN patient p ON d.pat_id = p.id
            """;

    // =============================================================================
    //                             BASIC CRUD
    // =============================================================================

    @Override
    public List<RendezVous> findAll() {
        String sql = BASE_SELECT;

        List<RendezVous> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(RowMappers.mapRendezVous(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération tous les RDV", e);
        }
        return out;
    }

    @Override
    public RendezVous findById(Long id) {
        String sql = BASE_SELECT + " WHERE r.id = ?";
        System.out.println("[JDBC RENDEZVOUS SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapRendezVous(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche RDV ID: " + id, e);
        }
        return null;
    }

    @Override
    public void create(RendezVous r) {
        String sql = """
            INSERT INTO rendezvous (
                dateRDV, heure_rdv, motif, status, note_medecin,
                dossier_med_id, consultation_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        System.out.println("[JDBC RENDEZVOUS SQL] INSERT | dateRDV=" + r.getDate());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, r.getDate() != null ? Timestamp.valueOf(r.getDate()) : null);
            ps.setTime(2, r.getTime() != null ? Time.valueOf(r.getTime()) : null);
            ps.setString(3, r.getMotif());
            ps.setString(4, r.getStatus() != null ? r.getStatus().name() : null);
            ps.setString(5, r.getNoteMedecin());
            ps.setObject(6, r.getDossierMed() != null ? r.getDossierMed().getId() : null, Types.BIGINT);
            ps.setObject(7, r.getConsultation() != null ? r.getConsultation().getId() : null, Types.BIGINT);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur création RDV", e);
        }
    }

    @Override
    public void update(RendezVous r) {
        String sql = """
            UPDATE rendezvous
            SET dateRDV = ?, heure_rdv = ?, motif = ?, status = ?,
                note_medecin = ?, dossier_med_id = ?, consultation_id = ?
            WHERE id = ?
            """;

        System.out.println("[JDBC RENDEZVOUS SQL] UPDATE | id=" + r.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, r.getDate() != null ? Timestamp.valueOf(r.getDate()) : null);
            ps.setTime(2, r.getTime() != null ? Time.valueOf(r.getTime()) : null);
            ps.setString(3, r.getMotif());
            ps.setString(4, r.getStatus() != null ? r.getStatus().name() : null);
            ps.setString(5, r.getNoteMedecin());
            ps.setObject(6, r.getDossierMed() != null ? r.getDossierMed().getId() : null, Types.BIGINT);
            ps.setObject(7, r.getConsultation() != null ? r.getConsultation().getId() : null, Types.BIGINT);
            ps.setLong(8, r.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour RDV id=" + r.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM rendezvous WHERE id = ?";
        System.out.println("[JDBC RENDEZVOUS SQL] DELETE | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression RDV id=" + id, e);
        }
    }

    @Override
    public void delete(RendezVous r) {
        if (r != null && r.getId() != null) {
            deleteById(r.getId());
        }
    }

    // =============================================================================
    //                             FILTERS
    // =============================================================================

    @Override
    public List<RendezVous> findByDossierMedId(Long dossierMedId) {
        String sql = BASE_SELECT + " WHERE r.dossier_med_id = ?";
        return executeListQuery(sql, ps -> ps.setLong(1, dossierMedId));
    }

    @Override
    public List<RendezVous> findByConsultationId(Long consultationId) {
        String sql = BASE_SELECT + " WHERE r.consultation_id = ?";
        return executeListQuery(sql, ps -> ps.setLong(1, consultationId));
    }

    @Override
    public List<RendezVous> findByStatus(String status) {
        String sql = BASE_SELECT + " WHERE r.status = ?";
        return executeListQuery(sql, ps -> ps.setString(1, status));
    }

    // =============================================================================
    //                       DASHBOARD STATISTICS
    // =============================================================================

    @Override
    public long countByCabinet(Long cabinetId) {
        String sql = """
            SELECT COUNT(r.id)
            FROM rendezvous r
            JOIN dossiermedical d ON r.dossier_med_id = d.id
            JOIN Utilisateur u ON d.medecin_id = u.id
            WHERE u.cabinetMedicale_id = ?
            """;
        return executeCountQuery(sql, ps -> ps.setLong(1, cabinetId));
    }

    @Override
    public long countByCabinetAndMonth(Long cabinetId, int year, int month) {
        String sql = """
            SELECT COUNT(r.id)
            FROM rendezvous r
            JOIN dossiermedical d ON r.dossier_med_id = d.id
            JOIN Utilisateur u ON d.medecin_id = u.id
            WHERE u.cabinetMedicale_id = ?
              AND YEAR(r.dateRDV) = ?
              AND MONTH(r.dateRDV) = ?
            """;
        return executeCountQuery(sql, ps -> {
            ps.setLong(1, cabinetId);
            ps.setInt(2, year);
            ps.setInt(3, month);
        });
    }

    @Override
    public long countByDateAndCabinet(LocalDate date, Long cabinetId) {
        String sql = """
            SELECT COUNT(r.id)
            FROM rendezvous r
            JOIN dossiermedical d ON r.dossier_med_id = d.id
            JOIN Utilisateur u ON d.medecin_id = u.id
            WHERE u.cabinetMedicale_id = ?
              AND DATE(r.dateRDV) = ?
            """;
        return executeCountQuery(sql, ps -> {
            ps.setLong(1, cabinetId);
            ps.setDate(2, Date.valueOf(date));
        });
    }

    @Override
    public long countRdvBetweenDatesAndCabinet(LocalDate debut, LocalDate fin, Long cabinetId) {
        String sql = """
            SELECT COUNT(r.id)
            FROM rendezvous r
            JOIN dossiermedical d ON r.dossier_med_id = d.id
            JOIN Utilisateur u ON d.medecin_id = u.id
            WHERE u.cabinetMedicale_id = ?
              AND DATE(r.dateRDV) BETWEEN ? AND ?
              AND r.status = 'CONFIRME'
            """;
        return executeCountQuery(sql, ps -> {
            ps.setLong(1, cabinetId);
            ps.setDate(2, Date.valueOf(debut));
            ps.setDate(3, Date.valueOf(fin));
        });
    }

    @Override
    public double calculateTauxAnnulationCeMois(Long cabinetId) {
        String sql = """
            SELECT 
                (SUM(CASE WHEN r.status = 'ANNULE' THEN 1 ELSE 0 END) * 100.0) / COUNT(r.id)
            FROM rendezvous r
            JOIN dossiermedical d ON r.dossier_med_id = d.id
            JOIN Utilisateur u ON d.medecin_id = u.id
            WHERE u.cabinetMedicale_id = ?
              AND MONTH(r.dateRDV) = MONTH(CURDATE())
              AND YEAR(r.dateRDV) = YEAR(CURDATE())
            """;
        return executeDoubleQuery(sql, ps -> ps.setLong(1, cabinetId));
    }

    @Override
    public long countCreneauxDisponiblesSemaine(Long cabinetId) {
        String sql = """
            SELECT COUNT(u.id) * 40
            FROM Utilisateur u
            WHERE u.cabinetMedicale_id = ?
              AND u.type = 'MEDECIN'
            """;
        return executeCountQuery(sql, ps -> ps.setLong(1, cabinetId));
    }

    // =============================================================================
    //                  AGENDA METHODS
    // =============================================================================

    @Override
    public List<LocalDate> findDaysWithAppointments(Long medecinId, LocalDate start, LocalDate end) {
        String sql = """
            SELECT DISTINCT DATE(r.dateRDV) AS jour
            FROM rendezvous r
            INNER JOIN DossierMedical d ON r.dossier_med_id = d.id
            WHERE d.medecin_id = ?
              AND DATE(r.dateRDV) BETWEEN ? AND ?
              AND r.status NOT IN ('ANNULE', 'SUPPRIME', 'CANCELLED')
            ORDER BY jour
            """;

        List<LocalDate> days = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    days.add(rs.getDate("jour").toLocalDate());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération jours avec RDV", e);
        }
        return days;
    }

    @Override
    public List<Object[]> countAppointmentsPerDay(Long medecinId, LocalDate start, LocalDate end) {
        String sql = """
            SELECT DATE(r.dateRDV) AS jour, COUNT(*) AS nb
            FROM rendezvous r
            INNER JOIN DossierMedical d ON r.dossier_med_id = d.id
            WHERE d.medecin_id = ?
              AND DATE(r.dateRDV) BETWEEN ? AND ?
              AND r.status NOT IN ('ANNULE', 'SUPPRIME', 'CANCELLED')
            GROUP BY jour
            ORDER BY jour
            """;

        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            rs.getDate("jour").toLocalDate(),
                            rs.getLong("nb")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur comptage RDV par jour", e);
        }
        return results;
    }

    @Override
    public Boolean existsByMedecinIdAndDateTime(Long medecinId, LocalDateTime dateTime) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM rendezvous r
                INNER JOIN DossierMedical d ON r.dossier_med_id = d.id
                WHERE d.medecin_id = ?
                  AND r.dateRDV = ?
                  AND r.status NOT IN ('ANNULE', 'SUPPRIME', 'CANCELLED')
            )
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setTimestamp(2, Timestamp.valueOf(dateTime));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur vérification créneau exact", e);
        }
    }

    @Override
    public long countByMedecinIdAndDate(Long medecinId, LocalDate date) {
        String sql = """
            SELECT COUNT(*)
            FROM rendezvous r
            INNER JOIN DossierMedical d ON r.dossier_med_id = d.id
            WHERE d.medecin_id = ?
              AND DATE(r.dateRDV) = ?
              AND r.status NOT IN ('ANNULE', 'SUPPRIME', 'CANCELLED')
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur comptage RDV pour date", e);
        }
    }

    @Override
    public List<RendezVous> getRendezVousByMedecinAndPeriod(Long medecinId, LocalDate start, LocalDate end) {
        if (medecinId == null || start == null || end == null || start.isAfter(end)) {
            return new ArrayList<>();
        }

        String sql = """
        SELECT
            r.*,
            d.id AS d_id,
            d.pat_id,
            p.id AS p_id,
            p.nom AS p_nom,
            p.prenom AS p_prenom
        FROM rendezvous r
        LEFT JOIN dossiermedical d ON r.dossier_med_id = d.id
        LEFT JOIN patient p ON d.pat_id = p.id
        WHERE d.medecin_id = ?
          AND r.dateRDV BETWEEN ? AND ?
          AND r.status NOT IN ('ANNULE', 'SUPPRIME')
        ORDER BY r.dateRDV ASC
        """;

        System.out.println("[DEBUG] SQL: " + sql);
        System.out.println("[DEBUG] Params: medecinId=" + medecinId + ", start=" + start.atStartOfDay() + ", end=" + end.atTime(23, 59, 59));

        List<RendezVous> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setTimestamp(2, Timestamp.valueOf(start.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(end.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(RowMappers.mapRendezVous(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération RDV par médecin et période", e);
        }

        return result;
    }


    @Override
    public List<RendezVous> findByDossierIdsAndDateBetween(List<Long> dossierIds, LocalDateTime start, LocalDateTime end) {
        if (dossierIds == null || dossierIds.isEmpty() || start == null || end == null || start.isAfter(end)) {
            return new ArrayList<>();
        }

        String placeholders = dossierIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = BASE_SELECT + """
            WHERE r.dossier_med_id IN (%s)
              AND r.dateRDV BETWEEN ? AND ?
              AND r.status NOT IN ('ANNULE', 'SUPPRIME')
            ORDER BY r.dateRDV ASC
            """.formatted(placeholders);

        List<RendezVous> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            for (Long dossierId : dossierIds) {
                ps.setLong(index++, dossierId);
            }

            ps.setTimestamp(index++, Timestamp.valueOf(start));
            ps.setTimestamp(index, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(RowMappers.mapRendezVous(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération RDV par dossiers et période", e);
        }

        return result;
    }

    // =============================================================================
    //                               HELPERS
    // =============================================================================

    private List<RendezVous> executeListQuery(String sql, SqlSetter setter) {
        List<RendezVous> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapRendezVous(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur exécution requête liste", e);
        }
        return list;
    }

    private long executeCountQuery(String sql, SqlSetter setter) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur exécution count query", e);
        }
    }

    private double executeDoubleQuery(String sql, SqlSetter setter) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur exécution double query", e);
        }
    }

    @FunctionalInterface
    private interface SqlSetter {
        void set(PreparedStatement ps) throws SQLException;
    }
}
