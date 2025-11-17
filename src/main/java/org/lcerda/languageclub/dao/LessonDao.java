package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Lesson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonDao {
    List<Lesson> findAll();
    List<Lesson> findByTeacherId(UUID teacherId);
    List<Lesson> findByStudentId(UUID studentId);
    Optional<Lesson> findById(UUID id);
    boolean deleteById(UUID id);
    boolean existsBySeriesIdAndTeacherId(UUID seriesId, UUID teacherId);
    UUID create(Lesson lesson);
}
