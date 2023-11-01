package com.fmc.reporting.document;

import com.fmc.reporting.constants.MongoCollections;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = MongoCollections.MISSING_REPORT_DOCS)
public class PresentDocId extends BaseDocument
{
    private String loanNumber;

    private String date;

    private String missingDoc;
}
