<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-15
  Time: 19:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin - Users</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container mt-4">

    <!-- Título + botón crear -->
    <div class="d-flex justify-content-between align-items-start mb-3">
        <h1 class="mb-0">Admin area - Users</h1>

        <div class="d-flex flex-column align-items-end">
            <a href="${pageContext.request.contextPath}/admin/users?mode=create"
               class="btn btn-primary mb-2">
                Create new user
            </a>

            <a href="${pageContext.request.contextPath}/app/home"
               class="btn btn-outline-secondary btn-sm">
                ← Back
            </a>
        </div>
    </div>

    <!-- mensajes -->
    <c:if test="${not empty success}">
        <div class="alert alert-success">
                ${success}
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- Users table -->
    <c:if test="${empty users}">
        <div class="alert alert-info">
            No users found.
        </div>
    </c:if>

    <c:if test="${not empty users}">
        <table class="table table-striped table-bordered align-middle">
            <thead>
            <tr>
                <th>Full name</th>
                <th>Email</th>
                <th>Active</th>
                <th>Roles</th>
                <th>Created at</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.fullName}</td>
                    <td>${u.email}</td>
                    <td>
                        <c:choose>
                            <c:when test="${u.active}">
                                <span class="badge bg-success">Active</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">Inactive</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:set var="rolesForUser" value="${userRolesMap[u.id]}" />

                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/users"
                              class="d-inline">
                            <input type="hidden" name="action" value="roles"/>
                            <input type="hidden" name="userId" value="${u.id}"/>

                            <c:forEach var="role" items="${allRoles}">
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input"
                                           type="checkbox"
                                           name="roles"
                                           value="${role.code}"
                                            <c:if test="${not empty rolesForUser and fn:contains(rolesForUser, role.code)}">
                                                checked
                                            </c:if> />
                                    <label class="form-check-label">
                                            ${role.code}
                                    </label>
                                </div>
                            </c:forEach>

                            <button type="submit" class="btn btn-sm btn-outline-primary ms-2">
                                Save
                            </button>
                        </form>
                    </td>
                    <td>
                        <c:if test="${not empty u.createdAt}">
                            ${fn:substring(u.createdAt, 0, 10)}
                            ${fn:substring(u.createdAt, 11, 16)}
                        </c:if>
                        <c:if test="${empty u.createdAt}">
                            <span class="text-muted">—</span>
                        </c:if>
                    </td>
                    <td>
                        <!-- Estado -->
                        <c:choose>
                            <c:when test="${u.active}">
                                <span class="badge bg-success">Active</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">Inactive</span>
                            </c:otherwise>
                        </c:choose>

                        <!-- Botón activar/desactivar -->
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/users"
                              class="d-inline ms-2">
                            <input type="hidden" name="action" value="toggleActive"/>
                            <input type="hidden" name="userId" value="${u.id}"/>
                            <input type="hidden" name="currentActive" value="${u.active}"/>

                            <button type="submit" class="btn btn-sm btn-outline-warning">
                                <c:choose>
                                    <c:when test="${u.active}">Deactivate</c:when>
                                    <c:otherwise>Activate</c:otherwise>
                                </c:choose>
                            </button>
                        </form>

                        <!-- Botón eliminar -->
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/users"
                              class="d-inline ms-2"
                              onsubmit="return confirm('Are you sure you want to delete this user?');">
                            <input type="hidden" name="action" value="delete"/>
                            <input type="hidden" name="userId" value="${u.id}"/>

                            <button type="submit" class="btn btn-sm btn-outline-danger">
                                Delete
                            </button>
                        </form>
                    </td>

                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>

