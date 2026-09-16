package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class DashboardDataDTO {

    private BigDecimal recetteDuJour;
    private BigDecimal recetteDuMois;
    private BigDecimal recetteDeAnnee;
    private BigDecimal depensesDuMois;
    private long nbrConsultationsDuJour;
    private long nbrConsultationsDuMois;
    private long nbrConsultationsDeAnnee;
    private long fileAttenteCount;
    private List<RendezVousDTO> rendezVousDuJour;

    // Admin specific fields
    private long totalUsers;
    private long totalCabinets;
    private List<UserSummaryDTO> latestUsers;
    private Map<String, Double> usersByRole;

    @Override
    public String toString() {
        return "DashboardDataDTO{" +
                "totalUsers=" + totalUsers +
                ", totalCabinets=" + totalCabinets +
                '}';
    }
}