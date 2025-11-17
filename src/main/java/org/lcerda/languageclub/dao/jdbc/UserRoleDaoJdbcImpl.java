package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.UserRoleDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class UserRoleDaoJdbcImpl implements UserRoleDao {

    private final Connection connection;

    private static final String DELETE_BY_USER = """
        DELETE FROM user_roles
        WHERE user_id = ?
    """;

    // insert usando el catálogo roles (code -> id)
    private static final String INSERT_ROLE_FOR_USER = """
        INSERT INTO user_roles (user_id, role_id)
        SELECT ?, r.id
        FROM roles as r
        WHERE r.code = ?
    """;

    @Override
    public void replaceUserRoles(UUID userId, Set<String> userRoles) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        try{
            try(PreparedStatement ps = connection.prepareStatement(DELETE_BY_USER)){
                ps.setObject(1,userId);
                ps.executeUpdate();
            }

            if(userRoles!=null){
                try(PreparedStatement ps = connection.prepareStatement(INSERT_ROLE_FOR_USER)){
                    for (String code: userRoles){
                        ps.setObject(1,userId);
                        ps.setString(2,code);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error replacing roles for user: " + userId,e);
        }
    }
}
