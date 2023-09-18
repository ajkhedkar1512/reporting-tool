package com.fmc.reporting.service;

import com.fmc.reporting.dto.FileDownloadDto;

public interface ReportingServiceV2  {

    FileDownloadDto export(String date);
}
