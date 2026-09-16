package ma.WhiteLab.service.modules.users.dto;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.Sexe;
import java.time.LocalDate;

public record CreateUserRequest(
    String nom,
    String prenom,
    String email,
    String motDePasse,
    String adresse,
    String cin,
    String telephone,
    Sexe sexe,
    LocalDate dateNaissance,
    RoleR role, // Primary role determines the subclass
    
    // Staff fields
    Double salaire,
    Double prime,
    LocalDate dateRecrutement,
    Integer soldeConge,
    
    // Medecin specific
    String specialite,
    
    // Secretaire specific
    String numCNSS,
    Double commission
) {}
