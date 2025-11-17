package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.SubmissionsDao;
import org.lcerda.languageclub.model.Submissions;
import org.lcerda.languageclub.model.SubmissionsStatus;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class SubmissionsDaoJdbcImpl implements SubmissionsDao {

    private Connection connection;

    private static final String BASE_SELECT = """
            SELECT id, assignment_id, user_id, status_id,
                   submitted_at, graded_at,
                   created_at, updated_at,
                   text_answer, attachment_path, grade
            FROM submissions
            """;

    private static final String FIND_BY_ASSIGNMENT =
            BASE_SELECT + " WHERE assignment_id = ? ORDER BY submitted_at NULLS LAST, created_at";

    private static final String FIND_ONE =
            BASE_SELECT + " WHERE assignment_id = ? AND user_id = ?";

    // upsert por (assignment_id, user_id)
    private static final String UPSERT = """
            INSERT INTO submissions
            (id, assignment_id, user_id, status_id, submitted_at,
             text_answer, attachment_path)
            VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?)
            ON CONFLICT (assignment_id, user_id) DO UPDATE
            SET status_id       = EXCLUDED.status_id,
                submitted_at    = EXCLUDED.submitted_at,
                text_answer     = EXCLUDED.text_answer,
                attachment_path = EXCLUDED.attachment_path,
                updated_at      = NOW()
            """;

    private static final String UPDATE_STATUS_GRADE = """
            UPDATE submissions
            SET status_id = ?, grade = ?, graded_at = NOW()
            WHERE id = ?
            """;

    private static final String FIND_ALL_STATUSES = """
            SELECT id, code
            FROM submissions_status
            ORDER BY id
            """;


    //helper
    private Submissions mapRow(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        UUID assignmentId = rs.getObject("assignment_id", UUID.class);
        UUID userId = rs.getObject("user_id", UUID.class);
        short statusId = rs.getShort("status_id");
        OffsetDateTime submittedAt = rs.getObject("submitted_at", OffsetDateTime.class);
        OffsetDateTime gradedAt = rs.getObject("graded_at", OffsetDateTime.class);
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
        String textAnswer = rs.getString("text_answer");
        String attachmentPath = rs.getString("attachment_path");
        Integer grade = (Integer) rs.getObject("grade");

        return Submissions.builder()
                .id(id)
                .assignmentId(assignmentId)
                .userId(userId)
                .statusId(statusId)
                .submittedAt(submittedAt)
                .gradedAt(gradedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .textAnswer(textAnswer)
                .attachmentPath(attachmentPath)
                .grade(grade)
                .build();
    }

    @Override
    public List<Submissions> findByAssignmentId(UUID assignmentId) {
        List<Submissions> list = new ArrayList<>();
        if (assignmentId == null) return list;

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ASSIGNMENT)) {
            ps.setObject(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading submissions for assignment " + assignmentId, e);
        }
        return list;
    }

    @Override
    public Optional<Submissions> findByAssignmentIdAndUserId(UUID assignmentId, UUID userId) {
        if (assignmentId == null || userId == null) return Optional.empty();

        try (PreparedStatement ps = connection.prepareStatement(FIND_ONE)) {
            ps.setObject(1, assignmentId);
            ps.setObject(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading submission", e);
        }
        return Optional.empty();
    }

    @Override
    public void upsert(Submissions submission) {
        if (submission == null) {
            throw new IllegalArgumentException("submission cannot be null");
        }

        try (PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            ps.setObject(1, submission.getAssignmentId());
            ps.setObject(2, submission.getUserId());
            ps.setShort(3, submission.getStatusId());
            ps.setObject(4, submission.getSubmittedAt());
            ps.setString(5, submission.getTextAnswer());
            ps.setString(6, submission.getAttachmentPath());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error upserting submission", e);
        }
    }

    @Override
    public void updateStatusAndGrade(UUID submissionId, short statusId, Integer grade) {
        if (submissionId == null) return;

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_STATUS_GRADE)) {
            ps.setShort(1, statusId);
            if (grade != null) {
                ps.setInt(2, grade);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setObject(3, submissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error grading submission " + submissionId, e);
        }
    }

    @Override
    public List<SubmissionsStatus> findAllStatuses() {
        List<SubmissionsStatus> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_STATUSES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                short id = rs.getShort("id");
                String code = rs.getString("code");

                SubmissionsStatus status = SubmissionsStatus.builder()
                        .id(id)
                        .code(code)
                        .build();

                result.add(status);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading submissions statuses", e);
        }
        return result;
    }

}
