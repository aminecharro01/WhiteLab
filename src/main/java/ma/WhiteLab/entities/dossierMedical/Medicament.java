package ma.WhiteLab.entities.dossierMedical;

import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.Forme;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Medicament extends BaseEntity {
    private String nom;
    private String labo;
    private String type;
    private Forme forme;
    private boolean remboursable;
    private double prixUnitaire;
    private String description;

    private List<Prescription> prescriptions;

}
