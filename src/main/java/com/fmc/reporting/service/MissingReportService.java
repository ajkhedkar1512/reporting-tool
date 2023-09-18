package com.fmc.reporting.service;

import com.fmc.reporting.dto.MissingDocReport;

import java.util.List;

public interface MissingReportService {

    MissingDocReport build(String date,  final List<String> previousDates);
}
