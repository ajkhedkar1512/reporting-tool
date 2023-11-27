package com.fmc.reporting.document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewerHistory {

    private String stage;

    private Integer stageId;

    private String reviewer;

    private String startedOn;

    private String completedOn;
}
