package ma.WhiteLab.service.modules.caisse.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RevenusDTO {
    private Long id;
    private String titre;
    private String description;
    private Double montant;
    private LocalDateTime date;
    private Long cabinetId;
    private LocalDateTime dateCreation;
}