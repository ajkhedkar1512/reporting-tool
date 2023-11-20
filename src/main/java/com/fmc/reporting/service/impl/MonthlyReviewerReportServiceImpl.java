package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.FileDownloadDto;
import com.fmc.reporting.dto.MonthlyPackageDetailsDto;
import com.fmc.reporting.dto.MonthlyReviewerDetailsDto;
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
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MonthlyReviewerReportServiceImpl extends AbstractBaseService implements MonthlyReviewerReportService {

    private final MonthlyReviewerHistoryService monthlyReviewerHistoryService;

    @Override
    public FileDownloadDto export(String month) {
        final XSSFWorkbook workbook = new XSSFWorkbook();
        try  {
            createReportData(workbook, month);
            final StreamingResponseBody responseBody = outputStream -> {
                try (outputStream) {
                    workbook.write(outputStream);
                } catch (final IOException e) {
                    System.out.println(e.getMessage());
                } finally {
                    workbook.close();
                }
            };
            return FileDownloadDto.builder().data(responseBody).filename("Monthly_Reviewer_Report_"+month).build();
        } catch (final Exception e) {
            throw new BaseException(e);
        }
    }

    private void createReportData(final XSSFWorkbook workbook, final String month) {
        final List<MonthlyReviewerDetailsDto> monthlyPkg = monthlyReviewerHistoryService.build(month);
        final Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("Monthly Reviewer Details"));
        final CellStyle style =  getCellStyle(true, workbook);
        headerLine(sheet, style);
        writeDataLine(workbook, sheet, monthlyPkg);
    }

    private void headerLine(final Sheet mtdStatusSheet, final CellStyle style) {
        final Row row = mtdStatusSheet.createRow(0);
        final var headers =  Arrays.asList("Date", "Reviewer", "Stage", "Count");
        IntStream.range(0, headers.size()).forEach(i -> createCell(row, i, headers.get(i), style));
    }

    private void writeDataLine(final XSSFWorkbook workbook, final Sheet sheet, final List<MonthlyReviewerDetailsDto> exportData) {
        final AtomicInteger dataCounter = new AtomicInteger(1);
        final CellStyle dataStyle = getCellStyle(false, workbook);
        exportData.forEach(data -> {
            final Row row = sheet.createRow(dataCounter.getAndIncrement());
            final AtomicInteger column = new AtomicInteger(0);
            createCell(row, column.getAndIncrement(), data.getDate(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getReviewer(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getStage(), dataStyle);
            createCell(row, column.getAndIncrement(), data.getCount(), dataStyle);
        });
        IntStream.range(0, 3).forEach(sheet::autoSizeColumn);
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
