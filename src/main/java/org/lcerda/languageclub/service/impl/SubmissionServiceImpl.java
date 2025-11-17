package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AssignmentAssigneeDao;
import org.lcerda.languageclub.dao.AssignmentDao;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.dao.SubmissionsDao;
import org.lcerda.languageclub.model.*;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.SubmissionService;
import org.lcerda.languageclub.service.ValidationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionsDao submissionsDao;
    private final AssignmentDao assignmentDao;
    private final LessonDao lessonDao;
    private final AssignmentAssigneeDao assignmentAssigneeDao;

    private static final short STATUS_PENDING   = 1;
    private static final short STATUS_SUBMITTED = 2;
    private static final short STATUS_GRADED    = 3;

    private static final short ASG_STATUS_PUBLISHED = 2;
    private static final short ASG_STATUS_CLOSED    = 3;

    @Override
    public Submissions getSubmissionForStudent(UUID assignmentId, User currentUser) {
        if (assignmentId == null) {
            throw new ValidationException("Assignment id is required.");
        }
        if (currentUser == null) {
            throw new AuthException("You must be logged in.");
        }

        return submissionsDao.findByAssignmentIdAndUserId(assignmentId, currentUser.getId())
                .orElse(null);
    }

    @Override
    public void submitAssignment(UUID assignmentId,
                                 User currentUser,
                                 Set<String> roles,
                                 String textAnswer,
                                 String attachmentPath) {

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

        // Normalmente sólo los estudiantes entregan
        if (isAdmin || isTeacher) {
            throw new AuthException("Only students can submit assignments.");
        }

        // estado de la tarea
        if (asg.getStatusId() != ASG_STATUS_PUBLISHED) {
            if (asg.getStatusId() == ASG_STATUS_CLOSED) {
                throw new ValidationException("This assignment is already closed.");
            }
            throw new ValidationException("This assignment is not published.");
        }

        // deadline
        OffsetDateTime now = OffsetDateTime.now();
        if (asg.getDueAt() != null && now.isAfter(asg.getDueAt())) {
            throw new ValidationException("The deadline for this assignment has passed.");
        }

        // comprobar que el usuario ES un assignee de esta tarea
        boolean isAssignee = assignmentAssigneeDao.isUserAssignee(assignmentId, currentUser.getId());
        if (!isAssignee) {
            throw new AuthException("You are not assigned to this assignment.");
        }

        String cleanText = null;
        if (textAnswer != null) {
            cleanText = textAnswer.trim();
            if (cleanText.isEmpty()) {
                cleanText = null;
            }
        }

        Submissions submission = Submissions.builder()
                .assignmentId(assignmentId)
                .userId(currentUser.getId())
                .statusId(STATUS_SUBMITTED)
                .submittedAt(now)
                .textAnswer(cleanText)
                .attachmentPath(attachmentPath)
                .build();

        // ON CONFLICT (assignment_id, user_id) ... en el DAO
        submissionsDao.upsert(submission);
    }

    @Override
    public List<Submissions> getSubmissionsForAssignment(UUID assignmentId,
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

        if (!(isAdmin || (isTeacher && lesson.getTeacherId().equals(currentUser.getId())))) {
            throw new AuthException("Only the teacher or admin can see submissions.");
        }

        return submissionsDao.findByAssignmentId(assignmentId);
    }

    @Override
    public List<SubmissionsStatus> getAllStatuses() {
        return submissionsDao.findAllStatuses();
    }

    @Override
    public void gradeSubmission(UUID assignmentId,
                                UUID studentId,
                                User currentUser,
                                Set<String> roles,
                                Integer grade) {

        if (assignmentId == null || studentId == null) {
            throw new ValidationException("Assignment and student are required.");
        }
        if (currentUser == null) {
            throw new AuthException("You must be logged in.");
        }

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");

        if (!isAdmin && !isTeacher) {
            throw new AuthException("Only teachers or admins can grade submissions.");
        }

        // 1) Cargamos assignment y lesson para comprobar permisos
        Assignment asg = assignmentDao.findById(assignmentId)
                .orElseThrow(() -> new ValidationException("Assignment not found."));

        Lesson lesson = lessonDao.findById(asg.getLessonId())
                .orElseThrow(() -> new ValidationException("Lesson not found."));

        if (!isAdmin) {
            // si no es admin, tiene que ser el profesor de esa lección
            if (!lesson.getTeacherId().equals(currentUser.getId())) {
                throw new AuthException("You are not the teacher of this lesson.");
            }
        }

        // 2) Validar nota (0–100) o null
        if (grade != null && (grade < 0 || grade > 100)) {
            throw new ValidationException("Grade must be between 0 and 100.");
        }

        // 3) Recuperar la submission mediante (assignment_id, user_id)
        Submissions sub = submissionsDao
                .findByAssignmentIdAndUserId(assignmentId, studentId)
                .orElseThrow(() -> new ValidationException("Submission not found for this student."));

        // 4) Si hay nota -> GRADED, si quitamos la nota -> SUBMITTED
        short newStatus = (grade != null) ? STATUS_GRADED : STATUS_SUBMITTED;

        submissionsDao.updateStatusAndGrade(sub.getId(), newStatus, grade);
    }
}
