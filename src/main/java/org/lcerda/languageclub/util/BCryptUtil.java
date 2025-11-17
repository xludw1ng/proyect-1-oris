package org.lcerda.languageclub.util;

import lombok.NoArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

@NoArgsConstructor
public final class BCryptUtil {

    private static final int WORK_FACTOR=10; //coste de CPU


    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            throw  new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
