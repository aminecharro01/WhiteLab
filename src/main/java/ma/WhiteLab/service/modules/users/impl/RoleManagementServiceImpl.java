package ma.WhiteLab.service.modules.users.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.user.Role;
import ma.WhiteLab.entities.user.RolePrivilege;
import ma.WhiteLab.repository.modules.user.api.RoleRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;
import ma.WhiteLab.service.modules.users.dto.RoleDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoleManagementServiceImpl implements RoleManagementService {

    private final RepoFactory<RoleRepository> roleRepoFactory;
    private final RepoFactory<UtilisateurRepository> userRepoFactory;

    public RoleManagementServiceImpl(
            RepoFactory<RoleRepository> roleRepoFactory,
            RepoFactory<UtilisateurRepository> userRepoFactory) {
        this.roleRepoFactory = roleRepoFactory;
        this.userRepoFactory = userRepoFactory;
    }

    // ================== LECTURE (SELECT) ==================

    @Override
    public List<RoleDto> getAllRoles() {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);
            return repo.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public RoleDto getRoleById(Long id) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);
            Role r = repo.findById(id);
            if (r == null) throw new IllegalArgumentException("Role not found: " + id);
            return mapToDto(r);
        });
    }

    @Override
    public Optional<RoleDto> getRoleByLibelle(RoleR libelle) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);
            return repo.findByLibelle(libelle)
                    .map(this::mapToDto);
        });
    }

    @Override
    public long countRoles() {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);
            return repo.count();
        });
    }

    // ================== CREATE / UPDATE / DELETE ==================

    @Override
    public RoleDto createRole(RoleDto dto, String creePar) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);

            if (repo.existsByLibelle(dto.libelle())) {
                throw new IllegalArgumentException("Role already exists: " + dto.libelle());
            }

            Role r = new Role();
            r.setLibelle(dto.libelle());
            r.setDescription(dto.description());
            r.setCreePar(creePar);
            r.setDateCreation(LocalDateTime.now());

            if (dto.privileges() != null) {
                List<RolePrivilege> privs = dto.privileges().stream()
                        .map(p -> {
                            RolePrivilege rp = new RolePrivilege();
                            rp.setPrivilege(p);
                            rp.setRole(r);
                            return rp;
                        }).collect(Collectors.toList());
                r.setPrivileges(privs);
            }

            repo.create(r);
            return mapToDto(r);
        });
    }

    @Override
    public RoleDto updateRole(RoleDto dto, String modifierPar) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);

            Role r = repo.findById(dto.id());
            if (r == null) throw new IllegalArgumentException("Role not found");

            if (dto.libelle() != r.getLibelle() && repo.existsByLibelle(dto.libelle())) {
                throw new IllegalArgumentException("Role name conflict");
            }
            r.setLibelle(dto.libelle());
            r.setDescription(dto.description());
            r.setModifierPar(modifierPar);
            r.setDateMiseAJour(LocalDateTime.now());

            if (dto.privileges() != null) {
                List<RolePrivilege> newPrivs = dto.privileges().stream()
                        .map(p -> {
                            RolePrivilege rp = new RolePrivilege();
                            rp.setPrivilege(p);
                            rp.setRole(r);
                            return rp;
                        }).collect(Collectors.toList());
                r.setPrivileges(newPrivs);
            }

            repo.update(r);
            return mapToDto(r);
        });
    }

    @Override
    public void deleteRole(Long id) {
        Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    // ================== UTILISATEUR ==================

    @Override
    public void assignRoleToUser(Long userId, RoleR roleName) {
        Transaction.initTransaction(cnx -> {
            RoleRepository roleRepo = roleRepoFactory.create(cnx);
            UtilisateurRepository userRepo = userRepoFactory.create(cnx);

            Role role = roleRepo.findByLibelle(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

            userRepo.addRoleToUtilisateur(userId, role.getId());
            return null;
        });
    }

    @Override
    public void removeRoleFromUser(Long userId, RoleR roleName) {
        Transaction.initTransaction(cnx -> {
            RoleRepository roleRepo = roleRepoFactory.create(cnx);
            UtilisateurRepository userRepo = userRepoFactory.create(cnx);

            Role role = roleRepo.findByLibelle(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

            userRepo.removeRoleFromUtilisateur(userId, role.getId());
            return null;
        });
    }

    // ================== PRIVILEGES ==================

    @Override
    public void updateRolePrivileges(Long roleId, List<String> privileges) {
        Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);

            Role r = repo.findById(roleId);
            if (r == null) throw new IllegalArgumentException("Role not found");

            List<RolePrivilege> newPrivs = privileges.stream()
                    .map(p -> {
                        RolePrivilege rp = new RolePrivilege();
                        rp.setPrivilege(p);
                        rp.setRole(r);
                        return rp;
                    }).collect(Collectors.toList());

            r.setPrivileges(newPrivs);
            repo.update(r);
            return null;
        });
    }

    @Override
    public List<String> getRolePrivileges(Long roleId) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);

            Role r = repo.findById(roleId);
            if (r == null) throw new IllegalArgumentException("Role not found");

            return r.getPrivileges() == null ? List.of() :
                    r.getPrivileges().stream().map(RolePrivilege::getPrivilege).toList();
        });
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return Transaction.initTransaction(cnx -> {
            RoleRepository repo = roleRepoFactory.create(cnx);

            List<Role> roles = repo.findRolesByUserId(userId);
            return roles.stream()
                    .flatMap(r -> r.getPrivileges().stream())
                    .map(RolePrivilege::getPrivilege)
                    .distinct()
                    .toList();
        });
    }

    // ================== MAPPERS ==================

    private RoleDto mapToDto(Role r) {
        List<String> privs = r.getPrivileges() == null ? List.of() :
                r.getPrivileges().stream().map(RolePrivilege::getPrivilege).toList();
        return new RoleDto(r.getId(), r.getLibelle(), r.getDescription(), privs);
    }
}
