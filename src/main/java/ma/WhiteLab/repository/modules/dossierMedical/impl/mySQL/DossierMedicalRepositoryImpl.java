package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DossierMedicalRepositoryImpl implements DossierMedicalRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<DossierMedical> findAll() {
        String sql = "SELECT * FROM dossiermedical ORDER BY id DESC";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql);

        List<DossierMedical> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapDossierMedical(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les dossiers médicaux", e);
        }

        return out;
    }

    @Override
    public DossierMedical findById(Long id) {
        String sql = "SELECT * FROM dossiermedical WHERE id = ?";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapDossierMedical(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du dossier médical par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(DossierMedical d) {
        String sql = """
            INSERT INTO dossiermedical(historique, pat_id, medecin_id, dateCreation, creePar)
            VALUES(?,?,?,?,?)
            """;
        System.out.println("[JDBC DOSSIER MEDICAL SQL] INSERT DossierMedical | patId=" +
                (d.getPat() != null ? d.getPat().getId() : "null"));

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getHistorique());

            if (d.getPat() != null && d.getPat().getId() != null) {
                ps.setLong(2, d.getPat().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            if (d.getMedecine() != null && d.getMedecine().getId() != null) {
                ps.setLong(3, d.getMedecine().getId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setTimestamp(4, Timestamp.valueOf(d.getDateCreation() != null ? d.getDateCreation() : LocalDateTime.now()));
            ps.setString(5, d.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    d.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du dossier médical", e);
        }
    }

    @Override
    public void update(DossierMedical d) {
        String sql = """
            UPDATE dossiermedical
            SET historique=?, pat_id=?, medecin_id=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        System.out.println("[JDBC DOSSIER MEDICAL SQL] UPDATE DossierMedical | id=" + d.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, d.getHistorique());

            if (d.getPat() != null && d.getPat().getId() != null) {
                ps.setLong(2, d.getPat().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            if (d.getMedecine() != null && d.getMedecine().getId() != null) {
                ps.setLong(3, d.getMedecine().getId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, d.getModifierPar());
            ps.setLong(6, d.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du dossier médical ID=" + d.getId(), e);
        }
    }

    @Override
    public void delete(DossierMedical d) {
        if (d != null && d.getId() != null) {
            deleteById(d.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM dossiermedical WHERE id = ?";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] DELETE DossierMedical | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du dossier médical ID=" + id, e);
        }
    }

    // --- Extras ---

    @Override
    public Optional<DossierMedical> findByPatientId(Long patientId) {
        String sql = "SELECT * FROM dossiermedical WHERE pat_id = ?";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | patientId=" + patientId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapDossierMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du dossier médical par patient ID=" + patientId, e);
        }

        return Optional.empty();
    }

    @Override
    public List<DossierMedical> findByPatientName(String nomOrPrenom) {
        String sql = """
            SELECT dm.* 
            FROM DossierMedical dm 
            JOIN Patient p ON dm.pat_id = p.id 
            WHERE p.nom LIKE ? OR p.prenom LIKE ?
            """;
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | nomOrPrenom=" + nomOrPrenom);

        List<DossierMedical> out = new ArrayList<>();
        String searchPattern = "%" + nomOrPrenom + "%";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapDossierMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du dossier par nom/prénom : " + nomOrPrenom, e);
        }

        return out;
    }

    @Override
    public List<DossierMedical> searchByHistorique(String keyword) {
        String sql = "SELECT * FROM DossierMedical WHERE historique LIKE ?";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | keyword=" + keyword);

        List<DossierMedical> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapDossierMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche dans l'historique avec mot-clé : " + keyword, e);
        }

        return out;
    }

    @Override
    public List<DossierMedical> findRecent(int days) {
        String sql = "SELECT * FROM DossierMedical WHERE dateCreation >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | days=" + days);

        List<DossierMedical> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, days);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapDossierMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des dossiers récents (" + days + " jours)", e);
        }

        return out;
    }

    @Override
    public List<DossierMedical> findByMedecinId(Long medecinId) {
        String sql = "SELECT * FROM dossiermedical WHERE medecin_id = ?";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql + " | medecinId=" + medecinId);

        List<DossierMedical> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapDossierMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des dossiers par médecin ID=" + medecinId, e);
        }

        return out;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dossiermedical";
        System.out.println("[JDBC DOSSIER MEDICAL SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des dossiers médicaux", e);
        }
    }
}