package com.fmc.reporting.repo;

import com.fmc.reporting.document.MtdReportDetails;

import java.util.Optional;

public interface MtdReportRepo extends BaseRepository<MtdReportDetails, String> {


    Optional<MtdReportDetails> findByDate(String date);
}
