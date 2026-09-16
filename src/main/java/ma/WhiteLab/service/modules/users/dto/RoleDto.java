package ma.WhiteLab.service.modules.users.dto;

import ma.WhiteLab.entities.enums.RoleR;
import java.util.List;

public record RoleDto(
        Long id,
        RoleR libelle,
        String description,
        List<String> privileges
) {}
