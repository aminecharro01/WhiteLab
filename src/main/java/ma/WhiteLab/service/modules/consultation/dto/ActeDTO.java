package ma.WhiteLab.service.modules.consultation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActeDTO {
    private Long id;
    private String libelle;
    private String categorie;
    private float prixDeBase;
    private String creePar;
}
