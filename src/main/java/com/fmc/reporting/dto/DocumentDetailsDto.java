package com.fmc.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetailsDto extends BaseDto {

    private String loanNumber;

    private String packageId;

    private String uniqueDocumentId;

    private String packageCreatedDate;

    private String sourceFileName;

    private String sourceSystemName;

    private String documentPath;

    private String stage;

    private Integer stageId;

    private String systemDocStatus;

    private Integer systemDocStatusId;

    private String userDocStatus;

    private Integer userDocStatusId;

    private Integer pageCount;

    private Integer icrBatchPriority;

    private String icrMode;

    private String icrQueue;

    private String icrQueueCode;

    private String classificationEndedOn;

    private String extractionEndedOn;

    private String errorMessage;

    private String startedOn;

    private String endedOn;

    private Integer count;

    private String reviewer;

}
