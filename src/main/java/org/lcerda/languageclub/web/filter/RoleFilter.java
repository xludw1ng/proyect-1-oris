package org.lcerda.languageclub.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.model.User;

import java.io.IOException;
import java.util.Set;

public class RoleFilter implements Filter {
    private static final String REQUIRED_ROLE = "ADMIN";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse res =  (HttpServletResponse) servletResponse;
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        HttpSession session = req.getSession(false);
        User currentUser = null;
        Set<String> roles = null;

        if (session!= null) {
            Object u = session.getAttribute("currentUser");
            if (u instanceof User) {
                currentUser = (User) u;
            }
            Object r = session.getAttribute("currentUserRoles");
            if (r instanceof Set) {
                roles = (Set<String>) r;
            }
        }

        if (currentUser == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (roles == null || !roles.contains(REQUIRED_ROLE)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this resource");
            return;
        }

        chain.doFilter(req,res);
    }
}
