package org.lcerda.languageclub.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lcerda.languageclub.config.DataSourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // RUTA relativa guardada en BD, tipo: "submissions/....pdf"
        String relPath = req.getParameter("path");
        if (relPath == null || relPath.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path");
            return;
        }

        // pequeña protección contra ../
        relPath = relPath.replace("\\", "/");
        if (relPath.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path");
            return;
        }

        // 1) base de uploads
        String uploadRoot = getServletContext().getInitParameter("UPLOAD_DIR");
        if (uploadRoot == null || uploadRoot.isBlank()) {
            uploadRoot = getServletContext().getRealPath("/uploads");
        }

        Path baseDir = Paths.get(uploadRoot).normalize();
        Path filePath = baseDir.resolve(relPath).normalize();

        // no permitir salir de la carpeta base
        if (!filePath.startsWith(baseDir)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path");
            return;
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        // Tipo de contenido (mime)
        String mime = Files.probeContentType(filePath);
        if (mime == null) {
            mime = "application/octet-stream";
        }
        resp.setContentType(mime);

        // nombre de archivo
        String fileName = filePath.getFileName().toString();
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

        resp.setContentLengthLong(Files.size(filePath));

        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
