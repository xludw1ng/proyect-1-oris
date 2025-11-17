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
public class Attendance {
    private UUID lessonId;
    private UUID userId;
    private short statusId;
    private OffsetDateTime attendedAt;
    private OffsetDateTime createdAt;
    private String comment;
}