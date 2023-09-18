package com.fmc.reporting.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailsReport {

   private List<LoanDetailsDto> loanDetails = new ArrayList<>();

   private Integer count;
}
