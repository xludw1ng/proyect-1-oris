<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard - Language Club</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous"/>

    <!-- CSS propio -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/app.css">
</head>
<body class="bg-light">

<jsp:include page="/pages/layout/header.jsp"/>

<main class="container main-content">

    <div class="mb-4">
        <h1 class="lc-dashboard-title">Dashboard</h1>
        <p class="lc-dashboard-subtitle">
            Welcome to the Language Club management system.
        </p>
    </div>

    <c:if test="${not empty sessionScope.currentUser}">
        <div class="alert alert-light border">
            <strong>${sessionScope.currentUser.fullName}</strong><br/>
            <span class="text-muted">${sessionScope.currentUser.email}</span>
        </div>
    </c:if>

    <div class="row g-3 mt-3">
        <!-- Courses -->
        <div class="col-md-4">
            <div class="card lc-card">
                <div class="card-body">
                    <h5 class="card-title">Courses (Series)</h5>
                    <p class="card-text">
                        View the list of courses and create new ones (admin only).
                    </p>
                    <a href="${pageContext.request.contextPath}/series"
                       class="btn btn-sm btn-primary">
                        Go to courses
                    </a>
                </div>
            </div>
        </div>


        <!-- Admin users (solo admin) -->
        <c:if test="${sessionScope.isAdmin}">
            <div class="col-md-4">
                <div class="card lc-card">
                    <div class="card-body">
                        <h5 class="card-title">Admin panel</h5>
                        <p class="card-text">
                            Manage users and roles.
                        </p>
                        <a href="${pageContext.request.contextPath}/admin/users"
                           class="btn btn-sm btn-danger">
                            Open admin users
                        </a>
                    </div>
                </div>
            </div>
        </c:if>
    </div>

</main>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>

<!-- JS propio -->
<script src="${pageContext.request.contextPath}/resources/js/app.js"></script>
</body>
</html>
