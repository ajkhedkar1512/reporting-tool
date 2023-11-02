package com.fmc.reporting.repo;

import com.fmc.reporting.document.PresentDocId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PresentDocIdRepo extends BaseRepository<PresentDocId, String> {
    PresentDocId findByLoanNumberAndDate(String loanNumber,String date);
}
