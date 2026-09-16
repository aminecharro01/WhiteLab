package ma.WhiteLab.service.modules.auth.impl;

import ma.WhiteLab.service.modules.auth.api.PasswordEncoder;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int LOG_ROUNDS = 12; // complexité du hash

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) throw new IllegalArgumentException("Mot de passe ne peut pas être null");
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}
