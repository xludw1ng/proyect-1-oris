package org.lcerda.languageclub.dao;


import java.util.Set;
import java.util.UUID;

public interface UserRoleDao {

    void replaceUserRoles(UUID userId, Set<String> userRoles);
}
