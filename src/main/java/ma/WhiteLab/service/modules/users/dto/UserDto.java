package ma.WhiteLab.service.modules.users.dto;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.Sexe;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserDto(
    Long id,
    String nom,
    String prenom,
    String email,
    String adresse,
    String cin,
    String telephone,
    Sexe sexe,
    LocalDate dateNaissance,
    RoleR role, // The main role (Type)
    LocalDateTime lastLoginDate,
    boolean active, // account status
    
    // Staff fields
    Double salaire,
    Double prime,
    LocalDate dateRecrutement,
    Integer soldeConge,
    
    // Medecin specific
    String specialite,
    
    // Secretaire specific
    String numCNSS,
    Double commission,
    
    List<String> assignedRoles // List of all role names assigned
) {}
