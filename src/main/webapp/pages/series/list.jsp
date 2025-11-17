<%--
  My courses page
  User: xludw1ng
  Date: 2025-11-14
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My courses</title>
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <!-- header con título + back -->
    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <div>
            <h1 class="h3 mb-1">My courses</h1>
            <p class="text-muted mb-0">
                Here you can see the series you are enrolled in.
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/app/home" class="btn btn-outline-secondary btn-sm">
            ← Back
        </a>
    </header>

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

    <!-- solo ADMIN puede ver el formulario de crear curso -->
    <c:if test="${isAdmin}">
        <hr class="my-4"/>

        <h2 class="h4 mb-3">Create new course</h2>

        <form method="post" action="${pageContext.request.contextPath}/series">
            <!-- acción create -->
            <input type="hidden" name="action" value="create"/>

            <div class="mb-3">
                <label for="code" class="form-label">Code</label>
                <input id="code"
                       type="text"
                       name="code"
                       class="form-control"
                       required
                       value="${formCode}">
                <div class="form-text">Example: ES-A1-2025-G1</div>
            </div>

            <div class="row">
                <!-- LANGUAGE como select -->
                <div class="col-md-2 mb-3">
                    <label for="language" class="form-label">Language</label>
                    <select id="language" name="language" class="form-select" required>
                        <option value="">-- Select --</option>
                        <option value="EN" ${formLanguage == 'EN' ? 'selected' : ''}>English (EN)</option>
                        <option value="ZH" ${formLanguage == 'ZH' ? 'selected' : ''}>Chinese (ZH)</option>
                        <option value="HI" ${formLanguage == 'HI' ? 'selected' : ''}>Hindi (HI)</option>
                        <option value="ES" ${formLanguage == 'ES' ? 'selected' : ''}>Spanish (ES)</option>
                        <option value="FR" ${formLanguage == 'FR' ? 'selected' : ''}>French (FR)</option>
                        <option value="AR" ${formLanguage == 'AR' ? 'selected' : ''}>Arabic (AR)</option>
                        <option value="BN" ${formLanguage == 'BN' ? 'selected' : ''}>Bengali (BN)</option>
                        <option value="RU" ${formLanguage == 'RU' ? 'selected' : ''}>Russian (RU)</option>
                        <option value="PT" ${formLanguage == 'PT' ? 'selected' : ''}>Portuguese (PT)</option>
                        <option value="UR" ${formLanguage == 'UR' ? 'selected' : ''}>Urdu (UR)</option>
                    </select>
                    <div class="form-text">Stored codes: EN, ES, RU, ...</div>
                </div>

                <!-- nivel -->
                <div class="col-md-2 mb-3">
                    <label for="level" class="form-label">Level</label>
                    <select id="level" name="level" class="form-select" required>
                        <option value="">-- Select --</option>
                        <option value="A1" ${formLevel == 'A1' ? 'selected' : ''}>A1</option>
                        <option value="A2" ${formLevel == 'A2' ? 'selected' : ''}>A2</option>
                        <option value="B1" ${formLevel == 'B1' ? 'selected' : ''}>B1</option>
                        <option value="B2" ${formLevel == 'B2' ? 'selected' : ''}>B2</option>
                        <option value="C1" ${formLevel == 'C1' ? 'selected' : ''}>C1</option>
                        <option value="C2" ${formLevel == 'C2' ? 'selected' : ''}>C2</option>
                    </select>
                </div>

                <!-- titulo -->
                <div class="col-md-8 mb-3">
                    <label for="title" class="form-label">Title</label>
                    <input id="title"
                           type="text"
                           name="title"
                           class="form-control"
                           required
                           value="${formTitle}">
                </div>
            </div>

            <div class="mb-3">
                <label for="description" class="form-label">Description (optional)</label>
                <textarea id="description"
                          name="description"
                          class="form-control"
                          rows="3">${formDescription}</textarea>
            </div>

            <button type="submit" class="btn btn-primary">
                Create course
            </button>
        </form>
    </c:if>


</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
