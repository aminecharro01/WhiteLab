package ma.WhiteLab.entities.user;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@AllArgsConstructor
@SuperBuilder

@EqualsAndHashCode(callSuper = true)
public class Admin extends Utilisateur {

}
