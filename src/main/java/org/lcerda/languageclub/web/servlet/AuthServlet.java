package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.config.DataSourceProvider;
import org.lcerda.languageclub.dao.RoleDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.jdbc.RoleDaoJdbcImpl;
import org.lcerda.languageclub.dao.jdbc.UserDaoJdbcImpl;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.AuthService;
import org.lcerda.languageclub.service.ValidationException;
import org.lcerda.languageclub.service.impl.AuthServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;


public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath();

        switch (path) {
            case "/login" -> showLoginForm(req, resp);
            case "/logout" -> doLogout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/app/home");
            return;
        }

        //no se debe cachear la pagina
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        resp.setHeader("Pragma", "no-cache");                                   // HTTP 1.0
        resp.setDateHeader("Expires", 0);
        ///
        req.getRequestDispatcher("/pages/auth/login.jsp")
                .forward(req, resp);
    }

    private void doLogout(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/login".equals(path)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            //salva en caso de error
            req.setAttribute("email", email);

            try (Connection conn = DataSourceProvider.getConnection(getServletContext())) {
                UserDao userDao = new UserDaoJdbcImpl(conn);
                AuthService authService = new AuthServiceImpl(userDao);

                User user = authService.login(email, password);

                RoleDao roleDao = new RoleDaoJdbcImpl(conn);
                Set<String> roleCodes = roleDao.findRoleCodesByUserId(user.getId());

                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", user);
                session.setAttribute("currentUserRoles", roleCodes);
                session.setAttribute("isAdmin", roleCodes.contains("ADMIN"));
                session.setAttribute("isTeacher", roleCodes.contains("TEACHER"));

                resp.sendRedirect(req.getContextPath() + "/");
            } catch (ValidationException | AuthException e) {
                req.setAttribute("error", e.getMessage());
                req.getRequestDispatcher("/pages/auth/login.jsp")
                        .forward(req, resp);
            } catch (SQLException e) {
                throw new ServletException("Database error while trying to login.", e);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }



}
