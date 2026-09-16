package ma.WhiteLab.service.modules.caisse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChargesDTO {
    private Long id;
    private String titre;
    private String description;
    private double montant;
    private LocalDateTime date;
    private Long cabinetId;
}