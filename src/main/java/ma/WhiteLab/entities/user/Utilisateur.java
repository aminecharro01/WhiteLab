package ma.WhiteLab.entities.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.Sexe;
import ma.WhiteLab.entities.enums.RoleR;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public abstract class Utilisateur extends BaseEntity {

    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String cin;
    private String telephone;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private LocalDateTime lastLoginDate;
    private String motDePasse;

    // 🔥 IMPORTANT
    private RoleR type;

    private List<Role> roles;
    private List<Notification> notifications;

    // ===== NEW FIELDS FOR FORGOT PASSWORD =====
    private String tempPassword;
    private LocalDateTime tempPasswordExpiry;
    private boolean mustChangePassword;
}
