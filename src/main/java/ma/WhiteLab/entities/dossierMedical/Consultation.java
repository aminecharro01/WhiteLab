package ma.WhiteLab.entities.dossierMedical;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.enums.StatusConsultation;
import ma.WhiteLab.entities.agenda.RendezVous;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Consultation extends BaseEntity {
    private LocalDateTime date;
    private StatusConsultation status;
    private String notes;
    private String observationsMedecin;

    private List<Ordonnance> ordonnances;
    private Certificat certificats;
    private List<InterventionMedecin> ims;
    private List<Facture> factures;
    private List<RendezVous> rendezVouses;
    private DossierMedical dossierMedical;
}
