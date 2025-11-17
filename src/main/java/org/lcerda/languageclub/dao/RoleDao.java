package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleDao {

    List<Role> findAll();

    Set<String> findRoleCodesByUserId(UUID userId);

}
