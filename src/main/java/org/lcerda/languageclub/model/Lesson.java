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
public class Lesson {
    private UUID id;
    private UUID teacherId;
    private UUID seriesId;
    private String topic;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String room;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
