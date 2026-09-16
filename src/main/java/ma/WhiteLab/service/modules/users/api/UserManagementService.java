package ma.WhiteLab.service.modules.users.api;

import ma.WhiteLab.service.modules.users.dto.CreateUserRequest;
import ma.WhiteLab.service.modules.users.dto.UpdateUserRequest;
import ma.WhiteLab.service.modules.users.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface UserManagementService {

    // CRUD
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto createUser(CreateUserRequest request, String creePar);
    UserDto updateUser(UpdateUserRequest request, String modifierPar);
    void deleteUser(Long id);

    // Security & Account
    void resetUserPassword(Long userId, String newPassword);
    void toggleUserAccountStatus(Long userId, boolean enable); // Assuming we have an 'active' field or similar
    List<LocalDateTime> getUserConnectionHistory(Long userId);
}
