package com.fmc.reporting.service.impl;

import com.fmc.reporting.dto.MissingFieldMappingDto;
import com.fmc.reporting.repo.MissingFieldMappingRepo;
import com.fmc.reporting.service.AbstractBaseService;
import com.fmc.reporting.service.MissingFieldMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissingFieldMappingServiceImpl extends AbstractBaseService implements MissingFieldMappingService {

    private final MissingFieldMappingRepo repo;


    @Override
    public List<MissingFieldMappingDto> getAll() {
        return repo.findAll().stream()
                .map(data -> (MissingFieldMappingDto) convertToDTO(data, MissingFieldMappingDto.class))
                .collect(Collectors.toList());
    }
}
