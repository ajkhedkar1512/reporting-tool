package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.DocumentDetailsDto;
import com.fmc.reporting.dto.MonthlyReportDto;
import com.fmc.reporting.dto.MonthlyReviewerDetailsDto;
import com.fmc.reporting.enums.MonthEnum;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.MonthlyReviewerHistoryService;
import com.fmc.reporting.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyReviewerHistoryServiceImpl extends AbstractBaseService implements MonthlyReviewerHistoryService {

    private final DocumentDetailsService documentDetailsService;

    @Override
    public List<MonthlyReviewerDetailsDto> build(String month) {
        List<MonthlyReviewerDetailsDto> data = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of( 2023, MonthEnum.getMonthIndex(month));
        LocalDate firstOfMonth = yearMonth.atDay( 1 );
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();
        System.out.println(firstOfMonth);
        System.out.println(lastOfMonth);
        firstOfMonth.datesUntil(lastOfMonth.plusDays(1)).forEach(date -> {
            List<DocumentDetailsDto> allReviewerHistoryByDate = documentDetailsService.findAllReviewerHistoryByDate(date.toString(), date.toString());
            allReviewerHistoryByDate.forEach(rev -> data.add(MonthlyReviewerDetailsDto.build(rev, date.toString())));
        });
        /*List<DocumentDetailsDto> allReviewerHistoryByDate = documentDetailsService.findAllReviewerHistoryByDate(firstOfMonth.toString(),
                lastOfMonth.toString());
        allReviewerHistoryByDate.forEach(rev -> data.add(MonthlyReviewerDetailsDto.build(rev)));*/
        return data;
    }
}
