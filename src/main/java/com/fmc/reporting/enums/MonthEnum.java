package com.fmc.reporting.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MonthEnum {

    JAN("JAN", 1),
    FEB("FEB", 2),
    MAR("MAR", 3),
    APR("APR", 4),
    MAY("MAY", 5),
    JUN("JUN", 6),
    JUL("JUL", 7),
    AUG("AUG", 8),
    SEP("SEP", 9),
    OCT("OCT", 10),
    NOV("NOV", 11),
    DEC("DEC", 12);

    private final String month;
    private final Integer index;

    public static Integer getMonthIndex(final String month) {
        for (final MonthEnum monthEnum : MonthEnum.values()) {
            if (monthEnum.getMonth().equals(month)) {
                return monthEnum.getIndex();
            }
        }
        return JAN.getIndex();
    }

}
