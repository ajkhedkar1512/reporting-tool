package com.fmc.reporting.service;

import com.fmc.reporting.dto.DocumentDetailsDto;

import java.util.List;

public interface DocumentDetailsService {

    List<DocumentDetailsDto> getAllDocumentsForDate(String date);

    List<DocumentDetailsDto> getAllDocumentsBetweenDate(String from, String to);

    List<DocumentDetailsDto> getAllDocumentsForCurrentDate(String to);

    Integer getAllCompleted(String date);


    Integer getAllDocumentsNotCompleted(String to);

    Integer getAllCompletedPackages(String date);

    Integer getFailedPackageCount(String date);

    List<DocumentDetailsDto> findAllReviewerHistoryByDate(String from, String to);
}
