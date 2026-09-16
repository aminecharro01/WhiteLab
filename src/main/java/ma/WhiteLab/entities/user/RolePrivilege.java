package ma.WhiteLab.entities.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "role") // ← important !
public class RolePrivilege extends BaseEntity {

    private Role role;       // rôle associé (avec RoleR libelle)

    private String privilege; // nom du privilège (ex: "USER_CREATE")
}
