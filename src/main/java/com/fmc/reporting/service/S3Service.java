package com.fmc.reporting.service;

import java.io.InputStream;

public interface S3Service {

    InputStream download(String path);
}
