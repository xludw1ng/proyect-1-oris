<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-17
  Time: 14:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Assignment unavailable</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<jsp:include page="/pages/layout/header.jsp"/>

<main class="container main-content">

    <h1 class="h4 mt-4 mb-3">Assignment not available</h1>

    <div class="alert alert-warning">
        <c:choose>
            <c:when test="${not empty error}">
                ${error}
            </c:when>
            <c:otherwise>
                This assignment is not available for submissions right now.
            </c:otherwise>
        </c:choose>
    </div>
    <!-- NUEVO: info de tu entrega si existe -->
    <c:if test="${not empty submission}">
        <div class="card mt-3">
            <div class="card-body">
                <h2 class="h6 mb-2">Your submission</h2>

                <!-- Estado -->
                <p class="mb-1">
                    <strong>Status:</strong>
                    <c:choose>
                        <c:when test="${submission.statusId == 1}">Pending</c:when>
                        <c:when test="${submission.statusId == 2}">Submitted</c:when>
                        <c:when test="${submission.statusId == 3}">Graded</c:when>
                        <c:otherwise>Unknown</c:otherwise>
                    </c:choose>
                </p>

                <!-- Nota -->
                <c:if test="${not empty submission.grade}">
                    <p class="mb-1">
                        <strong>Grade:</strong> ${submission.grade}
                    </p>
                </c:if>

                <!-- Fecha de envío (si quieres) -->
                <c:if test="${not empty submission.submittedAt}">
                    <p class="mb-1">
                        <strong>Submitted at:</strong>
                            ${fn:substring(submission.submittedAt, 0, 10)}
                            ${fn:substring(submission.submittedAt, 11, 16)}
                    </p>
                </c:if>
            </div>
        </div>
    </c:if>

    <p class="text-muted mb-3">
        Possible reasons:
    </p>
    <ul class="text-muted">
        <li>The assignment is still in <strong>DRAFT</strong>.</li>
        <li>The assignment is <strong>CLOSED</strong> or the deadline has passed.</li>
    </ul>

    <a href="${pageContext.request.contextPath}/app/lessons"
       class="btn btn-secondary mt-3">
        ← Back to lessons
    </a>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>

