package com.fmc.reporting.service;

import com.fmc.reporting.dto.MTDStatusReport;

import java.util.List;

public interface MtdReportService {

    MTDStatusReport build(String date);
}
