package com.fmc.reporting.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Getter
@Setter
public class BaseDocument {

    @Id
    private String id;

    private Integer status;

    private String createdBy;

    private String modifiedBy;

    private String createdOn;

    private String modifiedOn;

    @Version
    private Integer version;
}
