package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Submissions;
import org.lcerda.languageclub.model.SubmissionsStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionsDao {

    List<Submissions> findByAssignmentId(UUID assignmentId);

    Optional<Submissions> findByAssignmentIdAndUserId(UUID assignmentId, UUID userId);

    // crea o actualiza (ON CONFLICT) una entrega
    void upsert(Submissions submission);

    void updateStatusAndGrade(UUID submissionId, short statusId, Integer grade);

    List<SubmissionsStatus> findAllStatuses();
}
