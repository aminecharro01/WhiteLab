package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.PrescriptionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRepositoryImpl implements PrescriptionRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<Prescription> findAll() {
        String sql = """
        SELECT 
            p.id, p.qte, p.frequence, p.duree, p.ordonnance_id, p.medicament_id,
            m.id AS medicament_id_real,           -- on prend l'id du médicament depuis m
            m.nom AS medicament_nom,
            m.prix_unitaire AS medicament_prix
            -- ajoute ici d'autres champs du médicament si tu en as besoin (forme, dosage, etc.)
        FROM prescription p
        LEFT JOIN medicament m ON p.medicament_id = m.id
        """;

        System.out.println("[JDBC PRESCRIPTION SQL] " + sql);

        List<Prescription> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getLong("id"));
                p.setQte(rs.getInt("qte"));
                p.setFrequence(rs.getString("frequence"));
                p.setDuree(rs.getInt("duree"));

                // Ordonnance est volontairement laissée à null ici (pas nécessaire pour l'affichage)
                // p.setOrdonnance(...);  ← on ne le charge pas dans findAll()

                // Chargement du médicament
                Long medId = rs.getLong("medicament_id");
                if (!rs.wasNull()) {
                    Medicament m = new Medicament();
                    m.setId(medId);
                    m.setNom(rs.getString("medicament_nom"));
                    m.setPrixUnitaire(rs.getDouble("medicament_prix"));
                    // m.setForme(rs.getString("medicament_forme"));  ← si tu ajoutes le champ
                    // m.setDosage(...) etc.

                    p.setMedicament(m);
                }

                out.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les prescriptions", e);
        }

        return out;
    }

    @Override
    public Prescription findById(Long id) {
        String sql = "SELECT * FROM prescription WHERE id = ?";
        System.out.println("[JDBC PRESCRIPTION SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapPrescription(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la prescription par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Prescription p) {
        String sql = """
            INSERT INTO prescription (qte, frequence, duree, ordonnance_id, medicament_id)
            VALUES (?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC PRESCRIPTION SQL] INSERT Prescription | qte=" + p.getQte() + ", medicamentId=" +
                (p.getMedicament() != null ? p.getMedicament().getId() : "null"));

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getQte());
            ps.setString(2, p.getFrequence());
            ps.setInt(3, p.getDuree());

            ps.setObject(4, p.getOrdonnance() != null ? p.getOrdonnance().getId() : null, Types.BIGINT);
            ps.setObject(5, p.getMedicament() != null ? p.getMedicament().getId() : null, Types.BIGINT);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la prescription", e);
        }
    }

    @Override
    public void update(Prescription p) {
        String sql = """
            UPDATE prescription
            SET qte = ?, frequence = ?, duree = ?, ordonnance_id = ?, medicament_id = ?
            WHERE id = ?
            """;
        System.out.println("[JDBC PRESCRIPTION SQL] UPDATE Prescription | id=" + p.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, p.getQte());
            ps.setString(2, p.getFrequence());
            ps.setInt(3, p.getDuree());

            ps.setObject(4, p.getOrdonnance() != null ? p.getOrdonnance().getId() : null, Types.BIGINT);
            ps.setObject(5, p.getMedicament() != null ? p.getMedicament().getId() : null, Types.BIGINT);
            ps.setLong(6, p.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la prescription ID=" + p.getId(), e);
        }
    }

    @Override
    public void delete(Prescription p) {
        if (p != null && p.getId() != null) {
            deleteById(p.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM prescription WHERE id = ?";
        System.out.println("[JDBC PRESCRIPTION SQL] DELETE Prescription | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la prescription ID=" + id, e);
        }
    }

    // -----------------------------------------
    //            MÉTHODES SPÉCIFIQUES
    // -----------------------------------------

    @Override
    public List<Prescription> findByOrdonnanceId(Long ordonnanceId) {
        String sql = """
        SELECT 
            p.id, p.qte, p.frequence, p.duree, p.ordonnance_id, p.medicament_id,
            m.nom AS medicament_nom,
            m.prix_unitaire AS medicament_prix
        FROM prescription p
        LEFT JOIN medicament m ON p.medicament_id = m.id
        WHERE p.ordonnance_id = ?
        """;

        System.out.println("[JDBC PRESCRIPTION SQL] " + sql + " | ordonnanceId=" + ordonnanceId);

        List<Prescription> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ordonnanceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prescription p = new Prescription();
                    p.setId(rs.getLong("id"));
                    p.setQte(rs.getInt("qte"));
                    p.setFrequence(rs.getString("frequence"));
                    p.setDuree(rs.getInt("duree"));

                    // Créer et remplir l'objet Medicament
                    Long medicamentId = rs.getLong("medicament_id");
                    if (!rs.wasNull()) {  // Si medicament_id n'est pas null
                        Medicament m = new Medicament();
                        m.setId(medicamentId);
                        m.setNom(rs.getString("medicament_nom"));
                        m.setPrixUnitaire(rs.getDouble("medicament_prix"));
                        p.setMedicament(m);
                    }

                    list.add(p);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des prescriptions par ordonnance ID=" + ordonnanceId, e);
        }

        return list;
    }

    @Override
    public List<Prescription> findByMedicamentId(Long medicamentId) {
        String sql = "SELECT * FROM prescription WHERE medicament_id = ?";
        System.out.println("[JDBC PRESCRIPTION SQL] " + sql + " | medicamentId=" + medicamentId);

        List<Prescription> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medicamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapPrescription(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des prescriptions par médicament ID=" + medicamentId, e);
        }

        return list;
    }

    @Override
    public List<Prescription> findByFrequence(String frequence) {
        String sql = "SELECT * FROM prescription WHERE frequence LIKE ?";
        System.out.println("[JDBC PRESCRIPTION SQL] " + sql + " | frequence=" + frequence);

        List<Prescription> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + frequence + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapPrescription(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des prescriptions par fréquence : " + frequence, e);
        }

        return list;
    }

    @Override
    public boolean exists(Long ordonnanceId, Long medicamentId) {
        String sql = """
            SELECT COUNT(*) FROM prescription
            WHERE ordonnance_id = ? AND medicament_id = ?
            """;
        System.out.println("[JDBC PRESCRIPTION SQL] " + sql + " | ordonnanceId=" + ordonnanceId + ", medicamentId=" + medicamentId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ordonnanceId);
            ps.setLong(2, medicamentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification d'existence de la prescription (ordonnanceId=" + ordonnanceId + ", medicamentId=" + medicamentId + ")", e);
        }

        return false;
    }
}