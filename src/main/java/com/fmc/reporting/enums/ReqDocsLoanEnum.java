package com.fmc.reporting.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum ReqDocsLoanEnum {

    REG("reg", Map.of("98", "1003", "56", "Apprisal", "2353", "CD",  "1199", "IEADS", "76", "DOT", "77", "Note")),

    USDA("usda", Map.of( "601", "MIC/LGC/LNG")),

    VA("va", Map.of("601", "MIC/LGC/LNG", "235", "VA 26-1805 Request for Determination of Reasonable Value")),

    FHA("fha", Map.of("628", "FHA Case", "648", "Case Query","601", "MIC/LGC/LNG","692", "Appraisal Logging"));

    private final String loanType;
    private final Map<String, String> requiredDocIds ;

    public static Map<String, String> mergedMapWithREG(ReqDocsLoanEnum param1){
        Map<String, String> map1 = ReqDocsLoanEnum.REG.getRequiredDocIds();
        Map<String, String> map2 = param1.getRequiredDocIds();
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream()).collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
