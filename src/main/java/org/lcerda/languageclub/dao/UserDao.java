package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    UUID create(User user);

    //admin
    List<User> findAll();
    void updateActive(UUID id, boolean active);
    boolean deleteById(UUID id);
    List<User> findActiveStudents();
    List<User> findAllByRoleCode(String roleCode);
}
