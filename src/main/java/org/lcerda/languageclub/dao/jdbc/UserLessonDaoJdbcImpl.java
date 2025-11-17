package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.UserLessonDao;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class UserLessonDaoJdbcImpl implements UserLessonDao {

    private final Connection connection;

    private static final String FIND_STUDENTS_BY_LESSON = """
        SELECT user_id
        FROM user_lessons
        WHERE lesson_id = ?
        """;

    private static final String DELETE_BY_LESSON = """
        DELETE FROM user_lessons
        WHERE lesson_id = ?
        """;

    private static final String INSERT_ENROLLMENT = """
        INSERT INTO user_lessons (user_id, lesson_id)
        VALUES (?, ?)
        """;

    @Override
    public Set<UUID> findStudentIdsByLesson(UUID lessonId) {
        Set<UUID> ids = new HashSet<>();
        if (lessonId == null) return ids;

        try (PreparedStatement ps = connection.prepareStatement(FIND_STUDENTS_BY_LESSON)) {
            ps.setObject(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getObject("user_id", UUID.class));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading students for lesson " + lessonId, e);
        }
        return ids;
    }

    @Override
    public void replaceEnrollments(UUID lessonId, Set<UUID> studentIds) {
        if (lessonId == null) {
            throw new IllegalArgumentException("lessonId cannot be null");
        }

        boolean oldAutoCommit;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1) borrar inscripciones viejas
            try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_LESSON)) {
                ps.setObject(1, lessonId);
                ps.executeUpdate();
            }

            // 2) insertar nuevas
            if (studentIds != null) {
                try (PreparedStatement ps = connection.prepareStatement(INSERT_ENROLLMENT)) {
                    for (UUID studentId : studentIds) {
                        ps.setObject(1, studentId);
                        ps.setObject(2, lessonId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            connection.commit();
            connection.setAutoCommit(oldAutoCommit);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error replacing enrollments for lesson " + lessonId, e);
        }
    }
}
