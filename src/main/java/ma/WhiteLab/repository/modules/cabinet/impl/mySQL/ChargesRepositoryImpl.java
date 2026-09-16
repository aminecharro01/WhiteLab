package ma.WhiteLab.repository.modules.cabinet.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.cabinet.api.ChargesRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargesRepositoryImpl implements ChargesRepository {

    private Connection connection; // Injectée par ApplicationContext

    private static final String INSERT_SQL = """
        INSERT INTO charges (titre, description, montant, date, cabinet_id, dateCreation, creePar)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE charges
        SET titre = ?, description = ?, montant = ?, date = ?, cabinet_id = ?,
            dateMiseAJour = ?, modifierPar = ?
        WHERE id = ?
        """;



    @Override
    public List<Charges> findAll() {
        String sql = "SELECT * FROM charges ORDER BY date DESC";
        System.out.println("[JDBC CHARGES SQL] " + sql);

        List<Charges> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(RowMappers.mapCharges(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les charges", e);
        }

        return list;
    }

    @Override
    public Charges findById(Long id) {
        String sql = "SELECT * FROM charges WHERE id = ?";
        System.out.println("[JDBC CHARGES SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapCharges(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la charge par ID : " + id, e);
        }
    }

    @Override
    public void create(Charges c) {
        System.out.println("[JDBC CHARGES SQL] INSERT Charges | titre=" + c.getTitre() + ", montant=" + c.getMontant());

        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setDouble(3, c.getMontant());

            if (c.getDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(c.getDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            Long cabinetId = c.getCabinetMedicale() != null ? c.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(c.getDateCreation() != null ? c.getDateCreation() : now));
            ps.setString(7, c.getCreePar());

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
            throw new RuntimeException("Erreur lors de la création de la charge pour le cabinet ID=" +
                    (c.getCabinetMedicale() != null ? c.getCabinetMedicale().getId() : "null"), e);
        }
    }

    @Override
    public void update(Charges c) {
        System.out.println("[JDBC CHARGES SQL] UPDATE Charges | id=" + c.getId());

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setDouble(3, c.getMontant());

            if (c.getDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(c.getDate()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            Long cabinetId = c.getCabinetMedicale() != null ? c.getCabinetMedicale().getId() : null;
            if (cabinetId != null) {
                ps.setLong(5, cabinetId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, c.getModifierPar());
            ps.setLong(8, c.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Aucune charge trouvée avec l'id " + c.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la charge ID=" + c.getId(), e);
        }
    }

    @Override
    public void delete(Charges c) {
        if (c != null && c.getId() != null) {
            deleteById(c.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM charges WHERE id = ?";
        System.out.println("[JDBC CHARGES SQL] DELETE Charges | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la charge ID=" + id, e);
        }
    }

    @Override
    public List<Charges> findByCabinetId(Long cabinetId) {
        String sql = "SELECT * FROM charges WHERE cabinet_id = ? ORDER BY date DESC";
        System.out.println("[JDBC CHARGES SQL] " + sql + " | cabinetId=" + cabinetId);

        List<Charges> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cabinetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCharges(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des charges par cabinet ID=" + cabinetId, e);
        }

        return list;
    }

    @Override
    public List<Charges> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM charges WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        System.out.println("[JDBC CHARGES SQL] " + sql + " | du " + start + " au " + end);

        List<Charges> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(RowMappers.mapCharges(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des charges entre les dates " + start + " et " + end, e);
        }

        return list;
    }
}