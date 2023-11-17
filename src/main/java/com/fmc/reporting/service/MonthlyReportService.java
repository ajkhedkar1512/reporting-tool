package com.fmc.reporting.service;

import com.fmc.reporting.dto.FileDownloadDto;

public interface MonthlyReportService {

    FileDownloadDto export(String date);

}
