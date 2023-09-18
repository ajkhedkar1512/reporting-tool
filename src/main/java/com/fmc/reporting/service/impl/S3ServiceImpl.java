package com.fmc.reporting.service.impl;

import com.fmc.reporting.config.ApplicationPropertiesConfig;
import com.fmc.reporting.dto.BaseDto;
import com.fmc.reporting.exception.BaseException;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl extends AbstractBaseService implements S3Service {

    private final S3Client amazonS3;

    @Override
    public InputStream download(final String path) {
        try {
            System.out.println("************************ " + path);
            final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(appProperties.getBucket()).key(path).build();
            return amazonS3.getObject(getObjectRequest);
        } catch (final Exception ex) {
            throw new BaseException("Error occurred while downloading Paged data from s3.", ex);
        }
    }
}
