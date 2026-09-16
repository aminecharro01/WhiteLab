package ma.WhiteLab.entities.dossierMedical;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.StatutFacture;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Facture extends BaseEntity {
    private float totalFact;
    private float totalPaye;
    private float reste;
    private LocalDate date;
    private StatutFacture statut;

    private Consultation consultation;
    private SituationFinanciere sf;
}
