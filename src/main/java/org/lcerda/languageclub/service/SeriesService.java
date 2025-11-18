package org.lcerda.languageclub.service;

import org.lcerda.languageclub.model.Series;
import org.lcerda.languageclub.model.User;

import java.util.List;
import java.util.Set;

public interface SeriesService {
    List<Series> getSeriesForUser(User user, Set<String> roles);
    void createSeries(String code, String language, String level, String title, String description);
}
