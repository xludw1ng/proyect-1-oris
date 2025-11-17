package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.RoleDao;
import org.lcerda.languageclub.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@AllArgsConstructor
public class RoleDaoJdbcImpl implements RoleDao {

    private final Connection connection;

    private static final String FIND_ALL = """
        SELECT id, code, title
        FROM roles
        ORDER BY code
    """;

    private static final String FIND_CODES_BY_USER_ID = """
        SELECT r.code
        FROM roles AS r
        INNER JOIN user_roles AS ur
            ON ur.role_id = r.id
        WHERE ur.user_id = ?
    """;


    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();

        try(PreparedStatement ps = connection.prepareStatement(FIND_ALL);
        ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                Role role = Role.builder()
                        .id(rs.getObject("id", UUID.class))
                        .code(rs.getString("code"))
                        .title(rs.getString("title"))
                        .build();
                roles.add(role);
            }
        }catch (SQLException e){
            throw new RuntimeException("Error loading all roles",e);
        }
        return roles;
    }

    @Override
    public Set<String> findRoleCodesByUserId(UUID userId) {
        Set<String> roles = new HashSet<>();
        if (userId == null) {
            return roles;
        }

        try(PreparedStatement ps = connection.prepareStatement(FIND_CODES_BY_USER_ID)){
            ps.setObject(1,userId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    roles.add(rs.getString("code"));
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error loading roles for user: " + userId, e);
        }
        return roles;
    }
}
