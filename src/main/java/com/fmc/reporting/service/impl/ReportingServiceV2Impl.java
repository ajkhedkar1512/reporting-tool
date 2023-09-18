package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.*;
import com.fmc.reporting.exception.BaseException;
import com.fmc.reporting.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReportingServiceV2Impl extends AbstractBaseService implements ReportingServiceV2 {

    private final MtdReportService mtdReportService;

    private final LoanDetailsReportService loanDetailsReportService;

    private final MissingReportService missingReportService;

    @Override
    public FileDownloadDto export(final String date) {
        final XSSFWorkbook workbook = new XSSFWorkbook();
        try  {
            final MTDStatusReport mtdStatusReportData = createMTDStatusReportData(workbook, date);
            List<String> dates = mtdStatusReportData.getDetails().stream().map(MTDStatusDto::getDate).collect(Collectors.toList());
            createLoanDetailsReportData(workbook, date, dates);
            createMissingDocReportData(workbook, date, dates);
            final StreamingResponseBody responseBody = outputStream -> {
                try (outputStream) {
                    workbook.write(outputStream);
                } catch (final IOException e) {
                   System.out.println(e.getMessage());
                } finally {
                    workbook.close();
                }
            };
            return FileDownloadDto.builder().data(responseBody).filename("Daily_Report"+date).build();
        } catch (final Exception e) {
            throw new BaseException(e);
        }
    }

    private void createMissingDocReportData(final XSSFWorkbook workbook, final String date, List<String> dates) {
        final MissingDocReport missingDocReport = missingReportService.build(date, dates);
        final Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("Missing Documents"));
        final CellStyle style =  getCellStyle(true, workbook);
        headerLineForMissingDoc(sheet, style);
        writeDataLineForMissingDocReport(workbook, sheet, missingDocReport);
    }

    private void headerLineForLoanStatus(final Sheet mtdStatusSheet, final CellStyle style) {
        final Row row = mtdStatusSheet.createRow(0);
        final var headers =  Arrays.asList("Date", "LoanId", "PackageId","Status", "Comments");
        IntStream.range(0, headers.size()).forEach(i -> createCell(row, i, headers.get(i), style));
    }

    private void headerLineForMissingDoc(final Sheet missingDocSheet, final CellStyle style) {
        final Row row = missingDocSheet.createRow(0);
        final var headers =  Arrays.asList("Date", "LoanId", "PackageId", "Status", "Missing documents details");
        IntStream.range(0, headers.size()).forEach(i -> createCell(row, i, headers.get(i), style));
    }

    private void headerLineForMTDStatus(final Sheet mtdStatusSheet, final CellStyle style) {
        final Row row = mtdStatusSheet.createRow(0);
        final var headers =  Arrays.asList("Date", "Carry Over (A)", "New Additions (B)", "Completed (C)",
                "Pending (D = A+B-C)", "Within SLA", "Outside SLA");
        IntStream.range(0, headers.size()).forEach(i -> createCell(row, i, headers.get(i), style));
    }

    private void writeDataLineForMissingDocReport(final XSSFWorkbook workbook, final Sheet mtdStatusSheet, final MissingDocReport exportData) {
        final AtomicInteger dataCounter = new AtomicInteger(1);
        final CellStyle dataStyle = getCellStyle(false, workbook);
        exportData.getMissingDocs().forEach(data -> {
            final Row row = mtdStatusSheet.createRow(dataCounter.getAndIncrement());
            final AtomicInteger column = new AtomicInteger(0);
            createCell(row, column.getAndIncrement(), data.getDate(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getLoanId(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getPackageId(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getStatus(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getMissingDocs(), dataStyle);
        });
        IntStream.range(0, 4).forEach(mtdStatusSheet::autoSizeColumn);
    }

    private void createLoanDetailsReportData(final XSSFWorkbook workbook,final String currentDate, final  List<String> dates) {
        final LoanDetailsReport loanDetailsReport = loanDetailsReportService.build(currentDate, dates);
        final Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("Loan Details"));
        final CellStyle style =  getCellStyle(true, workbook);
        headerLineForLoanStatus(sheet, style);
        writeDataLineForLoanDetailsReport(workbook, sheet, loanDetailsReport);
    }

    private void writeDataLineForLoanDetailsReport(final XSSFWorkbook workbook, final Sheet mtdStatusSheet, final LoanDetailsReport exportData) {
        final AtomicInteger dataCounter = new AtomicInteger(1);
        final CellStyle dataStyle = getCellStyle(false, workbook);
        exportData.getLoanDetails().forEach(data -> {
            final Row row = mtdStatusSheet.createRow(dataCounter.getAndIncrement());
            final AtomicInteger column = new AtomicInteger(0);
            createCell(row, column.getAndIncrement(), data.getDate(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getLoanId(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getPackageId(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getStatus(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getComment(), dataStyle);
        });
        IntStream.range(0, 4).forEach(mtdStatusSheet::autoSizeColumn);
    }

    private MTDStatusReport createMTDStatusReportData(final XSSFWorkbook workbook, final String date) {
        final MTDStatusReport mtdStatusReport = mtdReportService.build(date);
        final Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("MTD Status"));
        final CellStyle style =  getCellStyle(true, workbook);
        headerLineForMTDStatus(sheet, style);
        writeDataLineForMTDStatus(workbook, sheet, mtdStatusReport);
        return mtdStatusReport;
    }

    private void writeDataLineForMTDStatus(final XSSFWorkbook workbook, final Sheet mtdStatusSheet, final MTDStatusReport exportData) {
        final AtomicInteger dataCounter = new AtomicInteger(1);
        final CellStyle dataStyle = getCellStyle(false, workbook);
        exportData.getDetails().forEach(data -> {
            final Row row = mtdStatusSheet.createRow(dataCounter.getAndIncrement());
            final AtomicInteger column = new AtomicInteger(0);
            createCell(row, column.getAndIncrement(), data.getDate(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getCarryOver(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getNewAdditions(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getCompleted(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getPending(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getWithinSLA(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getOutsideSLA(), dataStyle);
        });
        IntStream.range(0, 7).forEach(mtdStatusSheet::autoSizeColumn);
    }

    private void createCell(final Row row, final int countColumn, final Object value, final CellStyle style) {
        final Cell cell = row.createCell(countColumn);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private CellStyle getCellStyle(final Boolean isBold, final XSSFWorkbook workbook) {
        final CellStyle dataStyle = workbook.createCellStyle();
        final XSSFFont font = workbook.createFont();
        font.setFontHeight(12);
        font.setBold(isBold);
        dataStyle.setFont(font);
        dataStyle.setBorderBottom(BorderStyle.MEDIUM);
        dataStyle.setBorderTop(BorderStyle.MEDIUM);
        dataStyle.setBorderRight(BorderStyle.MEDIUM);
        dataStyle.setBorderLeft(BorderStyle.MEDIUM);
        return dataStyle;
    }

}
