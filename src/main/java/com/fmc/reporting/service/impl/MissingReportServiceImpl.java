package com.fmc.reporting.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmc.reporting.dto.*;
import com.fmc.reporting.enums.ReqDocsLoanEnum;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import com.fmc.reporting.service.MissingReportService;
import com.fmc.reporting.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fmc.reporting.utils.DateTimeUtils.getFormattedDate;
import static com.fmc.reporting.utils.DateTimeUtils.minusDays;

@Service
@RequiredArgsConstructor
public class MissingReportServiceImpl extends AbstractBaseService implements MissingReportService {

    private final DocumentDetailsService documentDetailsService;

    private final S3Service s3Service;


    @Override
    public MissingDocReport build(final String currentDate,  final List<String> previousDates) {
        final List<DocumentDetailsDto> docDetails = new ArrayList<>();
        previousDates.stream().skip(1)
                .forEach(date -> docDetails.addAll(documentDetailsService.getAllDocumentsBetweenDate(minusDays(date), date)));
        docDetails.addAll(documentDetailsService.getAllDocumentsForDate(currentDate));
        final List<MissingDocDto> loanDetails = new ArrayList<>();
        docDetails.forEach(doc -> {
            if (((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3))
                    || doc.getStageId().equals(6) )
                loanDetails.addAll(buildMissingDocForCurrentDate(doc));
        });
        return MissingDocReport.builder().missingDocs(loanDetails).build();
    }

    private  List<MissingDocDto> buildMissingDocForCurrentDate(final DocumentDetailsDto doc) {
        final  List<MissingDocDto> loanDetails = new ArrayList<>();
        final MissingDocDto build = MissingDocDto.builder().date(getFormattedDate(doc.getPackageCreatedDate()))
                .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).build();
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
                build.setMissingDocs("Closing Disclosure missing");
            } else {
                dto.getDetails().stream()
                        .filter(data -> data.getDocId().equals("2353")).findFirst()
                        .ifPresent(data -> {
                            final KeywordsDto loanType = data.getKeywords().stream().filter(keyword -> keyword.getKeywordName().equals("Loan Type"))
                                    .findFirst().orElse(KeywordsDto.builder().build());
                            if (!ObjectUtils.isEmpty(loanType.getKeywordValue())) {
                                switch (loanType.getKeywordValue()) {
                                    case "VA":
                                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.mergedMapWithREG(ReqDocsLoanEnum.VA)));
                                        break;
                                    case "FHA":
                                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.mergedMapWithREG(ReqDocsLoanEnum.FHA)));
                                        break;
                                    case "USDA":
                                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.mergedMapWithREG(ReqDocsLoanEnum.USDA)));
                                        break;
                                    default:
                                        build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.REG.getRequiredDocIds()));
                                        break;
                                }
                            } else {
                                build.setMissingDocs(getMissingDocNames(docIdsExistInLoan, ReqDocsLoanEnum.REG.getRequiredDocIds()));
                            }
                            //Check FI
                           data.getKeywords().stream()
                                    .filter(keyword -> keyword.getKeywordName().equals("Flood Certification Fee"))
                                    .findFirst().ifPresent(keyword -> {
                                        if (!ObjectUtils.isEmpty(keyword.getKeywordValue())
                                                && Double.parseDouble(keyword.getKeywordValue().substring(1)) > 0.0) {
                                            if (!docIdsExistInLoan.contains("71")) {
                                                build.setMissingDocs(build.getMissingDocs() + ", Flood Cert");
                                            }
                                       }
                                    });
                            //Check HOA
                            data.getKeywords().stream()
                                    .filter(keyword -> keyword.getKeywordName().equals("Mortgage Insurance"))
                                    .findFirst().ifPresent(keyword -> {
                                        if (!ObjectUtils.isEmpty(keyword.getKeywordValue())
                                                && Double.parseDouble(keyword.getKeywordValue().substring(1)) > 0.0) {
                                            if (!docIdsExistInLoan.contains("75")) {
                                                build.setMissingDocs(build.getMissingDocs() + ", HOI");
                                            }
                                        }
                                    });
                            //Check MI Cert
                            data.getKeywords().stream()
                                    .filter(keyword -> keyword.getKeywordName().equals("Mortgage Insurance"))
                                    .findFirst().ifPresent(keyword -> {
                                        if (!ObjectUtils.isEmpty(keyword.getKeywordValue())
                                                && Double.parseDouble(keyword.getKeywordValue().substring(1)) > 0.0) {
                                            if (!docIdsExistInLoan.contains("35")) {
                                                build.setMissingDocs(build.getMissingDocs() + ", MI Cert");
                                            }
                                        }
                                    });
                        });
            }
            if (!ObjectUtils.isEmpty(build.getMissingDocs())) {
                build.setMissingDocs(build.getMissingDocs().substring(1) + " missing");
                build.setStatus("open");
                loanDetails.add(build);
            }
        } catch (final Exception ex) {
            System.out.println("Error occurred to get data for package." + ex);
            build.setComment(ex.getMessage());
        }
        return loanDetails;
    }

    private String getMissingDocNames(final Set<String> docIdsExistInLoan, final Map<String, String> reqDocIdNameMap) {
        final Set<String> reqDocIds = new HashSet<>(reqDocIdNameMap.keySet());
        final List<String> missingDocIds = new ArrayList<>(reqDocIds);
        missingDocIds.removeAll(docIdsExistInLoan);
        StringBuilder missingDocName = new StringBuilder();
        for (String docId: missingDocIds) {
            missingDocName.append(", ").append(reqDocIdNameMap.get(docId));
        }
        return ObjectUtils.isEmpty(missingDocName) ? "" : missingDocName.toString();
    }

}
