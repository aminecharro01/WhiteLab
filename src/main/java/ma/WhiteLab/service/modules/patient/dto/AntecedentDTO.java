package ma.WhiteLab.service.modules.patient.dto;

import lombok.Getter;
import lombok.Setter;
import ma.WhiteLab.entities.patient.Antecedent;

import java.time.LocalDateTime;

@Getter
@Setter
public class AntecedentDTO {

    private Long id;

    private String nom;
    private String description;
    private String categorie;
    private String niveauRisque;

    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String creePar;
    private String modifierPar;

    public static AntecedentDTO fromEntity(Antecedent antecedent) {
        if (antecedent == null) return null;

        AntecedentDTO dto = new AntecedentDTO();
        dto.setId(antecedent.getId());
        dto.setNom(antecedent.getNom());
        dto.setDescription(antecedent.getDescription());
        dto.setCategorie(antecedent.getCategorie() != null ? antecedent.getCategorie().name() : null);
        dto.setNiveauRisque(antecedent.getNiveauDeRisk() != null ? antecedent.getNiveauDeRisk().name() : null);
        dto.setDateCreation(antecedent.getDateCreation());
        dto.setDateMiseAJour(antecedent.getDateMiseAJour());
        dto.setCreePar(antecedent.getCreePar());
        dto.setModifierPar(antecedent.getModifierPar());

        return dto;
    }
}