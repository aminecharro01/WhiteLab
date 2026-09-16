package ma.WhiteLab.service.modules.caisse.api;

import ma.WhiteLab.service.modules.caisse.dto.CaisseReportDTO;

import java.time.LocalDate;

public interface CaisseService {

    // Bilan global
    CaisseReportDTO getCaisseStats(Long cabinetId);

    // Profit global
    double calculateProfit(Long cabinetId);

    // Rapport mensuel
    CaisseReportDTO getMonthlyCaisseReport(Long cabinetId, int year, int month);

    // Rapport hebdomadaire
    CaisseReportDTO getWeeklyCaisseReport(Long cabinetId, LocalDate dateInWeek);

    // Rapport journalier
    CaisseReportDTO getDailyCaisseReport(Long cabinetId, LocalDate date);
}
