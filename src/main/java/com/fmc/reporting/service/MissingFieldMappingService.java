package com.fmc.reporting.service;

import com.fmc.reporting.dto.MissingFieldMappingDto;

import java.util.List;

public interface MissingFieldMappingService {

    List<MissingFieldMappingDto> getAll();
}
