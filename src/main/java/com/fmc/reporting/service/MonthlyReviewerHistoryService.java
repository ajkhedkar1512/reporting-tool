package com.fmc.reporting.service;

import com.fmc.reporting.dto.MonthlyReviewerDetailsDto;

import java.util.List;

public interface MonthlyReviewerHistoryService {

    List<MonthlyReviewerDetailsDto> build(String month);
}
