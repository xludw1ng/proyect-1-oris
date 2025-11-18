package org.lcerda.languageclub.service.impl;

import lombok.AllArgsConstructor;
import org.lcerda.languageclub.dao.SeriesDao;
import org.lcerda.languageclub.model.Series;
import org.lcerda.languageclub.model.User;
import org.lcerda.languageclub.service.SeriesService;
import org.lcerda.languageclub.service.ValidationException;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesDao seriesDao;

    @Override
    public List<Series> getSeriesForUser(User user, Set<String> roles) {
        if (user ==null){
            throw new IllegalArgumentException("User cannot be null");
        }

        if (roles == null) {
            roles = Set.of();
            //set inmutable .of
        }

        if (roles.contains("ADMIN")) {
            return seriesDao.findAll();
        } else if (roles.contains("TEACHER")) {
            return seriesDao.findByTeacherId(user.getId());
        } else {
            return seriesDao.findByStudentId(user.getId());
        }
    }

    @Override
    public void createSeries(String code, String language, String level, String title, String description) {
        String c = code != null ? code.trim() : "";
        String lang = language != null ? language.trim() : "";
        String lvl = level != null ? level.trim() : "";
        String t = title != null ? title.trim() : "";
        String desc = (description != null && !description.isBlank())
                ? description.trim()
                : null;

        if (c.isBlank() || lang.isBlank() || lvl.isBlank() || t.isBlank()) {
            throw new ValidationException("Code, language, level and title are required.");
        }

        if (c.length() > 64) {
            throw new ValidationException("Code is too long (max 64 chars).");
        }

        Series series = Series.builder()
                .code(c)
                .language(lang)
                .level(lvl)
                .title(t)
                .description(desc)
                .build();

        seriesDao.create(series);
    }
}
