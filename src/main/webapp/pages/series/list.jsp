<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My courses</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- header con título + botones -->
    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h3 mb-1">My courses</h1>
            <p class="text-muted mb-0">
                Here you can see the series you are enrolled in.
            </p>
        </div>

        <div class="d-flex flex-column align-items-end">
            <c:if test="${isAdmin}">
                <a href="${pageContext.request.contextPath}/series?mode=create"
                   class="btn btn-primary mb-2">
                    Create new course
                </a>
            </c:if>

            <a href="${pageContext.request.contextPath}/app/home"
               class="btn btn-outline-secondary btn-sm">
                ← Back
            </a>
        </div>
    </header>

    <!-- mensajes de éxito -->
    <c:if test="${param.created == '1'}">
        <div class="alert alert-success">Course created successfully.</div>
    </c:if>
    <c:if test="${param.deleted == '1'}">
        <div class="alert alert-success">Course deleted successfully.</div>
    </c:if>

    <!-- mensaje de error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- lista de cursos -->
    <c:if test="${empty seriesList}">
        <div class="alert alert-light border">
            For now you don’t have any courses attached to your account.
        </div>
    </c:if>

    <c:if test="${not empty seriesList}">
        <ul class="list-group shadow-sm mb-4">
            <c:forEach var="s" items="${seriesList}">
                <li class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <div>
                            <div class="fw-semibold">
                                    ${s.title}
                            </div>
                            <div class="small text-muted">
                                    ${s.language} · ${s.level}
                            </div>
                            <div class="mt-2 small">
                                <c:choose>
                                    <c:when test="${not empty s.description}">
                                        ${s.description}
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">No description for this course yet.</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="badge text-bg-secondary">
                                    ${s.code}
                            </span>
                            <div class="small text-muted mt-2">
                                <span class="d-block">Created:</span>
                                <span>
                                    <c:if test="${not empty s.createdAt}">
                                        ${fn:substring(s.createdAt, 0, 10)}
                                        ${fn:substring(s.createdAt, 11, 16)}
                                    </c:if>
                                </span>
                            </div>

                            <a href="${pageContext.request.contextPath}/app/lessons?seriesId=${s.id}"
                               class="btn btn-sm btn-outline-primary mt-2 w-100">
                                View lessons
                            </a>

                            <!-- botón eliminar solo para ADMIN -->
                            <c:if test="${isAdmin}">
                                <form method="post"
                                      action="${pageContext.request.contextPath}/series"
                                      class="mt-2"
                                      onsubmit="return confirm('Are you sure you want to delete this course?');">
                                    <input type="hidden" name="action" value="delete"/>
                                    <input type="hidden" name="seriesId" value="${s.id}"/>

                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                        Delete course
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
