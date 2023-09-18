package com.fmc.reporting.document;

import com.fmc.reporting.constants.MongoCollections;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = MongoCollections.DOCUMENT_DETAILS)
public class DocumentDetails extends BaseDocument {

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

}
