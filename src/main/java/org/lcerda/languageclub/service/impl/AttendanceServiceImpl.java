package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.AttendanceDao;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.model.Attendance;
import org.lcerda.languageclub.model.AttendanceStatus;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AttendanceService;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.ValidationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceDao attendanceDao;
    private final LessonDao lessonDao;

    @Override
    public List<Attendance> getAttendanceForLesson(UUID lessonId, User currentUser, Set<String> roles) {
        Lesson lesson = checkPermissionAndGetLesson(lessonId, currentUser, roles);
        return attendanceDao.findByLessonId(lesson.getId());
    }

    @Override
    public void saveAttendanceForLesson(UUID lessonId, Map<UUID, Short> statusByUserId, Map<UUID, String> commentByUserId, User currentUser, Set<String> roles) {
        Lesson lesson = checkPermissionAndGetLesson(lessonId, currentUser, roles);
        if (statusByUserId == null || statusByUserId.isEmpty()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();

        for (Map.Entry<UUID, Short> entry : statusByUserId.entrySet()) {
            UUID studentId = entry.getKey();
            Short statusId = entry.getValue();
            if (statusId == null || statusId <= 0) {
                continue;
            }

            String comment = null;
            if (commentByUserId != null) {
                comment = commentByUserId.get(studentId);
            }
            if (comment != null) {
                comment = comment.trim();
                if (comment.isEmpty()) {
                    comment = null;
                }
            }

            Attendance att = Attendance.builder()
                    .lessonId(lesson.getId())
                    .userId(studentId)
                    .statusId(statusId)
                    .attendedAt(now)
                    .comment(comment)
                    .build();

            attendanceDao.upsert(att);
        }
    }

    @Override
    public List<AttendanceStatus> getAllStatuses() {
        return  attendanceDao.findAllStatuses();
    }

    @Override
    public Lesson loadLessonIfAuthorized(UUID lessonId, User currentUser, Set<String> roles) {
        return checkPermissionAndGetLesson(lessonId, currentUser, roles);
    }

    // helper
    private Lesson checkPermissionAndGetLesson(UUID lessonId, User currentUser, Set<String> roles) {
        if (lessonId == null) {
            throw new ValidationException("Lesson id is required.");
        }
        if (currentUser == null) {
            throw new AuthException("User is required.");
        }

        boolean isAdmin = roles != null && roles.contains("ADMIN");
        boolean isTeacher = roles != null && roles.contains("TEACHER");

        if (!isAdmin && !isTeacher) {
            throw new AuthException("Only admins or teachers can manage attendance.");
        }

        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new ValidationException("Lesson not found."));

        // Si es TEACHER pero NO ADMIN, debe ser el teacher de la lesson
        if (!isAdmin && isTeacher) {
            if (!lesson.getTeacherId().equals(currentUser.getId())) {
                throw new AuthException("You are not the teacher of this lesson.");
            }
        }

        return lesson;
    }
}
