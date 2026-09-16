package ma.WhiteLab.mvc.dto.profileDtos;

import java.time.LocalDate;

import lombok.Builder;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.Sexe;

@Builder
public record ProfileData(
        Long id,
        RoleR rolePrincipal,

        // Utilisateur
        String prenom,
        String nom,
        String email,
        String adresse,
        String cin,
        String tel,
        Sexe sexe,
        String login,
        LocalDate lastLoginDate,
        LocalDate dateNaissance,
        String avatar,

        // Staff (optionnel)
        Double salaire,
        Double prime,
        LocalDate dateRecrutement,
        Integer soldeConge,
        Long cabinetId,

        // Medecin (optionnel)
        String specialite,

        // Secretaire (optionnel)
        String numCNSS,
        Double commission
) {}
