package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.DocumentDetailsDto;
import com.fmc.reporting.dto.LoanDetailsDto;
import com.fmc.reporting.dto.LoanDetailsReport;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.LoanDetailsReportService;
import com.fmc.reporting.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fmc.reporting.utils.DateTimeUtils.minusDays;
import static com.fmc.reporting.utils.DateTimeUtils.plusDays;

@Service
@RequiredArgsConstructor
public class LoanDetailsReportServiceImpl extends AbstractBaseService implements LoanDetailsReportService {

    private final DocumentDetailsService documentDetailsService;


    @Override
    public LoanDetailsReport build(final String currentDate, final List<String> previousDates) {
        final List<DocumentDetailsDto> docDetails = new ArrayList<>();
        previousDates.stream()
                .forEach(date -> {
                    if(DateTimeUtils.isFriday(date)) {
                        String getMonday = DateTimeUtils.getMondayDate(date);
                        docDetails.addAll(documentDetailsService.getAllDocumentsBetweenDate(minusDays(date), plusDays(date))) ;
                    } else {
                        docDetails.addAll(documentDetailsService.getAllDocumentsBetweenDate(minusDays(date), date)) ;
                    }

                        }
                        );
        docDetails.addAll(documentDetailsService.getAllDocumentsForDate(currentDate));
        Set<DocumentDetailsDto> docSet = new HashSet<>(docDetails);
        final List<LoanDetailsDto> loanDetails = new ArrayList<>();
        docDetails.forEach(doc -> {
            final String date = DateTimeUtils.getFormattedDate(doc.getPackageCreatedDate());
            if (((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3)) ||
                    doc.getStageId().equals(6)) {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).status("Completed").build());
            } else if ((doc.getStageId().equals(4) || doc.getStageId().equals(5))
                    && (doc.getUserDocStatusId().equals(1) || doc.getUserDocStatusId().equals(2) || doc.getUserDocStatusId().equals(4))){
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).status("Ready for review").build());
            } else if (doc.getStageId().equals(99)) {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).status("Failed").comment(doc.getErrorMessage()).build());
            } else {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).status("In process").build());
            }
        });
        return LoanDetailsReport.builder().loanDetails(loanDetails).build();
    }

}
