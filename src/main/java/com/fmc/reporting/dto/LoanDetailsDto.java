package com.fmc.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailsDto {

    private String date;
    private String loanId;
    private String packageId;
    private String status;
    private String comment;
}
