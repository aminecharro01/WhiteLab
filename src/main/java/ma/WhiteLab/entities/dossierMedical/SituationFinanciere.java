package ma.WhiteLab.entities.dossierMedical;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.entities.enums.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SituationFinanciere extends BaseEntity {
    private float totalDesActes;
    private float totalPaye;
    private float credit;
    private PromoStatus enPromo;
    private Status status;

    private List<Facture> listeFactures;
    private DossierMedical dossierMedical;
}
