package ma.WhiteLab.service.modules.consultation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDTO {
    private Long id;
    private LocalDateTime date;
    private String status;
    private String notes;
    private String observationsMedecin;
    private Long dossierMedicalId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String creePar;
    private String modifierPar;
}