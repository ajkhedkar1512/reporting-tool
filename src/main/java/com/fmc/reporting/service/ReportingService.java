package com.fmc.reporting.service;

import com.fmc.reporting.dto.LoanDetailsReport;
import com.fmc.reporting.dto.MTDStatusReport;
import com.fmc.reporting.dto.MissingDocReport;

public interface ReportingService {

    MTDStatusReport buildMTDStatusReport(String date);

    LoanDetailsReport buildLoanDetailReport(String date);

    MissingDocReport buildMissingDocReport(String date);

}


