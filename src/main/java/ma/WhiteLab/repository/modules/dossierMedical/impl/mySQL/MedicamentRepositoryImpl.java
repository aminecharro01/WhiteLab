package ma.WhiteLab.repository.modules.dossierMedical.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.dossierMedical.api.MedicamentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class MedicamentRepositoryImpl implements MedicamentRepository {

    private final Connection connection; // Injectée par ApplicationContext

    @Override
    public List<Medicament> findAll() {
        String sql = "SELECT * FROM medicament";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapMedicament(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les médicaments", e);
        }

        return out;
    }

    @Override
    public Medicament findById(Long id) {
        String sql = "SELECT * FROM medicament WHERE id = ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RowMappers.mapMedicament(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du médicament par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(Medicament m) {
        String sql = """
            INSERT INTO medicament(nom, labo, type, forme, remboursable, prix_unitaire, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        System.out.println("[JDBC MEDICAMENT SQL] INSERT Medicament | nom=" + m.getNom());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getLabo());
            ps.setString(3, m.getType());
            ps.setString(4, m.getForme() != null ? m.getForme().name() : null);
            ps.setBoolean(5, m.isRemboursable());
            ps.setDouble(6, m.getPrixUnitaire());
            ps.setString(7, m.getDescription());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du médicament", e);
        }
    }

    @Override
    public void update(Medicament m) {
        String sql = """
            UPDATE medicament
            SET nom=?, labo=?, type=?, forme=?, remboursable=?, prix_unitaire=?, description=?
            WHERE id=?
            """;
        System.out.println("[JDBC MEDICAMENT SQL] UPDATE Medicament | id=" + m.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getLabo());
            ps.setString(3, m.getType());
            ps.setString(4, m.getForme() != null ? m.getForme().name() : null);
            ps.setBoolean(5, m.isRemboursable());
            ps.setDouble(6, m.getPrixUnitaire());
            ps.setString(7, m.getDescription());
            ps.setLong(8, m.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du médicament ID=" + m.getId(), e);
        }
    }

    @Override
    public void delete(Medicament m) {
        if (m != null && m.getId() != null) {
            deleteById(m.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM medicament WHERE id=?";
        System.out.println("[JDBC MEDICAMENT SQL] DELETE Medicament | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du médicament ID=" + id, e);
        }
    }

    // ------------------------------------------------------
    //            MÉTHODES SPÉCIFIQUES
    // ------------------------------------------------------

    @Override
    public List<Medicament> searchByName(String nom) {
        String sql = "SELECT * FROM medicament WHERE nom = ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | nom=" + nom);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapMedicament(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche exacte par nom : " + nom, e);
        }

        return out;
    }

    @Override
    public List<Medicament> searchByNameLike(String text) {
        String sql = "SELECT * FROM medicament WHERE nom LIKE ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | text=" + text);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + text + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapMedicament(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche partielle par nom : " + text, e);
        }

        return out;
    }

    @Override
    public List<Medicament> findByLabo(String labo) {
        String sql = "SELECT * FROM medicament WHERE labo = ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | labo=" + labo);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, labo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapMedicament(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par laboratoire : " + labo, e);
        }

        return out;
    }

    @Override
    public List<Medicament> findRemboursables() {
        String sql = "SELECT * FROM medicament WHERE remboursable = 1";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(RowMappers.mapMedicament(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des médicaments remboursables", e);
        }

        return out;
    }

    @Override
    public List<Medicament> findByForme(String forme) {
        String sql = "SELECT * FROM medicament WHERE forme = ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | forme=" + forme);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, forme);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapMedicament(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par forme : " + forme, e);
        }

        return out;
    }

    @Override
    public List<Medicament> findByPrixBetween(double min, double max) {
        String sql = "SELECT * FROM medicament WHERE prix_unitaire BETWEEN ? AND ?";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql + " | min=" + min + ", max=" + max);

        List<Medicament> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, min);
            ps.setDouble(2, max);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(RowMappers.mapMedicament(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par tranche de prix [" + min + " - " + max + "]", e);
        }

        return out;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM Medicament";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage total des médicaments", e);
        }
    }

    @Override
    public long countRemboursables() {
        String sql = "SELECT COUNT(*) FROM Medicament WHERE remboursable = 1";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des médicaments remboursables", e);
        }
    }

    @Override
    public double getPrixMoyen() {
        String sql = "SELECT AVG(prix_unitaire) FROM Medicament";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0.0;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du calcul du prix moyen des médicaments", e);
        }
    }

    @Override
    public Medicament findTopByPrixDesc() {
        String sql = "SELECT * FROM Medicament ORDER BY prix_unitaire DESC LIMIT 1";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? RowMappers.mapMedicament(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du médicament le plus cher", e);
        }
    }

    @Override
    public Medicament findTopByPrixAsc() {
        String sql = "SELECT * FROM Medicament WHERE prix_unitaire > 0 ORDER BY prix_unitaire ASC LIMIT 1";
        System.out.println("[JDBC MEDICAMENT SQL] " + sql);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? RowMappers.mapMedicament(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du médicament le moins cher", e);
        }
    }
}