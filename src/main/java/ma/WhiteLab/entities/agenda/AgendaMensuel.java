package ma.WhiteLab.entities.agenda;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.Jour;
import ma.WhiteLab.entities.enums.Mois;
import ma.WhiteLab.entities.user.Medecin;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AgendaMensuel extends BaseEntity {
    private Mois mois;
    private List<Jour> joursNonDisponible;

    private Medecin medecin;
}
