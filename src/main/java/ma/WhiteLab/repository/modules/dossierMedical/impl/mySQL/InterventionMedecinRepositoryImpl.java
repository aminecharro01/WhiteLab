package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.InterventionMedecinRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterventionMedecinRepositoryImpl implements InterventionMedecinRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<InterventionMedecin> findAll() {
        String sql = "SELECT * FROM InterventionMedecin ORDER BY id ASC";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] " + sql);

        List<InterventionMedecin> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapInterventionMedecin(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les interventions médecin", e);
        }

        return out;
    }

    @Override
    public InterventionMedecin findById(Long id) {
        String sql = "SELECT * FROM InterventionMedecin WHERE id = ?";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapInterventionMedecin(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'intervention médecin par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(InterventionMedecin im) {
        String sql = """
            INSERT INTO InterventionMedecin(prixDePatient, numDent, consultation_id, acteMedical_id, dateCreation, creePar)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] INSERT InterventionMedecin | numDent=" + im.getNumDent());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, im.getPrixDePatient());
            ps.setInt(2, im.getNumDent());

            ps.setObject(3, im.getConsultation() != null ? im.getConsultation().getId() : null, Types.BIGINT);
            ps.setObject(4, im.getActeMedical() != null ? im.getActeMedical().getId() : null, Types.BIGINT);

            ps.setTimestamp(5, Timestamp.valueOf(im.getDateCreation() != null ? im.getDateCreation() : LocalDateTime.now()));
            ps.setString(6, im.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    im.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'intervention médecin", e);
        }
    }

    @Override
    public void update(InterventionMedecin im) {
        String sql = """
            UPDATE InterventionMedecin
            SET prixDePatient=?, numDent=?, consultation_id=?, acteMedical_id=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] UPDATE InterventionMedecin | id=" + im.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDouble(1, im.getPrixDePatient());
            ps.setInt(2, im.getNumDent());

            ps.setObject(3, im.getConsultation() != null ? im.getConsultation().getId() : null, Types.BIGINT);
            ps.setObject(4, im.getActeMedical() != null ? im.getActeMedical().getId() : null, Types.BIGINT);

            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(6, im.getModifierPar());
            ps.setLong(7, im.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'intervention médecin ID=" + im.getId(), e);
        }
    }

    @Override
    public void delete(InterventionMedecin im) {
        if (im != null && im.getId() != null) {
            deleteById(im.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM InterventionMedecin WHERE id = ?";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] DELETE InterventionMedecin | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'intervention médecin ID=" + id, e);
        }
    }

    @Override
    public List<InterventionMedecin> findByNumDent(int numDent) {
        String sql = "SELECT * FROM InterventionMedecin WHERE numDent = ? ORDER BY id ASC";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] " + sql + " | numDent=" + numDent);

        List<InterventionMedecin> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, numDent);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapInterventionMedecin(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par numéro de dent : " + numDent, e);
        }

        return out;
    }

    @Override
    public List<InterventionMedecin> findByConsultationId(Long consultationId) {
        String sql = "SELECT * FROM InterventionMedecin WHERE consultation_id = ? ORDER BY id ASC";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] " + sql + " | consultationId=" + consultationId);

        List<InterventionMedecin> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, consultationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapInterventionMedecin(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par consultation ID=" + consultationId, e);
        }

        return out;
    }

    @Override
    public List<InterventionMedecin> findByActeMedicalId(Long acteMedicalId) {
        String sql = "SELECT * FROM InterventionMedecin WHERE acteMedical_id = ? ORDER BY id ASC";
        System.out.println("[JDBC INTERVENTION MEDECIN SQL] " + sql + " | acteMedicalId=" + acteMedicalId);

        List<InterventionMedecin> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, acteMedicalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapInterventionMedecin(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par acte médical ID=" + acteMedicalId, e);
        }

        return out;
    }
}