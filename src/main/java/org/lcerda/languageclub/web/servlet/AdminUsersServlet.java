package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.RoleDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.UserRoleDao;
import org.lcerda.languageclub.dao.jdbc.RoleDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserRoleDaoJdbcImpl;
import org.lcerda.languageclub.model.Role;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthService;
import org.lcerda.languageclub.service.DuplicateEmailException;
import org.lcerda.languageclub.service.UserService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.AuthServiceImpl;
import org.lcerda.languageclub.service.impl.UserServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class AdminUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String mode = req.getParameter("mode");

        // Si viene ?mode=create -> mostrar sólo el formulario de creación
        if ("create".equals(mode)) {
            req.getRequestDispatcher("/pages/admin/user-form.jsp")
                    .forward(req, resp);
            return;
        }

        // Si viene ?success=userCreated -> mensaje de éxito en la lista
        String success = req.getParameter("success");
        if ("userCreated".equals(success)) {
            req.setAttribute("success", "User created successfully.");
        }

        loadUsersAndForward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        switch (action) {
            case "create" -> handleCreateUser(req, resp);
            case "roles" -> handleUpdateRoles(req, resp);
            case "toggleActive" -> handleToggleActive(req, resp);
            case "delete" -> handleDeleteUser(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
        }
    }

    // ===== helpers para crear el servicio con la misma Connection =====

    private UserService buildUserService(Connection conn) {
        UserDao userDao = new UserDaoJdbcImpl(conn);
        RoleDao roleDao = new RoleDaoJdbcImpl(conn);
        UserRoleDao userRoleDao = new UserRoleDaoJdbcImpl(conn);
        AuthService authService = new AuthServiceImpl(userDao);

        return new UserServiceImpl(userDao, roleDao, userRoleDao, authService);
    }

    // ===== create user =====
    private void handleCreateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String fullName = req.getParameter("fullName");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        req.setAttribute("formEmail", email);
        req.setAttribute("formFullName", fullName);

        if (password == null || !password.equals(confirmPassword)) {
            req.setAttribute("error", "Passwords do not match.");
            // Volvemos al formulario de creación, no a la lista
            req.getRequestDispatcher("/pages/admin/user-form.jsp")
                    .forward(req, resp);
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserService userService = buildUserService(conn);

            userService.createUser(email, password, fullName);

            // Redirigimos a la lista con mensaje de éxito
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=userCreated");

        } catch (ValidationException | DuplicateEmailException e) {
            req.setAttribute("error", e.getMessage());
            // Volvemos al formulario con los datos rellenados
            req.getRequestDispatcher("/pages/admin/user-form.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error creating user from admin.", e);
        }
    }

    // ===== update roles =====
    private void handleUpdateRoles(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userIdStr = req.getParameter("userId");
        String[] rolesArr = req.getParameterValues("roles");

        if (userIdStr == null || userIdStr.isBlank()) {
            req.setAttribute("error", "Missing userId for roles update.");
            loadUsersAndForward(req, resp);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        Set<String> newRoles = new HashSet<>();
        if (rolesArr != null) {
            newRoles.addAll(Arrays.asList(rolesArr));
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserService userService = buildUserService(conn);
            userService.updateUserRoles(userId, newRoles);

            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (SQLException e) {
            throw new ServletException("Error updating roles for user.", e);
        }
    }

    // ===== toggle active =====
    private void handleToggleActive(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userIdStr = req.getParameter("userId");
        String currentActiveStr = req.getParameter("currentActive");

        if (userIdStr == null || userIdStr.isBlank()) {
            req.setAttribute("error", "Missing userId for toggle active.");
            loadUsersAndForward(req, resp);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        boolean currentActive = Boolean.parseBoolean(currentActiveStr);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserService userService = buildUserService(conn);
            userService.toggleUserActive(userId, currentActive);

            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (SQLException e) {
            throw new ServletException("Error updating user active flag.", e);
        }
    }

    // ===== delete user =====
    private void handleDeleteUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.isBlank()) {
            req.setAttribute("error", "Missing userId for delete.");
            loadUsersAndForward(req, resp);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserService userService = buildUserService(conn);

            boolean deleted = userService.deleteUser(userId);
            if (!deleted) {
                req.setAttribute("error", "User not found or could not be deleted.");
                loadUsersAndForward(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (SQLException e) {
            // Puede ser por restricciones FK
            req.setAttribute("error", "Error deleting user. Maybe this user is referenced by lessons or other data.");
            loadUsersAndForward(req, resp);
        }
    }

    // ===== helper: list users + roles =====
    private void loadUsersAndForward(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserService userService = buildUserService(conn);

            List<User> users = userService.findAllUsers();
            Map<UUID, Set<String>> userRolesMap = userService.buildUserRolesMap(users);
            List<Role> allRoles = userService.findAllRoles();

            req.setAttribute("users", users);
            req.setAttribute("userRolesMap", userRolesMap);
            req.setAttribute("allRoles", allRoles);

            req.getRequestDispatcher("/pages/admin/users.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Error loading users for admin.", e);
        }
    }
}
