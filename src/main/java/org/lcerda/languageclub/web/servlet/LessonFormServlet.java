package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.RoleDao;
import org.lcerda.languageclub.dao.SeriesDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.jdbc.LessonDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.RoleDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.SeriesDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserDaoJdbcImpl;
import org.lcerda.languageclub.model.Series;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.LessonService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.LessonServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LessonFormServlet extends HttpServlet {

    // GET: mostrar formulario
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
        @SuppressWarnings("unchecked")
        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        if (!(isAdmin || isTeacher)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admins and teachers can create lessons.");
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            SeriesDao seriesDao = new SeriesDaoJdbcImpl(conn);
            UserDao userDao = new UserDaoJdbcImpl(conn);
            RoleDao roleDao = new RoleDaoJdbcImpl(conn);

            // Todas las series para el select
            List<Series> seriesList = seriesDao.findAll();

            // Lista de profesores (usuarios con rol TEACHER)
            List<User> teachersList = new ArrayList<>();
            for (User user : userDao.findAll()) {
                Set<String> userRoles = roleDao.findRoleCodesByUserId(user.getId());
                if (userRoles.contains("TEACHER")) {
                    teachersList.add(user);
                }
            }

            req.setAttribute("seriesList", seriesList);
            req.setAttribute("teachersList", teachersList);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isTeacher", isTeacher);

            if (isTeacher) {
                // El teacher solo puede crearse a sí mismo como profesor
                req.setAttribute("teacherId", currentUser.getId().toString());
            }

            req.getRequestDispatcher("/pages/lessons/form.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Error loading data for lesson form.", e);
        }
    }

    // POST: procesar creación de lección
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
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
        @SuppressWarnings("unchecked")
        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        if (!(isAdmin || isTeacher)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admins and teachers can create lessons.");
            return;
        }

        // Parámetros del formulario
        String seriesIdStr = req.getParameter("seriesId");
        String teacherIdStr = req.getParameter("teacherId");
        String topic = req.getParameter("topic");
        String startsAtStr = req.getParameter("startsAt");
        String endsAtStr = req.getParameter("endsAt");
        String room = req.getParameter("room");
        String notes = req.getParameter("notes");

        // Guardar valores para repintar en caso de error
        req.setAttribute("formSeriesId", seriesIdStr);
        req.setAttribute("formTeacherId", teacherIdStr);
        req.setAttribute("formTopic", topic);
        req.setAttribute("formStartsAt", startsAtStr);
        req.setAttribute("formEndsAt", endsAtStr);
        req.setAttribute("formRoom", room);
        req.setAttribute("formNotes", notes);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            SeriesDao seriesDao = new SeriesDaoJdbcImpl(conn);
            LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
            LessonService lessonService = new LessonServiceImpl(lessonDao);

            // Necesitamos seriesList y teachersList para reusar el form si falla
            UserDao userDao = new UserDaoJdbcImpl(conn);
            RoleDao roleDao = new RoleDaoJdbcImpl(conn);

            List<Series> seriesList = seriesDao.findAll();
            List<User> teachersList = new ArrayList<>();
            for (User user : userDao.findAll()) {
                Set<String> userRoles = roleDao.findRoleCodesByUserId(user.getId());
                if (userRoles.contains("TEACHER")) {
                    teachersList.add(user);
                }
            }

            req.setAttribute("seriesList", seriesList);
            req.setAttribute("teachersList", teachersList);
            req.setAttribute("isAdmin", isAdmin);
            req.setAttribute("isTeacher", isTeacher);

            if (isTeacher) {
                // ignoramos lo que venga del form, siempre el teacher actual
                teacherIdStr = currentUser.getId().toString();
                req.setAttribute("teacherId", teacherIdStr);
            }

            if (seriesIdStr == null || seriesIdStr.isBlank()) {
                throw new ValidationException("Series is required.");
            }
            if (teacherIdStr == null || teacherIdStr.isBlank()) {
                throw new ValidationException("Teacher is required.");
            }

            UUID seriesId;
            UUID teacherId;
            try {
                seriesId = UUID.fromString(seriesIdStr);
                teacherId = UUID.fromString(teacherIdStr);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid UUID for series or teacher.");
            }

            OffsetDateTime startsAt;
            OffsetDateTime endsAt;
            try {
                // aquí asumimos formato ISO-8601, p.ej. 2025-11-14T10:00:00Z
                startsAt = OffsetDateTime.parse(startsAtStr);
                endsAt = OffsetDateTime.parse(endsAtStr);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Invalid date/time format. Use ISO-8601, e.g. 2025-11-14T10:00:00Z");
            }

            lessonService.createLessonForUser(
                    currentUser,
                    roles,
                    seriesId,
                    teacherId,
                    topic,
                    startsAt,
                    endsAt,
                    room,
                    notes
            );

            // Éxito
            resp.sendRedirect(req.getContextPath() + "/app/lessons");

        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            // volvemos a pintar el formulario con el error
            req.getRequestDispatcher("/pages/lessons/form.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error creating lesson.", e);
        }
    }
}
