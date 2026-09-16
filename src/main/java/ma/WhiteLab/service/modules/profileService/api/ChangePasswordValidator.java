package ma.WhiteLab.service.modules.profileService.api;


import java.util.Map;
import ma.WhiteLab.mvc.dto.profileDtos.ChangePasswordRequest;

public interface ChangePasswordValidator {
    Map<String, String> validate(ChangePasswordRequest req);
}
