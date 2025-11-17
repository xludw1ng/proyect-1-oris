<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-17
  Time: 19:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Create new course</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-body-tertiary">

<div class="container mt-4 mb-5">

    <header class="mb-4 border-bottom pb-2 d-flex justify-content-between align-items-center">
        <h1 class="h3 mb-0">Create new course</h1>

        <a href="${pageContext.request.contextPath}/series"
           class="btn btn-outline-secondary btn-sm">
            ← Back to courses
        </a>
    </header>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

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
            <!-- LANGUAGE -->
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

            <!-- LEVEL -->
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

            <!-- TITLE -->
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

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>

