package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Assignment;
import org.lcerda.languageclub.model.AssignmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentDao {

    List<Assignment> findByLessonId(UUID lessonId);

    Optional<Assignment> findById(UUID id);

    UUID create(Assignment assignment);

    void updateStatus(UUID assignmentId, short statusId);

    List<AssignmentStatus> findAllStatuses();
}
