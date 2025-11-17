package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.AssignmentAssigneeDao;
import org.lcerda.languageclub.dao.AssignmentDao;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.SubmissionsDao;
import org.lcerda.languageclub.dao.UserLessonDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.jdbc.AssignmentAssigneeDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.AssignmentDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.LessonDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.SubmissionsDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserLessonDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserDaoJdbcImpl;
import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.Submissions;
import org.lcerda.languageclub.model.SubmissionsStatus;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AssignmentService;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.SubmissionService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.AssignmentServiceImpl;
import org.lcerda.languageclub.service.impl.SubmissionServiceImpl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@MultipartConfig
public class SubmissionsServlet extends HttpServlet {

    // ===== helpers para construir services/daos =====

    private SubmissionService buildSubmissionService(Connection conn) {
        SubmissionsDao submissionsDao = new SubmissionsDaoJdbcImpl(conn);
        AssignmentDao assignmentDao = new AssignmentDaoJdbcImpl(conn);
        LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
        AssignmentAssigneeDao assignmentAssigneeDao = new AssignmentAssigneeDaoJdbcImpl(conn);

        return new SubmissionServiceImpl(
                submissionsDao,
                assignmentDao,
                lessonDao,
                assignmentAssigneeDao
        );
    }

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

    private UserDao buildUserDao(Connection conn) {
        return new UserDaoJdbcImpl(conn);
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

    private Set<String> getRoles(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object r = (session != null) ? session.getAttribute("currentUserRoles") : null;
        return (r instanceof Set) ? (Set<String>) r : Set.of();
    }

    // ===== GET =====

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = requireUser(req, resp);
        if (currentUser == null) return;

        Set<String> roles = getRoles(req);
        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        String assignmentIdStr = req.getParameter("assignmentId");
        if (assignmentIdStr == null || assignmentIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing assignmentId");
            return;
        }

        UUID assignmentId;
        try {
            assignmentId = UUID.fromString(assignmentIdStr);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid assignmentId");
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            SubmissionService submissionService = buildSubmissionService(conn);
            AssignmentService assignmentService = buildAssignmentService(conn);
            UserDao userDao = buildUserDao(conn);

            // ---- aquí puede saltar ValidationException si:
            //  - tarea no existe
            //  - alumno no tiene acceso
            //  - tarea no está publicada / está cerrada / deadline pasada (según tu service)
            Assignment assignment = assignmentService.getAssignmentForUser(
                    assignmentId, currentUser, roles);

            req.setAttribute("assignment", assignment);

            if (isAdmin || isTeacher) {
                // ===== vista PROFESOR: lista de entregas =====
                List<Submissions> submissions =
                        submissionService.getSubmissionsForAssignment(
                                assignmentId, currentUser, roles);

                // mapa userId -> User (para mostrar nombre completo)
                Map<UUID, User> studentsById = new HashMap<>();
                for (Submissions s : submissions) {
                    UUID uid = s.getUserId();
                    if (!studentsById.containsKey(uid)) {
                        userDao.findById(uid).ifPresent(u -> studentsById.put(uid, u));
                    }
                }

                // catálogo de estados
                List<SubmissionsStatus> statuses = submissionService.getAllStatuses();
                Map<Short, String> statusCodeById = new HashMap<>();
                for (SubmissionsStatus st : statuses) {
                    statusCodeById.put(st.getId(), st.getCode());
                }

                req.setAttribute("submissions", submissions);
                req.setAttribute("studentsById", studentsById);
                req.setAttribute("submissionStatuses", statuses);
                req.setAttribute("statusCodeById", statusCodeById);
                req.setAttribute("isAdmin", isAdmin);
                req.setAttribute("isTeacher", isTeacher);

                req.getRequestDispatcher("/pages/submissions/list.jsp")
                        .forward(req, resp);

            } else {
                // ===== vista ALUMNO: su propia entrega =====
                Submissions mySubmission =
                        submissionService.getSubmissionForStudent(
                                assignmentId, currentUser);

                req.setAttribute("submission", mySubmission);

                req.getRequestDispatcher("/pages/submissions/form.jsp")
                        .forward(req, resp);
            }

        } catch (ValidationException e) {
            // Assignment no disponible (DRAFT, CLOSED, deadline, sin acceso, etc.)
            req.setAttribute("error", e.getMessage());

            //  NUEVO: intentar cargar la submission del alumno para mostrar nota/estado
            try (Connection conn2 = DataSourceProvider.getConnection(getServletContext())) {
                SubmissionService submissionService2 = buildSubmissionService(conn2);

                try {
                    Submissions mySubmission =
                            submissionService2.getSubmissionForStudent(
                                    assignmentId, currentUser);
                    req.setAttribute("submission", mySubmission);
                } catch (ValidationException | AuthException ex) {
                    // si tampoco podemos obtener la entrega, la ignoramos silenciosamente
                }

            } catch (SQLException ex) {
                throw new ServletException("Error loading submission for unavailable assignment.", ex);
            }

            req.getRequestDispatcher("/pages/submissions/unavailable.jsp")
                    .forward(req, resp);

        }  catch (AuthException e) {
            // permisos muy raros -> de momento a 500
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/pages/error/500.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Error loading submissions.", e);
        }
    }


