package ma.WhiteLab.mvc.dto.profileDtos;


import lombok.Builder;

@Builder
public record ChangePasswordRequest(
        Long userId,
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
