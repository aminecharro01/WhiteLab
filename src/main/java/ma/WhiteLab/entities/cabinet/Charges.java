package ma.WhiteLab.entities.cabinet;

import java.time.LocalDateTime;
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
public class Charges extends BaseEntity {
    private String titre;
    private String description;
    private Double montant;
    private LocalDateTime date;

    private CabinetMedicale cabinetMedicale;
}
