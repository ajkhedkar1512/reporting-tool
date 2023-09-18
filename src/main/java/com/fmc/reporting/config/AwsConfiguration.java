package com.fmc.reporting.config;

import com.fmc.reporting.constants.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.AttributeMap;

@Configuration
public class AwsConfiguration  {

    @Bean
    public S3Client buildS3Client() {
        final AttributeMap attributeMap = AttributeMap.builder()
                .put(SdkHttpConfigurationOption.MAX_CONNECTIONS, Constants.S3_MAX_CONNECTION).build();
        final SdkHttpClient httpClient = new DefaultSdkHttpClientBuilder().buildWithDefaults(attributeMap);
        return S3Client.builder().httpClient(httpClient).build();
    }

}
