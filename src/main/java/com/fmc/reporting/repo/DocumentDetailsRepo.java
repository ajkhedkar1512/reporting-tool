package com.fmc.reporting.repo;

import com.fmc.reporting.document.DocumentDetails;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentDetailsRepo extends BaseRepository<DocumentDetails, String> {

    @Query(value = "{'packageCreatedDate': { $gte : ?0, $lt : ?1}}")
    List<DocumentDetails> getAllBetweenDate(String from, String to);

    @Query(value = "{'packageCreatedDate': { $gte : ?0, $lt : ?1}, $or:[{stageId:{$in:[1,2,3]}},{$and:[ {stageId:4},{userDocStatusId:{$in:[1,2]}}]}]}")
    List<DocumentDetails> getAllDocumentsNotCompletedBetweenDate(String from, String to);

    @Query(value = "{'status': 1}")
    List<DocumentDetails> getAll();

    @Query(value = "{stageId: 5, userDocStatusId: 3, packageCreatedDate: {$gte: ?0,$lt: ?1}, extractionEndedOn: {$gte: ?0,$lt: ?2}}")
    List<DocumentDetails> findAllExtractionCompleted(String previousDate, String currentDate, String nextDate);

    @Query(value = "{stageId: 4, userDocStatusId: 3, packageCreatedDate: {$gte: ?0,$lt: ?1}, classificationEndedOn: {$gte: ?0,$lt: ?2}}")
    List<DocumentDetails> findAllClassificationCompleted(String previousDate, String currentDate, String nextDate);

    @Query(value = "{stageId: 6, packageCreatedDate: {$gte: ?0,$lt: ?1}, extractionEndedOn: {$gte: ?0,$lt: ?2}}")
    List<DocumentDetails> findAllQCRecords(String previousDate, String currentDate, String nextDate);

    @Query(value = "{$or:[" +
            "{stageId:4,userDocStatusId:3,classificationEndedOn:{$gte: ?0,$lt:?1 }}," +
            "{stageId:5,userDocStatusId:3,extractionEndedOn:{$gte:?0,$lt:?1}}," +
            "{stageId:6, eligibleForQcOn:{$gte:?0,$lt:?1}}]}")
    List<DocumentDetails> findAllCompleted(String startDate, String endDate);

    @Query(value = "{stageId: 99, packageCreatedDate: {$gte: ?0,$lt: ?1}}")
    List<DocumentDetails> findAllFailedPackages(String startDate, String endDate);


    @Meta(allowDiskUse = true)
    @Aggregation(pipeline = {
                 "  {\n" +
                 "    $match: {\n" +
                 "      'reviewerHistoryDetails.startedOn': {\n" +
                 "        $gte: ?0,\n" +
                 "        $lt: ?1,\n" +
                 "      },\n" +
                 "    },\n" +
                 "  }",
                 "  {\n" +
                 "    $unwind: {\n" +
                 "      path: \"$reviewerHistoryDetails\",\n" +
                 "    },\n" +
                 "  }",
                 "  {\n" +
                 "    $group: {\n" +
                 "      _id: {\n" +
                 "        reviewer:\n" +
                 "          \"$reviewerHistoryDetails.reviewer\",\n" +
                 "        stage: \"$reviewerHistoryDetails.stage\",\n" +
                 "      },\n" +
                 "      count: {\n" +
                 "        $sum: 1,\n" +
                 "      },\n" +
                 "    },\n" +
                 "  }",
                 "  {\n" +
                 "    $project: {\n" +
                 "      _id: 0,\n" +
                 "      reviewer: \"$_id.reviewer\",\n" +
                 "      stage: \"$_id.stage\",\n" +
                 "      count: 1,\n" +
                 "    },\n" +
                 "  }"
    })
    AggregationResults<DocumentDetails> findAllReviewerHistoryByDate(String startDate, String endDate);



}
