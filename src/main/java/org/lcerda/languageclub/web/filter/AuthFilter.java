package org.lcerda.languageclub.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lcerda.languageclub.model.User;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        HttpSession session = req.getSession(false);

        User currentUser = null;

        if (session !=null){
            Object o =  session.getAttribute("currentUser");
            if (o instanceof User){
                currentUser = (User) o;
            }
        }

        if (currentUser == null){
            res.sendRedirect(req.getContextPath() +"/login");
            return;
        }

        //googleado porque mostraba cache con la pagina anterior
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        res.setHeader("Pragma", "no-cache");                                   // HTTP 1.0
        res.setDateHeader("Expires", 0);

        chain.doFilter(req, res);
    }
}
