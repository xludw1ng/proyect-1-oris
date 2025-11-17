package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Role;
import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    void createUser(String email,String rawPassword, String fullName);
    void updateUserRoles(UUID userId, Set<String> rolesCodes);
    void toggleUserActive(UUID userId, boolean currentActive);
    boolean deleteUser(UUID userId);
    List<User> findAllUsers();
    Map<UUID, Set<String>> buildUserRolesMap(List<User> users);
    List<Role> findAllRoles();
}
