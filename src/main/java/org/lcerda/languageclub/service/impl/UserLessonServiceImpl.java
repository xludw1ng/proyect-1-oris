package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.UserDao;
import org.lcerda.languageclub.dao.UserLessonDao;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.UserLessonService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class UserLessonServiceImpl implements UserLessonService {

    private final LessonDao lessonDao;
    private final UserDao userDao;
    private final UserLessonDao userLessonDao;

    @Override
    public Lesson loadLessonIfAuthorized(User currentUser, Set<String> roles, UUID lessonId) {
        if (currentUser == null) {
            throw new AuthException("You must be logged in.");
        }
        if (lessonId == null) {
            throw new IllegalArgumentException("lessonId is required.");
        }

        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");

        if (isAdmin) {
            return lesson;
        }

        if (isTeacher && lesson.getTeacherId() != null
                && lesson.getTeacherId().equals(currentUser.getId())) {
            return lesson;
        }

        throw new AuthException("You are not allowed to manage enrollments for this lesson.");
    }

    @Override
    public List<User> getEligibleStudents() {
        // rol STUDENT
        List<User> allStudents = userDao.findAllByRoleCode("STUDENT");
        //solo activos
        return allStudents.stream()
                .filter(User::isActive)
                .toList();
    }

    @Override
    public Set<UUID> getEnrolledStudentIds(UUID lessonId) {
        return userLessonDao.findStudentIdsByLesson(lessonId);
    }

    @Override
    public void saveEnrollments(User currentUser, Set<String> roles,
                                UUID lessonId, Set<UUID> studentIds) {

        // volvemos a verificar permisos
        loadLessonIfAuthorized(currentUser, roles, lessonId);

        userLessonDao.replaceEnrollments(lessonId, studentIds);
    }
}
