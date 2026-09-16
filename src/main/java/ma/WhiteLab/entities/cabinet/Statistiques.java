package ma.WhiteLab.entities.cabinet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.CategorieStatistique;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Statistiques extends BaseEntity {
    private String nom;
    private CategorieStatistique categorie;
    private Double chiffre;
    private LocalDate dateCalcul;

    private CabinetMedicale cabinetMedicale;
}
