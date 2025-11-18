<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-10
  Time: 19:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My lessons</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet"
          crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container mt-4">
    <h1 class="mb-4">My lessons</h1>

    <!-- mensajes de error -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
                ${error}
        </div>
    </c:if>

    <!-- botones: New lesson + AJAX -->
    <div class="d-flex gap-2 mb-3">
        <!-- botón para crear lección (solo ADMIN o TEACHER) -->
        <c:if test="${isAdmin or isTeacher}">
            <a href="${pageContext.request.contextPath}/app/lessons/new"
               class="btn btn-primary">
                New lesson
            </a>
        </c:if>

        <!-- botón AJAX solo si NO hay seriesId en la URL -->
        <c:if test="${empty param.seriesId}">
            <button type="button"
                    id="lessonsAjaxBtn"
                    class="btn btn-outline-secondary btn-sm">
                Reload via AJAX (JSON)
            </button>
        </c:if>
    </div>


    <!-- lista de lecciones -->
    <c:if test="${empty lessons}">
        <div class="alert alert-info">
            No lessons found for your account.
        </div>
    </c:if>

    <c:if test="${not empty lessons}">
        <table class="table table-striped table-bordered align-middle">
            <thead>
            <tr>
                <th scope="col">Topic</th>
                <th scope="col">Starts at</th>
                <th scope="col">Ends at</th>
                <th scope="col">Room</th>
                <th scope="col">Series ID</th>
                <th scope="col">Actions</th>
            </tr>
            </thead>
            <!-- IMPORTANTE: id para el JS -->
            <tbody id="lessonsTableBody">
            <c:forEach var="l" items="${lessons}">
                <tr>
                    <td>${l.topic}</td>

                    <!-- Starts at formateado -->
                    <td>
                        <c:if test="${not empty l.startsAt}">
                            ${fn:substring(l.startsAt, 0, 10)}
                            ${fn:substring(l.startsAt, 11, 16)}
                        </c:if>
                    </td>

                    <!-- Ends at formateado -->
                    <td>
                        <c:if test="${not empty l.endsAt}">
                            ${fn:substring(l.endsAt, 0, 10)}
                            ${fn:substring(l.endsAt, 11, 16)}
                        </c:if>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${not empty l.room}">
                                ${l.room}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">No room</span>
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>${l.seriesId}</td>

                    <td>
                        <!-- botón ver tareas, disponible para todos los usuarios -->
                        <a href="${pageContext.request.contextPath}/app/assignments?lessonId=${l.id}"
                           class="btn btn-sm btn-outline-secondary mb-1">
                            View assignments
                        </a>

                        <!-- Solo ADMIN o TEACHER ven acciones extra -->
                        <c:if test="${isAdmin or isTeacher}">
                            <a href="${pageContext.request.contextPath}/app/lessons/enroll?lessonId=${l.id}"
                               class="btn btn-sm btn-outline-primary ms-1">
                                Enroll students
                            </a>

                            <a href="${pageContext.request.contextPath}/app/attendance?lessonId=${l.id}"
                               class="btn btn-sm btn-outline-secondary ms-1">
                                Mark attendance
                            </a>

                            <form method="post"
                                  action="${pageContext.request.contextPath}/app/lessons"
                                  class="d-inline">
                                <input type="hidden" name="action" value="delete"/>
                                <input type="hidden" name="lessonId" value="${l.id}"/>
                                <button type="submit"
                                        class="btn btn-sm btn-outline-danger ms-1"
                                        onclick="return confirm('Delete this lesson?');">
                                    Delete
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <a href="${pageContext.request.contextPath}/app/home" class="btn btn-secondary mt-4">
        Back to dashboard
    </a>
</div>

