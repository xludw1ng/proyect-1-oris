<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-15
  Time: 22:37
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Attendance</title>
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- encabezado -->
    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h3 mb-1">Attendance for lesson</h1>
            <c:if test="${not empty lesson}">
                <p class="text-muted mb-0">
                    <strong>Topic:</strong> ${lesson.topic}
                    <br/>
                    <strong>Starts at:</strong>
                    <c:if test="${not empty lesson.startsAt}">
                        ${fn:substring(lesson.startsAt, 0, 10)}
                        ${fn:substring(lesson.startsAt, 11, 16)}
                    </c:if>
                    <br/>
                    <strong>Ends at:</strong>
                    <c:if test="${not empty lesson.endsAt}">
                        ${fn:substring(lesson.endsAt, 0, 10)}
                        ${fn:substring(lesson.endsAt, 11, 16)}
                    </c:if>
                    <c:if test="${not empty lesson.room}">
                        <br/>
                        <strong>Room:</strong> ${lesson.room}
                    </c:if>
                </p>
            </c:if>
        </div>

        <a href="${pageContext.request.contextPath}/app/lessons"
           class="btn btn-outline-secondary btn-sm">
            ← Back to lessons
        </a>
    </header>

    <!-- mensaje de error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- si no hay alumnos -->
    <c:if test="${empty students}">
        <div class="alert alert-light border">
            There are no students enrolled in this lesson yet.
        </div>
    </c:if>

    <!-- formulario de asistencia -->
    <c:if test="${not empty students}">
        <form action="${pageContext.request.contextPath}/app/attendance" method="post" >

            <!-- enviamos el id de la lección -->
            <input type="hidden" name="lessonId" value="${lesson.id}"/>

            <table class="table table-striped align-middle">
                <thead>
                <tr>
                    <th>Student</th>
                    <th>Email</th>
                    <th style="width: 220px;">Status</th>
                    <th>Comment (optional)</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${students}">
                    <!-- Attendance actual para este alumno (puede ser null) -->
                    <c:set var="att" value="${attendanceByUserId[s.id]}"/>

                    <tr>
                        <td>${s.fullName}</td>
                        <td>${s.email}</td>
                        <td>
                            <!-- mismo nombre en todos -> llegará como array studentId[] -->
                            <input type="hidden" name="studentId" value="${s.id}"/>

                            <select name="statusId" class="form-select">
                                <option value="">-- Not set --</option>
                                <c:forEach var="st" items="${statuses}">
                                    <option value="${st.id}"
                                            <c:if test="${att != null && att.statusId == st.id}">selected</c:if>>
                                            ${st.code}
                                    </option>
                                </c:forEach>
                            </select>
                        </td>
                        <td>
                            <input type="text"
                                   name="comment"
                                   class="form-control"
                                   placeholder="Optional comment"
                                   value="${att.comment}"/>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <button type="submit" class="btn btn-primary">
                Save attendance
            </button>
            <a href="${pageContext.request.contextPath}/app/lessons"
               class="btn btn-secondary ms-2">
                Cancel
            </a>
        </form>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
