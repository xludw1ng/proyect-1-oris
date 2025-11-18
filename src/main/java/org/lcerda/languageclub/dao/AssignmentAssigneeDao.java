package org.lcerda.languageclub.dao;

import java.util.Set;
import java.util.UUID;

public interface AssignmentAssigneeDao {

    void replaceAssignees(UUID assignmentId, Set<UUID> userIds);

    boolean isUserAssignee(UUID assignmentId, UUID userId);
}
