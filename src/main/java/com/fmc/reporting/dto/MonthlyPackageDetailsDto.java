package com.fmc.reporting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPackageDetailsDto {

    private List<MonthlyReportDto> details;
}
