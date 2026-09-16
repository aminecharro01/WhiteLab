package ma.WhiteLab.service.modules.caisse.dto;

import lombok.Data;
import ma.WhiteLab.entities.enums.StatutFacture;
import java.time.LocalDate;

@Data
public class FactureDTO {
    private Long id;
    private Float totalFact;
    private Float totalPaye;
    private Float reste;
    private LocalDate date;
    private StatutFacture statut;
    private Long consultationId;
    private Long suiviFinancierId;
}