package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AssignmentAssigneeDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AssignmentAssigneeDaoJdbcImpl implements AssignmentAssigneeDao {

    private final Connection connection;

    private static final String FIND_USERS = "SELECT user_id FROM assignment_assignees WHERE assignment_id = ?";

    private static final String DELETE_BY_ASSIGNMENT = """
        DELETE FROM assignment_assignees
        WHERE assignment_id = ?
        """;

    private static final String INSERT_ASSIGNEE = """
        INSERT INTO assignment_assignees (assignment_id, user_id)
        VALUES (?, ?)
        """;

    private static final String EXISTS_ASSIGNEE = """
        SELECT 1
        FROM assignment_assignees
        WHERE assignment_id = ? AND user_id = ?
        """;

    @Override
    public Set<UUID> findUserIdsByAssignment(UUID assignmentId) {
        Set<UUID> ids = new HashSet<>();
        if (assignmentId == null) return ids;

        try (PreparedStatement ps = connection.prepareStatement(FIND_USERS)) {
            ps.setObject(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getObject("user_id", UUID.class));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading assignees for assignment " + assignmentId, e);
        }
        return ids;
    }

    @Override
    public void replaceAssignees(UUID assignmentId, Set<UUID> userIds) {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId cannot be null");
        }

        try {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement del = connection.prepareStatement(DELETE_BY_ASSIGNMENT)) {
                del.setObject(1, assignmentId);
                del.executeUpdate();
            }

            if (userIds != null && !userIds.isEmpty()) {
                try (PreparedStatement ins = connection.prepareStatement(INSERT_ASSIGNEE)) {
                    for (UUID userId : userIds) {
                        ins.setObject(1, assignmentId);
                        ins.setObject(2, userId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            connection.commit();
            connection.setAutoCommit(oldAutoCommit);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e2) {
                //no hago nada el primero catch es el importnate
            }
            throw new RuntimeException("Error replacing assignees for assignment " + assignmentId, e);
        }
    }

    @Override
    public boolean isUserAssignee(UUID assignmentId, UUID userId) {
        if (assignmentId == null || userId == null) return false;

        try (PreparedStatement ps = connection.prepareStatement(EXISTS_ASSIGNEE)) {
            ps.setObject(1, assignmentId);
            ps.setObject(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking assignee for assignment " + assignmentId, e);
        }
    }
}
