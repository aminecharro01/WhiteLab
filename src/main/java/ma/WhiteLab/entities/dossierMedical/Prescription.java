package ma.WhiteLab.entities.dossierMedical;

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
public class Prescription extends BaseEntity {
    private int qte;
    private String frequence;
    private int duree;

    private Ordonnance ordonnance;
    private Medicament medicament;
}
