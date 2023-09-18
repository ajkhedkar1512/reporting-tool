package com.fmc.reporting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDto {

    private String id;

    private Integer status;

    private String createdOn;

    private String createdBy;

    private String modifiedOn;

    private String modifiedBy;

    private Integer version;
}
