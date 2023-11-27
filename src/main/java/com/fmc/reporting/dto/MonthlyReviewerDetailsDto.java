package com.fmc.reporting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReviewerDetailsDto {

    private String startedOn;

    private String endedOn;

    private Integer count;

    private String reviewer;

    private String stage;

    private String date;


    public static MonthlyReviewerDetailsDto build(final DocumentDetailsDto data, final String date) {
        return MonthlyReviewerDetailsDto.builder().date(date).reviewer(data.getReviewer()).count(data.getCount())
                .stage(data.getStage()).build();
    }
}
