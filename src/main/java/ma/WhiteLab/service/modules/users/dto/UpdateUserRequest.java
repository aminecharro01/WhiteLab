package ma.WhiteLab.service.modules.users.dto;

import ma.WhiteLab.entities.enums.Sexe;
import java.time.LocalDate;

public record UpdateUserRequest(
    Long id,
    String nom,
    String prenom,
    String email,
    String adresse,
    String cin,
    String telephone,
    Sexe sexe,
    LocalDate dateNaissance,
    
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
