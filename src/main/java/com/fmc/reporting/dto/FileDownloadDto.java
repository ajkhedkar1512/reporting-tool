package com.fmc.reporting.dto;

import lombok.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDownloadDto {

    private StreamingResponseBody data;

    private String filename;
}
