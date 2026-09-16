package ma.WhiteLab.repository.modules.cabinet.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.cabinet.api.RevenusRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenusRepositoryImpl implements RevenusRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String INSERT_SQL = """
        INSERT INTO revenus (titre, description, montant, date, cabinet_id, dateCreation, creePar)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE revenus
        SET titre = ?, description = ?, montant = ?, date = ?, cabinet_id = ?,
            dateMiseAJour = ?, modifierPar = ?
        WHERE id = ?
        """;



    @Override
    public List<Revenus> findAll() {
        String sql = "SELECT * FROM revenus ORDER BY date DESC";
        System.out.println("[JDBC REVENUS SQL] " + sql);

        List<Revenus> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(RowMappers.mapRevenus(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les revenus", e);
        }

        return list;
    }

    @Override
    public Revenus findById(Long id) {
        String sql = "SELECT * FROM revenus WHERE id = ?";
        System.out.println("[JDBC REVENUS SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapRevenus(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du revenu par ID : " + id, e);
        }
    }

    @Override
    public void create(Revenus r) {
        System.out.println("[JDBC REVENUS SQL] INSERT Revenus | titre=" + r.getTitre() + ", montant=" + r.getMontant());

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDouble(3, r.getMontant());

            if (r.getDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(r.getDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            Long cabinetId = r.getCabinetMedicale() != null ? r.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(r.getDateCreation() != null ? r.getDateCreation() : now));
            ps.setString(7, r.getCreePar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }

            if (r.getDateCreation() == null) {
                r.setDateCreation(now);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du revenu pour le cabinet ID=" +
                    (r.getCabinetMedicale() != null ? r.getCabinetMedicale().getId() : "null"), e);
        }
    }

    @Override
    public void update(Revenus r) {
        System.out.println("[JDBC REVENUS SQL] UPDATE Revenus | id=" + r.getId());

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDouble(3, r.getMontant());

            if (r.getDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(r.getDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            Long cabinetId = r.getCabinetMedicale() != null ? r.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, r.getModifierPar());
            ps.setLong(8, r.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Aucun revenu trouvé avec l'id " + r.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du revenu ID=" + r.getId(), e);
        }
    }

    @Override
    public void delete(Revenus r) {
        if (r != null && r.getId() != null) {
            deleteById(r.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM revenus WHERE id = ?";
        System.out.println("[JDBC REVENUS SQL] DELETE Revenus | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du revenu ID=" + id, e);
        }
    }

    @Override
    public List<Revenus> findByCabinetId(Long cabinetId) {
        String sql = "SELECT * FROM revenus WHERE cabinet_id = ? ORDER BY date DESC";
        System.out.println("[JDBC REVENUS SQL] " + sql + " | cabinetId=" + cabinetId);

        List<Revenus> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapRevenus(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des revenus par cabinet ID=" + cabinetId, e);
        }

        return list;
    }

    @Override
    public List<Revenus> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM revenus WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        System.out.println("[JDBC REVENUS SQL] " + sql + " | du " + start + " au " + end);

        List<Revenus> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapRevenus(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des revenus entre les dates " + start + " et " + end, e);
        }

        return list;
    }

    @Override
    public List<Revenus> findByTitre(String titre) {
        String sql = "SELECT * FROM revenus WHERE titre LIKE ? ORDER BY date DESC";
        System.out.println("[JDBC REVENUS SQL] " + sql + " | titre=" + titre);

        List<Revenus> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + titre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapRevenus(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des revenus par titre : " + titre, e);
        }

        return list;
    }
}