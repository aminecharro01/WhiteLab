package ma.WhiteLab.entities.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.RoleR;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

    private RoleR libelle; // Enum

    private String description;

    // Relation vers Role_Privilege
    private List<RolePrivilege> privileges;

    private List<Utilisateur> utilisateurs;
}
