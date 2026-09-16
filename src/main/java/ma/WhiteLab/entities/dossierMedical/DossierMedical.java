package ma.WhiteLab.entities.dossierMedical;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.mvc.dto.DossierMedicalDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DossierMedical extends BaseEntity {
    private String historique;

    private Patient pat;
    private Medecin medecine;
    private List<RendezVous> rendezVous;
    private List<Consultation> consultations;
    private List<Ordonnance> ordonnance;
    private List<Certificat> certificat;
    private SituationFinanciere situationFinanciere;

}
