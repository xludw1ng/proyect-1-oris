package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserLessonService {

    Lesson loadLessonIfAuthorized(User currentUser, Set<String> roles, UUID lessonId);

    List<User> getEligibleStudents();  // usually active STUDENTs

    Set<UUID> getEnrolledStudentIds(UUID lessonId);

    void saveEnrollments(User currentUser, Set<String> roles,
                         UUID lessonId, Set<UUID> studentIds);
}
