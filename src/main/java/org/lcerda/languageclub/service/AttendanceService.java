package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Attendance;
import org.lcerda.languageclub.model.AttendanceStatus;
import org.lcerda.languageclub.model.Lesson;
import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AttendanceService {

    List<Attendance> getAttendanceForLesson(UUID lessonId, User currentUser, Set<String> roles);
    void saveAttendanceForLesson(UUID lessonId, Map<UUID, Short> statusByUserId, Map<UUID, String> commentByUserId, User currentUser, Set<String> roles);
    List<AttendanceStatus> getAllStatuses();
    Lesson loadLessonIfAuthorized(UUID lessonId, User currentUser, Set<String> roles);
}
