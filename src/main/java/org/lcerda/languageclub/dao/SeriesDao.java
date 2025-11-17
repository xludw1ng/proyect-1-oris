package org.lcerda.languageclub.dao;

import org.lcerda.languageclub.model.Series;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeriesDao {
    List<Series> findAll();

    Optional<Series> findById(UUID id);

    Optional<Series> findByCode(String code);

    UUID create(Series series);

    List<Series> findByTeacherId(UUID teacherId);

    List<Series> findByStudentId(UUID studentId);

    boolean deleteById(UUID id);

}
