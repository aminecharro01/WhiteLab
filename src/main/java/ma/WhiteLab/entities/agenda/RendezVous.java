package ma.WhiteLab.entities.agenda;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RendezVous extends BaseEntity {

    private LocalDateTime date;
    private LocalTime time;
    private String motif;
    private Status status;
    private String noteMedecin;

    private DossierMedical dossierMed;
    private Consultation consultation;
}
