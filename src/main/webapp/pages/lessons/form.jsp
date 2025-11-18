<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-13
  Time: 19:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Create lesson</title>
</head>
<body class="bg-light">

<div class="container mt-4">
    <h1 class="mb-4">Create lesson</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/app/lessons/new" class="row g-3">

        <!-- COURSE / SERIES -->
        <div class="col-md-6">
            <label for="seriesId" class="form-label">Course (series)</label>
            <select id="seriesId" name="seriesId" class="form-select" required>
                <option value="">-- Select course --</option>
                <c:forEach var="s" items="${seriesList}">
                    <option value="${s.id}"
                            <c:if test="${formSeriesId == s.id}">selected</c:if>>
                            ${s.title} (${s.code})
                    </option>
                </c:forEach>
            </select>
        </div>

        <!-- TEACHER -->
        <div class="col-md-6">
            <label for="teacherId" class="form-label">Assigned teacher</label>

                <!-- Si es TEACHER: solo puede crearse a sí mismo -->
                <!-- Si es ADMIN: puede elegir teacher de la lista -->
            <c:choose>
                <c:when test="${isTeacher and not isAdmin}">
                    <input id="teacherId" type="text" class="form-control"
                           value="${teacherId}" readonly>
                    <div class="form-text">
                        The lesson will be assigned to you.
                    </div>
                    <input type="hidden" name="teacherId" value="${teacherId}"/>
                </c:when>

                <c:otherwise>
                    <select id="teacherId" name="teacherId" class="form-select" required>
                        <option value="">-- Select teacher --</option>
                        <c:forEach var="t" items="${teachersList}">
                            <option value="${t.id}"
                                    <c:if test="${formTeacherId == t.id}">selected</c:if>>
                                    ${t.fullName} (${t.email})
                            </option>
                        </c:forEach>
                    </select>
                    <div class="form-text">
                        Only users with TEACHER role are listed here.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- TOPIC -->
        <div class="col-12">
            <label for="topic" class="form-label">Topic</label>
            <input id="topic" type="text" name="topic" class="form-control"
                   required value="${formTopic}">
        </div>

        <!-- START / END (ISO-8601 por ahora) -->
        <div class="col-md-6">
            <label for="startsAt" class="form-label">Starts at (ISO)</label>
            <input id="startsAt" type="text" name="startsAt" class="form-control"
                   placeholder="2025-11-14T10:00:00Z"
                   value="${formStartsAt}">
        </div>
        <div class="col-md-6">
            <label for="endsAt" class="form-label">Ends at (ISO)</label>
            <input id="endsAt" type="text" name="endsAt" class="form-control"
                   placeholder="2025-11-14T11:30:00Z"
                   value="${formEndsAt}">
        </div>

        <!-- ROOM -->
        <div class="col-md-6">
            <label for="room" class="form-label">Room</label>
            <input id="room" type="text" name="room" class="form-control"
                   value="${formRoom}">
        </div>

        <!-- NOTES -->
        <div class="col-12">
            <label for="notes" class="form-label">Notes (optional)</label>
            <textarea id="notes" name="notes" class="form-control"
                      rows="3">${formNotes}</textarea>
        </div>

        <div class="col-12">
            <button type="submit" class="btn btn-primary">Create lesson</button>
            <a href="${pageContext.request.contextPath}/app/lessons" class="btn btn-secondary ms-2">
                Back to lessons
            </a>
        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
