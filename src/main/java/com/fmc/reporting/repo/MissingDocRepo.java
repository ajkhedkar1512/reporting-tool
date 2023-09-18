package com.fmc.reporting.repo;

import com.fmc.reporting.document.MissingDocDetails;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MissingDocRepo extends BaseRepository<MissingDocDetails, String>{

    @Query(value = "{'status': 1}")
    List<MissingDocDetails> findAllActive();
}
