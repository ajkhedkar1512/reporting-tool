package com.fmc.reporting.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MTDStatusReport extends BaseDto {

    private List<MTDStatusDto> details = new ArrayList<>();

    private Integer count;
}
