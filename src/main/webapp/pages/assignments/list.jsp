<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-16
  Time: 14:38
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Assignments</title>
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- encabezado -->
    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h3 mb-1">Assignments for lesson</h1>
            <p class="text-muted mb-0">
                Here you can see and manage the homework for this lesson.
            </p>
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

    <!-- lista -->
    <c:if test="${empty assignments}">
        <div class="alert alert-light border">
            There are no assignments for this lesson yet.
        </div>
    </c:if>

    <c:if test="${not empty assignments}">
        <table class="table table-striped align-middle shadow-sm">
            <thead>
            <tr>
                <th>Title</th>
                <th>Due at</th>
                <th>Status</th>
                <th>Created</th>
                <th>Updated</th>
                <th style="width: 220px;">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="a" items="${assignments}">
                <!-- obtener el código del status actual -->
                <c:set var="statusCode" value=""/>
                <c:forEach var="st" items="${assignmentStatuses}">
                    <c:if test="${st.id == a.statusId}">
                        <c:set var="statusCode" value="${st.code}"/>
                    </c:if>
                </c:forEach>

                <tr>
                    <td>${a.title}</td>
                    <td>
                        <c:choose>
                            <c:when test="${not empty a.dueAt}">
                                ${fn:substring(a.dueAt, 0, 10)}
                                ${fn:substring(a.dueAt, 11, 16)}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">No deadline</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <span class="badge text-bg-secondary">${statusCode}</span>
                    </td>
                    <td>
                        <c:if test="${not empty a.createdAt}">
                            ${fn:substring(a.createdAt, 0, 10)}
                            ${fn:substring(a.createdAt, 11, 16)}
                        </c:if>
                    </td>
                    <td>
                        <c:if test="${not empty a.updatedAt}">
                            ${fn:substring(a.updatedAt, 0, 10)}
                            ${fn:substring(a.updatedAt, 11, 16)}
                        </c:if>
                    </td>
                    <td>
                        <!-- link a entregas (para todos) -->
                        <a href="${pageContext.request.contextPath}/app/submissions?assignmentId=${a.id}"
                           class="btn btn-sm btn-outline-primary mb-1">
                            View submissions
                        </a>

                        <!-- cambio de status sólo para TEACHER/ADMIN -->
                        <c:if test="${isTeacher or isAdmin}">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/app/assignments"
                                  class="d-inline">
                                <input type="hidden" name="action" value="status"/>
                                <input type="hidden" name="assignmentId" value="${a.id}"/>
                                <input type="hidden" name="lessonId" value="${lessonId}"/>

                                <select name="statusId" class="form-select form-select-sm d-inline w-auto">
                                    <c:forEach var="st" items="${assignmentStatuses}">
                                        <option value="${st.id}"
                                                <c:if test="${st.id == a.statusId}">selected</c:if>>
                                                ${st.code}
                                        </option>
                                    </c:forEach>
                                </select>

                                <button type="submit" class="btn btn-sm btn-outline-secondary ms-1">
                                    Update
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <!-- crear tarea: solo TEACHER / ADMIN -->
    <c:if test="${isTeacher or isAdmin}">
        <hr class="my-4"/>

        <h2 class="h4 mb-3">Create new assignment</h2>

        <form method="post" action="${pageContext.request.contextPath}/app/assignments">
            <input type="hidden" name="action" value="create"/>
            <input type="hidden" name="lessonId" value="${lessonId}"/>

            <div class="mb-3">
                <label for="title" class="form-label">Title</label>
                <input id="title"
                       type="text"
                       name="title"
                       class="form-control"
                       required
                       value="${formTitle}">
            </div>

            <div class="mb-3">
                <label for="description" class="form-label">Description (optional)</label>
                <textarea id="description"
                          name="description"
                          class="form-control"
                          rows="3">${formDescription}</textarea>
            </div>

            <div class="mb-3">
                <label for="dueAt" class="form-label">Deadline (optional)</label>
                <input id="dueAt"
                       type="text"
                       name="dueAt"
                       class="form-control"
                       value="${formDueAt}">
                <div class="form-text">
                    Use ISO format, e.g. <code>2025-11-15T23:59:00Z</code>.
                </div>
            </div>

            <button type="submit" class="btn btn-primary">
                Create assignment
            </button>
        </form>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>