package ma.WhiteLab.entities.patient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.EtatDent;

/**
 * Represents the state of a single tooth (FDI two-digit numbering, e.g. 11-18,
 * 21-28, 31-38, 41-48) for one patient's odontogram.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Dent extends BaseEntity {
    private Long patientId;
    private int numero;
    private EtatDent etat;
    private String note;
}
