package com.fmc.reporting.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmc.reporting.dto.*;
import com.fmc.reporting.enums.ReqDocsLoanEnum;
import com.fmc.reporting.exception.BaseException;
import com.fmc.reporting.service.*;
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

    private final MissingFieldMappingService mappingService;


    @Override
    public MissingDocReport build(final String currentDate,  final List<String> previousDates) {
        final List<DocumentDetailsDto> docDetails = new ArrayList<>();
        previousDates.stream().skip(1)
                .forEach(date -> docDetails.addAll(documentDetailsService.getAllDocumentsBetweenDate(minusDays(date), date)));
        docDetails.addAll(documentDetailsService.getAllDocumentsForDate(currentDate));
        final List<MissingDocDto> loanDetails = new ArrayList<>();
        final List<MissingFieldMappingDto> mappingFields = mappingService.getAll();
        docDetails.forEach(doc -> {
            if (((doc.getStageId().equals(4) || doc.getStageId().equals(5)) && doc.getUserDocStatusId().equals(3))
                    || doc.getStageId().equals(6) )
                loanDetails.addAll(buildMissingDocForCurrentDate(doc));
               //loanDetails.add(buildMissingDocForCurrentDateV2(doc, mappingFields));
        });
        return MissingDocReport.builder().missingDocs(loanDetails).build();
    }

    private ExternalServiceDtos getExternalDetailsData(final DocumentDetailsDto doc) {
        final String fileName = String.join("_", doc.getLoanNumber(),
                doc.getSourceSystemName(), doc.getPackageId(), doc.getUniqueDocumentId() + ".json");
        final String filePath = String.join("/", "dash/dare-files", fileName);
        try (InputStream inputStream = s3Service.download(filePath)) {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(inputStream, ExternalServiceDtos.class);
        } catch (final Exception ex) {
            System.out.println("Error occurred to get data for package." + ex);
            throw new BaseException("Error occurred while downloading file from S3. Error " + ex.getMessage());
        }
    }

    private static Set<String> getMappingFieldDocIds(final List<MissingFieldMappingDto> mappingFields) {
        return mappingFields.stream().map(MissingFieldMappingDto::getDocId).collect(Collectors.toSet());
    }

    private static List<String> getMappingFieldForDocId(final List<MissingFieldMappingDto> mappingFields, final String docId) {
        List<String> fields = new ArrayList<>();
        for (MissingFieldMappingDto data : mappingFields) {
            if (data.getDocId().equals(docId)) {
                fields = data.getFields();
            }
        }
        return fields;
    }

    private  MissingDocDto buildMissingDocForCurrentDateV2(final DocumentDetailsDto doc, final List<MissingFieldMappingDto> mappingFields) {
        final MissingDocDto build = MissingDocDto.builder().date(getFormattedDate(doc.getPackageCreatedDate()))
                .loanId(doc.getLoanNumber()).packageId(doc.getPackageId()).build();
        final ExternalServiceDtos dto = getExternalDetailsData(doc);
        final List<String> missingFields = new ArrayList<>();
        Set<String> mappingFieldDocIds = getMappingFieldDocIds(mappingFields);
        mappingFieldDocIds.forEach(docId -> {
            final Optional<ExternalServiceDto> oDataByDocId = getDataByDocId(docId, dto);
            List<String> mappingFieldForDocId = getMappingFieldForDocId(mappingFields, docId);
            if (oDataByDocId.isPresent()) {
                final ExternalServiceDto dataByDocId = oDataByDocId.get();
                mappingFieldForDocId.forEach(field -> {
                    if (isFieldValueMissing(field, dataByDocId)) {
                        missingFields.add(field);
                    }
                });
            } else {
                missingFields.addAll(mappingFieldForDocId);
            }
        });
        //Custom Field Logic

        //Check for fields related to 71
        //Get Flood certificate fee
        //if present check for 71 fields
        getDataByDocId("2353", dto).flatMap(data -> getFieldValue("Flood Certification Fee", data)).ifPresent(fieldValue -> {
            if (!ObjectUtils.isEmpty(fieldValue)) {
                Optional<ExternalServiceDto> detailsForFloodCertificate = getDataByDocId("71", dto);
                if (detailsForFloodCertificate.isPresent()) {
                    detailsForFloodCertificate.get().getKeywords().forEach(keywordsDto -> {
                        if (keywordsDto.getKeywordName().equals("Flood Zone")) {
                            if(isFieldValueMissing(keywordsDto.getKeywordName(), detailsForFloodCertificate.get())) {
                                missingFields.add(keywordsDto.getKeywordName());
                            }
                        }
                        if (keywordsDto.getKeywordName().equals("Flood Cert Provider")) {
                            if(isFieldValueMissing(keywordsDto.getKeywordName(), detailsForFloodCertificate.get())) {
                                missingFields.add(keywordsDto.getKeywordName());
                            }
                        }
                    });
                } else {
                    missingFields.addAll(List.of("Flood Zone", "Flood Cert Provider"));
                }
            }

        });
        //Check Borrower 1 fields for 77/98
        List.of("Borrower 1 First Name", "Borrower 1 Middle Name", "Borrower 1 Last Name", "Borrower 1 Suffix")
                .forEach(field -> {
                    if (iFieldValueMissingForDocId("77", field, dto) &&
                            iFieldValueMissingForDocId("98", field, dto)) {
                        missingFields.add(field);
                    }
                });


        //Check Loan term
        if (iFieldValueMissingForDocId("628", "loan term", dto) &&
                iFieldValueMissingForDocId("648", "loan term", dto) &&
                iFieldValueMissingForDocId("98", "loan term", dto)) {
            missingFields.add("loan term");
        }

        //Check Fields as per Loan Type
        getDataByDocId("2353", dto)
                .flatMap(data -> getFieldValue("Loan Type", data))
                .ifPresent(loanType -> {
                    if (loanType.equals("FHA")) {
                        //Check ADP code in 628, 648
                        if (iFieldValueMissingForDocId("628", "adp code", dto) &&
                                iFieldValueMissingForDocId("648", "adp code", dto)) {
                            missingFields.add("adp code");
                        }
                    }
                    if (loanType.equals("FHA") || loanType.equals("VA") || loanType.equals("USDA")) {
                        if (iFieldValueMissingForDocId("601", "Application ID", dto)) {
                            missingFields.add("adp code");
                        }
                    }
                });
        StringBuilder missingField = new StringBuilder();
        for (String field: missingFields) {
            missingField.append(", ").append(field);
        }
        build.setMissingDocs(missingField.substring(1) + " missing");
        return build;
    }

    private boolean iFieldValueMissingForDocId(String docId, String field, final ExternalServiceDtos dto) {
        return getDataByDocId(docId, dto)
                .map(externalServiceDto -> isFieldValueMissing(field, externalServiceDto)).orElse(true);
    }

    private boolean isFieldValueMissing(String field, ExternalServiceDto dataByDocId) {
        return getFieldValue(field, dataByDocId).isEmpty();
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
