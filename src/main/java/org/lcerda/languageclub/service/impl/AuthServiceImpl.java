package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.AuthService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.util.BCryptUtil;

import java.util.Locale;
import java.util.regex.Pattern;

@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao; //desde el Servlet

    //googleado formato de email valido
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public User login(String email, String rawPassword) {
        if (email == null || email.isBlank() ||  rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are obligatory");
        }

        String normalizedEmail = normalizeEmail(email);
        //googleado formato de email valido
        if (!EMAIL_RE.matcher(normalizedEmail).matches()) {
            throw new ValidationException("Invalid email format");
        }

        //buscar con el dato el usuario
        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        //verificar hash
        if(!BCryptUtil.matches(rawPassword,user.getPasswordHash())){
            throw new AuthException("Invalid password");
        }

        if (!user.isActive()) {
            throw new AuthException("User is deactivated");
        }

        return user;
    }

    @Override
    public void register(String email, String rawPassword, String fullName) {
        if (email == null || email.isBlank()
                || rawPassword == null || rawPassword.isBlank()
                || fullName == null || fullName.isBlank()) {
            throw new ValidationException("Email, password and full name are required.");
        }

        String normalizedEmail = normalizeEmail(email);
        if (!EMAIL_RE.matcher(normalizedEmail).matches()) {
            throw new ValidationException("Invalid email format");
        }

        validatePassword(rawPassword);

        String cleanName = fullName.trim();
        if (cleanName.length() < 3) {
            throw new ValidationException("Full name is too short, at least 3 characters");
        }

        String hash = BCryptUtil.hashPassword(rawPassword);

        // Construimos el User para pasarlo al DAO
        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(hash)
                .fullName(cleanName)
                .isActive(true)
                .build();

        userDao.create(user);
    }

    // ===== helpers privados =====

    // Normaliza el email (trim + lowercase) antes de usarlo
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Validación password: mínimo 8 caracteres, al menos una letra y un número
    private static void validatePassword(String rawPassword) {
        if (rawPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);

        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password must contain at least one letter and one digit.");
        }
    }
}
