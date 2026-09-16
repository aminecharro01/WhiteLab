package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.ActeMedicalRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActeMedicalRepositoryImpl implements ActeMedicalRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<ActeMedical> findAll() {
        String sql = "SELECT * FROM Acte_Medical ORDER BY libelle ASC";
        System.out.println("[JDBC ACTE MEDICAL SQL] " + sql);

        List<ActeMedical> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapActeMedical(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les actes médicaux", e);
        }

        return out;
    }

    @Override
    public ActeMedical findById(Long id) {
        String sql = "SELECT * FROM Acte_Medical WHERE id = ?";
        System.out.println("[JDBC ACTE MEDICAL SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapActeMedical(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'acte médical par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(ActeMedical acte) {
        String sql = """
            INSERT INTO Acte_Medical(libelle, categorie, prixDeBase, dateCreation, creePar)
            VALUES (?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC ACTE MEDICAL SQL] INSERT ActeMedical | libelle=" + acte.getLibelle());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, acte.getLibelle());
            ps.setString(2, acte.getCategorie());
            ps.setFloat(3, acte.getPrixDeBase());
            ps.setTimestamp(4, Timestamp.valueOf(acte.getDateCreation() != null ? acte.getDateCreation() : LocalDateTime.now()));
            ps.setString(5, acte.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    acte.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'acte médical", e);
        }
    }

    @Override
    public void update(ActeMedical acte) {
        String sql = """
            UPDATE Acte_Medical
            SET libelle=?, categorie=?, prixDeBase=?, dateMiseAJour=?, modifierPar=?
            WHERE id=?
            """;
        System.out.println("[JDBC ACTE MEDICAL SQL] UPDATE ActeMedical | id=" + acte.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, acte.getLibelle());
            ps.setString(2, acte.getCategorie());
            ps.setFloat(3, acte.getPrixDeBase());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, acte.getModifierPar());
            ps.setLong(6, acte.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'acte médical ID=" + acte.getId(), e);
        }
    }

    @Override
    public void delete(ActeMedical acte) {
        if (acte != null && acte.getId() != null) {
            deleteById(acte.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Acte_Medical WHERE id = ?";
        System.out.println("[JDBC ACTE MEDICAL SQL] DELETE ActeMedical | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'acte médical ID=" + id, e);
        }
    }

    @Override
    public Optional<ActeMedical> findByLibelle(String libelle) {
        String sql = "SELECT * FROM Acte_Medical WHERE libelle = ?";
        System.out.println("[JDBC ACTE MEDICAL SQL] " + sql + " | libelle=" + libelle);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, libelle);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(RowMappers.mapActeMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par libellé : " + libelle, e);
        }

        return Optional.empty();
    }

    // =========================
    // STATISTIQUES / DASHBOARD
    // =========================

    @Override
    public String findMostFrequentActeNomCeMois(Long cabinetId) {
        String sql = """
            SELECT a.libelle
            FROM Acte_Medical a
            JOIN InterventionMedecin im ON a.id = im.acteMedical_id
            JOIN Consultation c ON im.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND MONTH(c.date) = MONTH(CURDATE())
              AND YEAR(c.date) = YEAR(CURDATE())
            GROUP BY a.libelle
            ORDER BY COUNT(a.id) DESC
            LIMIT 1
        """;
        System.out.println("[JDBC ACTE MEDICAL SQL] Acte le plus fréquent ce mois | cabinetId=" + cabinetId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'acte le plus fréquent ce mois pour le cabinet ID=" + cabinetId, e);
        }
    }

    @Override
    public List<ActeMedical> findTop5ActesByPeriodeAndCabinet(LocalDate debutMois, LocalDate finMois, Long cabinetId) {
        String sql = """
            SELECT a.*, COUNT(a.id) AS total
            FROM Acte_Medical a
            JOIN InterventionMedecin im ON a.id = im.acteMedical_id
            JOIN Consultation c ON im.consultation_id = c.id
            JOIN DossierMedical d ON c.dossier_medical_id = d.id
            JOIN Utilisateur s ON d.medecin_id = s.id
            WHERE s.cabinetMedicale_id = ?
              AND DATE(c.date) BETWEEN ? AND ?
            GROUP BY a.id
            ORDER BY total DESC
            LIMIT 5
        """;
        System.out.println("[JDBC ACTE MEDICAL SQL] Top 5 actes | cabinetId=" + cabinetId +
                ", du " + debutMois + " au " + finMois);

        List<ActeMedical> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);
            ps.setDate(2, Date.valueOf(debutMois));
            ps.setDate(3, Date.valueOf(finMois));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapActeMedical(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du top 5 actes par période et cabinet", e);
        }

        return list;
    }
}