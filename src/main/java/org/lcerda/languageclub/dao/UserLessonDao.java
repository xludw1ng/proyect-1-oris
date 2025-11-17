package org.lcerda.languageclub.dao;

import java.util.Set;
import java.util.UUID;

public interface UserLessonDao {
    Set<UUID> findStudentIdsByLesson(UUID lessonId);
    void replaceEnrollments(UUID lessonId, Set<UUID> studentIds);
}
