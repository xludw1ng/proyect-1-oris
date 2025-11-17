package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.SeriesDao;
import org.lcerda.languageclub.dao.jdbc.LessonDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.SeriesDaoJdbcImpl;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.Series;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.LessonService;
import org.lcerda.languageclub.service.SeriesService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.LessonServiceImpl;
import org.lcerda.languageclub.service.impl.SeriesServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SeriesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object u =session.getAttribute("currentUser");
        Object r = session.getAttribute("currentUserRoles");

        if (!(u instanceof User)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currectUser = (User) u;

        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        boolean isAdmin = roles.contains("ADMIN");

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            SeriesDao seriesDao = new SeriesDaoJdbcImpl(conn);
            SeriesService seriesService = new SeriesServiceImpl(seriesDao);

            List<Series> seriesList = seriesService.getSeriesForUser(currectUser, roles);
            req.setAttribute("seriesList", seriesList);
            req.setAttribute("userRoles", roles);
            req.setAttribute("isAdmin", isAdmin);

            req.getRequestDispatcher("/pages/series/list.jsp").forward(req, resp);
        }catch (SQLException e) {
            throw new ServletException("Error series lesson list",e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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


        Set<String> roles = (r instanceof Set) ? (Set<String>) r : Set.of();

        if (!roles.contains("ADMIN")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only admins can manage courses.");
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        switch (action) {
            case "create" -> handleCreateSeries(req, resp);
            case "delete" -> handleDeleteSeries(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
        }
    }


    private void handleCreateSeries(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = req.getParameter("code");
        String language = req.getParameter("language");
        String level = req.getParameter("level");
        String title = req.getParameter("title");
        String description = req.getParameter("description");

        req.setAttribute("formCode", code);
        req.setAttribute("formLanguage", language);
        req.setAttribute("formLevel", level);
        req.setAttribute("formTitle", title);
        req.setAttribute("formDescription", description);

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {

            SeriesDao seriesDao = new SeriesDaoJdbcImpl(conn);
            SeriesService seriesService = new SeriesServiceImpl(seriesDao);

            seriesService.createSeries(code, language, level, title, description);

            resp.sendRedirect(req.getContextPath() + "/series");
        } catch (ValidationException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error creating course(series)", e);
        }
    }

    private void handleDeleteSeries(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String seriesIdStr = req.getParameter("seriesId");
        if (seriesIdStr == null || seriesIdStr.isBlank()) {
            req.setAttribute("error", "Missing seriesId for delete.");
            doGet(req, resp);
            return;
        }

        try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
            SeriesDao seriesDao = new SeriesDaoJdbcImpl(conn);

            boolean deleted = seriesDao.deleteById(UUID.fromString(seriesIdStr));
            if (!deleted) {
                req.setAttribute("error", "Course not found or could not be deleted.");
                doGet(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/series");
        } catch (RuntimeException e) {
            // Probable FK: tiene lessons asociadas
            req.setAttribute("error", "Cannot delete course with existing lessons.");
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error deleting course(series)", e);
        }
    }

}
