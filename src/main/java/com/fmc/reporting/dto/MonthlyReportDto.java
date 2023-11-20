package com.fmc.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDto {

    private String date;

    private Integer count;

    private Integer failedCount;
}
