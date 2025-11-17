package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.RoleDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.UserRoleDao;
import org.lcerda.languageclub.model.Role;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthService;
import org.lcerda.languageclub.service.UserService;
import org.lcerda.languageclub.service.ValidationException;

import java.util.*;

@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final UserRoleDao userRoleDao;
    private final AuthService authService;

    @Override
    public void createUser(String email, String rawPassword, String fullName) {
        if(email == null || rawPassword == null || fullName == null) {
            throw new ValidationException("Email, password and full are required");
        }
        authService.register(email, rawPassword, fullName);
    }

    @Override
    public void updateUserRoles(UUID userId, Set<String> rolesCodes) {
        if(userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if(rolesCodes == null) {
            rolesCodes = Collections.emptySet();
        }
        userRoleDao.replaceUserRoles(userId, rolesCodes);
    }

    @Override
    public void toggleUserActive(UUID userId, boolean currentActive) {
        if(userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        boolean newActive = !currentActive;
        userDao.updateActive(userId, newActive);
    }

    @Override
    public boolean deleteUser(UUID userId) {
        if(userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return userDao.deleteById(userId);
    }

    @Override
    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    @Override
    public Map<UUID, Set<String>> buildUserRolesMap(List<User> users) {
        Map<UUID, Set<String>> map = new HashMap<>();
        if(users == null) return map;
        for(User user : users) {
            Set<String> codes = roleDao.findRoleCodesByUserId(user.getId());
            map.put(user.getId(), codes);
        }
        return map;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleDao.findAll();
    }
}
