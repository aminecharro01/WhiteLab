package ma.WhiteLab.service.modules.consultation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionDTO {
    private Long id;
    private double prixDePatient;
    private int numDent;
    private Long consultationId;
    private Long acteMedicalId;
    private String creePar;
    private LocalDateTime dateCreation;
}