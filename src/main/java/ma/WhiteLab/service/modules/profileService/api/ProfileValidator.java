package ma.WhiteLab.service.modules.profileService.api;


import java.util.Map;
import ma.WhiteLab.mvc.dto.profileDtos.ProfileUpdateRequest;

public interface ProfileValidator {
    Map<String, String> validate(ProfileUpdateRequest req);
}
