package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AssignmentDao;
import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.AssignmentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@AllArgsConstructor
public class AssignmentDaoJdbcImpl implements AssignmentDao {

    private final Connection connection;

    private static final String BASE_SELECT = """
            SELECT id, lesson_id, status_id, title, description,
                   due_at, created_at, updated_at
            FROM assignments
            """;

    private static final String FIND_BY_LESSON =
            BASE_SELECT + " WHERE lesson_id = ? ORDER BY due_at NULLS LAST, created_at DESC";

    private static final String FIND_BY_ID =
            BASE_SELECT + " WHERE id = ?";

    private static final String INSERT = """
            INSERT INTO assignments (id, lesson_id, status_id, title, description, due_at)
            VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String UPDATE_STATUS = """
            UPDATE assignments
            SET status_id = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_STATUSES = """
            SELECT id, code
            FROM assignment_status
            ORDER BY id
            """;


    //helper
    private Assignment mapRow(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        UUID lessonId = rs.getObject("lesson_id", UUID.class);
        short statusId = rs.getShort("status_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        OffsetDateTime dueAt = rs.getObject("due_at", OffsetDateTime.class);
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);

        return Assignment.builder()
                .id(id)
                .lessonId(lessonId)
                .statusId(statusId)
                .title(title)
                .description(description)
                .dueAt(dueAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    @Override
    public List<Assignment> findByLessonId(UUID lessonId) {
        List<Assignment> result = new ArrayList<>();
        if (lessonId == null) return result;

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_LESSON)) {
            ps.setObject(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading assignmets for lesson: " + lessonId, e);
        }
        return result;
    }


    @Override
    public Optional<Assignment> findById(UUID id) {
        if (id == null) return Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading assignment " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public UUID create(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("assignment cannot be null");
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setObject(1, assignment.getLessonId());
            ps.setShort(2, assignment.getStatusId());
            ps.setString(3, assignment.getTitle());
            ps.setString(4, assignment.getDescription());
            ps.setObject(5, assignment.getDueAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("id", UUID.class);
                }
                throw new RuntimeException("INSERT into assignments did not return id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating assignment", e);
        }
    }


    @Override
    public void updateStatus(UUID assignmentId, short statusId) {
        if (assignmentId == null) return;

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_STATUS)) {
            ps.setShort(1, statusId);
            ps.setObject(2, assignmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status for assignment " + assignmentId, e);
        }
    }

    @Override
    public List<AssignmentStatus> findAllStatuses() {
        List<AssignmentStatus> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_STATUSES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                short id = rs.getShort("id");
                String code = rs.getString("code");

                AssignmentStatus status = AssignmentStatus.builder()
                        .id(id)
                        .code(code)
                        .build();

                result.add(status);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading assignment statuses", e);
        }
        return result;
    }

}
