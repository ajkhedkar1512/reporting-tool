package com.fmc.reporting.service;

import com.fmc.reporting.dto.ExternalServiceDto;
import com.fmc.reporting.dto.ExternalServiceDtos;
import com.fmc.reporting.dto.KeywordsDto;
import com.fmc.reporting.dto.MissingDocReport;

import java.util.List;
import java.util.Optional;

public interface MissingReportService {

    MissingDocReport build(String date,  final List<String> previousDates);

    default Optional<ExternalServiceDto> getDataByDocId(final String docId, ExternalServiceDtos dto) {
        return dto.getDetails().stream().filter(data -> data.getDocId().equals(docId)).findAny();
    }

    default Optional<String> getFieldValue(final String fieldName, ExternalServiceDto dto) {
        return dto.getKeywords().stream().filter(data -> data.getKeywordName().equals(fieldName))
                .map(KeywordsDto::getKeywordValue).findAny();
    }
}
