package com.fmc.reporting.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmc.reporting.document.MtdReportDetails;
import com.fmc.reporting.dto.*;
import com.fmc.reporting.enums.ReqDocsLoanEnum;
import com.fmc.reporting.repo.MtdReportRepo;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.ReportingService;
import com.fmc.reporting.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fmc.reporting.utils.DateTimeUtils.isWeekend;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl extends AbstractBaseService implements ReportingService {

    private final DocumentDetailsService documentDetailsService;

    private final MtdReportRepo repo;

    private final S3Service s3Service;

    @Override
    public MTDStatusReport buildMTDStatusReport(final String date) {
        final List<MtdReportDetails> details = repo.findAll();
        int previousPending = 0;
        AtomicInteger carryOverCompleted = new AtomicInteger();
        MtdReportDetails notCompletedRecords = details.stream().reduce((one, two) -> two).get();
        if (!ObjectUtils.isEmpty(notCompletedRecords) && !notCompletedRecords.getIsAllCompleted()) {
            previousPending = notCompletedRecords.getPending();
            System.out.println("*****************************");
            final List<DocumentDetailsDto> docDetails = documentDetailsService.getAllDocumentsForCurrentDate(notCompletedRecords.getDate());
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
        }
        final String fromDate = minusDays(date);
        if (repo.findByDate(fromDate).isEmpty()) {
            final List<DocumentDetailsDto> docDetails = documentDetailsService.getAllDocumentsBetweenDate(fromDate, date);
            final Integer completedCount = documentDetailsService.getAllCompleted(date);
            int carryOver = documentDetailsService.getAllDocumentsNotCompleted(fromDate) + previousPending;
            int newAdditions = docDetails.size();
            int completed =completedCount +  carryOverCompleted.get();
            int pending = carryOver + newAdditions - completed;
            int withinSLA = completedCount;
            int outsideSLA = isWeekend(fromDate) ? 0 : carryOver +  carryOverCompleted.get();
            repo.save(MtdReportDetails.builder().date(fromDate).newAdditions(newAdditions).completed(completed)
                    .carryOver(carryOver).pending(pending).withinSLA(withinSLA).outsideSLA(outsideSLA)
                    .isAllCompleted(newAdditions == completed).build());
        }
        final List<MTDStatusDto> statusDetails =  repo.findAll().stream()
                .map(data -> (MTDStatusDto) convertToDTO(data, MTDStatusDto.class)).collect(Collectors.toList());
        return MTDStatusReport.builder().details(statusDetails).count(statusDetails.size()).build();
    }

    private Boolean isDocCompletedWithinSLA(final String packageCreatedDate, final String completedOn) {
        //Unparseable date: "2023-09-06T00:33:43.436543Z"
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            final Date d1 = sdf.parse(packageCreatedDate);
            final Date d2 = sdf.parse(completedOn);
            //Calculate time difference
            final int difference = Math.toIntExact(TimeUnit.MILLISECONDS.toHours(d2.getTime() - d1.getTime()) % 24);
            return 24 >= difference;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private String minusDays(final String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(input, formatter);
        LocalDate newDate = date.minusDays(1);
        System.out.println( newDate.format(formatter));
        return newDate.format(formatter);

    }


   /* private Boolean isDocCompletedWithinSLA(final String packageCreatedDate, final String completedOn) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            final Date d1 = sdf.parse(packageCreatedDate);
            final Date d2 = sdf.parse(completedOn);
            //Calculate time difference
            final int difference = Math.toIntExact(TimeUnit.MILLISECONDS.toHours(d2.getTime() - d1.getTime()) % 24);
            return 24 >= difference;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }*/


    @Override
    public LoanDetailsReport buildLoanDetailReport(String date) {
        final List<DocumentDetailsDto> docDetails = documentDetailsService.getAllDocumentsForDate(date);
        final List<LoanDetailsDto> loanDetails = new ArrayList<>();
        docDetails.forEach(doc -> {
            if ((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3)) {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).status(doc.getUserDocStatus()).build());
            } else if ((doc.getStageId().equals(4) || doc.getStageId().equals(5))
                    && (doc.getUserDocStatusId().equals(1) || doc.getUserDocStatusId().equals(2) || doc.getUserDocStatusId().equals(4))){
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).status("Ready for review").build());
            } else if (doc.getStageId().equals(99)) {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).status("Failed").comment(doc.getErrorMessage()).build());
            } else {
                loanDetails.add(LoanDetailsDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).status("In process").build());
            }
        });
        return LoanDetailsReport.builder().loanDetails(loanDetails).build();
    }

    @Override
    public MissingDocReport buildMissingDocReport(final String date) {
        final List<DocumentDetailsDto> docDetails = documentDetailsService.getAllDocumentsForDate(date);
        final List<MissingDocDto> loanDetails = new ArrayList<>();
        docDetails.forEach(doc -> {
            if ((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3)) {
                final MissingDocDto build = MissingDocDto.builder().date(date)
                        .loanId(doc.getLoanNumber()).status(doc.getUserDocStatus()).build();
                final String fileName = String.join("_", doc.getLoanNumber(),
                        doc.getSourceSystemName(), doc.getPackageId(), doc.getUniqueDocumentId() + ".json");
                final String filePath = String.join("/", "dash/dare-files", fileName);
                ExternalServiceDtos dto;
                try (InputStream inputStream = s3Service.download(filePath)) {
                    final ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    dto  = mapper.readValue(inputStream, ExternalServiceDtos.class);
                    final Set<String> docIdsExistInLoan = dto.getDetails().stream()
                            .map(ExternalServiceDto::getDocId).collect(Collectors.toSet());
                    if (!docIdsExistInLoan.contains("2353")) {
                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.REG.getRequiredDocIds()));
                    } else {
                        dto.getDetails().stream()
                                .filter(data -> data.getDocId().equals("2353")).findFirst()
                                .ifPresent(data -> {
                                    final KeywordsDto loanType = data.getKeywords().stream().filter(keyword -> keyword.getKeywordName().equals("Loan Type"))
                                            .findFirst().orElse(KeywordsDto.builder().build());
                                    if (!ObjectUtils.isEmpty(loanType.getKeywordValue())) {
                                        switch (loanType.getKeywordValue()) {
                                            case "VA":
                                                build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.VA.getRequiredDocIds()));
                                                break;
                                            case "FHA":
                                                build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.FHA.getRequiredDocIds()));
                                                break;
                                            case "USDA":
                                                build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.USDA.getRequiredDocIds()));
                                                break;
                                            default:
                                                build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.REG.getRequiredDocIds()));
                                                break;
                                        }
                                    } else {
                                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.REG.getRequiredDocIds()));
                                    }
                                });
                    }
                    loanDetails.add(build);
                } catch (final Exception ex) {
                    System.out.println("Error occurred to get data for package." + ex);
                    build.setComment(ex.getMessage());
                }
            }
        });
        return MissingDocReport.builder().missingDocs(loanDetails).build();
    }

    private String getMissingDocNames(final Set<String> docIdsExistInLoan, final Map<String, String> reqDocIdNameMap) {
        final Set<String> reqDocIds = new HashSet<>(reqDocIdNameMap.keySet());
        final List<String> missingDocIds = new ArrayList<>(reqDocIds);
        missingDocIds.removeAll(docIdsExistInLoan);
        String missingDocName = "";
        for (String docId: missingDocIds) {
            missingDocName  = String.join(", ", reqDocIdNameMap.get(docId));
        }
        return missingDocName;
    }
}
