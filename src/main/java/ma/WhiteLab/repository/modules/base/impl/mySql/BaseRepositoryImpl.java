package ma.WhiteLab.repository.modules.base.impl.mySql;

import ma.WhiteLab.repository.modules.base.api.BaseRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//auteur : Aymane Akarbich

public abstract class BaseRepositoryImpl<T> implements BaseRepository<T> {

    protected final DataSource dataSource;
    protected final String tableName;

    protected BaseRepositoryImpl(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    protected abstract T map(ResultSet rs) throws SQLException;


    // ===============================================
    //  FIND ALL()
    // ===============================================
    @Override
    public List<T> findAll() {
        List<T> list = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }


    // ===============================================
    //  FIND BY ID
    // ===============================================
    @Override
    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }


    // ===============================================
    //  DELETE BY ID
    // ===============================================
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // ===============================================
    //  COUNT
    // ===============================================
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }


    // ===============================================
    //  EXISTS
    // ===============================================
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
