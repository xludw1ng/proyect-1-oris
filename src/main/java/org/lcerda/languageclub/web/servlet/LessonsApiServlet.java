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
import org.lcerda.languageclub.service.LessonService;
import org.lcerda.languageclub.service.impl.LessonServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LessonsApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // --- Comprobamos sesión / login (igual que en tus otros servlets) ---
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Object u = session.getAttribute("currentUser");
        Object r = session.getAttribute("currentUserRoles");

        if (!(u instanceof User)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        User currentUser = (User) u;
        @SuppressWarnings("unchecked")
        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        // --- Filtro opcional por seriesId (igual que en LessonsListServlet) ---
        String seriesIdStr = req.getParameter("seriesId");
        UUID seriesFilterId = null;
        if (seriesIdStr != null && !seriesIdStr.isBlank()) {
            try {
                seriesFilterId = UUID.fromString(seriesIdStr);
            } catch (IllegalArgumentException ignored) {
                // si viene basura, simplemente ignoramos el filtro
            }
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            LessonDao lessonDao = new LessonDaoJdbcImpl(conn);
            LessonService lessonService = new LessonServiceImpl(lessonDao);

            // mismas reglas de visibilidad que en LessonsListServlet
            List<Lesson> lessons = lessonService.getLessonsForUser(currentUser, roles);

            if (seriesFilterId != null) {
                final UUID finalFilter = seriesFilterId;
                lessons = lessons.stream()
                        .filter(l -> finalFilter.equals(l.getSeriesId()))
                        .collect(Collectors.toList());
            }

            // --- Construimos JSON manualmente ---
            resp.setContentType("application/json;charset=UTF-8");

            String json = toJsonArray(lessons);
            try (PrintWriter out = resp.getWriter()) {
                out.write(json);
            }

        } catch (SQLException e) {
            throw new ServletException("Error loading lessons as JSON", e);
        }
    }

    private String toJsonArray(List<Lesson> lessons) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Lesson l : lessons) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("{");

            sb.append("\"id\":\"").append(l.getId()).append("\"");
            sb.append(",\"seriesId\":\"").append(l.getSeriesId()).append("\"");
            sb.append(",\"teacherId\":\"").append(l.getTeacherId()).append("\"");
            sb.append(",\"topic\":\"").append(escapeJson(l.getTopic())).append("\"");
            sb.append(",\"startsAt\":\"").append(l.getStartsAt()).append("\"");
            sb.append(",\"endsAt\":\"").append(l.getEndsAt()).append("\"");

            // room
            sb.append(",\"room\":");
            if (l.getRoom() == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escapeJson(l.getRoom())).append("\"");
            }

            // notes
            sb.append(",\"notes\":");
            if (l.getNotes() == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escapeJson(l.getNotes())).append("\"");
            }

            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
