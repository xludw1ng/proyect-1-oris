package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AttendanceDao;
import org.lcerda.languageclub.model.Attendance;
import org.lcerda.languageclub.model.AttendanceStatus;

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
public class AttendanceDaoJdbcImpl implements AttendanceDao {

    private final Connection connection;

    private static final String FIND_ALL_STATUES = "SELECT id,code FROM attendance_status ORDER BY id";

    private static final String FIND_BY_LESSON = """
        SELECT lesson_id,user_id,status_id,attended_at,created_at,comment
        FROM attendance
        WHERE lesson_id = ?
        ORDER BY user_id
    """;

    private static final String FIND_ONE = """
        SELECT lesson_id,
               user_id,
               status_id,
               attended_at,
               created_at,
               comment
        FROM attendance
        WHERE lesson_id = ? AND user_id = ?
    """;

        private static final String UPSERT = """
        INSERT INTO attendance (lesson_id, user_id, status_id, attended_at, comment)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (lesson_id, user_id) DO UPDATE
        SET status_id   = EXCLUDED.status_id,
            attended_at = EXCLUDED.attended_at,
            comment     = EXCLUDED.comment
    """;

    private static final String DELETE_BY_LESSON = """
        DELETE FROM attendance WHERE lesson_id = ?
    """;

    private static final String DELETE_ONE = """
        DELETE FROM attendance WHERE lesson_id = ? AND user_id = ?
    """;

    // helper para mapear una fila a Attendance
    private static Attendance mapAtt(ResultSet rs) throws SQLException {
        UUID lessonId = rs.getObject("lesson_id", UUID.class);
        UUID userId   = rs.getObject("user_id",   UUID.class);
        short statusId = rs.getShort("status_id");
        OffsetDateTime attendedAt = rs.getObject("attended_at", OffsetDateTime.class);
        OffsetDateTime createdAt  = rs.getObject("created_at",  OffsetDateTime.class);
        String comment = rs.getString("comment");

        return Attendance.builder()
                .lessonId(lessonId)
                .userId(userId)
                .statusId(statusId)
                .attendedAt(attendedAt)
                .createdAt(createdAt)
                .comment(comment)
                .build();
    }


    @Override
    public List<Attendance> findByLessonId(UUID lessonId) {
        List<Attendance> result = new ArrayList<>();
        if (lessonId == null) {
            return result;
        }

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_LESSON)) {
            ps.setObject(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapAtt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByLessonId error: " + lessonId, e);
        }
        return result;
    }

    @Override
    public Optional<Attendance> findByLessonIdAndUserId(UUID lessonId, UUID userId) {
        if (lessonId == null || userId == null) {
            return Optional.empty();
        }

        try (PreparedStatement ps = connection.prepareStatement(FIND_ONE)) {
            ps.setObject(1, lessonId);
            ps.setObject(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAtt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "findByLessonIdAndUserId error: lessonId=" + lessonId + ", userId=" + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public void upsert(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null.");
        }

        try (PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            ps.setObject(1, attendance.getLessonId());
            ps.setObject(2, attendance.getUserId());
            ps.setShort(3, attendance.getStatusId());

            // attended_at puede ser null (si solo marcas ausencia sin timestamp)
            if (attendance.getAttendedAt() != null) {
                ps.setObject(4, attendance.getAttendedAt());
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP_WITH_TIMEZONE);
            }

            ps.setString(5, attendance.getComment());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("upsert attendance error: " + attendance, e);
        }
    }

    @Override
    public void deleteByLessonId(UUID lessonId) {
        if (lessonId == null) return;

        try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_LESSON)) {
            ps.setObject(1, lessonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByLessonId error: " + lessonId, e);
        }
    }

    @Override
    public void deleteByLessonIdAndUserId(UUID lessonId, UUID userId) {
        if (lessonId == null || userId == null) return;

        try (PreparedStatement ps = connection.prepareStatement(DELETE_ONE)) {
            ps.setObject(1, lessonId);
            ps.setObject(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "deleteByLessonIdAndUserId error: lessonId=" + lessonId + ", userId=" + userId, e);
        }
    }

    @Override
    public List<AttendanceStatus> findAllStatuses() {
        List<AttendanceStatus> result = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(FIND_ALL_STATUES);
        ResultSet rs = ps.executeQuery()){
            while (rs.next()){
                AttendanceStatus attendanceStatus = AttendanceStatus.builder()
                        .id(rs.getShort("id"))
                        .code(rs.getString("code"))
                        .build();
                result.add(attendanceStatus);
            }
        }catch (SQLException e) {
            throw new RuntimeException("findAllStatuses error: " + e.getMessage(), e);
        }
        return result;
    }
}
