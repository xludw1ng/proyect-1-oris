package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.*;
import org.lcerda.languageclub.dao.jdbc.*;
import org.lcerda.languageclub.model.*;
import org.lcerda.languageclub.service.*;
import org.lcerda.languageclub.service.impl.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.*;

public class AttendanceServlet extends HttpServlet {

    private AttendanceService buildAttendanceService(Connection conn) {
        AttendanceDao attendanceDao = new AttendanceDaoJdbcImpl(conn);
        LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
        return new AttendanceServiceImpl(attendanceDao, lessonDao);
    }

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
        AuthService authService = new AuthServiceImpl(userDao);
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

        String lessonIdString = req.getParameter("lessonId");
        if (lessonIdString == null || lessonIdString.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lessonId");
            return;
        }

        UUID lessonId = UUID.fromString(lessonIdString);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            AttendanceService attendanceService = buildAttendanceService(conn);
            UserLessonService userLessonService = buildUserLessonService(conn);
            UserService userService = buildUserService(conn);

            // 1) Comprueba permisos y devuelve la lesson
            Lesson lesson = attendanceService.loadLessonIfAuthorized(lessonId, currentUser, roles);

            // 2) Asistencias ya guardadas
            List<Attendance> attendanceList =
                    attendanceService.getAttendanceForLesson(lessonId, currentUser, roles);

            // 3) IDs de estudiantes matriculados en esta lesson
            Set<UUID> enrolledIds = userLessonService.getEnrolledStudentIds(lessonId);

            // 4) Lista de usuarios y roles, para filtrar solo STUDENT activos
            List<User> allUsers = userService.findAllUsers();
            Map<UUID, Set<String>> userRolesMap = userService.buildUserRolesMap(allUsers);

            List<User> students = new ArrayList<>();
            for (User usr : allUsers) {
                Set<String> roleCodes = userRolesMap.getOrDefault(usr.getId(), Set.of());
                if (usr.isActive()
                        && enrolledIds.contains(usr.getId())
                        && roleCodes.contains("STUDENT")) {
                    students.add(usr);
                }
            }

            // 5) Mapear Attendance por userId para la vista
            Map<UUID, Attendance> attendanceByUserId = new HashMap<>();
            for (Attendance attendance : attendanceList) {
                attendanceByUserId.put(attendance.getUserId(), attendance);
            }

            // 6) Estados disponibles
            List<AttendanceStatus> statuses = attendanceService.getAllStatuses();

            req.setAttribute("lesson", lesson);
            req.setAttribute("students", students);
            req.setAttribute("attendanceByUserId", attendanceByUserId);
            req.setAttribute("statuses", statuses);

            req.getRequestDispatcher("/pages/attendance/mark.jsp")
                    .forward(req, resp);

        } catch (ValidationException | AuthException e) {
            session.setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/app/lessons");
        } catch (SQLException e) {
            throw new ServletException("Error loading attendance form.", e);
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

        // Arrays del formulario
        String[] studentIdsArr = req.getParameterValues("studentId");
        String[] statusIdsArr  = req.getParameterValues("statusId");
        String[] commentsArr   = req.getParameterValues("comment");

        if (studentIdsArr == null || studentIdsArr.length == 0) {
            resp.sendRedirect(req.getContextPath() + "/app/attendance?lessonId=" + lessonId);
            return;
        }

        // Construimos los mapas userId -> statusId / comment
        Map<UUID, Short> statusByUserId = new HashMap<>();
        Map<UUID, String> commentByUserId = new HashMap<>();

        for (int i = 0; i < studentIdsArr.length; i++) {
            UUID studentId = UUID.fromString(studentIdsArr[i]);

            String statusStr = (statusIdsArr != null && i < statusIdsArr.length) ? statusIdsArr[i] : null;

            String comment = (commentsArr != null && i < commentsArr.length) ? commentsArr[i] : null;

            if (statusStr != null && !statusStr.isBlank()) {
                short statusId = Short.parseShort(statusStr);
                statusByUserId.put(studentId, statusId);
            }
            if (comment != null && !comment.isBlank()) {
                commentByUserId.put(studentId, comment.trim());
            }
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            AttendanceService attendanceService = buildAttendanceService(conn);

            attendanceService.saveAttendanceForLesson(lessonId, statusByUserId, commentByUserId, currentUser, roles);

            resp.sendRedirect(req.getContextPath() + "/app/attendance?lessonId=" + lessonId);

        } catch (ValidationException | AuthException e) {
            session.setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/app/lessons");
        } catch (SQLException e) {
            throw new ServletException("Error saving attendance.", e);
        }
    }


}
