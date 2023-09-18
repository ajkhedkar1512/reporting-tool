package com.fmc.reporting.service;

import com.fmc.reporting.config.ApplicationPropertiesConfig;
import com.fmc.reporting.document.BaseDocument;
import com.fmc.reporting.dto.BaseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
public abstract class AbstractBaseService {

    @Autowired
    protected ApplicationPropertiesConfig appProperties;

    public <T extends BaseDto> BaseDocument convertToDocument(final T t, final Class<? extends BaseDocument> clazz) {
        return new ModelMapper().map(t, (Type) clazz);
    }

    public <T extends BaseDocument> BaseDto convertToDTO(final T t, final Class<? extends BaseDto> clazz) {
        return new ModelMapper().map(t, (Type) clazz);
    }
}
