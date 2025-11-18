package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LessonService {

    List<Lesson> getLessonsForUser(User user, Set<String> roles);

    void createLessonForUser(
            User user,
            Set<String> roles,
            UUID seriesId,
            UUID teacherId,
            String topic,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String room,
            String notes
    );

    void deleteLessonForUser(UUID lessonId, User user, Set<String> roles);
}
