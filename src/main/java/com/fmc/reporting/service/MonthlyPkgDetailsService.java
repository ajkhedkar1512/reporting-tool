package com.fmc.reporting.service;

import com.fmc.reporting.dto.MonthlyPackageDetailsDto;

public interface MonthlyPkgDetailsService {

    MonthlyPackageDetailsDto build(String month);
}
