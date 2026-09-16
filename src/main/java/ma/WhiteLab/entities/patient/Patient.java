package ma.WhiteLab.entities.patient;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.entities.enums.Sexe;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Patient extends BaseEntity implements Serializable {
    private String nom;
    private String prenom;
    private Sexe sexe;
    private String email;
    private LocalDate dateNaissance;
    private String adresse;
    private String telephone;
    private Assurance assurance;


    private DossierMedical dossierMed;
    private List<Antecedent> antecedents;

    public String getNomComplet() {

        return this.prenom + " " + this.nom;
    }

}
