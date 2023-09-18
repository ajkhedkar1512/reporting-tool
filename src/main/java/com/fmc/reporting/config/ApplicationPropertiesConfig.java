package com.fmc.reporting.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "reporting-tool")
@Getter
@Setter
public class ApplicationPropertiesConfig {

    private String region;

    private String bucket;

    private String dareFolderPath;

}
