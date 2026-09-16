package ma.WhiteLab.repository.modules.dashboard.impl;

import ma.WhiteLab.conf.SessionFactory;
import ma.WhiteLab.repository.modules.dashboard.api.DashboardRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;

public class DashboardRepositoryImpl implements DashboardRepository {

    @Override
    public BigDecimal getTotalRevenus(Long cabinetId, LocalDate start, LocalDate end) {
        String sql = "SELECT COALESCE(SUM(montant),0.0) FROM revenus WHERE cabinet_id=? AND DATE(date) BETWEEN ? AND ?";
        return querySum(sql, cabinetId, start, end);
    }

    @Override
    public BigDecimal getTotalCharges(Long cabinetId, LocalDate start, LocalDate end) {
        String sql = "SELECT COALESCE(SUM(montant),0.0) FROM charges WHERE cabinet_id=? AND DATE(date) BETWEEN ? AND ?";
        return querySum(sql, cabinetId, start, end);
    }

    @Override
    public int getTotalConsultations(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM Consultation cons " +
                "INNER JOIN DossierMedical dm ON cons.dossier_medical_id = dm.id " +
                "WHERE DATE(cons.date) BETWEEN ? AND ?";
        return queryCount(sql, start, end);
    }

    @Override
    public int getTotalPatients(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(DISTINCT dm.pat_id) FROM Consultation cons " +
                "INNER JOIN DossierMedical dm ON cons.dossier_medical_id = dm.id " +
                "WHERE DATE(cons.date) BETWEEN ? AND ?";
        return queryCount(sql, start, end);
    }

    // Méthodes utilitaires
    private BigDecimal querySum(String sql, Object... params) {
        try (Connection conn = SessionFactory.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? BigDecimal.valueOf(rs.getDouble(1)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur querySum: " + e.getMessage(), e);
        }
    }

    private int queryCount(String sql, Object... params) {
        try (Connection conn = SessionFactory.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur queryCount: " + e.getMessage(), e);
        }
    }

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof Long) ps.setLong(i + 1, (Long) p);
            else if (p instanceof LocalDate) ps.setDate(i + 1, Date.valueOf((LocalDate) p));
            else ps.setObject(i + 1, p);
        }
    }
}
