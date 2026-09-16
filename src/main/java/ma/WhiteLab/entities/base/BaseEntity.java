package ma.WhiteLab.entities.base;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    private Long id;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String modifierPar;
    private String creePar;
}
