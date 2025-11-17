package org.lcerda.languageclub.dao;

import java.util.Set;
import java.util.UUID;

public interface AssignmentAssigneeDao {
    Set<UUID> findUserIdsByAssignment(UUID assignmentId);

    void replaceAssignees(UUID assignmentId, Set<UUID> userIds);

    boolean isUserAssignee(UUID assignmentId, UUID userId);
}
