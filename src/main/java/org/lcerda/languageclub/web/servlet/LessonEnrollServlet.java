package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.*;
import org.lcerda.languageclub.dao.jdbc.*;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.AuthService;
import org.lcerda.languageclub.service.UserLessonService;
import org.lcerda.languageclub.service.UserService;
import org.lcerda.languageclub.service.impl.AuthServiceImpl;
import org.lcerda.languageclub.service.impl.UserLessonServiceImpl;
import org.lcerda.languageclub.service.impl.UserServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class LessonEnrollServlet extends HttpServlet {


    private UserLessonService buildUserLessonService(Connection conn) {
        LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
        UserDao userDao = new UserDaoJdbcImpl(conn);
        UserLessonDao userLessonDao = new UserLessonDaoJdbcImpl(conn);
        return new UserLessonServiceImpl(lessonDao, userDao, userLessonDao);
    }

    private UserService buildUserService(Connection conn) {
        UserDao userDao = new UserDaoJdbcImpl(conn);
        RoleDao roleDao = new RoleDaoJdbcImpl(conn);
        UserRoleDao userRoleDao = new UserRoleDaoJdbcImpl(conn);
        AuthService authService = new AuthServiceImpl(userDao); // ya lo usas así en admin
        return new UserServiceImpl(userDao, roleDao, userRoleDao, authService);
    }

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

        String lessonIdStr = req.getParameter("lessonId");
        if (lessonIdStr == null || lessonIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lessonId");
            return;
        }

        UUID lessonId = UUID.fromString(lessonIdStr);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserLessonService userLessonService = buildUserLessonService(conn);

            // Aquí se comprueban permisos y se carga la lesson
            Lesson lesson = userLessonService.loadLessonIfAuthorized(currentUser, roles, lessonId);

            // Alumnos candidatos (students activos)
            List<User> students = userLessonService.getEligibleStudents();

            // IDs ya matriculados en esta lesson
            Set<UUID> enrolledIds = userLessonService.getEnrolledStudentIds(lessonId);

            req.setAttribute("lesson", lesson);
            req.setAttribute("students", students);
            req.setAttribute("enrolledIds", enrolledIds);

            req.getRequestDispatcher("/pages/lessons/enroll.jsp")
                    .forward(req, resp);

        } catch (AuthException e) {
            session.setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/app/lessons");
        } catch (SQLException e) {
            throw new ServletException("Error loading enroll form", e);
        }
    }



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

        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        String lessonIdStr = req.getParameter("lessonId");
        if (lessonIdStr == null || lessonIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lessonId");
            return;
        }
        UUID lessonId = UUID.fromString(lessonIdStr);

        String[] studentIdsArr = req.getParameterValues("studentIds");
        Set<UUID> studentIds = new HashSet<>();
        if (studentIdsArr != null) {
            for (String s : studentIdsArr) {
                studentIds.add(UUID.fromString(s));
            }
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            UserLessonService userLessonService = buildUserLessonService(conn);

            // se comprieba adentro del service
            userLessonService.saveEnrollments(currentUser, roles, lessonId, studentIds);

            resp.sendRedirect(req.getContextPath() + "/app/lessons");

        } catch (AuthException e) {
            session.setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/app/lessons");
        } catch (SQLException e) {
            throw new ServletException("Error saving enrollments", e);
        }
    }

}
