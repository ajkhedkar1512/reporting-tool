package com.fmc.reporting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingDocDto {

    private String date;
    private String loanId;
    private String packageId;
    private String status;
    private String comment;
    private String missingDocs;
}
