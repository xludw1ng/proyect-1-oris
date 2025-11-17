<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar navbar-expand-lg navbar-light bg-light border-bottom">
    <div class="container-fluid">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/app/home">
            Language Club
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#mainNavbar" aria-controls="mainNavbar"
                aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/series">
                        Courses
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/app/lessons">
                        Lessons
                    </a>
                </li>

                <!-- Enlace de admin sólo si es admin -->
                <c:if test="${sessionScope.isAdmin}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/users">
                            Admin users
                        </a>
                    </li>
                </c:if>
            </ul>

            <div class="d-flex ms-auto">
                <c:if test="${not empty sessionScope.currentUser}">
                    <span class="navbar-text me-3">
                            ${sessionScope.currentUser.fullName}
                    </span>
                    <a class="btn btn-outline-secondary btn-sm"
                       href="${pageContext.request.contextPath}/logout">
                        Sign out
                    </a>
                </c:if>

                <c:if test="${empty sessionScope.currentUser}">
                    <a class="btn btn-primary btn-sm"
                       href="${pageContext.request.contextPath}/login">
                        Sign in
                    </a>
                </c:if>
            </div>
        </div>
    </div>
</nav>
