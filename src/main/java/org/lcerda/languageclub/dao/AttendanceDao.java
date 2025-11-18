package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Attendance;
import org.lcerda.languageclub.model.AttendanceStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceDao {


    List<Attendance> findByLessonId(UUID lessonId);

    // insetar o actulizar la asistencia
    void upsert(Attendance attendance);

    List<AttendanceStatus> findAllStatuses();
}
