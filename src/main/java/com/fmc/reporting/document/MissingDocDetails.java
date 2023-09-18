package com.fmc.reporting.document;

import com.fmc.reporting.constants.MongoCollections;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = MongoCollections.MISSING_REPORT)
public class MissingDocDetails {

    private String date;
    private String loanId;
    private String packageId;
    private String status;
    private String comment;
    private List<String> missingDocs;
}
