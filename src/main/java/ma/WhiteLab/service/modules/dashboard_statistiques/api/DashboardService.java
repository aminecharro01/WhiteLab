package ma.WhiteLab.service.modules.dashboard_statistiques.api;

import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardDataDTO;

/**
 * Service interface for gathering data for the main application dashboard.
 */
public interface DashboardService {

    /**
     * Gathers all calculated data points needed to display the main dashboard.
     * @param cabinetId The ID of the cabinet to get data for.
     * @return A DTO containing all the dashboard metrics.
     */
    DashboardDataDTO getDashboardData(Long cabinetId);
}
