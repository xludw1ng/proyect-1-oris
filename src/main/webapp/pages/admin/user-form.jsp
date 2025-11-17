<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-17
  Time: 19:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin - Create user</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h4 mb-0">Create new user</h1>
        <a href="${pageContext.request.contextPath}/admin/users"
           class="btn btn-outline-secondary btn-sm">
            ← Back to users
        </a>
    </div>

    <!-- error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/admin/users"
          class="row g-3">
        <input type="hidden" name="action" value="create"/>

        <div class="col-md-4">
            <label for="email" class="form-label">Email</label>
            <input id="email"
                   type="email"
                   name="email"
                   class="form-control"
                   required
                   value="${formEmail}">
        </div>

        <div class="col-md-4">
            <label for="fullName" class="form-label">Full name</label>
            <input id="fullName"
                   type="text"
                   name="fullName"
                   class="form-control"
                   required
                   value="${formFullName}">
        </div>

        <div class="col-md-4">
            <label for="password" class="form-label">Password</label>
            <input id="password"
                   type="password"
                   name="password"
                   class="form-control"
                   required>
        </div>

        <div class="col-md-4">
            <label for="confirmPassword" class="form-label">Confirm password</label>
            <input id="confirmPassword"
                   type="password"
                   name="confirmPassword"
                   class="form-control"
                   required>
        </div>

        <div class="col-12">
            <button type="submit" class="btn btn-primary">
                Create user
            </button>
            <a href="${pageContext.request.contextPath}/admin/users"
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

