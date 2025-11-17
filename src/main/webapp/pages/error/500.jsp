<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Server error</title>

    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container-fluid px-2 text-center">
    <div class="min-vh-100 d-flex flex-column justify-content-center align-items-center">
        <!-- Parte "bonita" principal -->
        <h1 class="display-1 fw-bold text-danger">500</h1>
        <p class="fs-1 fw-semibold text-dark">Internal Server Error</p>
        <p class="fs-5 text-secondary mb-4">
            We apologize for the inconvenience. Please try again later.
        </p>

        <!-- Info técnica (lo que tú tenías antes) -->
        <div class="mt-4 text-start" style="max-width: 700px;">
            <h2 class="h5 mb-3">Technical details (for debugging)</h2>
            <p><strong>Message:</strong> ${requestScope['jakarta.servlet.error.message']}</p>
            <p><strong>Exception:</strong> ${pageContext.exception}</p>
        </div>
    </div>
</div>

</body>
</html>
