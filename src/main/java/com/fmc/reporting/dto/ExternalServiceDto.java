package com.fmc.reporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExternalServiceDto extends BaseDto {

    private String uniqueDocumentId;

    private String packageId;

    private String loanNumber;

    private String docPath;

    private Integer numberOfPages;

    private String docId;

    private String docName;

    private String bucketName;

    private String docGuid;

    private String parentDocGuid;

    private List<KeywordsDto> keywords;

    private String icrMode;

    private Integer docIndex;

    private List<PageDataDto> pageDetails = new ArrayList<>();

}
