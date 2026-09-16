package ma.WhiteLab.mvc.dto.profileDtos;

import java.time.LocalDate;
import lombok.Builder;
import ma.WhiteLab.entities.enums.Sexe;

@Builder
public record ProfileUpdateRequest(
        Long id,

        // champs modifiables
        String prenom,
        String nom,
        String email,
        String adresse,
        String cin,
        String tel,
        Sexe sexe,
        LocalDate dateNaissance,
        String avatar,

        // Staff (si staff)
        Double salaire,
        Double prime,
        LocalDate dateRecrutement,
        Integer soldeConge,

        // Medecin
        String specialite,

        // Secretaire
        String numCNSS,
        Double commission
) {}
