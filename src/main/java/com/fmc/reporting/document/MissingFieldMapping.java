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
@Document(collection = MongoCollections.MISSING_FIELD_MAPPING)
public class MissingFieldMapping extends BaseDocument {

    private String docId;

    private String loanType;

    private List<String> fields;
}
