package ma.WhiteLab.entities.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.PrioriteNotification;
import ma.WhiteLab.entities.enums.TitreNotification;
import ma.WhiteLab.entities.enums.TypeNotification;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    private TitreNotification titre;
    private String message;
    private LocalDate date;
    private LocalTime time;
    private TypeNotification type;
    private PrioriteNotification priorite;

    private List<Utilisateur> utilisateurs;
}
