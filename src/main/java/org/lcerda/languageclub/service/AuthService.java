package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.User;

public interface AuthService {
    User login(String email, String rawPassword);           // throws AuthException si credenciales invalidas
    void register(String email, String rawPassword, String fullName); // throws Validation/Duplicate
}
