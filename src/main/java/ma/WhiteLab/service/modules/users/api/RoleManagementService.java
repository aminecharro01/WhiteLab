package ma.WhiteLab.service.modules.users.api;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.service.modules.users.dto.RoleDto;

import java.util.List;
import java.util.Optional;

public interface RoleManagementService {

    // Role CRUD
    List<RoleDto> getAllRoles();
    RoleDto getRoleById(Long id);

    Optional<RoleDto> getRoleByLibelle(RoleR libelle);

    long countRoles();

    RoleDto createRole(RoleDto roleDto, String creePar); // Assuming RoleDto sufficient for create
    RoleDto updateRole(RoleDto roleDto, String modifierPar);
    void deleteRole(Long id);

    // Assignment & Permissions
    void assignRoleToUser(Long userId, RoleR roleName); // or RoleId
    void removeRoleFromUser(Long userId, RoleR roleName);
    
    // Permission Management (Privileges)
    void updateRolePrivileges(Long roleId, List<String> privileges);
    List<String> getRolePrivileges(Long roleId);
    
    List<String> getUserPermissions(Long userId); // Aggregated permissions
}
