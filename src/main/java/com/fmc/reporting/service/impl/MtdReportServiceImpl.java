package com.fmc.reporting.service.impl;

import com.fmc.reporting.document.MtdReportDetails;
import com.fmc.reporting.dto.DocumentDetailsDto;
import com.fmc.reporting.dto.MTDStatusDto;
import com.fmc.reporting.dto.MTDStatusReport;
import com.fmc.reporting.repo.MtdReportRepo;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.MtdReportService;
import com.fmc.reporting.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fmc.reporting.utils.DateTimeUtils.isWeekend;

@Service
@RequiredArgsConstructor
public class MtdReportServiceImpl extends AbstractBaseService implements MtdReportService {

    private final DocumentDetailsService documentDetailsService;

    private final MtdReportRepo repo;

    @Override
    public MTDStatusReport build(final String currentDate) {
        final List<MtdReportDetails> details = repo.findAll();
        AtomicInteger previousPending = new AtomicInteger();
        AtomicInteger carryOverCompleted = new AtomicInteger();
        details.stream().reduce((one, two) -> two).ifPresent(notCompletedRecords -> {
            if (!notCompletedRecords.getIsAllCompleted()) {
                previousPending.set(notCompletedRecords.getPending());
                final List<DocumentDetailsDto> docDetails = documentDetailsService
                        .getAllDocumentsForCurrentDate(notCompletedRecords.getDate());
                System.out.println("***************************** Total Size " + docDetails.size());
                final int completedCount = (int) docDetails.stream()
                        .filter(doc -> ((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3) ||
                                doc.getStageId().equals(6))).count();
                System.out.println("***************************** Total Completed Size " + completedCount);
                if (notCompletedRecords.getNewAdditions() == completedCount) {
                    repo.findByDate(notCompletedRecords.getDate()).ifPresent(dataByDate -> {
                        dataByDate.setIsAllCompleted(true);
                        repo.save(dataByDate);
                        carryOverCompleted.set(notCompletedRecords.getPending());
                    });
                }
            }});
        final String fromDate = DateTimeUtils.minusDays(currentDate);
        if (repo.findByDate(fromDate).isEmpty()) {
            final List<DocumentDetailsDto> docDetails = documentDetailsService.getAllDocumentsBetweenDate(fromDate, currentDate);
            final Integer completedCount = documentDetailsService.getAllCompleted(currentDate);
            int carryOver = documentDetailsService.getAllDocumentsNotCompleted(fromDate) + previousPending.get();
            int newAdditions = docDetails.size();
            int completed = completedCount +  carryOverCompleted.get();
            int pending = carryOver + newAdditions - completed;
            int withinSLA = completedCount;
            int outsideSLA = isWeekend(fromDate) ? 0 :  carryOverCompleted.get();
            repo.save(MtdReportDetails.builder().date(fromDate).newAdditions(newAdditions).completed(completed)
                    .carryOver(carryOver).pending(pending).withinSLA(withinSLA).outsideSLA(outsideSLA)
                    .isAllCompleted(newAdditions == completed).build());
        }
        final List<MTDStatusDto> statusDetails =  repo.findAll().stream()
                .map(data -> (MTDStatusDto) convertToDTO(data, MTDStatusDto.class)).collect(Collectors.toList());
        return MTDStatusReport.builder().details(statusDetails).count(statusDetails.size()).build();
    }
}
