<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Submissions</title>
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- encabezado -->
    <header class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h1 class="h3 mb-1">Submissions</h1>
            <c:if test="${not empty assignment}">
                <p class="text-muted mb-0">
                    <strong>Assignment:</strong> ${assignment.title}<br/>
                    <c:if test="${not empty assignment.dueAt}">
                        <strong>Deadline:</strong>
                        <!-- formateamos: 2025-11-16 12:02 -->
                        <c:set var="dueStr" value="${assignment.dueAt}" />
                        ${fn:substring(dueStr, 0, 10)}
                        ${fn:substring(dueStr, 11, 16)}
                    </c:if>
                    <c:if test="${empty assignment.dueAt}">
                        <strong>Deadline:</strong> No deadline
                    </c:if>
                </p>
            </c:if>
        </div>

        <a class="btn btn-outline-secondary btn-sm"
           href="${pageContext.request.contextPath}/app/assignments?lessonId=${assignment.lessonId}">
            ← Back to assignments
        </a>
    </header>

    <!-- mensaje de error (si lo hubiera) -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <!-- si no hay entregas -->
    <c:if test="${empty submissions}">
        <div class="alert alert-light border">
            There are no submissions yet for this assignment.
        </div>
    </c:if>

    <!-- tabla de entregas -->
    <c:if test="${not empty submissions}">
        <table class="table table-striped align-middle">
            <thead>
            <tr>
                <th>Student</th>
                <th style="width: 120px;">Status</th>
                <th style="width: 190px;">Submitted at</th>
                <th style="width: 170px;">Grade (0–100)</th>
                <th>Answer (short)</th>
                <th style="width: 150px;">Attachment</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${submissions}">
                <tr>
                    <!-- alumno -->
                    <td>
                        <c:set var="stu" value="${studentsById[s.userId]}"/>
                        <c:choose>
                            <c:when test="${not empty stu}">
                                ${stu.fullName}
                                <br/>
                                <small class="text-muted">${stu.email}</small>
                            </c:when>
                            <c:otherwise>
                                <!-- por si no encontramos el User -->
                                ${s.userId}
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <!-- estado -->
                    <td>
                        <c:set var="code" value="${statusCodeById[s.statusId]}"/>
                        <c:choose>
                            <c:when test="${code == 'SUBMITTED'}">
                                <span class="badge bg-primary">SUBMITTED</span>
                            </c:when>
                            <c:when test="${code == 'GRADED'}">
                                <span class="badge bg-success">GRADED</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">
                                    <c:out value="${code}" />
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <!-- fecha de envío -->
                    <td>
                        <c:if test="${not empty s.submittedAt}">
                            <c:set var="subStr" value="${s.submittedAt}"/>
                            <!-- formateamos: 2025-11-16 12:02 -->
                            ${fn:substring(subStr, 0, 10)}
                            ${fn:substring(subStr, 11, 16)}
                        </c:if>
                        <c:if test="${empty s.submittedAt}">
                            —
                        </c:if>
                    </td>

                    <!-- nota (formulario por fila) -->
                    <td>
                        <form method="post"
                              action="${pageContext.request.contextPath}/app/submissions"
                              class="d-flex align-items-center gap-1">

                            <input type="hidden" name="action" value="grade"/>
                            <input type="hidden" name="assignmentId" value="${assignment.id}"/>
                            <input type="hidden" name="studentId" value="${s.userId}"/>

                            <input type="number"
                                   name="grade"
                                   min="0" max="100" step="1"
                                   class="form-control form-control-sm"
                                   style="max-width: 80px;"
                                   value="${s.grade}"/>

                            <button type="submit"
                                    class="btn btn-sm btn-outline-primary">
                                Save
                            </button>
                        </form>
                    </td>

                    <!-- respuesta corta -->
                    <td>
                        <c:choose>
                            <c:when test="${not empty s.textAnswer}">
                                <c:set var="ans" value="${s.textAnswer}"/>
                                <c:choose>
                                    <c:when test="${fn:length(ans) > 80}">
                                        ${fn:substring(ans, 0, 80)}...
                                    </c:when>
                                    <c:otherwise>
                                        ${ans}
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">No text answer</span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <!-- archivo -->
                    <td>
                        <c:choose>
                            <c:when test="${not empty s.attachmentPath}">
                                <a href="${pageContext.request.contextPath}/app/files?path=${fn:escapeXml(s.attachmentPath)}"
                                   class="link-primary link-offset-1">
                                    Download file
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">No file</span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
