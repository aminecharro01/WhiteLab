package ma.WhiteLab.repository.modules.user.api;

import ma.WhiteLab.entities.user.Role;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;
//auteur : Aymane Akarbich

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByLibelle(RoleR libelle);

    boolean existsByLibelle(RoleR libelle);

    long count();

    List<Role> findRolesByUserId(Long id);
}
