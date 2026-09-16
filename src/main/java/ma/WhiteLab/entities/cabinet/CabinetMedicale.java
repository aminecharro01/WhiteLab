package ma.WhiteLab.entities.cabinet;

import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.user.Staff;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CabinetMedicale extends BaseEntity {

    private String nom;
    private String email;
    private String logo;
    private String categorie;
    private String tel1;
    private String tel2;
    private String siteWeb;
    private String instagram;
    private String facebook;
    private String description;

    private List<Staff> staff;
    private List<Statistiques> statistiques;
    private List<Charges> charges;
    private List<Revenus> revenus;
}
