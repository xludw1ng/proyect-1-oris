package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Submissions;
import org.lcerda.languageclub.model.SubmissionsStatus;
import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SubmissionService {

    Submissions getSubmissionForStudent(UUID assignmentId, User currentUser);

    void submitAssignment(UUID assignmentId, User currentUser, Set<String> roles, String textAnswer, String attachmentPath);

    List<Submissions> getSubmissionsForAssignment(UUID assignmentId, User currentUser, Set<String> roles);

    // usamos tu modelo SubmissionsStatus
    List<SubmissionsStatus> getAllStatuses();

    // 🔹 nuevo: poner/quitar nota
    void gradeSubmission(UUID assignmentId, UUID studentId, User currentUser, Set<String> roles, Integer grade);
}
