package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.CertificatRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificatRepositoryImpl implements CertificatRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String INSERT_SQL = """
        INSERT INTO Certificat 
        (dateDebut, dateFin, dureeRepos, contenu, 
         consultation_id, dossier_med_id, 
         dateCreation, creePar)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE Certificat
        SET dateDebut = ?, dateFin = ?, dureeRepos = ?, contenu = ?,
            consultation_id = ?, dossier_med_id = ?,
            dateMiseAJour = ?, modifierPar = ?
        WHERE id = ?
        """;

    @Override
    public List<Certificat> findAll() {
        String sql = "SELECT * FROM Certificat ORDER BY dateDebut DESC";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql);

        List<Certificat> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(RowMappers.mapCertificat(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les certificats", e);
        }

        return list;
    }

    @Override
    public Certificat findById(Long id) {
        String sql = "SELECT * FROM Certificat WHERE id = ?";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapCertificat(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du certificat par ID : " + id, e);
        }
    }

    @Override
    public void create(Certificat c) {
        System.out.println("[JDBC CERTIFICAT SQL] INSERT Certificat | dureeRepos=" + c.getDureeRepos() + " jours");

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // dateDebut
            if (c.getDateDebut() != null) {
                ps.setDate(1, Date.valueOf(c.getDateDebut()));
            } else {
                ps.setNull(1, Types.DATE);
            }

            // dateFin
            if (c.getDateFin() != null) {
                ps.setDate(2, Date.valueOf(c.getDateFin()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setInt(3, c.getDureeRepos());
            ps.setString(4, c.getContenu());

            // consultation_id
            Long consultId = c.getConsultation() != null ? c.getConsultation().getId() : null;
            if (consultId != null) {
                ps.setLong(5, consultId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            // dossier_med_id
            Long dossierId = c.getDossierMedical() != null ? c.getDossierMedical().getId() : null;
            if (dossierId != null) {
                ps.setLong(6, dossierId);
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(7, Timestamp.valueOf(c.getDateCreation() != null ? c.getDateCreation() : now));
            ps.setString(8, c.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getLong(1));
                }
            }

            if (c.getDateCreation() == null) {
                c.setDateCreation(now);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du certificat", e);
        }
    }

    @Override
    public void update(Certificat c) {
        System.out.println("[JDBC CERTIFICAT SQL] UPDATE Certificat | id=" + c.getId());

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            if (c.getDateDebut() != null) {
                ps.setDate(1, Date.valueOf(c.getDateDebut()));
            } else {
                ps.setNull(1, Types.DATE);
            }

            if (c.getDateFin() != null) {
                ps.setDate(2, Date.valueOf(c.getDateFin()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setInt(3, c.getDureeRepos());
            ps.setString(4, c.getContenu());

            Long consultId = c.getConsultation() != null ? c.getConsultation().getId() : null;
            if (consultId != null) {
                ps.setLong(5, consultId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            Long dossierId = c.getDossierMedical() != null ? c.getDossierMedical().getId() : null;
            if (dossierId != null) {
                ps.setLong(6, dossierId);
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, c.getModifierPar());
            ps.setLong(9, c.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Certificat non trouvé pour mise à jour (id=" + c.getId() + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du certificat ID=" + c.getId(), e);
        }
    }

    @Override
    public void delete(Certificat c) {
        if (c != null && c.getId() != null) {
            deleteById(c.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Certificat WHERE id = ?";
        System.out.println("[JDBC CERTIFICAT SQL] DELETE Certificat | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du certificat ID=" + id, e);
        }
    }

    @Override
    public List<Certificat> findByDateDebutRange(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM Certificat WHERE dateDebut BETWEEN ? AND ? ORDER BY dateDebut DESC";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql + " | du " + start + " au " + end);

        List<Certificat> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCertificat(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des certificats par période", e);
        }
        return list;
    }

    @Override
    public List<Certificat> findByDureeRepos(int duree) {
        String sql = "SELECT * FROM Certificat WHERE dureeRepos = ? ORDER BY dateDebut DESC";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql + " | dureeRepos=" + duree);

        List<Certificat> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, duree);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCertificat(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des certificats par durée de repos", e);
        }
        return list;
    }

    @Override
    public List<Certificat> findByDossierMedicalId(Long dossierId) {
        String sql = "SELECT * FROM Certificat WHERE dossier_med_id = ? ORDER BY dateDebut DESC";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql + " | dossierId=" + dossierId);

        List<Certificat> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, dossierId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCertificat(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des certificats par dossier médical ID=" + dossierId, e);
        }
        return list;
    }

    @Override
    public List<Certificat> findByConsultationId(Long consultationId) {
        String sql = "SELECT * FROM Certificat WHERE consultation_id = ? ORDER BY dateDebut DESC";
        System.out.println("[JDBC CERTIFICAT SQL] " + sql + " | consultationId=" + consultationId);

        List<Certificat> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, consultationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCertificat(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des certificats par consultation ID=" + consultationId, e);
        }
        return list;
    }
}