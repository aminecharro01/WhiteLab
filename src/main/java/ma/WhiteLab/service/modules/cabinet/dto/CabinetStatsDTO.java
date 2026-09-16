package ma.WhiteLab.service.modules.cabinet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CabinetStatsDTO {
    private long totalCabinets;
    private long cabinetsAvecLogo;
    private long cabinetsAvecSiteWeb;
    private long cabinetsAvecReseauxSociaux;
    private String categorieLaPlusFrequente;
    private long nombreCategoriesDifferentes;
    private double pourcentageAvecEmail;
    private double pourcentageAvecTelephone;
}
