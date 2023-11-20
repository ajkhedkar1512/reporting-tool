package com.fmc.reporting.service.impl;

import com.fmc.reporting.document.DocumentDetails;
import com.fmc.reporting.dto.DocumentDetailsDto;
import com.fmc.reporting.repo.DocumentDetailsRepo;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.DocumentDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.fmc.reporting.utils.DateTimeUtils.*;

@Service
@RequiredArgsConstructor
public class DocumentDetailsServiceImpl extends AbstractBaseService implements DocumentDetailsService {

    private final DocumentDetailsRepo repo;

    private final String UTC_TIME= "T04:00:00";

    @Override
    public List<DocumentDetailsDto> getAllDocumentsForDate(final String date) {
        final  List<DocumentDetails> docDetails = repo
                .getAllBetweenDate(minusDays(date)+UTC_TIME, date+UTC_TIME);
        return docDetails.stream().map(doc -> (DocumentDetailsDto) convertToDTO(doc, DocumentDetailsDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<DocumentDetailsDto> getAllDocumentsBetweenDate(final String from, final String to) {
        final  List<DocumentDetails> docDetails = repo
                .getAllBetweenDate(from+UTC_TIME, to+UTC_TIME);
        return docDetails.stream().map(doc -> (DocumentDetailsDto) convertToDTO(doc, DocumentDetailsDto.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<DocumentDetailsDto> getAllDocumentsForCurrentDate(String date) {
        final  List<DocumentDetails> docDetails = repo.getAllBetweenDate(date+UTC_TIME, plusDays(date)+UTC_TIME);
        return docDetails.stream().map(doc -> (DocumentDetailsDto) convertToDTO(doc, DocumentDetailsDto.class)).collect(Collectors.toList());

    }

    @Override
    public Integer getAllCompleted(final String date) {
        final String previousDate = minusDays(date) + UTC_TIME;
        final String nextDate = (isFriday(minusDays(date)) ?  getMondayDate(minusDays(date)) :  plusDays(date) ) + UTC_TIME;
        final String currentDate = date + UTC_TIME;
         Integer classificationCompletedCount =0;
         Integer extractionCompletedCount = 0;
        Integer qcCount = 0;
        if (isFriday(minusDays(date))) {
            classificationCompletedCount = repo.findAllClassificationNewCompleted(previousDate, currentDate, nextDate).size();
            extractionCompletedCount = repo.findAllExtractionNewCompleted(previousDate, currentDate, nextDate).size();
             qcCount = repo.findAllQCNewRecords(previousDate, currentDate, nextDate).size();
        } else {
            classificationCompletedCount = repo.findAllClassificationCompleted(previousDate, currentDate, nextDate).size();
             extractionCompletedCount = repo.findAllExtractionCompleted(previousDate, currentDate, nextDate).size();
              qcCount = repo.findAllQCRecords(previousDate, currentDate, nextDate).size();
        }
        return classificationCompletedCount + extractionCompletedCount + qcCount;
    }

    @Override
    public Integer getAllDocumentsNotCompleted(final String to) {
        final String fromDate = minusDays(to);
        final  List<DocumentDetails> docDetails = repo.getAllDocumentsNotCompletedBetweenDate(fromDate+UTC_TIME, to+UTC_TIME);
        return (int) docDetails.stream().map(doc -> (DocumentDetailsDto) convertToDTO(doc, DocumentDetailsDto.class)).count();
    }

    @Override
    public Integer getAllCompletedPackages(String date) {
        final String fromDate = date + UTC_TIME;
        System.out.println("fromDate" + fromDate);
        final String toDate = plusDays(date) + UTC_TIME;
        System.out.println("toDate" + toDate);
        return repo.findAllCompleted(fromDate, toDate).size();
    }

    @Override
    public Integer getFailedPackageCount(String date) {
        final String fromDate = date + UTC_TIME;
        System.out.println("fromDate" + fromDate);
        final String toDate = plusDays(date) + UTC_TIME;
        System.out.println("toDate" + toDate);
        return repo.findAllFailedPackages(fromDate, toDate).size();
    }

}
