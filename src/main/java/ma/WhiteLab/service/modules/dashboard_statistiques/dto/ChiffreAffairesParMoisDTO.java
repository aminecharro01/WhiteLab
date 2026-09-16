package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiffreAffairesParMoisDTO {
    private int mois;
    private double montant;
}