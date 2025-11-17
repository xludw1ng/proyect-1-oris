package org.lcerda.languageclub.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Series {
    private UUID id;
    private String code;
    private String language;
    private String level;
    private String title;
    private String description;
    private OffsetDateTime createdAt;
}
