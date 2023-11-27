package com.fmc.reporting.service;

import com.fmc.reporting.dto.FileDownloadDto;

public interface MonthlyReviewerReportService {

    FileDownloadDto export(String month);
}
