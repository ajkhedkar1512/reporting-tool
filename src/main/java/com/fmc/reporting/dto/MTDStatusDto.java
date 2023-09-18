package com.fmc.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MTDStatusDto extends BaseDto {

    private String date;

    private Integer carryOver;

    private Integer newAdditions;

    private Integer completed;

    private Integer pending;

    private Integer withinSLA;

    private Integer outsideSLA;
}