    // ===== POST: envío alumno o guardado de nota =====

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = requireUser(req, resp);
        if (currentUser == null) return;

        Set<String> roles = getRoles(req);
        String ctx = req.getContextPath();

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "submit";   // por defecto: envío de alumno
        }

        String assignmentIdStr = req.getParameter("assignmentId");
        if (assignmentIdStr == null || assignmentIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing assignmentId");
            return;
        }

        UUID assignmentId;
        try {
            assignmentId = UUID.fromString(assignmentIdStr);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid assignmentId");
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            SubmissionService submissionService = buildSubmissionService(conn);

            // ===== PROFE: guardar nota =====
            if ("grade".equals(action)) {

                String studentIdStr = req.getParameter("studentId");
                if (studentIdStr == null || studentIdStr.isBlank()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing studentId");
                    return;
                }

                UUID studentId;
                try {
                    studentId = UUID.fromString(studentIdStr);
                } catch (IllegalArgumentException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid studentId");
                    return;
                }

                String gradeStr = req.getParameter("grade");
                Integer grade = null;
                if (gradeStr != null && !gradeStr.isBlank()) {
                    try {
                        grade = Integer.valueOf(gradeStr);
                    } catch (NumberFormatException ex) {
                        HttpSession session = req.getSession();
                        session.setAttribute("error",
                                "Grade must be a number between 0 and 100.");
                        resp.sendRedirect(ctx + "/app/submissions?assignmentId=" + assignmentId);
                        return;
                    }
                }

                submissionService.gradeSubmission(
                        assignmentId,
                        studentId,
                        currentUser,
                        roles,
                        grade
                );

                resp.sendRedirect(ctx + "/app/submissions?assignmentId=" + assignmentId);
                return;
            }

            // ===== ALUMNO: enviar/actualizar entrega =====

            String textAnswer = req.getParameter("textAnswer");

            // archivo (opcional)
            Part attachmentPart = null;
            try {
                attachmentPart = req.getPart("attachment");
            } catch (IllegalStateException ignore) {
                // tamaño excedido, etc.
            }

            String attachmentPath = null;
            if (attachmentPart != null && attachmentPart.getSize() > 0) {
                // 1) Primero intentamos con UPLOAD_DIR del web.xml
                String uploadRoot = getServletContext().getInitParameter("UPLOAD_DIR");

                // 2) Si no está definido, usamos /uploads dentro del proyecto
                if (uploadRoot == null || uploadRoot.isBlank()) {
                    uploadRoot = getServletContext().getRealPath("/uploads");
                }

                Path baseDir = Paths.get(uploadRoot, "submissions");
                Files.createDirectories(baseDir);

                String originalName = Path.of(attachmentPart.getSubmittedFileName())
                        .getFileName()
                        .toString();

                String storedName = assignmentId + "_" + currentUser.getId()
                        + "_" + System.currentTimeMillis() + "_" + originalName;

                Path target = baseDir.resolve(storedName);

                attachmentPart.write(target.toString());

                // guardamos ruta **relativa** respecto a UPLOAD_DIR o /uploads
                attachmentPath = "submissions/" + storedName;
            }

            submissionService.submitAssignment(
                    assignmentId,
                    currentUser,
                    roles,
                    textAnswer,
                    attachmentPath
            );

            // PRG
            resp.sendRedirect(ctx + "/app/submissions?assignmentId=" + assignmentId);

        } catch (ValidationException | AuthException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error processing submission.", e);
        }
    }

}
