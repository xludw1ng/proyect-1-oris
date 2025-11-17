<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-12
  Time: 14:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Language Club – Welcome</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous"/>

    <!-- CSS propio -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/app.css">
</head>
<body class="bg-light">

<jsp:include page="/pages/layout/header.jsp"/>

<div class="container main-content">
    <div class="row justify-content-center">
        <div class="col-lg-8">

            <!-- Tarjeta principal -->
            <div class="card lc-card border-0">
                <div class="card-body p-4 p-md-5">

                    <h1 class="lc-dashboard-title mb-2">
                        Language Club Manager
                    </h1>
                    <p class="lc-dashboard-subtitle mb-4">
                        Small information system to manage language courses, lessons,
                        attendance and assignments for our club.
                    </p>

                    <!-- Mensaje según si hay usuario en sesión o no -->
                    <c:choose>
                        <c:when test="${not empty sessionScope.currentUser}">
                            <p class="mb-4">
                                Welcome,
                                <strong>${sessionScope.currentUser.fullName}</strong>!<br/>
                                Use the dashboard to manage your courses and activities.
                            </p>

                            <a href="${pageContext.request.contextPath}/app/home"
                               class="btn btn-primary me-2">
                                Go to dashboard
                            </a>

                            <a href="${pageContext.request.contextPath}/logout"
                               class="btn btn-outline-secondary">
                                Logout
                            </a>
                        </c:when>
                    </c:choose>

                    <hr class="my-4"/>

                    <!-- Pequeño resumen de funcionalidades -->
                    <div class="row g-3">
                        <div class="col-md-4">
                            <h6 class="fw-semibold mb-1">Courses &amp; series</h6>
                            <p class="text-muted small mb-0">
                                Create language series (ES, PT, EN…), levels (A1, A2…)
                                and group students by course.
                            </p>
                        </div>
                        <div class="col-md-4">
                            <h6 class="fw-semibold mb-1">Lessons &amp; attendance</h6>
                            <p class="text-muted small mb-0">
                                Plan lessons with teacher, room and time. Mark attendance
                                and track who was present, late or absent.
                            </p>
                        </div>
                        <div class="col-md-4">
                            <h6 class="fw-semibold mb-1">Assignments &amp; submissions</h6>
                            <p class="text-muted small mb-0">
                                Create assignments for each lesson, assign them to students
                                and register their submissions and grades.
                            </p>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
<script src="${pageContext.request.contextPath}/resources/js/appp.js"></script>
</body>
</html>

