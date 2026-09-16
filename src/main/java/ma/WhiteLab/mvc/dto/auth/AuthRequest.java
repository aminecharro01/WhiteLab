package ma.WhiteLab.mvc.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String login;  // email ou téléphone
    private String password;
}
