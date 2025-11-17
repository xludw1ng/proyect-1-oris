package org.lcerda.languageclub.config;

import jakarta.servlet.ServletContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceProvider {
    public static DataSource getDataSource(ServletContext ctx) {
        Object o = ctx.getAttribute("dataSource");
        if (o == null) throw new IllegalStateException("DataSource 'ds' isn't in ServletContext.");
        return (DataSource) o;
    }

    public static Connection getConnection(ServletContext ctx) throws SQLException {
        DataSource ds = getDataSource(ctx);

        Connection conn = ds.getConnection();

        return conn;
    }
}
