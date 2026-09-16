package ma.WhiteLab.service.modules.caisse.impl;

import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.service.modules.caisse.api.CaisseService;
import ma.WhiteLab.service.modules.caisse.api.ChargesService;
import ma.WhiteLab.service.modules.caisse.api.RevenusService;
import ma.WhiteLab.service.modules.caisse.dto.CaisseReportDTO;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public class CaisseServiceImpl implements CaisseService {

    private final RevenusService revenusService;
    private final ChargesService chargesService;

    public CaisseServiceImpl(RevenusService revenusService,
                             ChargesService chargesService) {
        this.revenusService = revenusService;
        this.chargesService = chargesService;
    }

    // ======================
    // GLOBAL STATS
    // ======================
    @Override
    public CaisseReportDTO getCaisseStats(Long cabinetId) {
        if (cabinetId == null || cabinetId <= 0) {
            throw new IllegalArgumentException("Invalid cabinet ID");
        }

        BigDecimal totalRevenus = BigDecimal.valueOf(
                revenusService.calculateTotalRevenus(cabinetId)
        );

        BigDecimal totalCharges = BigDecimal.valueOf(
                chargesService.calculateTotalCharges(cabinetId)
        );

        CaisseReportDTO dto = new CaisseReportDTO();
        dto.setTotalRevenus(totalRevenus);
        dto.setTotalCharges(totalCharges);
        dto.setSolde(totalRevenus.subtract(totalCharges));

        return dto;
    }

    @Override
    public double calculateProfit(Long cabinetId) {
        return getCaisseStats(cabinetId).getSolde().doubleValue();
    }

    // ======================
    // PERIODIC REPORTS
    // ======================

    @Override
    public CaisseReportDTO getDailyCaisseReport(Long cabinetId, LocalDate date) {
        if (cabinetId == null || cabinetId <= 0) throw new IllegalArgumentException("Invalid cabinet ID");
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return generateReportForPeriod(cabinetId, start, end);
    }

    @Override
    public CaisseReportDTO getWeeklyCaisseReport(Long cabinetId, LocalDate dateInWeek) {
        if (cabinetId == null || cabinetId <= 0) throw new IllegalArgumentException("Invalid cabinet ID");
        if (dateInWeek == null) throw new IllegalArgumentException("Date cannot be null");

        // Assuming week starts on Monday
        LocalDateTime start = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime end = dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);

        return generateReportForPeriod(cabinetId, start, end);
    }
    
    @Override
    public CaisseReportDTO getMonthlyCaisseReport(Long cabinetId, int year, int month) {
        if (cabinetId == null || cabinetId <= 0) throw new IllegalArgumentException("Invalid cabinet ID");

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        CaisseReportDTO dto = generateReportForPeriod(cabinetId, start, end);
        dto.setYear(year);
        dto.setMonth(month);
        return dto;
    }

    /**
     * Private helper method to generate a report for any given time period.
     */
    private CaisseReportDTO generateReportForPeriod(Long cabinetId, LocalDateTime start, LocalDateTime end) {
        List<Revenus> revenus = revenusService.findByDateBetween(start, end).stream()
                .filter(r -> r.getCabinetMedicale() != null && r.getCabinetMedicale().getId().equals(cabinetId))
                .collect(Collectors.toList());

        List<Charges> charges = chargesService.findByDateBetween(start, end).stream()
                .filter(c -> c.getCabinetMedicale() != null && c.getCabinetMedicale().getId().equals(cabinetId))
                .collect(Collectors.toList());

        BigDecimal totalRevenus = revenus.stream()
                .map(r -> BigDecimal.valueOf(r.getMontant() != null ? r.getMontant() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCharges = charges.stream()
                .map(c -> BigDecimal.valueOf(c.getMontant() != null ? c.getMontant() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CaisseReportDTO dto = new CaisseReportDTO();
        dto.setTotalRevenus(totalRevenus);
        dto.setTotalCharges(totalCharges);
        dto.setSolde(totalRevenus.subtract(totalCharges));
        return dto;
    }
}