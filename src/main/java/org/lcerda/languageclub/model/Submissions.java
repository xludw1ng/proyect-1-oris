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
public class Submissions {
    private UUID id;
    private UUID assignmentId;
    private UUID userId;
    private short statusId;
    private OffsetDateTime submittedAt;
    private OffsetDateTime gradedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String textAnswer;
    private String attachmentPath;
    private Integer grade;
}
