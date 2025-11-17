package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.LessonDao;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.AuthException;
import org.lcerda.languageclub.service.LessonService;
import org.lcerda.languageclub.service.ValidationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonDao lessonDao;

    @Override
    public List<Lesson> getLessonsForUser(User user, Set<String> roles) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (roles == null) {
            roles = Set.of();
            //set inmutable .of
        }

        if (roles.contains("ADMIN")) {
            return lessonDao.findAll();
        } else if (roles.contains("TEACHER")) {
            return lessonDao.findByTeacherId(user.getId());
        } else {
            return lessonDao.findByStudentId(user.getId());
        }
    }


    @Override
    public UUID createLessonForUser(
            User user,
            Set<String> roles,
            UUID seriesId,
            UUID teacherId,
            String topic,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String room,
            String notes
    ) {
        if (user == null) {
            throw new AuthException("You must be logged in.");
        }
        if (roles == null) {
            roles = Set.of();
        }
        if (seriesId == null) {
            throw new ValidationException("Series is required.");
        }
        if (teacherId == null) {
            throw new ValidationException("Teacher is required.");
        }
        if (topic == null || topic.isBlank()) {
            throw new ValidationException("Topic is required.");
        }
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new ValidationException("Invalid start/end time.");
        }

        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        // Reglas de permisos:
        if (isAdmin) {
            // Admin puede crear para cualquier teacher y cualquier serie
        } else if (isTeacher) {
            // Teacher solo puede crear para sí mismo
            if (!teacherId.equals(user.getId())) {
                throw new AuthException("Teachers can only create lessons for themselves.");
            }

            // y solo en series donde ya tenga al menos una lección (asignado)
            boolean assigned = lessonDao.existsBySeriesIdAndTeacherId(seriesId, user.getId());
            if (!assigned) {
                throw new AuthException("You are not assigned to this course (series).");
            }
        } else {
            throw new AuthException("You do not have permission to create lessons.");
        }

        Lesson lesson = Lesson.builder()
                .seriesId(seriesId)
                .teacherId(teacherId)
                .topic(topic.trim())
                .startsAt(startsAt)
                .endsAt(endsAt)
                .room((room != null && !room.isBlank()) ? room.trim() : null)
                .notes((notes != null && !notes.isBlank()) ? notes.trim() : null)
                .build();

        return lessonDao.create(lesson);
    }

    @Override
    public void deleteLessonForUser(UUID lessonId, User user, Set<String> roles) {
        if (lessonId == null) {
            throw new ValidationException("Lesson id is required.");
        }
        if (user == null) {
            throw new AuthException("You must be logged in.");
        }
        if (roles == null) roles = Set.of();

        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new ValidationException("Lesson not found."));

        boolean isAdmin = roles.contains("ADMIN");
        boolean isTeacher = roles.contains("TEACHER");

        if (!(isAdmin || (isTeacher && lesson.getTeacherId().equals(user.getId())))) {
            throw new AuthException("You do not have permission to delete this lesson.");
        }

        boolean deleted = lessonDao.deleteById(lessonId);
        if (!deleted) {
            throw new RuntimeException("Lesson could not be deleted.");
        }
    }
}


