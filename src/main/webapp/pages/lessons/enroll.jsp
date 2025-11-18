<%--
  Created by IntelliJ IDEA.
  User: xludw1ng
  Date: 2025-11-14
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
    <title>Enroll students</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
          rel="stylesheet" crossorigin="anonymous">
</head>
<body class="bg-light">

<div class="container mt-4">
    <h1 class="mb-4">Enroll students for lesson</h1>

    <p class="mb-3">
        <strong>Lesson:</strong> ${lesson.topic} <br/>

        <strong>Starts at:</strong>
        <c:if test="${not empty lesson.startsAt}">
            ${fn:substring(lesson.startsAt, 0, 10)}
            ${fn:substring(lesson.startsAt, 11, 16)}
        </c:if>
        <br/>

        <strong>Ends at:</strong>
        <c:if test="${not empty lesson.endsAt}">
            ${fn:substring(lesson.endsAt, 0, 10)}
            ${fn:substring(lesson.endsAt, 11, 16)}
        </c:if>
    </p>

    <form method="post" action="${pageContext.request.contextPath}/app/lessons/enroll">
        <input type="hidden" name="lessonId" value="${lesson.id}"/>

        <table class="table table-striped align-middle">
            <thead>
            <tr>
                <th>Enroll</th>
                <th>Full name</th>
                <th>Email</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${students}">
                <tr>
                    <td>
                        <input class="form-check-input"
                               type="checkbox"
                               name="studentIds"
                               value="${s.id}"
                                <c:if test="${not empty enrolledIds and enrolledIds.contains(s.id)}">
                                    checked
                                </c:if>
                        />
                    </td>
                    <td>${s.fullName}</td>
                    <td>${s.email}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <button type="submit" class="btn btn-primary">Save enrollments</button>
        <a href="${pageContext.request.contextPath}/app/lessons" class="btn btn-secondary ms-2">
            Back to lessons
        </a>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
        crossorigin="anonymous"></script>
</body>
</html>
