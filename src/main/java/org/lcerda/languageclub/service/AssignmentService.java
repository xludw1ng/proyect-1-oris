package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.AssignmentStatus;
import org.lcerda.languageclub.model.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AssignmentService {

    List<Assignment> getAssignmentsForLesson(UUID lessonId, User currentUser, Set<String> roles);

    UUID createAssignmentForLesson(UUID lessonId, User currentUser, Set<String> roles, String title, String description, OffsetDateTime dueAt);

    void changeStatus(UUID assignmentId, short newStatusId, User currentUser, Set<String> roles);

    Assignment getAssignmentForUser(UUID assignmentId, User currentUser, Set<String> roles);

    List<AssignmentStatus> getAllStatuses();
}
