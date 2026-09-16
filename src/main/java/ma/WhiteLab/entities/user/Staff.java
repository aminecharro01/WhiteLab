package ma.WhiteLab.entities.user;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Staff extends Utilisateur {
    private Double salaire;
    private Double prime;
    private LocalDate dateRecrutement;
    private int soldeConge;

    private CabinetMedicale cabinetMedicale;
}
