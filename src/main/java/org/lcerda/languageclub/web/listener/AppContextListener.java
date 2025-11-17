package org.lcerda.languageclub.web.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        try{
            //JDNI
            Context init = new InitialContext();
            Context env  = (Context) init.lookup("java:comp/env");
            DataSource dataSource = (DataSource) env.lookup("jdbc/LCDB");


            //test de pool
            try(Connection conection = dataSource.getConnection()){
                context.log("DB pool OK (jdbc/LCDB). autoCommit=" + conection.getAutoCommit());
            }

            //DataSource para toda la app
            context.setAttribute("dataSource", dataSource);
            context.log("DataSource register as/like 'dataSource'");
        }catch(NamingException | SQLException e){
            context.log("DataSource register error: " + e);
            throw new RuntimeException("Error while registering data source", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        //cerrar el pool
        context.removeAttribute("dataSource");
        context.log("DataSource 'dataSource' removed successfully.");
    }
}