<!-- ====================== JS AJAX + JSON ====================== -->
<script>
    // context path de la app, ej: /languageclub
    const contextPath = '${pageContext.request.contextPath}';
    const lessonsApiUrl = contextPath + '/app/api/lessons';

    // (Boolean en request)
    const isAdmin = ${isAdmin ? 'true' : 'false'};
    const isTeacher = ${isTeacher ? 'true' : 'false'};

    console.log('Lessons API URL:', lessonsApiUrl);
    console.log('isAdmin JS =', isAdmin, 'isTeacher JS =', isTeacher);

    function formatDateTime(isoString) {
        if (!isoString) return '';
        if (isoString.length < 16) {
            return isoString;
        }
        // 2025-11-14T10:00:00Z -> "2025-11-14 10:00"
        return isoString.substring(0, 10) + ' ' + isoString.substring(11, 16);
    }

    async function reloadLessonsAjax() {
        console.log('Reload via AJAX clicked');

        try {
            const resp = await fetch(lessonsApiUrl, {
                headers: {
                    'Accept': 'application/json'
                }
            });

            console.log('AJAX response status:', resp.status);

            if (!resp.ok) {
                alert('Error loading lessons via AJAX (status ' + resp.status + ')');
                return;
            }

            const lessons = await resp.json();
            console.log('Lessons from JSON:', lessons);

            const tbody = document.getElementById('lessonsTableBody');
            if (!tbody) {
                console.error('Not founded <tbody id="lessonsTableBody">');
                return;
            }

            tbody.innerHTML = '';

            if (!Array.isArray(lessons) || lessons.length === 0) {
                const tr = document.createElement('tr');
                const td = document.createElement('td');
                td.colSpan = 6;
                td.className = 'text-muted';
                td.textContent = 'No lessons found (AJAX).';
                tr.appendChild(td);
                tbody.appendChild(tr);
                return;
            }

            lessons.forEach(lesson => {
                const tr = document.createElement('tr');

                // Topic
                const topicTd = document.createElement('td');
                topicTd.textContent = lesson.topic;
                tr.appendChild(topicTd);

                // Starts at
                const startsTd = document.createElement('td');
                startsTd.textContent = formatDateTime(lesson.startsAt);
                tr.appendChild(startsTd);

                // Ends at
                const endsTd = document.createElement('td');
                endsTd.textContent = formatDateTime(lesson.endsAt);
                tr.appendChild(endsTd);

                // Room
                const roomTd = document.createElement('td');
                if (lesson.room) {
                    roomTd.textContent = lesson.room;
                } else {
                    roomTd.innerHTML = '<span class="text-muted">No room</span>';
                }
                tr.appendChild(roomTd);

                // Series ID
                const seriesTd = document.createElement('td');
                seriesTd.textContent = lesson.seriesId;
                tr.appendChild(seriesTd);

                // Actions
                const actionsTd = document.createElement('td');

                // View assignments (para todos)
                const viewLink = document.createElement('a');
                viewLink.href = contextPath + '/app/assignments?lessonId=' + encodeURIComponent(lesson.id);
                viewLink.textContent = 'View assignments';
                viewLink.className = 'btn btn-sm btn-outline-secondary mb-1';
                actionsTd.appendChild(viewLink);

                // Solo ADMIN o TEACHER ven las otras acciones
                if (isAdmin || isTeacher) {
                    // Enroll students
                    const enrollLink = document.createElement('a');
                    enrollLink.href = contextPath + '/app/lessons/enroll?lessonId=' + encodeURIComponent(lesson.id);
                    enrollLink.textContent = 'Enroll students';
                    enrollLink.className = 'btn btn-sm btn-outline-primary ms-1';
                    actionsTd.appendChild(enrollLink);

                    // Mark attendance
                    const attendanceLink = document.createElement('a');
                    attendanceLink.href = contextPath + '/app/attendance?lessonId=' + encodeURIComponent(lesson.id);
                    attendanceLink.textContent = 'Mark attendance';
                    attendanceLink.className = 'btn btn-sm btn-outline-secondary ms-1';
                    actionsTd.appendChild(attendanceLink);

                    // Delete (form POST)
                    const deleteForm = document.createElement('form');
                    deleteForm.method = 'post';
                    deleteForm.action = contextPath + '/app/lessons';
                    deleteForm.className = 'd-inline';

                    const actionInput = document.createElement('input');
                    actionInput.type = 'hidden';
                    actionInput.name = 'action';
                    actionInput.value = 'delete';
                    deleteForm.appendChild(actionInput);

                    const idInput = document.createElement('input');
                    idInput.type = 'hidden';
                    idInput.name = 'lessonId';
                    idInput.value = lesson.id;
                    deleteForm.appendChild(idInput);

                    const deleteBtn = document.createElement('button');
                    deleteBtn.type = 'submit';
                    deleteBtn.className = 'btn btn-sm btn-outline-danger ms-1';
                    deleteBtn.textContent = 'Delete';
                    deleteBtn.onclick = function () {
                        return confirm('Delete this lesson?');
                    };
                    deleteForm.appendChild(deleteBtn);

                    actionsTd.appendChild(deleteForm);
                }

                tr.appendChild(actionsTd);
                tbody.appendChild(tr);
            });

        } catch (e) {
            console.error('Network/JS error while loading lessons via AJAX', e);
            alert('Network/JS error while loading lessons via AJAX (ver Console)');
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const btn = document.getElementById('lessonsAjaxBtn');
        if (btn) {
            btn.addEventListener('click', reloadLessonsAjax);
        }
    });
</script>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
