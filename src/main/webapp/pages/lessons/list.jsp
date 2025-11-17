<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My lessons</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container mt-4">
    <h1 class="mb-4">My lessons</h1>

    <!-- mensajes de error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- botón para crear lección (solo ADMIN o TEACHER) -->
    <c:if test="${isAdmin or isTeacher}">
        <a href="${pageContext.request.contextPath}/app/lessons/new"
           class="btn btn-primary mb-3">
            New lesson
        </a>
    </c:if>

    <!-- lista de lecciones -->
    <c:if test="${empty lessons}">
        <div class="alert alert-info">
            No lessons found for your account.
        </div>
    </c:if>

    <c:if test="${not empty lessons}">
        <table class="table table-striped table-bordered align-middle">
            <thead>
            <tr>
                <th scope="col">Topic</th>
                <th scope="col">Starts at</th>
                <th scope="col">Ends at</th>
                <th scope="col">Room</th>
                <th scope="col">Series ID</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="l" items="${lessons}">
                <tr>
                    <td>${l.topic}</td>

                    <!-- Starts at formateado -->
                    <td>
                        <c:if test="${not empty l.startsAt}">
                            ${fn:substring(l.startsAt, 0, 10)}
                            ${fn:substring(l.startsAt, 11, 16)}
                        </c:if>
                    </td>

                    <!-- Ends at formateado -->
                    <td>
                        <c:if test="${not empty l.endsAt}">
                            ${fn:substring(l.endsAt, 0, 10)}
                            ${fn:substring(l.endsAt, 11, 16)}
                        </c:if>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${not empty l.room}">
                                ${l.room}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">No room</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${l.seriesId}</td>
                    <td>
                        <!-- botón ver tareas, disponible para todos los usuarios -->
                        <a href="${pageContext.request.contextPath}/app/assignments?lessonId=${l.id}"
                           class="btn btn-sm btn-outline-secondary mb-1">
                            View assignments
                        </a>

                        <!-- Solo ADMIN o TEACHER ven acciones -->
                        <c:if test="${isAdmin or isTeacher}">
                            <!-- inscripción de alumnos  -->
                            <a href="${pageContext.request.contextPath}/app/lessons/enroll?lessonId=${l.id}"
                               class="btn btn-sm btn-outline-primary ms-1">
                                Enroll students
                            </a>

                            <a href="${pageContext.request.contextPath}/app/attendance?lessonId=${l.id}"
                               class="btn btn-sm btn-outline-secondary">
                                Mark attendance
                            </a>

                            <!-- borrar lección -->
                            <form method="post"
                                  action="${pageContext.request.contextPath}/app/lessons"
                                  class="d-inline">
                                <input type="hidden" name="action" value="delete"/>
                                <input type="hidden" name="lessonId" value="${l.id}"/>
                                <button type="submit"
                                        class="btn btn-sm btn-outline-danger"
                                        onclick="return confirm('Delete this lesson?');">
                                    Delete
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <a href="${pageContext.request.contextPath}/app/home" class="btn btn-secondary mt-4">
        Back to dashboard
    </a>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
