package com.fmc.reporting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingDocReport {

    private List<MissingDocDto> missingDocs;
}
