package ma.WhiteLab.repository.modules.patient.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class PatientRepositoryImpl implements PatientRepository {

    private final Connection connection; // Injectée par ApplicationContext

    // =========================
    //        CRUD
    // =========================

    @Override
    public List<Patient> findAll() {
        String sql = "SELECT * FROM Patient ORDER BY id";
        System.out.println("[JDBC PATIENT SQL] " + sql);

        List<Patient> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapPatient(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les patients", e);
        }

        return out;
    }

    @Override
    public Patient findById(Long id) {
        String sql = "SELECT * FROM Patient WHERE id = ?";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapPatient(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du patient par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Patient p) {
        String sql = """
            INSERT INTO Patient(nom, prenom, adresse, telephone, email, 
                dateNaissance, dateCreation, sexe, assurance)
            VALUES(?,?,?,?,?,?,?,?,?)
        """;
        System.out.println("[JDBC PATIENT SQL] INSERT Patient | nom=" + p.getNom() + ", prenom=" + p.getPrenom());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getAdresse());
            ps.setString(4, p.getTelephone());
            ps.setString(5, p.getEmail());

            if (p.getDateNaissance() != null) {
                ps.setDate(6, Date.valueOf(p.getDateNaissance()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            LocalDateTime created = p.getDateCreation() != null ? p.getDateCreation() : LocalDateTime.now();
            ps.setTimestamp(7, Timestamp.valueOf(created));

            ps.setString(8, p.getSexe().name());
            ps.setString(9, p.getAssurance().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du patient", e);
        }
    }

    @Override
    public void update(Patient p) {
        String sql = """
            UPDATE Patient SET nom=?, prenom=?, adresse=?, telephone=?, email=?, 
                dateNaissance=?, dateMiseAJour=?, sexe=?, assurance=? 
            WHERE id=?
        """;
        System.out.println("[JDBC PATIENT SQL] UPDATE Patient | id=" + p.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getAdresse());
            ps.setString(4, p.getTelephone());
            ps.setString(5, p.getEmail());

            if (p.getDateNaissance() != null) {
                ps.setDate(6, Date.valueOf(p.getDateNaissance()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(8, p.getSexe().name());
            ps.setString(9, p.getAssurance().name());
            ps.setLong(10, p.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du patient ID=" + p.getId(), e);
        }
    }

    @Override
    public void delete(Patient p) {
        if (p != null && p.getId() != null) {
            deleteById(p.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Patient WHERE id = ?";
        System.out.println("[JDBC PATIENT SQL] DELETE Patient | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du patient ID=" + id, e);
        }
    }

    // =========================
    //        EXTRA QUERIES
    // =========================

    @Override
    public Optional<Patient> findByEmail(String email) {
        String sql = "SELECT * FROM Patient WHERE email = ?";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | email=" + email);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par email : " + email, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByTelephone(String telephone) {
        String sql = "SELECT * FROM Patient WHERE telephone = ?";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | telephone=" + telephone);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, telephone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par téléphone : " + telephone, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Patient> searchByNomPrenom(String keyword) {
        String sql = "SELECT * FROM Patient WHERE nom LIKE ? OR prenom LIKE ? ORDER BY nom, prenom";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | keyword=" + keyword);

        List<Patient> out = new ArrayList<>();
        String like = "%" + keyword + "%";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par nom/prénom : " + keyword, e);
        }

        return out;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM Patient WHERE id = ?";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | id=" + id);

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
        String sql = "SELECT COUNT(*) FROM Patient";
        System.out.println("[JDBC PATIENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des patients", e);
        }
    }

    @Override
    public List<Patient> findPage(int limit, int offset) {
        String sql = "SELECT * FROM Patient ORDER BY id LIMIT ? OFFSET ?";
        System.out.println("[JDBC PATIENT SQL] " + sql + " | limit=" + limit + ", offset=" + offset);

        List<Patient> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement de la page de patients", e);
        }

        return out;
    }

    // =========================
    //      MANY TO MANY
    // =========================

    @Override
    public void addAntecedentToPatient(Long patientId, Long antecedentId) {
        String sql = "INSERT INTO patient_antecedent(patient_id, antecedent_id) VALUES (?,?)";
        System.out.println("[JDBC PATIENT SQL] INSERT patient_antecedent | patientId=" + patientId + ", antecedentId=" + antecedentId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.setLong(2, antecedentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout d'un antécédent au patient", e);
        }
    }

    @Override
    public void removeAntecedentFromPatient(Long patientId, Long antecedentId) {
        String sql = "DELETE FROM Patient_Antecedent WHERE patient_id=? AND antecedent_id=?";
        System.out.println("[JDBC PATIENT SQL] DELETE Patient_Antecedent | patientId=" + patientId + ", antecedentId=" + antecedentId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.setLong(2, antecedentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression d'un antécédent du patient", e);
        }
    }

    @Override
    public void removeAllAntecedentsFromPatient(Long patientId) {
        String sql = "DELETE FROM Patient_Antecedent WHERE patient_id=?";
        System.out.println("[JDBC PATIENT SQL] DELETE ALL antecedents | patientId=" + patientId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de tous les antécédents du patient", e);
        }
    }

    @Override
    public List<Antecedent> getAntecedentsOfPatient(Long patientId) {
        String sql = """
            SELECT a.*
            FROM Antecedent a 
            JOIN Patient_Antecedent pa ON pa.antecedent_id = a.id
            WHERE pa.patient_id = ?
            ORDER BY a.categorie, a.niveauRisque, a.nom
        """;
        System.out.println("[JDBC PATIENT SQL] " + sql + " | patientId=" + patientId);

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

    @Override
    public List<Patient> getPatientsByAntecedent(Long antecedentId) {
        String sql = """
            SELECT p.*
            FROM Patient p 
            JOIN Patient_Antecedent pa ON pa.patient_id = p.id
            WHERE pa.antecedent_id = ?
            ORDER BY p.nom, p.prenom
        """;
        System.out.println("[JDBC PATIENT SQL] " + sql + " | antecedentId=" + antecedentId);

        List<Patient> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, antecedentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapPatient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des patients par antécédent ID=" + antecedentId, e);
        }

        return out;
    }

    // =========================
    //   DASHBOARD STATISTIQUES
    // =========================

    @Override
    public long countByCabinetId(Long cabinetId) {
        String sql = """
            SELECT COUNT(DISTINCT p.id)
            FROM Patient p
            JOIN DossierMedical d ON p.id = d.pat_id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
        """;
        System.out.println("[JDBC PATIENT SQL] COUNT by cabinet | cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des patients par cabinet ID=" + cabinetId, e);
        }
    }

    @Override
    public int countNouveauxPatientsDuMois(Long cabinetId, int annee, int mois) {
        String sql = """
            SELECT COUNT(DISTINCT p.id)
            FROM Patient p
            JOIN DossierMedical d ON p.id = d.pat_id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND YEAR(d.dateCreation) = ?
              AND MONTH(d.dateCreation) = ?
        """;
        System.out.println("[JDBC PATIENT SQL] COUNT nouveaux patients mois | cabinetId=" + cabinetId + ", année=" + annee + ", mois=" + mois);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setInt(2, annee);
            ps.setInt(3, mois);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des nouveaux patients du mois", e);
        }
    }

    @Override
    public List<Object[]> getRepartitionPatientsParTrancheAge(Long cabinetId) {
        String sql = """
            SELECT 
                CASE
                    WHEN TIMESTAMPDIFF(YEAR, p.dateNaissance, CURDATE()) < 18 THEN '0-17'
                    WHEN TIMESTAMPDIFF(YEAR, p.dateNaissance, CURDATE()) BETWEEN 18 AND 35 THEN '18-35'
                    WHEN TIMESTAMPDIFF(YEAR, p.dateNaissance, CURDATE()) BETWEEN 36 AND 60 THEN '36-60'
                    ELSE '60+'
                END AS trancheAge,
                COUNT(DISTINCT p.id) AS total
            FROM Patient p
            JOIN DossierMedical d ON p.id = d.pat_id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
            GROUP BY trancheAge
            ORDER BY trancheAge
        """;
        System.out.println("[JDBC PATIENT SQL] Répartition par tranche d'âge | cabinetId=" + cabinetId);

        List<Object[]> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getString("trancheAge"),
                            rs.getLong("total")
                    });
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul de la répartition par tranche d'âge", e);
        }

        return result;
    }

    @Override
    public long countPatientsByCabinet(Long cabinetId) {
        return countByCabinetId(cabinetId); // Redondance conservée si besoin, sinon tu peux supprimer une des deux
    }

    @Override
    public long countPatientsByCabinetAndMonth(Long cabinetId, int year, int month) {
        return countNouveauxPatientsDuMois(cabinetId, year, month);
    }
}