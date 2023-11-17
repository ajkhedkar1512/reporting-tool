package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.MonthlyPackageDetailsDto;
import com.fmc.reporting.dto.MonthlyReportDto;
import com.fmc.reporting.enums.MonthEnum;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.MonthlyPkgDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Service
@RequiredArgsConstructor
public class MonthlyPkgDetailsServiceImpl  extends AbstractBaseService implements MonthlyPkgDetailsService {

    private final DocumentDetailsService documentDetailsService;

    @Override
    public MonthlyPackageDetailsDto build(final String month) {
        List<MonthlyReportDto> data = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of( 2023, MonthEnum.getMonthIndex(month));
        LocalDate firstOfMonth = yearMonth.atDay( 1 );
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();
        System.out.println(firstOfMonth);
        System.out.println(lastOfMonth);
        AtomicReference<Integer> totalCount = new AtomicReference<>(0);
        firstOfMonth.datesUntil(lastOfMonth.plusDays(1)).forEach(date -> {
            Integer completedPackages = documentDetailsService.getAllCompletedPackages(date.toString());
            System.out.println(date + " = " +completedPackages);
            totalCount.set(totalCount.get() + completedPackages);
            Integer failedCount = documentDetailsService.getFailedPackageCount(date.toString());
            System.out.println("Failed Count " + failedCount);
            data.add(MonthlyReportDto.builder().date(date.toString()).count(completedPackages).failedCount(failedCount).build());
        });
        System.out.println(totalCount);
        return MonthlyPackageDetailsDto.builder().details(data).build();
    }
}
