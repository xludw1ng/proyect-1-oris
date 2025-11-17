package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.SeriesDao;
import org.lcerda.languageclub.model.Series;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class SeriesDaoJdbcImpl implements SeriesDao {

    private final Connection connection;

    private static final String BASE_SELECT = """
                SELECT id, code, language, level, title, description, created_at
                FROM series
            """;

    private static final String FIND_ALL = BASE_SELECT + """
                ORDER BY created_at DESC
            """;

    private static final String FIND_BY_ID = BASE_SELECT + """
                WHERE id = ?
            """;

    private static final String FIND_BY_CODE = BASE_SELECT + """
                WHERE lower(code) = ?
            """;

    private static final String INSERT = """
                INSERT INTO series (id, code, language, level, title, description)
                VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)
                RETURNING id
            """;

    // series where this teacher has at least one lesson
    private static final String FIND_BY_TEACHER = """
                SELECT DISTINCT s.id, s.code, s.language, s.level, s.title, s.description, s.created_at
                FROM series as s
                JOIN lessons as l ON l.series_id = s.id
                WHERE l.teacher_id = ?
                ORDER BY s.created_at DESC
            """;
    //ojo repasar query
    // series where this student is enrolled in at least one lesson
    private static final String FIND_BY_STUDENT = """
                SELECT DISTINCT s.id, s.code, s.language, s.level, s.title, s.description, s.created_at
                FROM series as s
                JOIN lessons as l      ON l.series_id = s.id
                JOIN user_lessons ul ON ul.lesson_id = l.id
                WHERE ul.user_id = ?
                ORDER BY s.created_at DESC
            """;

    private static final String DELETE_BY_ID = """
                DELETE FROM series
                WHERE id = ?
            """;


    private Series mapSeries(ResultSet rs) throws SQLException {
        return Series.builder()
                .id(rs.getObject("id", UUID.class))
                .code(rs.getString("code"))
                .language(rs.getString("language"))
                .level(rs.getString("level"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .build();
    }

    @Override
    public List<Series> findAll() {
        List<Series> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapSeries(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll series error", e);
        }
        return list;
    }

    @Override
    public Optional<Series> findById(UUID id) {
        if (id == null) return Optional.empty();

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSeries(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById series error: " + id, e);
        }
    }

    @Override
    public Optional<Series> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();

        String normalized = code.trim().toLowerCase(Locale.ROOT);

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_CODE)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSeries(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByCode series error: " + code, e);
        }
    }

    @Override
    public UUID create(Series series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null.");
        }

        String code = series.getCode() != null ? series.getCode().trim() : "";
        String language = series.getLanguage() != null ? series.getLanguage().trim() : "";
        String level = series.getLevel() != null ? series.getLevel().trim() : "";
        String title = series.getTitle() != null ? series.getTitle().trim() : "";
        String description = series.getDescription();

        if (code.isBlank() || language.isBlank() || level.isBlank() || title.isBlank()) {
            throw new IllegalArgumentException("code, language, level and title are required.");
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setString(1, code);
            ps.setString(2, language);
            ps.setString(3, level);
            ps.setString(4, title);
            ps.setString(5, description);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("id", UUID.class);
                }
                throw new RuntimeException("INSERT into series did not return an id.");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new RuntimeException("Series code already exists: " + code, e);
            }
            throw new RuntimeException("create series error: " + code, e);
        }
    }

    @Override
    public List<Series> findByTeacherId(UUID teacherId) {
        List<Series> list = new ArrayList<>();
        if (teacherId == null) return list;

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_TEACHER)) {
            ps.setObject(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSeries(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByTeacherId series error: " + teacherId, e);
        }
        return list;
    }

    @Override
    public List<Series> findByStudentId(UUID studentId) {
        List<Series> list = new ArrayList<>();
        if (studentId == null) return list;

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STUDENT)) {
            ps.setObject(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSeries(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByStudentId series error: " + studentId, e);
        }
        return list;
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) return false;

        try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_ID)) {
            ps.setObject(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            // Si hay lessons asociados → constraint violation por FK
            throw new RuntimeException("Error deleting series with id: " + id, e);
        }
    }

}
