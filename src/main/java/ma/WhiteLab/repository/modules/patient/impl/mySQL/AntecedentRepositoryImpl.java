package ma.WhiteLab.repository.modules.patient.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.entities.enums.NiveauDeRisk;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.patient.api.AntecedentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AntecedentRepositoryImpl implements AntecedentRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<Antecedent> findAll() {
        String sql = "SELECT * FROM Antecedent ORDER BY categorie, niveauRisque, nom";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql);

        List<Antecedent> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapAntecedent(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les antécédents", e);
        }

        return out;
    }

    @Override
    public Antecedent findById(Long id) {
        String sql = "SELECT * FROM Antecedent WHERE id = ?";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapAntecedent(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'antécédent par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Antecedent a) {
        String sql = """
            INSERT INTO Antecedent(nom, description, categorie, niveauRisque, 
                                    dateCreation, dateMiseAJour, creePar, modifierPar)
            VALUES(?,?,?,? ,NOW(), NOW(), ?, ?)
        """;
        System.out.println("[JDBC ANTECEDENT SQL] INSERT Antecedent | nom=" + a.getNom());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getCategorie().name());
            ps.setString(4, a.getNiveauDeRisk().name());
            ps.setString(5, a.getCreePar());
            ps.setString(6, a.getModifierPar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'antécédent", e);
        }
    }

    @Override
    public void update(Antecedent a) {
        String sql = """
            UPDATE Antecedent 
            SET nom=?, description=?, categorie=?, niveauRisque=?, 
                dateMiseAJour=NOW(), modifierPar=? 
            WHERE id=?
        """;
        System.out.println("[JDBC ANTECEDENT SQL] UPDATE Antecedent | id=" + a.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getCategorie().name());
            ps.setString(4, a.getNiveauDeRisk().name());
            ps.setString(5, a.getModifierPar());
            ps.setLong(6, a.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'antécédent ID=" + a.getId(), e);
        }
    }

    @Override
    public void delete(Antecedent a) {
        if (a != null && a.getId() != null) {
            deleteById(a.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Antecedent WHERE id = ?";
        System.out.println("[JDBC ANTECEDENT SQL] DELETE Antecedent | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'antécédent ID=" + id, e);
        }
    }

    // -------- Advanced Queries --------

    @Override
    public Optional<Antecedent> findByNom(String nom) {
        String sql = "SELECT * FROM Antecedent WHERE nom = ?";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | nom=" + nom);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapAntecedent(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par nom : " + nom, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Antecedent> findByCategorie(CategorieAntecedent categorie) {
        String sql = "SELECT * FROM Antecedent WHERE categorie = ? ORDER BY nom";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | categorie=" + categorie);

        List<Antecedent> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorie.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapAntecedent(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par catégorie : " + categorie, e);
        }

        return out;
    }

    @Override
    public List<Antecedent> findByNiveauRisque(NiveauDeRisk niveau) {
        String sql = "SELECT * FROM Antecedent WHERE niveauRisque = ? ORDER BY nom";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | niveauRisque=" + niveau);

        List<Antecedent> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, niveau.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapAntecedent(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par niveau de risque : " + niveau, e);
        }

        return out;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM Antecedent WHERE id = ?";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'existence par ID : " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM Antecedent";
        System.out.println("[JDBC ANTECEDENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des antécédents", e);
        }
    }

    @Override
    public List<Antecedent> findPage(int limit, int offset) {
        String sql = """
            SELECT * FROM Antecedent 
            ORDER BY categorie, niveauRisque, nom 
            LIMIT ? OFFSET ?
        """;
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | limit=" + limit + ", offset=" + offset);

        List<Antecedent> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapAntecedent(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement de la page d'antécédents", e);
        }

        return out;
    }

    @Override
    public List<Patient> getPatientsHavingAntecedent(Long antecedentId) {
        String sql = """
            SELECT p.* 
            FROM Patient p 
            JOIN Patient_Antecedent pa ON pa.patient_id = p.id
            WHERE pa.antecedent_id = ?
            ORDER BY p.nom, p.prenom
        """;
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | antecedentId=" + antecedentId);

        List<Patient> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, antecedentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des patients ayant l'antécédent ID=" + antecedentId, e);
        }

        return out;
    }

    @Override
    public List<Antecedent> getAntecedentsByPatientId(Long patientId) {
        String sql = """
            SELECT a.* 
            FROM Antecedent a
            JOIN Patient_Antecedent pa ON pa.antecedent_id = a.id
            WHERE pa.patient_id = ?
            ORDER BY a.categorie, a.niveauRisque, a.nom
        """;
        System.out.println("[JDBC ANTECEDENT SQL] " + sql + " | patientId=" + patientId);

        List<Antecedent> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapAntecedent(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des antécédents du patient ID=" + patientId, e);
        }

        return out;
    }
}