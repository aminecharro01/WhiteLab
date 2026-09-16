package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardGlobalDTO {
    private double chiffreAffairesTotal;
    private long nombrePatients;
    private long nombreRendezVous;
    private long nombreActes;
}