package com.fmc.reporting.service;

import com.fmc.reporting.dto.LoanDetailsReport;

import java.util.List;

public interface LoanDetailsReportService {

    LoanDetailsReport build(String currentDate, List<String> previousDates);
}
