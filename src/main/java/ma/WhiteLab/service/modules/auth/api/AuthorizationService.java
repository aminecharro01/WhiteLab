package ma.WhiteLab.service.modules.auth.api;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

public interface AuthorizationService {

    boolean hasRole(UserPrincipal principal, RoleR role);

    boolean hasAnyRole(UserPrincipal principal, RoleR... roles);

    boolean hasPrivilege(UserPrincipal principal, String privilege);

    /**
     * Lève AuthorizationException si le rôle / privilège est absent.
     */
    void checkRole(UserPrincipal principal, RoleR role);

    void checkAnyRole(UserPrincipal principal, RoleR... roles);

    void checkPrivilege(UserPrincipal principal, String privilege);
}
