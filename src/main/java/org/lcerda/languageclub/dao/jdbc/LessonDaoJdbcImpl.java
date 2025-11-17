package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.model.Lesson;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class LessonDaoJdbcImpl implements LessonDao {

    private final Connection connection;

    private static final String BASE_SELECT = """
        SELECT id, teacher_id, series_id, topic, starts_at, ends_at,
               room, notes, created_at, updated_at
        FROM lessons
    """;

    private static final String FIND_ALL = BASE_SELECT + " ORDER BY starts_at DESC";

    private static final String FIND_BY_TEACHER = BASE_SELECT + " WHERE teacher_id = ? ORDER BY starts_at DESC";

    private static final String FIND_BY_STUDENT = """
        SELECT l.id, l.teacher_id, l.series_id, l.topic, l.starts_at, l.ends_at,
               l.room, l.notes, l.created_at, l.updated_at
        FROM lessons l
        INNER JOIN user_lessons ul ON ul.lesson_id = l.id
        WHERE ul.user_id = ?
        ORDER BY l.starts_at DESC
    """;

    private static final String FIND_BY_ID = BASE_SELECT + " WHERE id = ?";

    private static final String DELETE_BY_ID = "DELETE FROM lessons WHERE id = ?";

    private static final String EXISTS_BY_SERIES_AND_TEACHER = """
        SELECT 1
        FROM lessons
        WHERE series_id = ?
          AND teacher_id = ?
        LIMIT 1
    """;

    private static final String INSERT = """
        INSERT INTO lessons (id, teacher_id, series_id, topic, starts_at, ends_at, room, notes)
        VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?, ?)
        RETURNING id
    """;

    // helper

    private Lesson mapLesson(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        UUID teacherId = rs.getObject("teacher_id", UUID.class);
        UUID seriesId = rs.getObject("series_id", UUID.class);
        String topic = rs.getString("topic");
        OffsetDateTime startsAt = rs.getObject("starts_at", OffsetDateTime.class);
        OffsetDateTime endsAt = rs.getObject("ends_at", OffsetDateTime.class);
        String room = rs.getString("room");
        String notes = rs.getString("notes");
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);

        return Lesson.builder()
                .id(id)
                .teacherId(teacherId)
                .seriesId(seriesId)
                .topic(topic)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .room(room)
                .notes(notes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }


    @Override
    public List<Lesson> findAll() {
        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Lesson> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapLesson(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll lessons error", e);
        }
    }

    @Override
    public List<Lesson> findByTeacherId(UUID teacherId) {
        if (teacherId == null) return List.of();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_TEACHER)) {
            ps.setObject(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Lesson> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapLesson(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByTeacherId lessons error: " + teacherId, e);
        }
    }

    @Override
    public List<Lesson> findByStudentId(UUID studentId) {
        if (studentId == null) return List.of();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STUDENT)) {
            ps.setObject(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Lesson> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapLesson(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByStudentId lessons error: " + studentId, e);
        }
    }

    @Override
    public Optional<Lesson> findById(UUID id) {
        if (id == null) return Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapLesson(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById lesson error: " + id, e);
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_ID)) {
            ps.setObject(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting lesson with id: " + id, e);
        }
    }

    @Override
    public boolean existsBySeriesIdAndTeacherId(UUID seriesId, UUID teacherId) {
        if (seriesId == null || teacherId == null) return false;
        try (PreparedStatement ps = connection.prepareStatement(EXISTS_BY_SERIES_AND_TEACHER)) {
            ps.setObject(1, seriesId);
            ps.setObject(2, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking teacher assignment to series", e);
        }
    }

    @Override
    public UUID create(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("lesson cannot be null");
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setObject(1, lesson.getTeacherId());
            ps.setObject(2, lesson.getSeriesId());
            ps.setString(3, lesson.getTopic());
            ps.setObject(4, lesson.getStartsAt());
            ps.setObject(5, lesson.getEndsAt());
            ps.setString(6, lesson.getRoom());
            ps.setString(7, lesson.getNotes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("id", UUID.class);
                }
                throw new RuntimeException("INSERT into lessons did not return an id.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating lesson", e);
        }
    }
}
