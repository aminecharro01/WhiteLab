package ma.WhiteLab.entities.dossierMedical;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ActeMedical extends BaseEntity {
    private String libelle;
    private String categorie;
    private float prixDeBase;

    private List<InterventionMedecin> interventionMedecin;
}
