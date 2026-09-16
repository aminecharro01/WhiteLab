package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.FactureRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactureRepositoryImpl implements FactureRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String INSERT_SQL = """
        INSERT INTO facture (totalFact, totalPaye, reste, date, statut,
                             consultation_id, sf_id,
                             dateCreation, creePar)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE facture
        SET totalFact = ?, totalPaye = ?, reste = ?, date = ?, statut = ?,
            consultation_id = ?, sf_id = ?,
            dateMiseAJour = ?, modifierPar = ?
        WHERE id = ?
        """;

    // =========================
    // CRUD
    // =========================

    @Override
    public List<Facture> findAll() {
        String sql = "SELECT * FROM facture ORDER BY date DESC";
        System.out.println("[JDBC FACTURE SQL] " + sql);

        List<Facture> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(RowMappers.mapFacture(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les factures", e);
        }

        return list;
    }

    @Override
    public Facture findById(Long id) {
        String sql = "SELECT * FROM facture WHERE id = ?";
        System.out.println("[JDBC FACTURE SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapFacture(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la facture par ID : " + id, e);
        }
    }

    @Override
    public void create(Facture f) {
        System.out.println("[JDBC FACTURE SQL] INSERT Facture | totalFact=" + f.getTotalFact());

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            ps.setDouble(1, f.getTotalFact());
            ps.setDouble(2, f.getTotalPaye());
            ps.setDouble(3, f.getReste());

            if (f.getDate() != null) {
                ps.setDate(4, Date.valueOf(f.getDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setString(5, f.getStatut() != null ? f.getStatut().name() : null);

            ps.setObject(6,
                    f.getConsultation() != null ? f.getConsultation().getId() : null,
                    Types.BIGINT);

            ps.setObject(7,
                    f.getSf() != null ? f.getSf().getId() : null,
                    Types.BIGINT);

            ps.setTimestamp(8, Timestamp.valueOf(f.getDateCreation() != null ? f.getDateCreation() : now));
            ps.setString(9, f.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    f.setId(keys.getLong(1));
                }
            }

            if (f.getDateCreation() == null) {
                f.setDateCreation(now);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la facture", e);
        }
    }

    @Override
    public void update(Facture f) {
        System.out.println("[JDBC FACTURE SQL] UPDATE Facture | id=" + f.getId());

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setDouble(1, f.getTotalFact());
            ps.setDouble(2, f.getTotalPaye());
            ps.setDouble(3, f.getReste());

            if (f.getDate() != null) {
                ps.setDate(4, Date.valueOf(f.getDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setString(5, f.getStatut() != null ? f.getStatut().name() : null);

            ps.setObject(6,
                    f.getConsultation() != null ? f.getConsultation().getId() : null,
                    Types.BIGINT);

            ps.setObject(7,
                    f.getSf() != null ? f.getSf().getId() : null,
                    Types.BIGINT);

            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(9, f.getModifierPar());
            ps.setLong(10, f.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Facture non trouvée pour mise à jour (id=" + f.getId() + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la facture ID=" + f.getId(), e);
        }
    }

    @Override
    public void delete(Facture f) {
        if (f != null && f.getId() != null) {
            deleteById(f.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM facture WHERE id = ?";
        System.out.println("[JDBC FACTURE SQL] DELETE Facture | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la facture ID=" + id, e);
        }
    }

    @Override
    public List<Facture> findByConsultationId(Long consultationId) {
        String sql = "SELECT * FROM facture WHERE consultation_id = ? ORDER BY date DESC";
        System.out.println("[JDBC FACTURE SQL] " + sql + " | consultationId=" + consultationId);

        List<Facture> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, consultationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapFacture(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des factures par consultation ID=" + consultationId, e);
        }

        return list;
    }

    @Override
    public List<Facture> findByStatut(String statut) {
        String sql = "SELECT * FROM facture WHERE statut = ? ORDER BY date DESC";
        System.out.println("[JDBC FACTURE SQL] " + sql + " | statut=" + statut);

        List<Facture> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapFacture(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des factures par statut : " + statut, e);
        }

        return list;
    }

    @Override
    public List<Facture> findByDateBetween(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM facture WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        System.out.println("[JDBC FACTURE SQL] " + sql + " | du " + start + " au " + end);

        List<Facture> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapFacture(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des factures par période", e);
        }

        return list;
    }

    // =========================
    // DASHBOARD CABINET
    // =========================

    @Override
    public double sumChiffreAffaireByDateAndCabinet(LocalDate date, Long cabinetId) {
        String sql = """
            SELECT COALESCE(SUM(f.totalPaye),0)
            FROM facture f
            JOIN Consultation c ON f.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND DATE(f.date) = ?
        """;
        System.out.println("[JDBC FACTURE SQL] Chiffre d'affaires par date et cabinet | date=" + date + ", cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du chiffre d'affaires par date et cabinet", e);
        }
    }

    @Override
    public double sumChiffreAffaireByCabinet(Long cabinetId) {
        String sql = """
            SELECT COALESCE(SUM(f.totalPaye),0)
            FROM facture f
            JOIN Consultation c ON f.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
        """;
        System.out.println("[JDBC FACTURE SQL] Chiffre d'affaires total par cabinet | cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du chiffre d'affaires total par cabinet", e);
        }
    }

    @Override
    public double sumChiffreAffaireByCabinetAndMonth(Long cabinetId, int year, int month) {
        String sql = """
            SELECT COALESCE(SUM(f.totalPaye),0)
            FROM facture f
            JOIN Consultation c ON f.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND YEAR(f.date) = ?
              AND MONTH(f.date) = ?
        """;
        System.out.println("[JDBC FACTURE SQL] Chiffre d'affaires par mois et cabinet | cabinetId=" + cabinetId +
                ", année=" + year + ", mois=" + month);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du chiffre d'affaires par mois et cabinet", e);
        }
    }

    @Override
    public long countFacturesByCabinet(Long cabinetId) {
        String sql = """
            SELECT COUNT(f.id)
            FROM facture f
            JOIN Consultation c ON f.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
        """;
        System.out.println("[JDBC FACTURE SQL] Count factures par cabinet | cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des factures par cabinet", e);
        }
    }
}