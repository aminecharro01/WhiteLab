package ma.WhiteLab.entities.patient;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.entities.enums.NiveauDeRisk;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Antecedent extends BaseEntity {
    private String description;
    private String nom;
    private CategorieAntecedent categorie;
    private NiveauDeRisk niveauDeRisk;

    private List<Patient> patients;
}
