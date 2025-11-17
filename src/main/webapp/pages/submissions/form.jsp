<%--
  My submission page
  User: xludw1ng
  Date: 2025-11-16
  Time: 14:39
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My submission</title>
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- encabezado -->
    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h3 mb-1">Assignment submission</h1>
            <c:if test="${not empty assignment}">
                <p class="text-muted mb-0">
                    <strong>Title:</strong> ${assignment.title}<br/>

                    <c:if test="${not empty assignment.dueAt}">
                        <strong>Deadline:</strong>
                        ${fn:substring(assignment.dueAt, 0, 10)}
                        ${fn:substring(assignment.dueAt, 11, 16)}<br/>
                    </c:if>

                    <c:if test="${not empty assignment.description}">
                        <strong>Description:</strong> ${assignment.description}
                    </c:if>
                </p>
            </c:if>
        </div>
        <a href="${pageContext.request.contextPath}/app/assignments?lessonId=${assignment.lessonId}"
           class="btn btn-outline-secondary btn-sm">
            ← Back to assignments
        </a>
    </header>

    <!-- error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- info de entrega actual -->
    <c:if test="${not empty submission}">
        <div class="alert alert-info">
            <h2 class="h6 mb-2">Current submission</h2>

            <!-- Status mapeado por id -->
            <p class="mb-2">
                <strong>Status:</strong>
                <c:choose>
                    <c:when test="${submission.statusId == 1}">Pending</c:when>
                    <c:when test="${submission.statusId == 2}">Submitted</c:when>
                    <c:when test="${submission.statusId == 3}">Graded</c:when>
                    <c:otherwise>Unknown</c:otherwise>
                </c:choose>
            </p>

            <p class="mb-1">
                <c:if test="${not empty submission.submittedAt}">
                    <strong>Submitted at:</strong>
                    ${fn:substring(submission.submittedAt, 0, 10)}
                    ${fn:substring(submission.submittedAt, 11, 16)}<br/>
                </c:if>

                <c:if test="${not empty submission.grade}">
                    <strong>Grade:</strong> ${submission.grade}
                </c:if>
            </p>

            <c:if test="${not empty submission.textAnswer}">
                <p class="mb-1">
                    <strong>Your answer:</strong><br/>
                <pre class="mb-0 small bg-light p-2 border rounded">${submission.textAnswer}</pre>
                </p>
            </c:if>
        </div>
    </c:if>

    <!-- formulario de envío / re-envío -->
    <form method="post"
          action="${pageContext.request.contextPath}/app/submissions"
          enctype="multipart/form-data"
          class="card shadow-sm">
        <div class="card-body">
            <input type="hidden" name="assignmentId" value="${assignment.id}"/>

            <div class="mb-3">
                <label for="textAnswer" class="form-label">Text answer (optional)</label>
                <textarea id="textAnswer"
                          name="textAnswer"
                          class="form-control"
                          rows="6"><c:out value="${submission.textAnswer}"/></textarea>
            </div>

            <!-- BLOQUE FILE CON LINK AL ARCHIVO ACTUAL -->
            <div class="mb-3">
                <label class="form-label">File (optional)</label>

                <c:if test="${not empty submission and not empty submission.attachmentPath}">
                    <p class="mb-2">
                        Current file:
                        <a href="${pageContext.request.contextPath}/app/files?path=${fn:escapeXml(submission.attachmentPath)}"
                           class="btn btn-sm btn-outline-secondary">
                            Download current file
                        </a>
                    </p>
                </c:if>

                <input type="file"
                       name="attachment"
                       class="form-control"/>
                <div class="form-text">
                    Maximum size is controlled by server config (around 25 MB).
                </div>
            </div>

            <button type="submit" class="btn btn-primary">
                Submit assignment
            </button>
            <a href="${pageContext.request.contextPath}/app/assignments?lessonId=${assignment.lessonId}"
               class="btn btn-secondary ms-2">
                Cancel
            </a>
        </div>
    </form>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
