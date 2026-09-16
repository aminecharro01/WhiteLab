package ma.WhiteLab.repository.modules.agenda.impl.mySQL;

import lombok.AllArgsConstructor;
import lombok.Data;
import ma.WhiteLab.entities.enums.Jour;
import ma.WhiteLab.entities.enums.Mois;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.agenda.AgendaMensuel;
import ma.WhiteLab.repository.modules.agenda.api.AgendaMensuelRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class AgendaMensuelRepositoryImpl implements AgendaMensuelRepository {

    private Connection connection; // Injectée par ApplicationContext

    // =========================
    // CRUD
    // =========================

    @Override
    public List<AgendaMensuel> findAll() {
        String sql = "SELECT * FROM AgendaMensuel";
        System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql);

        List<AgendaMensuel> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AgendaMensuel agenda = mapAgendaMensuel(rs);
                agenda.setJoursNonDisponible(getJoursOfAgenda(agenda.getId()));
                out.add(agenda);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les agendas mensuels", e);
        }

        return out;
    }

    @Override
    public AgendaMensuel findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'agenda ne peut pas être null.");
        }
        String sql = "SELECT * FROM AgendaMensuel WHERE id = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql + " | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AgendaMensuel agenda = mapAgendaMensuel(rs);
                    agenda.setJoursNonDisponible(getJoursOfAgenda(agenda.getId()));
                    return agenda;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'agenda mensuel par ID : " + id, e);
        }

        return null;
    }

    @Override
    public void create(AgendaMensuel a) {
        String sql = """
            INSERT INTO AgendaMensuel (mois, medecin_id, dateCreation, dateMiseAJour, creePar, modifierPar)
            VALUES (?, ?, NOW(), NOW(), ?, ?)
            """;
        System.out.println("[JDBC AGENDA MENSUEL SQL] INSERT AgendaMensuel | mois=" + a.getMois());

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getMois() != null ? a.getMois().name() : null);

            if (a.getMedecin() != null && a.getMedecin().getId() != null) {
                ps.setLong(2, a.getMedecin().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            ps.setString(3, a.getCreePar());
            ps.setString(4, a.getModifierPar());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getLong(1));
                }
            }

            addJoursToAgenda(a.getId(), a.getJoursNonDisponible());

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'agenda mensuel", e);
        }
    }

    @Override
    public void update(AgendaMensuel a) {
        String sql = """
            UPDATE AgendaMensuel SET mois = ?, medecin_id = ?, dateMiseAJour = NOW(), modifierPar = ? WHERE id = ?
            """;
        System.out.println("[JDBC AGENDA MENSUEL SQL] UPDATE AgendaMensuel | id=" + a.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, a.getMois() != null ? a.getMois().name() : null);

            if (a.getMedecin() != null && a.getMedecin().getId() != null) {
                ps.setLong(2, a.getMedecin().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            ps.setString(3, a.getModifierPar());
            ps.setLong(4, a.getId());

            ps.executeUpdate();

            removeAllJoursFromAgenda(a.getId());
            addJoursToAgenda(a.getId(), a.getJoursNonDisponible());

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'agenda mensuel ID=" + a.getId(), e);
        }
    }

    @Override
    public void delete(AgendaMensuel a) {
        if (a != null && a.getId() != null) {
            deleteById(a.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM AgendaMensuel WHERE id = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] DELETE AgendaMensuel | id=" + id);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'agenda mensuel ID=" + id, e);
        }
    }

    // =========================
    // Queries par Médecin / Mois
    // =========================

    @Override
    public List<AgendaMensuel> findByMedecinId(Long medecinId) {
        if (medecinId == null) {
            return findAll();
        }

        String sql = "SELECT * FROM AgendaMensuel WHERE medecin_id = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql + " | medecinId=" + medecinId);

        List<AgendaMensuel> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AgendaMensuel agenda = mapAgendaMensuel(rs);
                    agenda.setJoursNonDisponible(getJoursOfAgenda(agenda.getId()));
                    out.add(agenda);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des agendas par médecin ID=" + medecinId, e);
        }

        return out;
    }

    @Override
    public List<AgendaMensuel> findByMedecinIdAndMois(Long medecinId, String mois) {
        if (medecinId == null) {
            String sql = "SELECT * FROM AgendaMensuel WHERE mois = ?";
            System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql + " | mois=" + mois);

            List<AgendaMensuel> out = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, mois);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AgendaMensuel agenda = mapAgendaMensuel(rs);
                        agenda.setJoursNonDisponible(getJoursOfAgenda(agenda.getId()));
                        out.add(agenda);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la recherche des agendas pour le mois " + mois, e);
            }
            return out;
        }

        String sql = "SELECT * FROM AgendaMensuel WHERE medecin_id = ? AND mois = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql + " | medecinId=" + medecinId + ", mois=" + mois);

        List<AgendaMensuel> out = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, medecinId);
            ps.setString(2, mois);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AgendaMensuel agenda = mapAgendaMensuel(rs);
                    agenda.setJoursNonDisponible(getJoursOfAgenda(agenda.getId()));
                    out.add(agenda);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'agenda par médecin ID=" + medecinId + " et mois=" + mois, e);
        }

        return out;
    }

    // =========================
    // Gestion des jours (Many-to-Many simplifiée)
    // =========================

    private void addJoursToAgenda(Long agendaId, List<Jour> jours) {
        if (jours == null || jours.isEmpty()) return;

        String sql = "INSERT INTO AgendaMensuel_Jour (agenda_id, jour_nom) VALUES (?, ?)";
        System.out.println("[JDBC AGENDA MENSUEL SQL] INSERT AgendaMensuel_Jour | agendaId=" + agendaId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Jour jour : jours) {
                ps.setLong(1, agendaId);
                ps.setString(2, jour.name());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout des jours non disponibles à l'agenda ID=" + agendaId, e);
        }
    }

    private void removeAllJoursFromAgenda(Long agendaId) {
        String sql = "DELETE FROM AgendaMensuel_Jour WHERE agenda_id = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] DELETE ALL jours | agendaId=" + agendaId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, agendaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des jours de l'agenda ID=" + agendaId, e);
        }
    }

    private List<Jour> getJoursOfAgenda(Long agendaId) {
        List<Jour> jours = new ArrayList<>();
        String sql = "SELECT jour_nom FROM AgendaMensuel_Jour WHERE agenda_id = ?";
        System.out.println("[JDBC AGENDA MENSUEL SQL] " + sql + " | agendaId=" + agendaId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, agendaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jours.add(Jour.fromString(rs.getString("jour_nom")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des jours non disponibles pour l'agenda ID=" + agendaId, e);
        }

        return jours;
    }

    // =========================
    // Mapper
    // =========================

    private AgendaMensuel mapAgendaMensuel(ResultSet rs) throws SQLException {
        AgendaMensuel a = new AgendaMensuel();
        a.setId(rs.getLong("id"));
        a.setMois(rs.getString("mois") != null ? Mois.valueOf(rs.getString("mois")) : null);

        long medId = rs.getLong("medecin_id");
        if (!rs.wasNull()) {
            Medecin m = new Medecin();
            m.setId(medId);
            a.setMedecin(m);
        }

        a.setCreePar(rs.getString("creePar"));
        a.setModifierPar(rs.getString("modifierPar"));

        Timestamp tsCreation = rs.getTimestamp("dateCreation");
        if (tsCreation != null) a.setDateCreation(tsCreation.toLocalDateTime());

        Timestamp tsMiseAJour = rs.getTimestamp("dateMiseAJour");
        if (tsMiseAJour != null) a.setDateMiseAJour(tsMiseAJour.toLocalDateTime());

        return a;
    }
}
