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
public class Assignment {
    private UUID id;
    private UUID lessonId;
    private short statusId;
    private String title;
    private String description;
    private OffsetDateTime dueAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
