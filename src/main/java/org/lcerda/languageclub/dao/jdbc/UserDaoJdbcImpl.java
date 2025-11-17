package org.lcerda.languageclub.dao.jdbc;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.DuplicateEmailException;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

@AllArgsConstructor
public class UserDaoJdbcImpl implements UserDao {

    private final Connection connection;

    private static final String FIND_ALL = """
                SELECT id, email, password_hash, full_name, is_active, created_at, updated_at
                FROM users
                ORDER BY created_at DESC
            """;

    private static final String FIND_BY_EMAIL = """
                SELECT id, email, password_hash, full_name, is_active, created_at, updated_at
                FROM users WHERE lower(email) = ?
            """;

    private static final String FIND_BY_ID = """
                SELECT id, email, password_hash, full_name, is_active, created_at, updated_at
                FROM users WHERE id = ?
            """;

    // PostgreSQL: usamos RETURNING id para recuperar el UUID generado por gen_random_uuid()
    private static final String CREATE = """
                INSERT INTO users (id, email, password_hash, full_name, is_active)
                VALUES (gen_random_uuid(), ?, ?, ?, true) RETURNING id
            """;

    private static final String UPDATE_ACTIVE = """
                UPDATE users
                SET is_active = ?
                WHERE id = ?
            """;

    private static final String DELETE_BY_ID = """
                DELETE FROM users
                WHERE id = ?
            """;

    private static final String FIND_ACTIVE_STUDENTS = """
                SELECT u.id, u.email, u.password_hash, u.full_name, u.is_active, u.created_at, u.updated_at
                FROM users as u
                INNER JOIN user_roles as ur ON ur.user_id = u.id
                INNER JOIN roles as r ON r.id = ur.role_id
                WHERE u.is_active = TRUE
                  AND r.code = 'STUDENT'
                ORDER BY u.full_name ASC
            """;

    private static final String FIND_ALL_BY_ROLE_CODE = """
                SELECT u.id, u.email, u.password_hash, u.full_name,
                       u.is_active, u.created_at, u.updated_at
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.id
                JOIN roles r       ON r.id = ur.role_id
                WHERE r.code = ?
                  AND u.is_active = TRUE
                ORDER BY u.full_name
            """;


    //+++++++helpers++++

    private static Optional<String> normalizeEmail(String email) {
        if (email == null) return Optional.empty();
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (e.isEmpty()) return Optional.empty();
        // Validación mínima: un solo @ y dominio con punto
        int at = e.indexOf('@');
        if (at <= 0 || at != e.lastIndexOf('@')) return Optional.empty();
        String domain = e.substring(at + 1);
        if (domain.isBlank() || !domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return Optional.empty();
        }
        return Optional.of(e);
    }


    private static User getU(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        //getBoolean devuelve false si es NULL; si la columna puede ser NULL usa Boolean
        boolean isActive = rs.getBoolean("is_active");
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);

        return User.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .isActive(isActive)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }


    @Override
    public Optional<User> findByEmail(String email) {
        Optional<String> normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) return Optional.empty();

        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_EMAIL)) {
            ps.setString(1, normalizedEmail.get());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(getU(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByEmail error: " + email, e);
        }

    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(getU(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById error: " + id, e);
        }
    }

    @Override
    public UUID create(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");

        // chequeos de email hash y nombre
        String email = normalizeEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email is invalid or empty."));
        String hash = user.getPasswordHash();
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";

        if (hash == null || hash.isBlank()) throw new IllegalArgumentException("password_hash is required.");
        if (fullName.isBlank()) throw new IllegalArgumentException("full_name is required.");

        try (PreparedStatement ps = connection.prepareStatement(CREATE)) {
            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, fullName);
            // OJO: como hay RETURNING, se usa executeQuery() (no executeUpdate)
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //en la bd es UUID la id del usuior por eso el jdbc lo recoje como UUID
                    UUID idUser = rs.getObject("id", UUID.class);
                    return idUser;
                }
                throw new RuntimeException("INSERT into users did not return an id.");
            }
        } catch (SQLException e) {
//            23503 == foreign_key_violation
//
//            23502 == not_null_violation
//
//            23514 == check_violation
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateEmailException(e.getMessage());
            }
            throw new RuntimeException("create user error: " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(getU(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll error: " + users, e);
        }
        return users;
    }

    @Override
    public void updateActive(UUID id, boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_ACTIVE)) {
            ps.setBoolean(1, active);
            ps.setObject(2, id);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("No user found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user active flag: " + id, e);
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
            // OJO: FK (teacher en lessons, etc.) puede lanzar constraint violation
            throw new RuntimeException("Error deleting user with id: " + id, e);
        }
    }

    @Override
    public List<User> findActiveStudents() {
        List<User> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(FIND_ACTIVE_STUDENTS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(getU(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findActiveStudents error", e);
        }
        return result;
    }

    @Override
    public List<User> findAllByRoleCode(String roleCode) {
        List<User> users = new ArrayList<>();
        if (roleCode == null || roleCode.isBlank()) {
            return users;
        }

        try (PreparedStatement ps = connection.prepareStatement(FIND_ALL_BY_ROLE_CODE)) {
            ps.setString(1, roleCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(getU(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAllByRoleCode error: " + roleCode, e);
        }

        return users;
    }


}
