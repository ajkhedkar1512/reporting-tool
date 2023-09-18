package com.fmc.reporting.dto;

import lombok.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalServiceDtos extends BaseDto {

    private String screen;
    @Singular
    private List<ExternalServiceDto> details;

    @Builder.Default
    private Boolean stitchingRequired = Boolean.FALSE;

    private String channel;

    private String division;

    private String packageCreatedDate;

    private String processingEngine;

}
