package com.fmc.reporting.document;

import com.fmc.reporting.constants.MongoCollections;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = MongoCollections.MTD_REPORT)
public class MtdReportDetails extends BaseDocument {

    private String date;

    private Integer carryOver;

    private Integer newAdditions;

    private Integer completed;

    private Integer pending;

    private Integer withinSLA;

    private Integer outsideSLA;

    private Boolean isAllCompleted;
}
