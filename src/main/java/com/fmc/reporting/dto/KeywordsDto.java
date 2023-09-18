package com.fmc.reporting.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordsDto {

    private String keywordName;

    private String keywordValue;

    private Boolean isKeyword;

}
