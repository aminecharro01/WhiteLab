package ma.WhiteLab.service.modules.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentDTO {
    private Long id;
    private Long patientId;
    private int numero;
    private String etat;
    private String note;
}
