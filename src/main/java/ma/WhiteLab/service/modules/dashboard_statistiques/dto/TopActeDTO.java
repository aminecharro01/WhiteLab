package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopActeDTO {
    private String nomActe;
    private long nombre;
    private double chiffreAffaires;
}