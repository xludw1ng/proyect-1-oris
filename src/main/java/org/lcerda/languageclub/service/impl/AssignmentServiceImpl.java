package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AssignmentAssigneeDao;
import org.lcerda.languageclub.dao.AssignmentDao;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.UserLessonDao;
import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.AssignmentStatus;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AssignmentService;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.ValidationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {


    private final AssignmentDao assignmentDao;
    private final LessonDao lessonDao;
    private final UserLessonDao userLessonDao;
    private final AssignmentAssigneeDao assignmentAssigneeDao;

    private static final short STATUS_DRAFT     = 1;
    private static final short STATUS_PUBLISHED = 2;
    private static final short STATUS_CLOSED    = 3;

    @Override
    public List<Assignment> getAssignmentsForLesson(UUID lessonId,
                                                    User currentUser,
                                                    Set<String> roles) {

        Lesson lesson = loadLessonAndCheckAccess(lessonId, currentUser, roles);
        return assignmentDao.findByLessonId(lesson.getId());
    }

    @Override
    public UUID createAssignmentForLesson(UUID lessonId,
                                          User currentUser,
                                          Set<String> roles,
                                          String title,
                                          String description,
                                          OffsetDateTime dueAt) {

        Lesson lesson = loadLessonAndCheckTeacher(lessonId, currentUser, roles);

        String t = title != null ? title.trim() : "";
        String desc = (description != null && !description.isBlank())
                ? description.trim()
                : null;

        if (t.isBlank()) {
            throw new ValidationException("Title is required.");
        }

        if (dueAt != null && dueAt.isBefore(OffsetDateTime.now().minusYears(10))) {
            // check tonto para que no metan fechas locas
            throw new ValidationException("Due date looks incorrect.");
        }

        Assignment asg = Assignment.builder()
                .lessonId(lesson.getId())
                .statusId(STATUS_DRAFT)
                .title(t)
                .description(desc)
                .dueAt(dueAt)
                .build();

        return assignmentDao.create(asg);
    }

    @Override
    public void changeStatus(UUID assignmentId,
                             short newStatusId,
                             User currentUser,
                             Set<String> roles) {

        if (assignmentId == null) {
            throw new ValidationException("Assignment id is required.");
        }

        Assignment asg = assignmentDao.findById(assignmentId)
                .orElseThrow(() -> new ValidationException("Assignment not found."));

        Lesson lesson = lessonDao.findById(asg.getLessonId())
                .orElseThrow(() -> new ValidationException("Lesson for this assignment not found."));

        checkTeacherOrAdmin(lesson, currentUser, roles);

        if (newStatusId != STATUS_DRAFT
                && newStatusId != STATUS_PUBLISHED
                && newStatusId != STATUS_CLOSED) {
            throw new ValidationException("Invalid assignment status.");
        }

        assignmentDao.updateStatus(assignmentId, newStatusId);

        // Cuando se publica, asignamos la tarea a todos los alumnos inscritos
        if (newStatusId == STATUS_PUBLISHED) {
            var studentIds = userLessonDao.findStudentIdsByLesson(lesson.getId());
            assignmentAssigneeDao.replaceAssignees(assignmentId, studentIds);
        }
    }

    @Override
    public List<AssignmentStatus> getAllStatuses() {
        return assignmentDao.findAllStatuses();
    }

    @Override
    public Assignment getAssignmentForUser(UUID assignmentId,
                                           User currentUser,
                                           Set<String> roles) {

        if (assignmentId == null) {
            throw new ValidationException("Assignment id is required.");
        }
        if (currentUser == null) {
            throw new AuthException("You must be logged in.");
        }

        Assignment asg = assignmentDao.findById(assignmentId)
                .orElseThrow(() -> new ValidationException("Assignment not found."));

        Lesson lesson = lessonDao.findById(asg.getLessonId())
                .orElseThrow(() -> new ValidationException("Lesson for this assignment not found."));

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");
        boolean isStudent = roles != null && roles.contains("STUDENT");

        if (isAdmin || (isTeacher && lesson.getTeacherId().equals(currentUser.getId()))) {
            return asg;
        }

        if (isStudent) {
            var ids = userLessonDao.findStudentIdsByLesson(lesson.getId());
            if (!ids.contains(currentUser.getId())) {
                throw new AuthException("You are not enrolled in this lesson.");
            }
            if (asg.getStatusId() != STATUS_PUBLISHED) {
                throw new AuthException("This assignment is not available.");
            }
            return asg;
        }

        throw new AuthException("You are not allowed to view this assignment.");
    }

    // ===== helpers =====

    private Lesson loadLessonAndCheckAccess(UUID lessonId,
                                            User currentUser,
                                            Set<String> roles) {
        if (lessonId == null) {
            throw new ValidationException("Lesson id is required.");
        }
        if (currentUser == null) {
            throw new AuthException("You must be logged in.");
        }

        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new ValidationException("Lesson not found."));

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");
        boolean isStudent = roles != null && roles.contains("STUDENT");

        if (isAdmin || (isTeacher && lesson.getTeacherId().equals(currentUser.getId()))) {
            return lesson;
        }

        if (isStudent) {
            var ids = userLessonDao.findStudentIdsByLesson(lesson.getId());
            if (!ids.contains(currentUser.getId())) {
                throw new AuthException("You are not enrolled in this lesson.");
            }
            return lesson;
        }

        throw new AuthException("You are not allowed to access this lesson.");
    }

    private Lesson loadLessonAndCheckTeacher(UUID lessonId,
                                             User currentUser,
                                             Set<String> roles) {
        Lesson lesson = loadLessonAndCheckAccess(lessonId, currentUser, roles);

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");

        if (isAdmin) {
            return lesson;
        }

        if (isTeacher && lesson.getTeacherId().equals(currentUser.getId())) {
            return lesson;
        }

        throw new AuthException("Only teachers or admins can manage assignments.");
    }

    private void checkTeacherOrAdmin(Lesson lesson,
                                     User currentUser,
                                     Set<String> roles) {
        if (lesson == null || currentUser == null) {
            throw new AuthException("Invalid user/lesson.");
        }
        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");

        if (isAdmin) return;

        if (isTeacher && lesson.getTeacherId().equals(currentUser.getId())) {
            return;
        }

        throw new AuthException("You are not allowed to manage this assignment.");
    }
}
