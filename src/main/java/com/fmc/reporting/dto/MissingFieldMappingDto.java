package com.fmc.reporting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingFieldMappingDto extends BaseDto {

    private String docId;

    private String loanType;

    private List<String> fields;
}
