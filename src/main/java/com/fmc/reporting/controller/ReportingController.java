package com.fmc.reporting.controller;

import com.fmc.reporting.dto.FileDownloadDto;
import com.fmc.reporting.dto.LoanDetailsReport;
import com.fmc.reporting.dto.MTDStatusReport;
import com.fmc.reporting.dto.MissingDocReport;
import com.fmc.reporting.service.MonthlyReportService;
import com.fmc.reporting.service.ReportingService;
import com.fmc.reporting.service.ReportingServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/v1/report/")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    private final ReportingServiceV2 reportingServiceV2;

    private final MonthlyReportService monthlyReportService;

    @GetMapping(value = "MTDStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public MTDStatusReport buildMTDStatusReport(final @RequestParam(name = "date") String date) {
        return reportingService.buildMTDStatusReport(date);
    }

    @GetMapping(value = "loanStatusReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoanDetailsReport buildLoanStatusReportReport(final @RequestParam(name = "date") String date) {
        return reportingService.buildLoanDetailReport(date);
    }

    @GetMapping(value = "missingDocReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public MissingDocReport buildMissingDocReport(final @RequestParam(name = "date") String date) {
        return reportingService.buildMissingDocReport(date);
    }

    @GetMapping(value =  "export")
    public ResponseEntity<StreamingResponseBody> export(final @RequestParam(name = "date") String date,
                                                        final HttpServletResponse response) {
        final FileDownloadDto fileResponse = reportingServiceV2.export(date);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResponse.getFilename() + ".xlsx");
        return new ResponseEntity<>(fileResponse.getData(), HttpStatus.OK);
    }

    @GetMapping(value =  "monthly/export")
    public ResponseEntity<StreamingResponseBody> exportMonthlyDate(final @RequestParam(name = "month") String month,
                                                        final HttpServletResponse response) {
        final FileDownloadDto fileResponse = monthlyReportService.export(month);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResponse.getFilename() + ".xlsx");
        return new ResponseEntity<>(fileResponse.getData(), HttpStatus.OK);
    }
}
