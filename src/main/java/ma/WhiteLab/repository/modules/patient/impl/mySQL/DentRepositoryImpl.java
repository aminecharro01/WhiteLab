package ma.WhiteLab.repository.modules.patient.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.enums.EtatDent;
import ma.WhiteLab.entities.patient.Dent;
import ma.WhiteLab.repository.common.RowMappers;
import ma.WhiteLab.repository.modules.patient.api.DentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DentRepositoryImpl implements DentRepository {

    private Connection connection; // Injectée par ApplicationContext

    @Override
    public List<Dent> findAll() {
        String sql = "SELECT * FROM Dent ORDER BY patient_id, numero";
        List<Dent> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(RowMappers.mapDent(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des dents", e);
        }
        return out;
    }

    @Override
    public Dent findById(Long id) {
        String sql = "SELECT * FROM Dent WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? RowMappers.mapDent(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la dent " + id, e);
        }
    }

    @Override
    public List<Dent> findByPatientId(Long patientId) {
        String sql = "SELECT * FROM Dent WHERE patient_id = ? ORDER BY numero";
        System.out.println("[JDBC DENT SQL] " + sql + " | patientId=" + patientId);
        List<Dent> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(RowMappers.mapDent(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des dents du patient " + patientId, e);
        }
        return out;
    }

    @Override
    public Optional<Dent> findByPatientIdAndNumero(Long patientId, int numero) {
        String sql = "SELECT * FROM Dent WHERE patient_id = ? AND numero = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, patientId);
            ps.setInt(2, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(RowMappers.mapDent(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la dent " + numero, e);
        }
    }

    @Override
    public void create(Dent d) {
        String sql = """
            INSERT INTO Dent(patient_id, numero, etat, note, dateCreation, dateMiseAJour, creePar, modifierPar)
            VALUES(?,?,?,?, NOW(), NOW(), ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, d.getPatientId());
            ps.setInt(2, d.getNumero());
            ps.setString(3, d.getEtat() != null ? d.getEtat().name() : EtatDent.SAIN.name());
            ps.setString(4, d.getNote());
            ps.setString(5, d.getCreePar());
            ps.setString(6, d.getModifierPar());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la dent", e);
        }
    }

    @Override
    public void update(Dent d) {
        String sql = """
            UPDATE Dent SET etat = ?, note = ?, dateMiseAJour = NOW(), modifierPar = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getEtat().name());
            ps.setString(2, d.getNote());
            ps.setString(3, d.getModifierPar());
            ps.setLong(4, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la dent " + d.getId(), e);
        }
    }

    @Override
    public void upsert(Dent d) {
        Optional<Dent> existing = findByPatientIdAndNumero(d.getPatientId(), d.getNumero());
        if (existing.isPresent()) {
            d.setId(existing.get().getId());
            update(d);
        } else {
            create(d);
        }
    }

    @Override
    public void delete(Dent d) {
        deleteById(d.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM Dent WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la dent " + id, e);
        }
    }
}
