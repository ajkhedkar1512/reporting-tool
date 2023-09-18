package com.fmc.reporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageDataDto {

    private String uniquePageId;

    private String pagePath;

    private Integer pageNumber;

    private Boolean isCluster;

    private String pageBoundaries;

    private String pageUnit;

    private Boolean isDeleted;
}
