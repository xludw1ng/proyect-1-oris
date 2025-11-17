package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.jdbc.LessonDaoJdbcImpl;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.LessonService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.LessonServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LessonsListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object u = session.getAttribute("currentUser");
        Object r = session.getAttribute("currentUserRoles");

        if (!(u instanceof User)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) u;

        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        // --- leer parámetro seriesId (puede venir de /series -> "View lessons") ---
        String seriesIdStr = req.getParameter("seriesId");
        UUID seriesFilterId = null;
        if (seriesIdStr != null && !seriesIdStr.isBlank()) {
            try {
                seriesFilterId = UUID.fromString(seriesIdStr);
            } catch (IllegalArgumentException ignored) {
                // si viene basura, simplemente no filtramos
            }
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
            LessonService lessonService = new LessonServiceImpl(lessonDao);

            // lecciones visibles para ese usuario
            List<Lesson> lessons = lessonService.getLessonsForUser(currentUser, roles);

            // si nos pasaron seriesId, filtramos
            final UUID seriesFilterFinal = seriesFilterId;
            if (seriesFilterId != null) {
                lessons = lessons.stream()
                        .filter(l -> seriesFilterFinal.equals(l.getSeriesId()))
                        .collect(Collectors.toList());
            }

            req.setAttribute("lessons", lessons);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isTeacher", isTeacher);

            req.getRequestDispatcher("/pages/lessons/list.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error loading lessons.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (!"delete".equals(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object u = session.getAttribute("currentUser");
        Object r = session.getAttribute("currentUserRoles");

        if (!(u instanceof User)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) u;

        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        String lessonIdStr = req.getParameter("lessonId");
        if (lessonIdStr == null || lessonIdStr.isBlank()) {
            req.setAttribute("error", "Missing lesson id.");
            doGet(req, resp);
            return;
        }

        UUID lessonId = UUID.fromString(lessonIdStr);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
            LessonService lessonService = new LessonServiceImpl(lessonDao);

            lessonService.deleteLessonForUser(lessonId, currentUser, roles);

            resp.sendRedirect(req.getContextPath() + "/app/lessons");
        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error deleting lesson.", e);
        }
    }
}
