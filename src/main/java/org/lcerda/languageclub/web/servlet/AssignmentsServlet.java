package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.AssignmentAssigneeDao;
import org.lcerda.languageclub.dao.AssignmentDao;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.UserLessonDao;
import org.lcerda.languageclub.dao.jdbc.AssignmentAssigneeDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.AssignmentDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.LessonDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserLessonDaoJdbcImpl;
import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.AssignmentStatus;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AssignmentService;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.AssignmentServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AssignmentsServlet extends HttpServlet {

    // ===== helpers =====

    private AssignmentService buildAssignmentService(Connection conn) {
        AssignmentDao assignmentDao = new AssignmentDaoJdbcImpl(conn);
        LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
        AssignmentAssigneeDao assignmentAssigneeDao = new AssignmentAssigneeDaoJdbcImpl(conn);
        UserLessonDao userLessonDao = new UserLessonDaoJdbcImpl(conn);

        return new AssignmentServiceImpl(
                assignmentDao,
                lessonDao,
                userLessonDao,
                assignmentAssigneeDao
        );
    }

    private User requireUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Object u = session.getAttribute("currentUser");
        if (!(u instanceof User)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        return (User) u;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getRoles(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object r = (session != null) ? session.getAttribute("currentUserRoles") : null;
        return (r instanceof Set) ? (Set<String>) r : Set.of();
    }

    // ===== GET: lista de tareas por lección =====

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = requireUser(req, resp);
        if (currentUser == null) return;

        Set<String> roles = getRoles(req);
        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        String lessonIdStr = req.getParameter("lessonId");
        if (lessonIdStr == null || lessonIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lessonId");
            return;
        }

        UUID lessonId;
        try {
            lessonId = UUID.fromString(lessonIdStr);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lessonId");
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            AssignmentService assignmentService = buildAssignmentService(conn);

            List<Assignment> assignments =
                    assignmentService.getAssignmentsForLesson(lessonId, currentUser, roles);

            List<AssignmentStatus> statuses = assignmentService.getAllStatuses();

            req.setAttribute("lessonId", lessonId);
            req.setAttribute("assignments", assignments);
            req.setAttribute("assignmentStatuses", statuses);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isTeacher", isTeacher);

            req.getRequestDispatcher("/pages/assignments/list.jsp")
                    .forward(req, resp);

        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/pages/assignments/list.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error loading assignments.", e);
        }
    }

    // ===== POST: crear / cambiar estado =====

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        switch (action) {
            case "create" -> handleCreate(req, resp);
            case "status" -> handleChangeStatus(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Unknown action: " + action);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = requireUser(req, resp);
        if (currentUser == null) return;
        Set<String> roles = getRoles(req);

        String lessonIdStr = req.getParameter("lessonId");
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String dueAtStr = req.getParameter("dueAt");   // opcional

        req.setAttribute("formTitle", title);
        req.setAttribute("formDescription", description);
        req.setAttribute("formDueAt", dueAtStr);

        if (lessonIdStr == null || lessonIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lessonId");
            return;
        }

        UUID lessonId;
        try {
            lessonId = UUID.fromString(lessonIdStr);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lessonId");
            return;
        }

        OffsetDateTime dueAt = null;
        if (dueAtStr != null && !dueAtStr.isBlank()) {
            try {
                dueAt = OffsetDateTime.parse(dueAtStr);
            } catch (DateTimeParseException e) {
                req.setAttribute("error",
                        "Invalid date/time format. Use ISO, e.g. 2025-11-15T23:59:00Z");
                doGet(req, resp);
                return;
            }
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            AssignmentService assignmentService = buildAssignmentService(conn);

            assignmentService.createAssignmentForLesson(
                    lessonId,
                    currentUser,
                    roles,
                    title,
                    description,
                    dueAt
            );

            resp.sendRedirect(req.getContextPath()
                    + "/app/assignments?lessonId=" + lessonId);

        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error creating assignment.", e);
        }
    }

    private void handleChangeStatus(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = requireUser(req, resp);
        if (currentUser == null) return;
        Set<String> roles = getRoles(req);

        String assignmentIdStr = req.getParameter("assignmentId");
        String lessonIdStr = req.getParameter("lessonId");
        String statusIdStr = req.getParameter("statusId");

        if (assignmentIdStr == null || lessonIdStr == null || statusIdStr == null
                || assignmentIdStr.isBlank()
                || lessonIdStr.isBlank()
                || statusIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameters for status change.");
            return;
        }

        UUID assignmentId;
        UUID lessonId;
        short statusId;
        try {
            assignmentId = UUID.fromString(assignmentIdStr);
            lessonId = UUID.fromString(lessonIdStr);
            statusId = Short.parseShort(statusIdStr);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid assignmentId / lessonId / statusId");
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            AssignmentService assignmentService = buildAssignmentService(conn);
            assignmentService.changeStatus(assignmentId, statusId, currentUser, roles);

            resp.sendRedirect(req.getContextPath()
                    + "/app/assignments?lessonId=" + lessonId);

        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error updating assignment status.", e);
        }
    }
}
