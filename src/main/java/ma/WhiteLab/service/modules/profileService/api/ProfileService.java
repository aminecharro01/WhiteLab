package ma.WhiteLab.service.modules.profileService.api;


import ma.WhiteLab.mvc.dto.profileDtos.*;

public interface ProfileService {
    ProfileData loadByUserId(Long userId);
    ProfileUpdateResult update(ProfileUpdateRequest req);
    ChangePasswordResult changePassword(ChangePasswordRequest req);
}
